/*----------------------------------------------------------------------
  File    : report.h
  Contents: item set reporter management
  Author  : Christian Borgelt
  History : 2008.08.18 item set reporter created in tract.[ch]
            2008.08.30 handling of perfect extensions completed
            2008.09.01 handling of closed and maximal item sets added
            2008.10.15 function isr_xable() added (reporter extendable)
            2008.10.30 transaction identifier reporting added
            2008.10.31 item set reporter made a separate module
            2009.10.15 counter for reported item sets added
            2009.10.16 functions isr_wgt() and isr_wgtx() added
            2010.04.07 extended information reporting functions removed
            2010.07.02 function isr_all() added for easier access
            2010.07.22 adapted to closed/maximal item set filter
            2010.08.06 function isr_direct() for direct reporting added
            2010.08.11 function isr_directx() for extended items added
            2010.08.14 item set header for output added to isr_create()
            2010.10.15 functions isr_open(), isr_close(), isr_rule()
            2011.05.06 generalized to support type SUPP_T (int/double)
            2011.06.10 function isr_wgtsupp() added (weight/support)
            2011.07.21 function isr_evalwgt() added (for accretion)
            2011.07.23 parameter dir added to function isr_seteval()
----------------------------------------------------------------------*/
#ifndef __REPORT__
#define __REPORT__
#include <limits.h>
#include "tract.h"
#ifndef SUPP_T
#define SUPP_T int
#endif
#ifdef ISR_CLOMAX
#include "clomax.h"
#endif

/*----------------------------------------------------------------------
  Preprocessor Definitions
----------------------------------------------------------------------*/
/* --- report modes --- */
#define ISR_ALL        0x00     /* report all frequent item sets */
#define ISR_CLOSED     0x01     /* report only closed  item sets */
#define ISR_MAXIMAL    0x02     /* report only maximal item sets */
#define ISR_RULE       0x04     /* association rules */
#define ISR_NOEXP      0x08     /* do not expand perfect extensions */
#define ISR_WEIGHTS    0x10     /* allow for item set weights */
#define ISR_LOGS       0x20     /* compute sums of logarithms */
#define ISR_SCAN       0x40     /* report in scanable form */

/* --- delete modes --- */
#define ISR_DELISET    0x01     /* delete the item set */
#define ISR_FCLOSE     0x02     /* close the output file(s) */

/*----------------------------------------------------------------------
  Type Definitions
----------------------------------------------------------------------*/
struct _isreport;               /* --- an item set eval. function --- */
typedef double ISEVALFN (struct _isreport *rep, void *data);
typedef void   ISREPOFN (struct _isreport *rep, void *data);

typedef struct _isreport {      /* --- an item set reporter --- */
  ITEMBASE   *base;             /* underlying item base */
  FILE       *file;             /* output file to write to */
  const char *name;             /* name of item set output file */
  int        mode;              /* reporting mode (e.g. ISR_CLOSED) */
  int        rep;               /* number of reported item sets */
  int        min;               /* minimum number of items in set */
  int        max;               /* maximum number of items in set */
  int        cnt;               /* current number of items in set */
  int        pfx;               /* number of items in valid prefix */
  int        *items;            /* current item set (array of items) */
  int        *pexs;             /* perfect extension items */
  int        *pxpp;             /* number of perfect exts. per prefix */
  SUPP_T     *supps;            /* (prefix) item sets support values */
  double     *wgts;             /* (prefix) item sets weights */
  #ifdef ISR_CLOMAX             /* if to filter closed/maximal sets */
  int        *buf;              /* additional item set buffer */
  CLOMAX     *clomax;           /* closed/maximal item set filter */
  #endif
  FILE       *tidfile;          /* output file for transaction ids */
  const char *tidname;          /* name of tid output file */
  int        *tids;             /* array  of transaction ids */
  int        tidcnt;            /* number of transaction ids */
  int        tacnt;             /* total number of transactions */
  int        miscnt;            /* accepted number of missing items */
  double     logwgt;            /* logarithm of total trans. weight */
  double     *logs;             /* logarithms of item frequencies */
  double     *sums;             /* sums of logarithms for prefixes */
  ISEVALFN   *evalfn;           /* additional evaluation function */
  void       *evaldat;          /* additional evaluation data */
  double     evalthh;           /* threshold of evaluation */
  int        evaldir;           /* direction of evaluation */
  double     eval;              /* additional evaluation value */
  ISREPOFN   *repofn;           /* item set reporting function */
  void       *repodat;          /* item set reporting data */
  const char *hdr;              /* record header for output */
  const char *sep;              /* item separator for output */
  const char *imp;              /* implication sign for rule output */
  const char *iwfmt;            /* format for item weight output */
  const char *format;           /* format for information output */
  const char **inames;          /* (formatted) item names */
  char       *out;              /* output buffer for sets/rules */
  char       *pos[1];           /* append positions in output buffer */
} ISREPORT;                     /* (item set reporter) */

/*----------------------------------------------------------------------
  Functions
----------------------------------------------------------------------*/
extern ISREPORT*  isr_create   (ITEMBASE *base, int mode, int dir,
                                CCHAR *hdr, CCHAR *sep, CCHAR *imp);
extern int        isr_delete   (ISREPORT *rep, int mode);
extern ITEMBASE*  isr_base     (ISREPORT *rep);
extern int        isr_mode     (ISREPORT *rep);
extern int        isr_open     (ISREPORT *rep, FILE *file, CCHAR *name);
extern int        isr_close    (ISREPORT *rep);
extern FILE*      isr_file     (ISREPORT *rep);
extern CCHAR*     isr_name     (ISREPORT *rep);

extern void       isr_setfmt   (ISREPORT *rep, const char *format);
extern void       isr_setiwf   (ISREPORT *rep, const char *format);
extern void       isr_setsize  (ISREPORT *rep, int min, int max);
extern void       isr_seteval  (ISREPORT *rep, ISEVALFN evalfn,
                                void *data, double thresh, int dir);
extern void       isr_setrepo  (ISREPORT *rep, ISREPOFN repofn,
                                void *data);
extern int        isr_tidopen  (ISREPORT *rep, FILE *file, CCHAR *name);
extern int        isr_tidclose (ISREPORT *rep);
extern void       isr_tidcfg   (ISREPORT *rep, int tacnt, int miscnt);
extern FILE*      isr_tidfile  (ISREPORT *rep);
extern CCHAR*     isr_tidname  (ISREPORT *rep);
extern void       isr_setsupp  (ISREPORT *rep, int supp);
extern void       isr_setwgt   (ISREPORT *rep, double wgt);
extern int        isr_min      (ISREPORT *rep);
extern int        isr_max      (ISREPORT *rep);

extern int        isr_add      (ISREPORT *rep, int item, SUPP_T supp);
extern int        isr_addx     (ISREPORT *rep, int item, SUPP_T supp,
                                double wgt);
extern int        isr_addpex   (ISREPORT *rep, int item);
extern int        isr_uses     (ISREPORT *rep, int item);
extern int        isr_remove   (ISREPORT *rep, int n);
extern int        isr_xable    (ISREPORT *rep, int n);

extern int        isr_cnt      (ISREPORT *rep);
extern int        isr_item     (ISREPORT *rep);
extern int        isr_itemx    (ISREPORT *rep, int index);
extern const int* isr_items    (ISREPORT *rep);
extern int        isr_supp     (ISREPORT *rep);
extern int        isr_suppx    (ISREPORT *rep, int index);
extern double     isr_wgt      (ISREPORT *rep);
extern double     isr_wgtx     (ISREPORT *rep, int index);
extern double     isr_logsum   (ISREPORT *rep);
extern double     isr_logsumx  (ISREPORT *rep, int index);
extern double     isr_eval     (ISREPORT *rep);

extern int        isr_pexcnt   (ISREPORT *rep);
extern int        isr_pex      (ISREPORT *rep, int index);
extern const int* isr_pexs     (ISREPORT *rep);
extern int        isr_all      (ISREPORT *rep);
extern int        isr_lack     (ISREPORT *rep);

extern CCHAR*     isr_itname   (ISREPORT *rep, int item);
extern CCHAR*     isr_sep      (ISREPORT *rep);
extern CCHAR*     isr_impl     (ISREPORT *rep);

extern double     isr_log      (ISREPORT *rep, int item);
extern double     isr_logwgt   (ISREPORT *rep);

extern double     isr_logrto   (ISREPORT *rep, void *data);
extern double     isr_logsize  (ISREPORT *rep, void *data);
extern double     isr_sizewgt  (ISREPORT *rep, void *data);
extern double     isr_wgtsize  (ISREPORT *rep, void *data);
extern double     isr_wgtsupp  (ISREPORT *rep, void *data);
extern double     isr_evalwgt  (ISREPORT *rep, void *data);

extern int        isr_report   (ISREPORT *rep);
extern int        isr_reportx  (ISREPORT *rep, int *tids, int n);
extern void       isr_direct   (ISREPORT *rep, const int *items, int n,
                                SUPP_T supp, double wgt, double eval);
extern void       isr_directx  (ISREPORT *rep, const int *items, int n,
                                double *iwgts,
                                SUPP_T supp, double wgt, double eval);
extern void       isr_rule     (ISREPORT *rep, const int *items, int n,
                                SUPP_T supp, SUPP_T body, SUPP_T head,
                                double eval);
extern int        isr_repcnt   (ISREPORT *rep);

extern int        isr_intout   (ISREPORT *rep, int num);
extern int        isr_numout   (ISREPORT *rep, double num, int dec);
extern int        isr_wgtout   (ISREPORT *rep, SUPP_T supp, double wgt);
extern int        isr_sinfo    (ISREPORT *rep, SUPP_T supp, double wgt,
                                double eval);
extern int        isr_rinfo    (ISREPORT *rep, SUPP_T supp,
                                SUPP_T body, SUPP_T head, double eval);
extern void       isr_getinfo  (ISREPORT *rep, const char *sel,
                                double *vals);

#ifdef ISR_CLOMAX
extern int*       isr_buf      (ISREPORT *rep);
extern int        isr_tail     (ISREPORT *rep, const int *items, int n);
#endif

/*----------------------------------------------------------------------
  Preprocessor Definitions
----------------------------------------------------------------------*/
#define isr_base(r)       ((r)->base)
#define isr_mode(r)       ((r)->mode)
#define isr_file(r)       ((r)->file)
#define isr_name(r)       ((r)->name)

#define isr_setfmt(r,f)   ((r)->format = (f))
#define isr_setiwf(r,f)   ((r)->iwfmt  = (f))
#define isr_tidfile(r)    ((r)->tidfile)
#define isr_tidname(r)    ((r)->tidname)
#define isr_setsupp(r,s)  ((r)->supps[0] = (s))
#define isr_setwgt(r,w)   ((r)->wgts [0] = (w))
#define isr_min(r)        ((r)->min)
#define isr_max(r)        ((r)->max)

#define isr_uses(r,i)     ((r)->pxpp[i] < 0)
#define isr_xable(r,n)    ((r)->cnt+(n) <= (r)->max)

#define isr_cnt(r)        ((r)->cnt)
#define isr_item(r)       ((r)->items[(r)->cnt -1])
#define isr_itemx(r,i)    ((r)->items[i])
#define isr_items(r)      ((r)->items)
#define isr_supp(r)       ((r)->supps[(r)->cnt])
#define isr_suppx(r,i)    ((r)->supps[i])
#define isr_wgt(r)        ((r)->wgts [(r)->cnt])
#define isr_wgtx(r,i)     ((r)->wgts [i])
#define isr_logsum(r)     ((r)->sums [(r)->cnt])
#define isr_logsumx(r,i)  ((r)->sums [i])
#define isr_eval(r)       ((r)->eval)

#define isr_pexcnt(r)     ((int)((r)->items -(r)->pexs))
#define isr_pex(r,t)      ((r)->pexs [i])
#define isr_pexs(r)       ((r)->pexs)
#define isr_all(r)        ((r)->cnt +(int)((r)->items -(r)->pexs))
#define isr_lack(r)       ((r)->min -isr_all(r))

#define isr_itname(r,i)   ((r)->inames[i])
#define isr_sep(r)        ((r)->isep)
#define isr_impl(r)       ((r)->impl)

#define isr_log(r,i)      ((r)->logs[i])
#define isr_logwgt(r)     ((r)->logwgt)

#define isr_repcnt(r)     ((r)->rep)

#ifdef ISR_CLOMAX
#define isr_buf(r)        ((r)->buf)
#define isr_tail(r,i,n)   ((r)->clomax ? cm_tail((r)->clomax, i, n) : 0)
#endif

#endif
