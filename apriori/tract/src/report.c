/*----------------------------------------------------------------------
  File    : report.c
  Contents: item set reporter management
  Author  : Christian Borgelt
  History : 2008.08.18 item set reporter created in tract.[ch]
            2008.08.30 handling of perfect extensions completed
            2008.09.01 handling of closed and maximal item sets added
            2008.09.08 functions ist_intout() and isr_floatout() added
            2008.10.30 transaction identifier reporting added
            2008.10.31 item set reporter made a separate module
            2008.11.01 optional double precision support added
            2008.12.05 bug handling real-valued support fixed (_report)
            2009.10.15 counting of reported item sets added
            2010.02.11 closed/maximal item set filtering added
            2010.02.12 bugs in prefix tree handling fixed (clomax)
            2010.03.09 bug in reporting the empty item set fixed
            2010.03.11 filtering of maximal item sets improved
            2010.03.17 head union tail pruning for maximal sets added
            2010.03.18 parallel item set support and weight reporting
            2010.04.07 extended information reporting functions removed
            2010.07.01 correct output of infinite float values added
            2010.07.02 order of closed/maximal and size filtering fixed
            2010.07.04 bug in isr_report() fixed (closed set filtering)
            2010.07.12 null output file made possible (for benchmarking)
            2010.07.19 bug in function isr_report() fixed (clomax)
            2010.07.21 early closed/maximal repository pruning added
            2010.07.22 adapted to closed/maximal item set filter
            2010.08.06 function isr_direct() for direct reporting added
            2010.08.11 function isr_directx() for extended items added
            2010.08.14 item set header for output added to isr_create()
            2010.10.15 functions isr_open(), isr_close(), isr_rule()
            2010.10.27 handling of null names in isr_open() changed
            2011.05.06 generalized to support type SUPP_T (int/double)
            2011.06.10 function isr_wgtsupp() added (weight/support)
            2011.07.12 adapted to optional integer item names
            2011.07.21 function isr_evalwgt() added (for accretion)
            2011.07.23 parameter dir added to function isr_seteval()
----------------------------------------------------------------------*/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <float.h>
#include <assert.h>
#include <math.h>
#include "report.h"
#include "scanner.h"

/*----------------------------------------------------------------------
  Preprocessor Definitions
----------------------------------------------------------------------*/
#define BS_INT       32         /* buffer size for integer output */
#define BS_FLOAT    400         /* buffer size for float   output */
#define LN_2        0.69314718055994530942  /* ln(2) */

/*----------------------------------------------------------------------
  Functions
----------------------------------------------------------------------*/

ISREPORT* isr_create (ITEMBASE *base, int mode, int dir,
                      const char *hdr, const char *sep, const char *imp)
{                               /* --- create an item set reporter */
  int        i, k, n, b = 0;    /* loop variables, buffers */
  ISREPORT   *rep;              /* created item set reporter */
  int        len, sum;          /* length of an item name and sum */
  char       *buf;              /* buffer for formated item name */
  const char *name;             /* to traverse the item names */

  assert(base);                 /* check the function arguments */
  if (mode & (ISR_CLOSED|ISR_MAXIMAL))
    mode |= ISR_NOEXP;          /* make reporting mode consistent */
  n   = ib_cnt(base);           /* get the number of items/trans. */
  rep = (ISREPORT*)malloc(sizeof(ISREPORT) +(n+n+1) *sizeof(char*));
  if (!rep) return NULL;        /* allocate the base structure */
  rep->base    = base;          /* store the item base */
  rep->file    = NULL;          /* clear the output file */
  rep->name    = NULL;          /* and its name */
  rep->mode    = mode & (ISR_CLOSED|ISR_MAXIMAL|ISR_NOEXP);
  rep->rep     = 0;             /* init. the item set counter, */
  rep->min     = 1;             /* the range of item set sizes */
  rep->max     = n;             /* (minimum and maximum size), */
  rep->cnt     = rep->pfx = 0;  /* and the number of items */
  rep->evalfn  = (ISEVALFN*)0;  /* clear add. evaluation function */
  rep->evaldat = NULL;          /* and the corresponding data */
  rep->evalthh = rep->eval = 0; /* clear evaluation and its minimum */
  rep->evaldir = 1;             /* default: threshold is minimum */
  rep->repofn  = (ISREPOFN*)0;  /* clear item set report function */
  rep->repodat = NULL;          /* and the corresponding data */
  rep->tidfile = NULL;          /* clear the transaction id file */
  rep->tidname = NULL;          /* and its name */
  rep->tids    = NULL;          /* clear transaction ids array and */
  rep->tidcnt  = 0;             /* the number of transaction ids */
  rep->tacnt   = 0;             /* set default value for the other */
  rep->miscnt  = 0;             /* transaction ids array variables */
  rep->inames  = (const char**)(rep->pos +n+1);
  memset((void*)rep->inames, 0, n *sizeof(const char*));
  *rep->pos    = NULL;          /* organize the pointer arrays */
  rep->wgts    = rep->logs = rep->sums = NULL;
  #ifdef ISR_CLOMAX             /* if closed/maximal set filtering */
  rep->clomax  = NULL;          /* initialize all pointers */
  rep->buf     = NULL;          /* for an easier abort on failure */
  if (mode & (ISR_CLOSED|ISR_MAXIMAL)) b = n;
  #endif                        /* get size of extra item set buffer */
  rep->pxpp  = (int*)   malloc((n+1+n+n+b) *sizeof(int));
  rep->supps = (SUPP_T*)malloc((n+1)       *sizeof(SUPP_T));
  if (!rep->pxpp || !rep->supps) { isr_delete(rep, 0); return NULL; }
  rep->pexs  = rep->pxpp +n+1;  /* allocate memory for the arrays */
  rep->items = rep->pexs += n;  /* and organize and initialize it */
  memset(rep->pxpp, 0, n *sizeof(int));
  rep->supps[0] = base->wgt;    /* init. the empty set support */
  if (mode & ISR_WEIGHTS) {     /* if to use item set weights */
    rep->wgts = (double*)malloc((n+1) *sizeof(double));
    if (!rep->wgts) { isr_delete(rep, 0); return NULL; }
    rep->wgts[0] = base->wgt;   /* create a floating point array */
  }                             /* and store the empty set support */
  if (mode & ISR_LOGS) {        /* if to compute logarithms of freqs. */
    rep->logs = (double*)malloc((n+n+1) *sizeof(double));
    if (!rep->logs) { isr_delete(rep, 0); return NULL; }
    rep->sums = rep->logs +n;   /* allocate the needed arrays */
    for (i = 0; i < n; i++)     /* compute logarithms of item freqs. */
      rep->logs[i] = log((double)ib_getfrq(base, i));
    rep->logwgt  = log((double)base->wgt);
    rep->sums[0] = 0;           /* store the log of the total weight */
  }                             /* and init. the sum of logarithms */
  if ((n > 0)                   /* if item names are integers */
  &&  (ib_mode(base) & IB_INTNAMES)) {
    for (i = sum = 0; i < n; i++) {
      k = ib_int(base, i);      /* traverse the integer values */
      if (k < 0) { k = -k; sum++; }
      while ((k /= 10) > 0) sum++;
      sum++;                    /* determine the total size */
    }                           /* of the formatted integers */
    buf = (char*)malloc((sum +n) *sizeof(char));
    if (!buf) { isr_delete(rep, 0); return NULL; }
    for (i = 0; i < n; i++) {   /* traverse the items again and */
      rep->inames[i] = buf;     /* print the integer numbers */
      buf += sprintf(buf, "%d", ib_int(base, i)) +1;
    } }
  else {                        /* if item names are strings */
    for (i = sum = 0; i < n; i++) {
      name = ib_name(base, i);  /* traverse the items and their names */
      if (!(mode & ISR_SCAN))   /* if to use the names directly */
        sum += strlen(name);    /* simply sum the string lengths */
      else {                    /* if name formatting may be needed */
        sum += k = scn_fmtlen(name, &len);
        if (k > len) {          /* if name formatting is needed */
          buf = (char*)malloc((k+1) *sizeof(char));
          if (buf) scn_format(buf, name, 0);
          name = buf;           /* format the item name */
        }                       /* (quote certain characters) */
      }                         /* and replace the original name */
      rep->inames[i] = name;    /* store the (formatted) item name */
      if (!name) { isr_delete(rep, 0); return NULL; }
    }                           /* check for proper name copying */
  }                             /* (in case it was formatted) */
  rep->inames[n] = NULL;        /* store a sentinel after the names */
  if (!hdr) hdr = "";           /* get default header and separator */
  if (!sep) sep = " ";          /* and compute output buffer size */
  sum += (k = strlen(hdr)) +(n-1) *strlen(sep) +1;
  rep->out = (char*)malloc(sum *sizeof(char));
  if (!rep->out) { isr_delete(rep, 0); return NULL; }
  strcpy(rep->out, hdr);        /* allocate the output buffer */
  rep->pos[0] = rep->out +k;    /* and copy the header into it */
  rep->hdr    = hdr;            /* store header, item separator */
  rep->sep    = sep;            /* and implication sign */
  rep->imp    = (imp) ? imp : " <- ";
  rep->iwfmt  = ":%2w";         /* format for item weights */
  rep->format = "  (%a)";       /* format for absolute support */
  #ifdef ISR_CLOMAX             /* if closed/maximal filtering */
  if (b <= 0) return rep;       /* if to report all item sets, abort */
  rep->buf    = rep->items +n;  /* set the second item set buffer */
  rep->clomax = cm_create(dir, n);
  if (!rep->clomax) { isr_delete(rep, 0); return NULL; }
  #endif                        /* create a closed/maximal filter */
  return rep;                   /* return created item set reporter */
}  /* isr_create() */

/*--------------------------------------------------------------------*/

int isr_delete (ISREPORT *rep, int mode)
{                               /* --- delete an item set reporter */
  int i, k;                     /* loop variable, buffers */

  assert(rep);                  /* check the function argument */
  #ifdef ISR_CLOMAX             /* if closed/maximal filtering */
  if (rep->clomax) cm_delete(rep->clomax);
  #endif                        /* delete the closed/maximal filter */
  if (*rep->pos) free(*rep->pos);    /* delete the output buffer */
  if (ib_mode(rep->base) & IB_INTNAMES) {
    if (rep->inames[0]) free((void*)rep->inames[0]); }
  else {                        /* delete the integer names */
    for (i = 0; rep->inames[i]; i++)
      if (rep->inames[i] != ib_name(rep->base, i))
        free((void*)rep->inames[i]);
  }                             /* delete all cloned names */
  if (rep->logs)  free(rep->logs);   /* delete the arrays */
  if (rep->wgts)  free(rep->wgts);   /* (if they are present) */
  if (rep->supps) free(rep->supps);
  if (rep->pxpp)  free(rep->pxpp);   /* delete the item base */
  if (mode & ISR_DELISET) ib_delete(rep->base);
  k = (mode & ISR_FCLOSE) ? isr_tidclose(rep) : 0;
  i = (mode & ISR_FCLOSE) ? isr_close(rep)    : 0;
  free(rep);                    /* delete the base structure */
  return (i) ? i : k;           /* return file closing result */
}  /* isr_delete() */

/*--------------------------------------------------------------------*/

int isr_open (ISREPORT *rep, FILE *file, const char *name)
{                               /* --- open an output file */
  assert(rep);                  /* check the function arguments */
  if (file)                     /* if a file is given, */
    rep->name = name;           /* store the file name */
  else if (! name) {            /* if no name is given */
    file = NULL;   rep->name = "<none>"; }
  else if (!*name) {            /* if an empty name is given */
    file = stdout; rep->name = "<stdout>"; }
  else {                        /* if a proper name is given */
    file = fopen(rep->name = name, "w");
    if (!file) return -2;       /* open file with given name */
  }                             /* and check for an error */
  rep->file = file;             /* store the new output file */
  return 0;                     /* return 'ok' */
}  /* isr_open() */

/*--------------------------------------------------------------------*/

int isr_close (ISREPORT *rep)
{                               /* --- close the output file */
  int r;                        /* result of fclose()/fflush() */

  assert(rep);                  /* check the function arguments */
  if (!rep->file) return 0;     /* check for an output file */
  r = ((rep->file == stdout) || (rep->file == stderr))
    ? fflush(rep->file) : fclose(rep->file);
  rep->file = NULL;             /* close the current output file */
  return r;                     /* return the result of fclose() */
}  /* isr_close() */

/*--------------------------------------------------------------------*/

void isr_setsize (ISREPORT *rep, int min, int max)
{                               /* --- set size range for item set */
  assert(rep                    /* check the function arguments */
     && (min >= 0) && (max >= min));
  rep->min = min;               /* store the minimum and maximum */
  rep->max = max;               /* size of an item set to report */
}  /* isr_setsize() */

/*--------------------------------------------------------------------*/

void isr_seteval (ISREPORT *rep, ISEVALFN evalfn,
                  void *data, double thresh, int dir)
{                               /* --- set evaluation function */
  assert(rep);                  /* check the function argument */
  rep->evalfn  = evalfn;        /* store the evaluation function, */
  rep->evaldat = data;          /* the corresponding user data, */
  rep->evaldir = (dir >= 0) ? +1 : -1;  /* the evaluation direction */
  rep->evalthh = rep->evaldir *thresh;  /* and the threshold value  */
}  /* isr_seteval() */

/*--------------------------------------------------------------------*/

void isr_setrepo (ISREPORT *rep, ISREPOFN repofn, void *data)
{                               /* --- set evaluation function */
  assert(rep);                  /* check the function argument */
  rep->repofn  = repofn;        /* store the reporting function and */
  rep->repodat = data;          /* the corresponding user data */
}  /* isr_setrepo() */

/*--------------------------------------------------------------------*/

int isr_tidopen (ISREPORT *rep, FILE *file, const char *name)
{                               /* --- set/open trans. id output file */
  assert(rep);                  /* check the function arguments */
  if (file) {                   /* if a file is given directly, */
    if      (name)           rep->tidname = name; /* store name */
    else if (file == stdout) rep->tidname = "<stdout>";
    else if (file == stderr) rep->tidname = "<stderr>";
    else                     rep->tidname = "<unknown>"; }
  else if (! name) {            /* if no name is given */
    file = NULL;             rep->tidname = "<none>"; }
  else if (!*name) {            /* if an empty name is given */
    file = stdout;           rep->tidname = "<stdout>"; }
  else {                        /* if a proper name is given */
    file = fopen(rep->tidname = name, "w");
    if (!file) return -2;       /* open file with given name */
  }                             /* and check for an error */
  rep->tidfile = file;          /* store the new output file */
  return 0;                     /* return 'ok' */
}  /* isr_tidopen() */

/*--------------------------------------------------------------------*/

int isr_tidclose (ISREPORT *rep)
{                               /* --- close trans. id output file */
  int r;                        /* result of fclose() */

  assert(rep);                  /* check the function arguments */
  if (!rep->tidfile) return 0;  /* check for an output file */
  r = ((rep->tidfile == stdout) || (rep->tidfile == stderr))
    ? fflush(rep->tidfile) : fclose(rep->tidfile);
  rep->tidfile = NULL;          /* close the current output file */
  return r;                     /* return the result of fclose() */
}  /* isr_tidclose() */

/*--------------------------------------------------------------------*/

void isr_tidcfg (ISREPORT *rep, int tacnt, int miscnt)
{                               /* --- configure trans. id output */
  rep->tacnt  = tacnt;          /* note the number of transactions */
  rep->miscnt = miscnt;         /* and the accepted number of */
}  /* isr_tidcfg() */           /* missing items */

/*--------------------------------------------------------------------*/

int isr_add (ISREPORT *rep, int item, SUPP_T supp)
{                               /* --- add an item (only support) */
  assert(rep                    /* check the function arguments */
  && (item >= 0) && (item < ib_cnt(rep->base)));
  if (isr_uses(rep, item))      /* if the item is already in use, */
    return -2;                  /* abort the function */
  #ifdef ISR_CLOMAX             /* if closed/maximal filtering */
  if (rep->clomax) {            /* if a closed/maximal filter exists */
    int r = cm_add(rep->clomax, item, supp);
    if (r <= 0) return r;       /* add the item to the c/m filter */
  }                             /* and check whether to process it */
  #endif
  rep->pxpp [item] |= INT_MIN;  /* mark the item as used */
  rep->items[  rep->cnt] = item;/* store the item and its support */
  rep->supps[++rep->cnt] = supp;/* clear the perfect ext. counter */
  rep->pxpp [  rep->cnt] &= INT_MIN;
  return rep->cnt;              /* return the new number of items */
}  /* isr_add() */

/*--------------------------------------------------------------------*/

int isr_addx (ISREPORT *rep, int item, SUPP_T supp, double wgt)
{                               /* --- add an item (support & weight) */
  assert(rep && rep->wgts       /* check the function arguments */
  &&    (item >= 0) && (item < ib_cnt(rep->base)));
  if (isr_uses(rep, item))      /* if the item is already in use, */
    return -2;                  /* abort the function */
  #ifdef ISR_CLOMAX             /* if closed/maximal filtering */
  if (rep->clomax) {            /* if a closed/maximal filter exists */
    int r = cm_add(rep->clomax, item, supp);
    if (r <= 0) return r;       /* add the item to the c/m filter */
  }                             /* and check whether to process it */
  #endif
  rep->pxpp [item] |= INT_MIN;  /* mark the item as used */
  rep->items[  rep->cnt] = item;/* store the item and its support */
  rep->supps[++rep->cnt] = supp;/* as well as its weight */
  rep->wgts [  rep->cnt] = wgt; /* clear the perfect ext. counter */
  rep->pxpp [  rep->cnt] &= INT_MIN;
  return rep->cnt;              /* return the new number of items */
}  /* isr_addx() */

/*--------------------------------------------------------------------*/

int isr_addpex (ISREPORT *rep, int item)
{                               /* --- add a perfect extension */
  assert(rep                    /* check the function arguments */
  &&    (item >= 0) && (item < ib_cnt(rep->base)));
  if (isr_uses(rep, item))      /* if the item is already in use, */
    return -1;                  /* abort the function */
  rep->pxpp[item] |= INT_MIN;   /* mark the item as used */
  *--rep->pexs = item;          /* store the added item and */
  rep->pxpp[rep->cnt]++;        /* count it for the current prefix */
  return rep->items -rep->pexs; /* return the number of perf. exts. */
}  /* isr_addpex() */

/*--------------------------------------------------------------------*/

int isr_remove (ISREPORT *rep, int n)
{                               /* --- remove one or more items */
  int i;                        /* loop variable, buffer for an item */

  assert(rep && (n >= 0));      /* check the function arguments */
  #ifdef ISR_CLOMAX             /* if closed/maximal filtering */
  if (rep->clomax)              /* if a closed/maximal filter exists, */
    cm_remove(rep->clomax, n);  /* remove the same number of items */
  #endif                        /* from this filter */
  if (n > rep->cnt)             /* cannot remove more items */
    n = rep->cnt;               /* than have been added before */
  while (--n >= 0) {            /* traverse the items to remove */
    for (i = rep->pxpp[rep->cnt] & ~INT_MIN; --i >= 0; )
      rep->pxpp[*rep->pexs++] &= ~INT_MIN;
    i = rep->items[--rep->cnt]; /* traverse the item to remove */
    rep->pxpp[i] &= ~INT_MIN;   /* (current item and perfect exts.) */
  }                             /* and remove their "in use" markers */
  if (rep->cnt < rep->pfx)      /* if too few items are left, */
    rep->pfx = rep->cnt;        /* reduce the valid prefix */
  return rep->cnt;              /* return the new number of items */
}  /* isr_remove() */

/*--------------------------------------------------------------------*/

double isr_logrto (ISREPORT *rep, void *data)
{                               /* --- logarithm of support ratio */
  assert(rep);                  /* check the function arguments */
  return (log(rep->supps[rep->cnt])
             -rep->sums [rep->cnt] +(rep->cnt-1)*rep->logwgt) /LN_2;
}  /* isr_logrto() */

/* Evaluate an itemset by the logarithm of the quotient of the actual */
/* support of an item set and the support that is expected under full */
/* independence of the items (product of item probabilities times the */
/* total transaction weight). 'data' is needed for the interface.     */

/*--------------------------------------------------------------------*/

double isr_logsize (ISREPORT *rep, void *data)
{                               /* --- logarithm of support quotient */
  assert(rep);                  /* check the function arguments */
  return (log(rep->supps[rep->cnt])
             -rep->sums [rep->cnt] +(rep->cnt-1)*rep->logwgt)
       / (rep->cnt *LN_2);      /* divide by item set size */
}  /* isr_logsize() */

/*--------------------------------------------------------------------*/

double isr_sizewgt (ISREPORT *rep, void *data)
{                               /* --- item set size times weight */
  assert(rep);                  /* check the function arguments */
  return rep->wgts[rep->cnt] *rep->cnt;
}  /* isr_sizewgt() */

/* Evaluate an item set by the product of size and weight in order to */
/* favor large item sets and thus to compensate anti-monotone weights.*/

/*--------------------------------------------------------------------*/

double isr_wgtsize (ISREPORT *rep, void *data)
{                               /* --- item set weight / size */
  assert(rep);                  /* check the function arguments */
  return (rep->cnt > 0) ? rep->wgts[rep->cnt] /rep->cnt : 0;
}  /* isr_wgtsize() */

/*--------------------------------------------------------------------*/

double isr_wgtsupp (ISREPORT *rep, void *data)
{                               /* --- item set weight / size */
  double s;                     /* buffer for support */
  assert(rep);                  /* check the function arguments */
  return ((s = rep->supps[rep->cnt]) > 0) ? rep->wgts[rep->cnt] /s : 0;
}  /* isr_wgtsupp() */

/*--------------------------------------------------------------------*/

double isr_evalwgt (ISREPORT *rep, void *data)
{ return rep->wgts[rep->cnt]; } /* --- item set weight as evaluation */

/*--------------------------------------------------------------------*/

static void _output (ISREPORT *rep)
{                               /* --- output an item set */
  int        i, k;              /* loop variable, flag */
  int        min;               /* minimum number of items */
  char       *s;                /* to traverse the output buffer */
  const char *name;             /* to traverse the item names */
  double     sum;               /* to compute the logarithm sums */

  assert(rep                    /* check the function arguments */
     && (rep->cnt >= rep->min)
     && (rep->cnt <= rep->max));
  rep->eval = 0;                /* clear the additional evaluation */
  if (rep->evalfn) {            /* if an evaluation function is given */
    if (rep->logs) {            /* if to compute sums of logarithms */
      sum = rep->sums[rep->pfx];/* get the valid sum for a prefix */
      for (i = rep->pfx; i < rep->cnt; ) {
        sum += rep->logs[rep->items[i]];
        rep->sums[++i] = sum;   /* traverse the additional items */
      }                         /* and add the logarithms of */
    }                           /* their individual frequencies */
    rep->eval = rep->evalfn(rep, rep->evaldat);
    if (rep->evaldir *rep->eval < rep->evalthh)
      return;                   /* if the item set does not qualify, */
  }                             /* abort the output function */
  rep->rep++;                   /* count the reported item set */
  if (rep->repofn)              /* call reporting function if given */
    rep->repofn(rep, rep->repodat);
  if (!rep->file) return;       /* check for an output file */
  s = rep->pos[rep->pfx];       /* get the position for appending */
  while (rep->pfx < rep->cnt) { /* traverse the additional items */
    if (rep->pfx > 0)           /* if this is not the first item */
      for (name = rep->sep; *name; )
        *s++ = *name++;         /* copy the item separator */
    for (name = rep->inames[rep->items[rep->pfx]]; *name; )
      *s++ = *name++;           /* copy the item name to the buffer */
    rep->pos[++rep->pfx] = s;   /* record the new position */
  }                             /* for appending the next item */
  fwrite(rep->out, sizeof(char), s -rep->out, rep->file);
  /* Writing the formatted item set with fwrite seems to be slightly */
  /* faster than terminating the string and writing it with fputs(). */
  isr_sinfo(rep, rep->supps[rep->cnt],
            (rep->wgts) ? rep->wgts[rep->cnt] : 0, rep->eval);
  fputc('\n', rep->file);       /* print the item set information */
  if (!rep->tidfile || !rep->tids) /* check whether to report */
    return;                        /* a list of transaction ids */
  if      (rep->tidcnt > 0) {   /* if tids are in ascending order */
    for (i = 0; i < rep->tidcnt; i++) {
      if (i > 0) fputs(rep->sep, rep->tidfile);
      fprintf(rep->tidfile, "%d", rep->tids[i]+1);
    } }                         /* report the transaction ids */
  else if (rep->tidcnt < 0) {   /* if tids are in descending order */
    for (i = -rep->tidcnt; --i >= 0; ) {
      fprintf(rep->tidfile, "%d", rep->tids[i]+1);
      if (i > 0) fputs(rep->sep, rep->tidfile);
    } }                         /* report the transaction ids */
  else if (rep->tacnt  > 0) {   /* if item occurrence counters */
    min = rep->cnt -rep->miscnt;/* traverse all transaction ids */
    for (i = k = 0; i < rep->tacnt; i++) {
      if (rep->tids[i] < min)   /* skip all transactions that */
        continue;               /* do not contain enough items */
      if (k++ > 0) fputs(rep->sep, rep->tidfile);
      fprintf(rep->tidfile, "%d", i+1);    /* print transaction id */
      if (rep->miscnt <= 0) continue;
      fputc(':', rep->tidfile); /* print an item counter separator */
      fprintf(rep->tidfile, "%d", rep->tids[i]);
    }                           /* print number of contained items */
  }
  fputc('\n', rep->tidfile);    /* terminate the transaction id list */
}  /* _output() */

/*--------------------------------------------------------------------*/

static void _report (ISREPORT *rep, int k)
{                               /* --- recursively report item sets */
  assert(rep && (k > 0));       /* check the function arguments */
  do {                          /* traverse the perfect extensions */
    rep->items[rep->cnt++] = rep->pexs[--k];
    rep->supps[rep->cnt]   = rep->supps[rep->cnt-1];
    if (rep->wgts) rep->wgts[rep->cnt] = rep->wgts[rep->cnt-1];
    if ((k > 0)                 /* if another item can be added */
    &&  (rep->cnt >= rep->min-k)/* and a valid size can be reached */
    &&  (rep->cnt <  rep->max)) /* (in the interval [min, max]), */
      _report(rep, k);          /* recurse for remaining item sets */
    if  (rep->cnt >= rep->min)  /* if it has the req. minimum size, */
      _output(rep);             /* report the current item set */
    if (--rep->cnt < rep->pfx)  /* remove the current item again */
      rep->pfx = rep->cnt;      /* and adapt the valid prefix */
  } while (k > 0);              /* while not all items are processed */
}  /* _report() */

/*--------------------------------------------------------------------*/

int isr_report (ISREPORT *rep)
{                               /* --- report the current item set */
  int    k, n;                  /* number of perfect exts., buffer */
  int    min, max;              /* min. and max. item set size */
  #ifdef ISR_CLOMAX             /* if closed/maximal filtering */
  SUPP_T s, r;                  /* support buffers */
  int    *items;                /* item set for prefix tree update */
  #endif

  assert(rep);                  /* check the function argument */
  if (rep->cnt > rep->max)      /* if the item set is too large, */
    return 0;                   /* abort the function */
  #ifdef ISR_CLOMAX             /* if closed/maximal filtering */
  if (rep->clomax) {            /* if a closed/maximal filter exists */
    s = rep->supps[n = rep->cnt]; /* get the support of the item set */
    r = cm_supp(rep->clomax);     /* and the maximal known support */
    if (r >= s) return 0;       /* check if item set is not closed */
    k = rep->items -rep->pexs;  /* get number of perfect extensions */
    if (k <= 0)                 /* if there are no perfect extensions */
      items = rep->items;       /* use the items directly */
    else {                      /* if there are perfect extensions, */
      n += k;                   /* copy all items to add. buffer */
      memcpy(items = rep->buf, rep->pexs, n *sizeof(int));
      int_qsort(items, n);      /* sort the copied items */
      if (cm_dir(rep->clomax) < 0) int_reverse(items, n);
    }                           /* respect the repository item order */
    if (cm_update(rep->clomax, items, n, s) < 0)
      return -1;                /* add the item set to the filter */
    if ((rep->mode & ISR_MAXIMAL) && (r > 0))
      return  0;                /* check for a non-maximal item set */
  }                             /* (if the known support is > 0) */
  #endif
  k   = rep->items -rep->pexs;  /* get number of perfect extensions */
  max = rep->cnt +k;            /* compute the maximum set size and */
  min = rep->min;               /* check whether the minimum size */
  if (max < min) return 0;      /* can be reached with perfect exts. */
  if (rep->mode & ISR_NOEXP) {  /* if not to expand perfect exts. */
    if (max > rep->max) max = rep->max;
    if (max > rep->min) rep->min = max;
  }                             /* adapt the minimal item set size */
  n = rep->rep;                 /* note the number of reported sets */
  if ((k > 0)                   /* if there are perfect extensions */
  &&  (rep->cnt < rep->max))    /* and maximum size not yet reached, */
    _report(rep, k);            /* recursively add and report them */
  if (rep->cnt >= rep->min)     /* if the item set is large enough, */
    _output(rep);               /* report the current item set */
  rep->min = min;               /* restore the minimum item set size */
  return rep->rep -n;           /* return number of rep. item sets */
}  /* isr_report() */

/*--------------------------------------------------------------------*/

int isr_reportx (ISREPORT *rep, int *tids, int n)
{                               /* --- report the current item set */
  assert(rep);                  /* check the function arguments */
  rep->tids   = tids;           /* store the transaction id array */
  rep->tidcnt = n;              /* and the number of transaction ids */
  n = isr_report(rep);          /* report the current item set */
  rep->tids   = NULL;           /* clear the transaction id array */
  return n;                     /* return number of rep. item sets */
}  /* isr_reportx() */

/*--------------------------------------------------------------------*/

void isr_direct (ISREPORT *rep, const int *items, int n,
                 SUPP_T supp, double wgt, double eval)
{                               /* --- report an item set */
  int c;                        /* buffer for the item counter */

  assert(rep                    /* check the function arguments */
  &&    (items || (n <= 0)) && (supp >= 0));
  if ((n < rep->min) || (n > rep->max))
    return;                     /* check the item set size */
  rep->rep++;                   /* count the reported item set */
  if (!rep->file) return;       /* check for an output file */
  c = rep->cnt; rep->cnt = n;   /* note the number of items */
  fputs(rep->hdr, rep->file);   /* print the record header */
  if (n > 0)                    /* print the first item */
    fputs(rep->inames[*items++], rep->file);
  while (--n > 0) {             /* traverse the remaining items */
    fputs(rep->sep, rep->file); /* print an item separator */
    fputs(rep->inames[*items++], rep->file);
  }                             /* print the next item */
  isr_sinfo(rep, supp, wgt, eval);
  fputc('\n', rep->file);       /* print the item set information */
  rep->cnt = c;                 /* restore the number of items */
}  /* isr_direct() */

/*--------------------------------------------------------------------*/

void isr_directx (ISREPORT *rep, const int *items, int n, double *iwgts,
                  SUPP_T supp, double wgt, double eval)
{                               /* --- report an item set */
  int c;                        /* buffer for the item counter */

  assert(rep                    /* check the function arguments */
  &&    (items || (n <= 0)) && (supp >= 0));
  if ((n < rep->min) || (n > rep->max))
    return;                     /* check the item set size */
  rep->rep++;                   /* count the reported item set */
  if (!rep->file) return;       /* check for an output file */
  c = rep->cnt; rep->cnt = n;   /* note the number of items */
  fputs(rep->hdr, rep->file);   /* print the record header */
  if (n > 0) {                  /* if at least one item */
    fputs(rep->inames[*items], rep->file);
    isr_wgtout(rep, supp, *iwgts);
  }                             /* print first item and item weight */
  while (--n > 0) {             /* traverse the remaining items */
    fputs(rep->sep, rep->file); /* print an item separator */
    fputs(rep->inames[*++items], rep->file);
    isr_wgtout(rep, supp, *++iwgts);
  }                             /* print next item and item weight */
  isr_sinfo(rep, supp, wgt, eval);
  fputc('\n', rep->file);       /* print the item set information */
  rep->cnt = c;                 /* restore the number of items */
}  /* isr_directx() */

/*--------------------------------------------------------------------*/

void isr_rule (ISREPORT *rep, const int *items, int n,
               SUPP_T supp, SUPP_T body, SUPP_T head, double eval)
{                               /* --- report an association rule */
  int c;                        /* buffer for the item counter */

  assert(rep                    /* check the function arguments */
  &&     items && (n > 0) && (supp >= 0));
  if ((n < rep->min) || (n > rep->max))
    return;                     /* check the item set size */
  rep->rep++;                   /* count the reported item set */
  if (!rep->file) return;       /* check for an output file */
  c = rep->cnt; rep->cnt = n;   /* note the number of items */
  fputs(rep->hdr, rep->file);   /* print the record header */
  fputs(rep->inames[*items++], rep->file);
  fputs(rep->imp, rep->file);   /* print the rule head and imp. sign */
  if (--n > 0)                  /* print the first item in body */
    fputs(rep->inames[*items++], rep->file);
  while (--n > 0) {             /* traverse the remaining items */
    fputs(rep->sep, rep->file); /* print an item separator */
    fputs(rep->inames[*items++], rep->file);
  }                             /* print the next item */
  isr_rinfo(rep, supp, body, head, eval);
  fputc('\n', rep->file);       /* print the item set information */
  rep->cnt = c;                 /* restore the number of items */
}  /* isr_rule() */

/*--------------------------------------------------------------------*/

int isr_intout (ISREPORT *rep, int num)
{                               /* --- print an integer number */
  int  i, s;                    /* loop variable, sign flag */
  char buf[BS_INT];             /* output buffer */

  assert(rep && rep->file);     /* check the function arguments */
  if (num == 0) {               /* treat zero as a special case */
    fputc('0', rep->file); return 1; }
  if (num <= INT_MIN)           /* treat INT_MIN as a special case */
    return fwrite("-2147483648", sizeof(char), 11, rep->file);
  s = 0;                        /* default: no sign printed */
  if (num < 0) {                /* if the number is negative, */
    fputc('-', rep->file); s = 1; }   /* print a leading sign */
  num = abs(num);               /* remove the sign (just printed) */
  i   = BS_INT;                 /* start at the end of the buffer */
  do {                          /* digit output loop */
    buf[--i] = (num % 10) +'0'; /* store the next digit and */
    num /= 10;                  /* remove it from the number */
  } while (num > 0);            /* while there are more digits */
  fwrite(buf+i, sizeof(char), BS_INT-i, rep->file);
  return BS_INT -i +s;          /* print the digits and */
}  /* isr_intout() */           /* return the number of characters */

/*--------------------------------------------------------------------*/

int isr_numout (ISREPORT *rep, double num, int dec)
{                               /* --- print a floating point number */
  int    i, s;                  /* loop variable, sign flag */
  double x, r;                  /* integer value and fraction */
  char   buf[BS_FLOAT];         /* output buffer */

  assert(rep && rep->file);     /* check the function arguments */
  s = 0;                        /* default: no sign printed */
  if (num < 0) {                /* if the number is negative, */
    fputc('-', rep->file); s = 1; }   /* print a leading sign */
  num = fabs(num);              /* remove the sign (just printed) */
  if (num >= DBL_MAX)           /* check for an infinite value */
    return fputs("infinity", rep->file);
  if (dec > 32) dec = 32;       /* limit the number of decimals */
  r = (dec & 1) ? 0.05 : 0.5; x = 0.1;
  for (i = dec >> 1; i > 0; i >>= 1) {
    x *= x; if (i & 1) r *= x;} /* compute value for rounding and */
  num += r;                     /* round number for given decimals */
  num -= x = floor(num);        /* get integer part and fraction */
  i = BS_FLOAT;                 /* start at the end of the buffer */
  do {                          /* digit output loop */
    if (--i < 0) break;         /* prevent a buffer overflow */
    buf[i] = (char)fmod(x, 10) +'0';
    x = floor(x/10);            /* compute and store next digit */
  } while (x > 0);              /* while there are more digits */
  fwrite(buf+i, sizeof(char), BS_FLOAT-i, rep->file);
  i = BS_FLOAT -i +s;           /* print the stored characters */
  if (dec <= 0)                 /* if to print no decimals, */
    return i;                   /* return the number of characters */
  fputc('.', rep->file); i++;   /* print and count the decimal point */
  while (--dec >= 0) {          /* while to print more decimals */
    num *= 10;                  /* compute the next decimal */
    fputc((int)num +'0', rep->file); i++;
    num -= floor(num);          /* print and count the next decimal */
  }                             /* and remove it from the number */
  return i;                     /* return the number of characters */
}  /* isr_numout() */

/*--------------------------------------------------------------------*/

static int _getdec (const char *s, const char **end)
{                               /* --- get number of decimal places */
  int k = 0;                    /* number of decimal places */

  assert(s && end);             /* check the function arguments */
  if ((*s >= '0') && (*s <= '9')) {
    k = *s++ -'0';              /* get the first digit */
    if ((*s >= '0') && (*s <= '9'))
      k = 10 *k +*s++ -'0';     /* get a possible second digit and */
  }                             /* compute the number of decimals */
  *end = s; return k;           /* return  the number of decimals */
}  /* _getdec() */

/*--------------------------------------------------------------------*/

int isr_wgtout (ISREPORT *rep, SUPP_T supp, double wgt)
{                               /* --- print an item weight */
  int        k, n = 0;          /* number of decimals, char. counter */
  const char *s, *t;            /* to traverse the format */

  assert(rep);                  /* check the function arguments */
  if (!rep->iwfmt || !rep->file)
    return 0;                   /* check for a given format and file */
  for (s = rep->iwfmt; *s; ) {  /* traverse the output format */
    if (*s != '%') {            /* copy everything except '%' */
      fputc(*s++, rep->file); n++; continue; }
    t = s++; k = _getdec(s,&s); /* get the number of decimal places */
    switch (*s++) {             /* evaluate the indicator character */
      case '%': fputc('%', rep->file); n++;                    break;
      case 'w': n += isr_numout(rep, wgt,      k);             break;
      case 'm': n += isr_numout(rep, wgt/supp, k);             break;
      case  0 : --s;            /* print the requested quantity */
      default : while (t < s) { fputc(*t++, rep->file); n++; } break;
    }                           /* otherwise copy characters */
  }
  return n;                     /* return the number of characters */
}  /* isr_wgtout() */

/*--------------------------------------------------------------------*/

int isr_sinfo (ISREPORT *rep, SUPP_T supp, double wgt, double eval)
{                               /* --- print item set information */
  int        k, n = 0;          /* number of decimals, char. counter */
  double     smax, wmax;        /* maximum support and weight */
  const char *s, *t;            /* to traverse the format */

  assert(rep);                  /* check the function arguments */
  if (!rep->format || !rep->file)
    return 0;                   /* check for a given format and file */
  smax = rep->supps[0];         /* get maximum support and */
  if (smax <= 0) smax = 1;      /* avoid divisions by zero */
  wmax = (rep->wgts) ? rep->wgts[0] : smax;
  if (wmax <= 0) wmax = 1;      /* get maximum weight (if available) */
  for (s = rep->format; *s; ) { /* traverse the output format */
    if (*s != '%') {            /* copy everything except '%' */
      fputc(*s++, rep->file); n++; continue; }
    t = s++; k = _getdec(s,&s); /* get the number of decimal places */
    switch (*s++) {             /* evaluate the indicator character */
      case '%': fputc('%', rep->file); n++;                    break;
      case 'i': n += isr_intout(rep,      rep->cnt);           break;
      case 'n':
      #define double 0
      #define int    1
      #if SUPP_T==int
      case 'a': n += isr_intout(rep,      supp);               break;
      #else
      case 'a': n += isr_numout(rep,      supp,       k);      break;
      #endif
      #undef int
      #undef double
      case 's': n += isr_numout(rep,      supp/smax,  k);      break;
      case 'S': n += isr_numout(rep, 100*(supp/smax), k);      break;
      case 'x': n += isr_numout(rep,      supp/smax,  k);      break;
      case 'X': n += isr_numout(rep, 100*(supp/smax), k);      break;
      case 'w': n += isr_numout(rep,      wgt,        k);      break;
      case 'W': n += isr_numout(rep, 100* wgt,        k);      break;
      case 'r': n += isr_numout(rep,      wgt /wmax,  k);      break;
      case 'R': n += isr_numout(rep, 100*(wgt /wmax), k);      break;
      case 'p': n += isr_numout(rep,      wgt,        k);      break;
      case 'P': n += isr_numout(rep, 100* wgt,        k);      break;
      case 'z': n += isr_numout(rep, 100*(wgt *smax), k);      break;
      case 'e': n += isr_numout(rep,      eval,       k);      break;
      case 'E': n += isr_numout(rep, 100* eval,       k);      break;
      case  0 : --s;            /* print the requested quantity */
      default : while (t < s) { fputc(*t++, rep->file); n++; } break;
    }                           /* otherwise copy characters */
  }
  return n;                     /* return the number of characters */
}  /* isr_sinfo() */

/*--------------------------------------------------------------------*/

int isr_rinfo (ISREPORT *rep, SUPP_T supp, SUPP_T body, SUPP_T head,
               double eval)
{                               /* --- print ass. rule information */
  int        k, n = 0;          /* number of decimals, char. counter */
  double     smax;              /* maximum support */
  double     conf, lift;        /* buffers for computations */
  const char *s, *t;            /* to traverse the format */

  assert(rep);                  /* check the function arguments */
  if (!rep->format || !rep->file)
    return 0;                   /* check for a given format and file */
  smax = rep->supps[0];         /* get the total transaction weight */
  if (smax <= 0) smax = 1;      /* avoid divisions by zero */
  for (s = rep->format; *s; ) { /* traverse the output format */
    if (*s != '%') {            /* copy everything except '%' */
      fputc(*s++, rep->file); n++; continue; }
    t = s++; k = _getdec(s,&s); /* get the number of decimal places */
    switch (*s++) {             /* evaluate the indicator character */
      case '%': fputc('%', rep->file); n++;                    break;
      case 'n':
      #define double 0
      #define int    1
      #if SUPP_T==int
      case 'a': n += isr_intout(rep,      supp);               break;
      case 'b': n += isr_intout(rep,      body);               break;
      case 'h': n += isr_intout(rep,      head);               break;
      #else
      case 'a': n += isr_numout(rep,      supp,       k);      break;
      case 'b': n += isr_numout(rep,      body,       k);      break;
      case 'h': n += isr_numout(rep,      head,       k);      break;
      #endif
      #undef int
      #undef double
      case 's': n += isr_numout(rep,      supp/smax,  k);      break;
      case 'S': n += isr_numout(rep, 100*(supp/smax), k);      break;
      case 'x': n += isr_numout(rep,      body/smax,  k);      break;
      case 'X': n += isr_numout(rep, 100*(body/smax), k);      break;
      case 'y': n += isr_numout(rep,      head/smax,  k);      break;
      case 'Y': n += isr_numout(rep, 100*(head/smax), k);      break;
      case 'c': conf = (body > 0) ? supp/(double)body : 0;
                n += isr_numout(rep,      conf,      k);       break;
      case 'C': conf = (body > 0) ? supp/(double)body : 0;
                n += isr_numout(rep, 100* conf,      k);       break;
      case 'l': lift = ((body > 0) && (head > 0))
                     ? (supp*smax) /(body*(double)head) : 0;
                n += isr_numout(rep,      lift,      k);       break;
      case 'L': lift = ((body > 0) && (head > 0))
                     ? (supp*smax) /(body*head) : 0;
                n += isr_numout(rep, 100* lift,      k);       break;
      case 'e': n += isr_numout(rep,      eval,      k);       break;
      case 'E': n += isr_numout(rep, 100* eval,      k);       break;
      case  0 : --s;            /* print the requested quantity */
      default : while (t < s) { fputc(*t++, rep->file); n++; } break;
    }                           /* otherwise copy characters */
  }
  return n;                     /* return the number of characters */
}  /* isr_rinfo() */

/*--------------------------------------------------------------------*/

void isr_getinfo (ISREPORT *rep, const char *sel, double *vals)
{                               /* --- get item set information */
  SUPP_T supp;                  /* support of current item set */
  double wgt;                   /* weight of current item set */
  double smax, wmax;            /* maximum support and weight */

  supp = rep->supps[rep->cnt];  /* get the set support and weight */
  wgt  = (rep->wgts) ? rep->wgts[rep->cnt] : 0;
  smax = rep->supps[0];         /* get maximum support and */
  if (smax <= 0) smax = 1;      /* avoid divisions by zero */
  wmax = (rep->wgts) ? rep->wgts[0] : smax;
  if (wmax <= 0) wmax = 1;      /* get maximum weight (if available) */
  for (; *sel; sel++, vals++) { /* traverse the information selectors */
    switch (*sel) {             /* and evaluate them */
      case 'i': *vals = (double)rep->cnt; break;
      case 'n': *vals = (double)supp;     break;
      case 's': *vals =      supp/smax;   break;
      case 'S': *vals = 100*(supp/smax);  break;
      case 'x': *vals =      supp/smax;   break;
      case 'X': *vals = 100*(supp/smax);  break;
      case 'w': *vals =      wgt;         break;
      case 'W': *vals = 100* wgt;         break;
      case 'r': *vals =      wgt /wmax;   break;
      case 'R': *vals = 100*(wgt /wmax);  break;
      case 'p': *vals =      wgt;         break;
      case 'P': *vals = 100* wgt;         break;
      case 'z': *vals = 100*(wgt *smax);  break;
      case 'e': *vals =      rep->eval;   break;
      case 'E': *vals = 100* rep->eval;   break;
    }                           /* store the corresponding value */
  }                             /* in the output vector */
}  /* isr_getinfo() */
