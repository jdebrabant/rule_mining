/*----------------------------------------------------------------------
  File    : istree.c
  Contents: item set tree management
  Author  : Christian Borgelt
  History : 1996.01.22 file created
            1996.02.07 _child, _count, ist_addlvl, and ist_count
            1996.02.09 ist_rule() programmed and debugged
            1996.02.10 empty rule bodies made optional
            1996.03.28 support made relative to number of item sets
            1996.06.25 function _count() optimized
            1996.11.23 rule extraction redesigned
            1996.11.24 rule selection criteria added
            1997.08.18 chi^2 added, mincnt added to function ist_init()
            1998.01.15 confidence comparison changed to >=
            1998.01.23 integer support computation changed (ceil)
            1998.01.26 condition added to set extension in _child
            1998.02.10 bug in computation of IST_INFO fixed
            1998.02.11 parameter thresh added to function ist_init()
            1998.05.14 item set tree navigation functions added
            1998.08.08 item appearances considered for rule selection
            1998.08.20 deferred child node array allocation added
            1998.09.05 bug concerning node id fixed
            1998.09.22 bug in rule extraction fixed (item appearances)
            1998.09.23 computation of chi^2 measure simplified
            1999.08.25 rule extraction simplified
            1999.11.05 rule evaluation measure IST_LD21 added
            1999.11.08 parameter 'eval' added to function ist_rule()
            1999.11.11 rule consequents moved to first field
            1999.12.01 bug in node reallocation fixed
            2001.04.01 functions ist_set() and ist_getcntx() added
            2001.12.28 sort function moved to module tract
            2002.02.07 tree clearing removed, counting improved
            2002.02.08 child creation improved (check of body support)
            2002.02.10 APP_NONE bugs fixed (ist_set() and ist_hedge())
            2002.02.11 memory usage minimization option added
            2002.02.12 ist_first() and ist_last() replaced by ist_next()
            2002.02.19 transaction tree functions added
            2002.10.09 bug in function ist_hedge() fixed (conf. comp.)
            2003.07.17 check of item usage added (function ist_check())
            2003.07.18 maximally frequent item set filter added
            2003.08.11 item set filtering generalized (ist_mark())
            2003.08.15 renamed new to cur in ist_addlvl() (C++ compat.)
            2003.11.14 definition of F_HDONLY changed to INT_MIN
            2003.12.02 skipping unnecessary subtrees added (_checksub)
            2003.12.03 bug in ist_check() for rule mining fixed
            2003.12.12 padding for 64 bit architecture added
            2004.05.09 additional selection measure for sets added
            2004.12.09 bug in add. evaluation measure for sets fixed
            2006.11.26 support parameter changed to an absolute value
            2007.02.07 bug in function ist_addlvl() / _child() fixed
            2008.01.25 bug in filtering closed/maximal item sets fixed
            2008.03.13 additional rule evaluation redesigned
            2008.03.24 creation based on ITEMBASE structure
            2008.08.12 adapted to redesign of tract.[hc]
            2008.08.19 memory saving node structure simplified
            2008.08.21 function ist_report() added (recursive reporting)
            2008.09.01 parameter smax added to function ist_create()
            2008.09.07 ist_prune() added, memory saving always used
            2008.09.10 item set extraction and evaluation redesigned
            2008.09.11 pruning with evaluation measure added
            2008.11.19 adapted to modified transaction tree interface
            2008.12.02 bug in ist_create() fixed (support adaptation)
            2008.12.06 perfect extension pruning added (optional)
            2009.09.03 bugs in functions _countx and _report fixed
            2009.10.15 adapted to item set counter in reporter
            2009.11.13 optional zeroing of evaluation below expectation
            2010.03.02 bug in forward pruning fixed (executed too late)
            2010.06.17 rule evaluation aggregation without functions
            2010.06.18 filtering for increase of evaluation added
            2010.08.30 Fisher's exact test added as evaluation measure
            2010.10.22 chi^2 measure with Yates correction added
            2010.12.09 bug in child node pointer array allocation fixed
            2011.06.22 handling of roundoff errors in _fet_* improved
            2011.07.18 alternative transaction tree implementation added
            2011.07.25 threshold inverted for measures yielding p-values
----------------------------------------------------------------------*/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <float.h>
#include <math.h>
#include <assert.h>
#include "istree.h"
#include "chi2.h"
#include "gamma.h"
#ifdef STORAGE
#include "storage.h"
#endif

/*----------------------------------------------------------------------
  Preprocessor Definitions
----------------------------------------------------------------------*/
#define LN_2        0.69314718055994530942  /* ln(2) */
#define BLKSIZE     32          /* block size for level array */
#define F_HDONLY    INT_MIN     /* flag for head only item in path */
#define F_SKIP      INT_MIN     /* flag for subtree skipping */
#define ITEM(n)     ((int)((n)->item & ~F_HDONLY))
#define HDONLY(n)   ((int)((n)->item &  F_HDONLY))
#define COUNT(n)    ((n) & ~F_SKIP)
#define CHILDCNT(n) ((n)->chcnt & ~F_SKIP)

#ifdef ALIGN8                   /* if aligned 64 bit architecture, */
#define PAD(x)      ((x) & 1)   /* pad to an even number, */
#else                           /* otherwise (32 bit) */
#define PAD(x)      0           /* no padding is needed */
#endif
/* Note that not all 64 bit architectures need pointers to be aligned */
/* to addresses divisible by 8. Use ALIGN8 only if this is the case.  */

/*----------------------------------------------------------------------
  Auxiliary Functions
----------------------------------------------------------------------*/

static int _search (int id, ISNODE **chn, int n)
{                               /* --- find a child node (index) */
  int i, k, x;                  /* left and middle index */

  assert(chn && (n > 0));       /* check the function arguments */
  for (i = 0; i < n; ) {        /* while the range is not empty */
    k = (i+n) >> 1;             /* get index of the middle element */
    x = ITEM(chn[k]);           /* compare the item identifier */
    if      (id > x) i = k+1;   /* to the middle element and */
    else if (id < x) n = k;     /* adapt the range boundaries */
    else return k;              /* if there is an exact match, */
  }                             /* return the child node index */
  return -1-i;                  /* return the insertion position */
}  /* _search() */

/*--------------------------------------------------------------------*/

static int _getsupp (ISNODE *node, int *items, int n)
{                               /* --- get support of an item set */
  int    i, k;                  /* array indices, number of children */
  int    *map;                  /* item identifier map */
  ISNODE **chn;                 /* child node array */

  assert(node                   /* check the function arguments */
     && (n >= 0) && (items || (n <= 0)));
  while (--n > 0) {             /* follow the set/path from the node */
    k = CHILDCNT(node);         /* if there are no children, */
    if (k <= 0) return F_SKIP;  /* the support is less than minsupp */
    if (node->offset >= 0) {    /* if a pure array is used */
      chn = (ISNODE**)(node->cnts +node->size +PAD(node->size));
      i = *items++ -ITEM(chn[0]);   /* compute the child array index, */
      if (i >= k) return F_SKIP; }  /* abort if child does not exist */
    else {                      /* if an identifier map is used */
      chn = (ISNODE**)(node->cnts +node->size +node->size);
      i = _search(*items++, chn, k);
    }                           /* search for the proper index */
    if (i < 0) return F_SKIP;   /* abort if index is out of range */
    node = chn[i];              /* go to the corresponding child */
    if (!node) return F_SKIP;   /* if the child does not exists, */
  }                             /* the support is less than minsupp */
  if (node->offset >= 0) {      /* if a pure array is used, */
    i = *items -node->offset;   /* compute the counter index */
    if (i >= node->size) return F_SKIP; }
  else {                        /* if an identifier map is used */
    map = node->cnts +(k = node->size);
    i   = int_bsearch(*items, map, k);
  }                             /* search for the proper index */
  if (i < 0) return F_SKIP;     /* abort if index is out of range */
  return node->cnts[i];         /* return the item set support */
}  /* _getsupp() */

/*----------------------------------------------------------------------
  Item Set Evaluation Functions
----------------------------------------------------------------------*/

static double _ld_ratio (ISTREE *ist, ISNODE *node, int index)
{                               /* --- logarithm of support ratio */
  int    n, item;               /* loop variable, current item */
  int    *cnts;                 /* array of item frequencies */
  double sum;                   /* sum of logs. of item frequencies */

  assert(ist);                  /* check the function argument */
  cnts = ist->lvls[0]->cnts;    /* get item freqs. and set support */
  if (node->offset >= 0) item = node->offset +index;
  else                   item = node->cnts[node->size +index];
  sum = log((double)COUNT(node->cnts[index]))
      - log((double)COUNT(cnts[item]));
  for (n = 0; node->parent; node = node->parent) {
    sum -= log((double)COUNT(cnts[ITEM(node)])); n++; }
  if (n > 0) sum += n *log((double)COUNT(ist->wgt));
  return sum /LN_2;             /* sum logs. of item frequencies and */
}  /* _ld_ratio() */            /* subtract from log. of set freq. */

/*--------------------------------------------------------------------*/

static double _evaluate (ISTREE *ist, ISNODE *node, int index)
{                               /* --- average rule confidence */
  int       n, i;               /* loop variable, buffer */
  int       item;               /* current (head) item */
  int       supp;               /* support of item set */
  int       body, head;         /* support of rule body and head */
  int       base;               /* total transaction weight */
  int       *path;              /* path to follow for body support */
  ISNODE    *curr;              /* to traverse the nodes on the path */
  ISNODE    **chn;              /* child node array */
  RULEVALFN *refn;              /* rule evaluation function */
  double    val, sum;           /* (aggregated) value of measure */

  assert(ist && node);          /* check the function arguments */
  if (ist->eval <= IST_NONE)    /* if no evaluation measure is given, */
    return 0;                   /* the evaluation is always 0 */
  if (ist->eval >= IST_LDRATIO) /* check for log ratio evaluation */
    return _ld_ratio(ist, node, index);
  if (index >= 0) {             /* if an item index is given */
    item  = (node->offset >= 0) ? node->offset +index
          : node->cnts[node->size +index]; }
  else {                        /* determine the item identifier */
    item  = index & ~INT_MIN;   /* if an item identifier is given */
    index = (node->offset >= 0) ? item -node->offset
          : int_bsearch(item, node->cnts +node->size, node->size);
  }                             /* determine the item index */
  supp = COUNT(node->cnts[index]);
  head = COUNT(ist->lvls[0]->cnts[item]);
  base = COUNT(ist->wgt);       /* get the different support values */
  refn = re_function(ist->eval);/* and the evaluation function */
  curr = node->parent;          /* get subset support from parent */
  if (!curr) {                  /* if there is no parent (root node) */
    if (ist->invbxs && (head >= supp)) return 0;
    return refn(supp, base, head, base);
  }                             /* evaluate the item set directly */
  if (curr->offset >= 0)        /* if a pure array is used */
    body = COUNT(curr->cnts[ITEM(node) -curr->offset]);
  else {                        /* if an identifier map is used */
    path = curr->cnts +(n = curr->size);
    body = COUNT(curr->cnts[int_bsearch(ITEM(node), path, n)]);
  }                             /* find index and get subset support */
  sum = (ist->invbxs && (head *(double)body >= supp *(double)base))
      ? 0 : refn(supp, body, head, base);
  if (ist->agg <= IST_FIRST) {  /* compute the first measure value */
    if ((ist->minimp <= -DBL_MAX) || (index < 0))
      return sum;               /* check whether to return it */
    val = sum -_evaluate(ist, curr, node->item | INT_MIN);
    return (ist->dir *val < ist->minimp) ? -DBL_MAX : sum;
  }                             /* check for sufficient increase */
  path = ist->buf +ist->maxht;  /* initialize the path */
  *--path = item; n = 1;        /* for the support retrieval */
  item = ITEM(node);            /* get the next head item */
  for (i = 0; curr; curr = curr->parent) {
    head = COUNT(ist->lvls[0]->cnts[item]);
    body = COUNT(_getsupp(curr, path, n));
    val  = (ist->invbxs && (head *(double)body >= supp *(double)base))
         ? 0 : refn(supp, body, head, base);
    if      (ist->agg == IST_MIN) {
      if (val < sum) { sum = val; i = n; } }
    else if (ist->agg == IST_MAX) {
      if (val > sum) { sum = val; i = n; } }
    else sum += val;            /* compute the rule evaluation */
    *--path = item; ++n;        /* and aggregate it by min/max/avg */
    item = ITEM(curr);          /* then extend the path */
  }                             /* (store the head item) */
  if (ist->agg >= IST_AVG)      /* if to average the evaluations, */
    sum /= n;                   /* divide by the number of items */
  else if ((ist->minimp > -DBL_MAX)
  && (n > 2) && (index >= 0)) { /* if to check evaluation increase */
    item  = (i > 0) ? path[n-1] : node->item;
    path += n-i; n = i;         /* get the item best subset and */
    curr  = node->parent;       /* find the corresp. ancestor node */
    while (--i > 0) curr = curr->parent;
    while (--n > 0) {           /* follow the set/path from the node */
      if (curr->offset >= 0) {  /* if a pure counter array is used */
        chn = (ISNODE**)(curr->cnts +curr->size +PAD(curr->size));
        i   = *path++ -ITEM(chn[0]); }     /* compute the index */
      else {                    /* if an identifier map is used */
        chn = (ISNODE**)(curr->cnts +curr->size +curr->size);
        i   = _search(*path++, chn, CHILDCNT(curr));
      }                         /* search for the proper index */
      curr = chn[i];            /* go to the corresponding child */
    }                           /* until item set node is reached */
    val = sum -_evaluate(ist, curr, item | INT_MIN);
    if (ist->dir *val < ist->minimp) return -DBL_MAX;
  }                             /* check for sufficient increase */
  return sum;                   /* return the measure aggregate */
}  /* _evaluate() */

/*----------------------------------------------------------------------
  Auxiliary Functions
----------------------------------------------------------------------*/

static void _count (ISNODE *node,
                    const int *items, int n, int wgt, int min)
{                               /* --- count transaction recursively */
  int    i, k, o;               /* array index, offset, map size */
  int    *map;                  /* item identifier map */
  ISNODE **chn;                 /* array of child nodes */

  assert(node                   /* check the function arguments */
     && (n >= 0) && (items || (n <= 0)));
  if (node->offset >= 0) {      /* if a pure array is used */
    if (node->chcnt == 0) {     /* if this is a new node (leaf) */
      o = node->offset;         /* get the index offset */
      while ((n > 0) && (*items < o)) {
        n--; items++; }         /* skip items before first counter */
      while (--n >= 0) {        /* traverse the transaction's items */
        i = *items++ -o;        /* compute the counter array index */
        if (i >= node->size) return;
        node->cnts[i] += wgt;   /* if the corresp. counter exists, */
      } }                       /* add the transaction weight to it */
    else if (node->chcnt > 0) { /* if there are child nodes */
      chn = (ISNODE**)(node->cnts +node->size +PAD(node->size));
      o   = ITEM(chn[0]);       /* get the child node array */
      while ((n >= min) && (*items < o)) {
        n--; items++; }         /* skip items before the first child */
      for (--min; --n >= min;){ /* traverse the transaction's items */
        i = *items++ -o;        /* compute the child array index */
        if (i >= node->chcnt) return;
        if (chn[i]) _count(chn[i], items, n, wgt, min);
      }                         /* if the corresp. child node exists, */
    } }                         /* count the transaction recursively */
  else {                        /* if an identifer map is used */
    if (node->chcnt == 0) {     /* if this is a new node (leaf) */
      map = node->cnts +(k = node->size);
      o   = map[0];             /* get the identifier map */
      while ((n > 0) && (*items < o)) {
        n--; items++; }         /* skip items before first counter */
      o   = map[k-1];           /* get the last item with a counter */
      for (i = 0; --n >= 0; ) { /* traverse the transaction's items */
        if (*items > o) return; /* if beyond last item, abort */
        #ifdef IST_BSEARCH      /* if to use a binary search */
        i = int_bsearch(*items++, map, k);
        if (i >= 0) node->cnts[i] += wgt;
        #else                   /* if to use a linear search */
        while (*items > map[i]) i++;
        if (*items++ == map[i]) node->cnts[i] += wgt;
        #endif                  /* if the corresp. counter exists, */
      } }                       /* add the transaction weight to it */
    else if (node->chcnt > 0) { /* if there are child nodes */
      chn = (ISNODE**)(node->cnts +node->size +node->size);
      o   = ITEM(chn[0]);       /* get the child node array */
      while ((n >= min) && (*items < o)) {
        n--; items++; }         /* skip items before first child */
      k   = node->chcnt;        /* get the number of children and */
      o   = ITEM(chn[k-1]);     /* the index of the last item */
      for (--min; --n >= min; ) {
        if (*items > o) return; /* traverse the transaction */
        #ifdef IST_BSEARCH      /* if to use a binary search */
        i = _search(*items++, chn, k);
        if (i >= 0) _count(chn[i], items, n, wgt, min);
        else        i = -1-i;   /* count the transaction recursively */
        chn += i; k -= i;       /* and adapt the child node range */
        #else                   /* if to use a linear search */
        while (*items > ITEM(*chn)) chn++;
        if (*items++ == ITEM(*chn)) _count(*chn, items, n, wgt, min);
        #endif                  /* find the proper child node index */
      }                         /* if the corresp. child node exists, */
    }                           /* count the transaction recursively */
  }
}  /* _count() */

/*--------------------------------------------------------------------*/
#ifdef TATCOMPACT

static void _countx (ISNODE *node, const TANODE *tan, int min)
{                               /* --- count trans. tree recursively */
  int    i, k, o, n;            /* array indices, loop variables */
  int    item;                  /* buffer for an item */
  int    *map;                  /* item identifier map */
  ISNODE **chn;                 /* child node array */
  TANODE *cld;                  /* child node in transaction tree */

  assert(node && tree);         /* check the function arguments */
  k = abs(n = tan_max(tan));    /* if the transactions are too short, */
  if (k < min) return;          /* abort the recursion */
  if (n <= 0) {                 /* if this is a leaf node */
    if (n < 0) _count(node, tan_suffix(tan), k, tan_wgt(tan), min);
    return;                     /* count the transaction suffix */
  }                             /* and abort the function */
  for (cld = tan_children(tan); cld; cld = tan_sibling(cld))
    _countx(node, cld, min);    /* count the transactions recursively */
  if (node->offset >= 0) {      /* if a pure array is used */
    if (node->chcnt == 0) {     /* if this is a new node (leaf) */
      o = node->offset;         /* get the index offset */
      for (cld = tan_children(tan); cld; cld = tan_sibling(cld)) {
        i = tan_item(cld) -o;   /* traverse the child items */
        if (i < 0) return;      /* if before first item, abort */
        if (i < node->size) node->cnts[i] += tan_wgt(cld);
      } }                       /* otherwise add the trans. weight */
    else if (node->chcnt > 0) { /* if there are child nodes */
      chn = (ISNODE**)(node->cnts +node->size +PAD(node->size));
      o   = ITEM(chn[0]);       /* get the child node array */
      --min;                    /* traverse the child nodes */
      for (cld = tan_children(tan); cld; cld = tan_sibling(cld)) {
        i = tan_item(cld) -o;   /* traverse the child items */
        if  (i < 0) return;     /* if before first item, abort */
        if ((i < node->chcnt) && chn[i]) _countx(chn[i], cld, min);
      }                         /* if the corresp. child node exists, */
    } }                         /* count the trans. tree recursively */
  else {                        /* if an identifer map is used */
    if (node->chcnt == 0) {     /* if this is a new node (leaf) */
      map = node->cnts +(k = node->size);
      o   = map[0];             /* get the item identifier map */
      for (cld = tan_children(tan); cld; cld = tan_sibling(cld)) {
        item = tan_item(cld);   /* traverse the child items */
        if (item < o) return;   /* if before the first item, return */
        #ifdef IST_BSEARCH      /* if to use a binary search */
        i = int_bsearch(item, map, k);
        if (i >= 0) node->cnts[k = i] += tan_wgt(cld);
        else        k = -1-i;   /* add trans. weight to the counter */
        #else                   /* if to use a linear search */
        while (item < map[--k]);
        if (item == map[k]) node->cnts[k] += tan_wgt(cld);
        else k++;               /* if the corresp. counter exists, */
        #endif                  /* add the transaction weight to it, */
      } }                       /* otherwise adapt the map index */
    else if (node->chcnt > 0) { /* if there are child nodes */
      chn = (ISNODE**)(node->cnts +node->size +node->size);
      k   = node->chcnt;        /* get the child node array and */
      o   = ITEM(chn[0]);       /* the last item with a child */
      --min;                    /* traverse the child nodes */
      for (cld = tan_children(tan); cld; cld = tan_sibling(cld)) {
        item = tan_item(cld);   /* traverse the child items */
        if (item < o) return;   /* if before the first item, abort */
        #ifdef IST_BSEARCH      /* if to use a binary search */
        i = _search(item, chn, k);
        if (i >= 0) _countx(chn[i], cld, min);
        else        k = -1-i;   /* add trans. weight to the counter */
        #else                   /* if to use a linear search */
        while (item < ITEM(chn[--k]));
        if (item == ITEM(chn[k])) _countx(chn[k], cld, min);
        else k++;               /* if the corresp. counter exists, */
        #endif                  /* count the transaction recursively, */
      }                         /* otherwise adapt the child index */
    }                           /* into the child node array */
  }
}  /* _countx() */

/*--------------------------------------------------------------------*/
#else

static void _countx (ISNODE *node, const TANODE *tan, int min)
{                               /* --- count trans. tree recursively */
  int    i, k, o, n;            /* array indices, loop variables */
  int    item;                  /* buffer for an item */
  int    *map;                  /* item identifier map */
  ISNODE **chn;                 /* child node array */

  assert(node && tan);          /* check the function arguments */
  if (tan_max(tan) < min)       /* if the transactions are too short, */
    return;                     /* abort the recursion */
  n = tan_size(tan);            /* get the number of children */
  if (n <= 0) {                 /* if there are no children */
    if (n < 0) _count(node, tan_items(tan), -n, tan_wgt(tan), min);
    return;                     /* count the normal transaction */
  }                             /* and abort the function */
  while (--n >= 0)              /* count the transactions recursively */
    _countx(node, tan_child(tan, n), min);
  if (node->offset >= 0) {      /* if a pure array is used */
    if (node->chcnt == 0) {     /* if this is a new node (leaf) */
      o = node->offset;         /* get the index offset */
      for (n = tan_size(tan); --n >= 0; ) {
        i = tan_item(tan, n)-o; /* traverse the node's items */
        if (i < 0) return;      /* if before the first item, abort */
        if (i < node->size)     /* if the corresp. counter exists */
          node->cnts[i] += tan_wgt(tan_child(tan, n));
      } }                       /* add the transaction weight to it */
    else if (node->chcnt > 0) { /* if there are child nodes */
      chn = (ISNODE**)(node->cnts +node->size +PAD(node->size));
      o   = ITEM(chn[0]);       /* get the child node array */
      for (--min, n = tan_size(tan); --n >= 0; ) {
        i = tan_item(tan, n)-o; /* traverse the node's items */
        if (i < 0) return;      /* if before the first item, abort */
        if ((i < node->chcnt) && chn[i])
          _countx(chn[i], tan_child(tan, n), min);
      }                         /* if the corresp. child node exists, */
    } }                         /* count the trans. tree recursively */
  else {                        /* if an identifer map is used */
    if (node->chcnt == 0) {     /* if this is a new node (leaf) */
      map = node->cnts +(k = node->size);
      o   = map[0];             /* get the item identifier map */
      for (n = tan_size(tan); --n >= 0; ) {
        item = tan_item(tan,n); /* traverse the node's items */
        if (item < o) return;   /* if before the first item, abort */
        #ifdef IST_BSEARCH      /* if to use a binary search */
        i = int_bsearch(item, map, k);
        if (i >= 0) node->cnts[k = i] += tan_wgt(tan_child(tan, n));
        else        k = -1-i;   /* add trans. weight to the counter */
        #else                   /* if to use a linear search */
        while (item < map[--k]);
        if (item == map[k]) node->cnts[k] += tan_wgt(tan_child(tan, n));
        else k++;               /* if the corresp. counter exists, */
        #endif                  /* add the transaction weight to it, */
      } }                       /* otherwise adapt the map index */
    else if (node->chcnt > 0) { /* if there are child nodes */
      chn = (ISNODE**)(node->cnts +node->size +node->size);
      k   = node->chcnt;        /* get the child node array and */
      o   = ITEM(chn[0]);       /* the last item with a child */
      for (--min, n = tan_size(tan); --n >= 0; ) {
        item = tan_item(tan,n); /* traverse the node's items */
        if (item < o) return;   /* if before the first item, abort */
        #ifdef IST_BSEARCH      /* if to use a binary search */
        i = _search(item, chn, k);
        if (i >= 0) _countx(chn[i], tan_child(tan, n), min);
        else        k = -1-i;   /* add trans. weight to the counter */
        #else                   /* if to use a linear search */
        while (item < ITEM(chn[--k]));
        if (item == ITEM(chn[k]))
          _countx(chn[k], tan_child(tan, n), min);
        else k++;               /* if the corresp. counter exists, */
        #endif                  /* count the transaction recursively, */
      }                         /* otherwise adapt the child index */
    }                           /* into the child node array */
  }
}  /* _countx() */

#endif
/*--------------------------------------------------------------------*/

static int _needed (ISNODE *node)
{                               /* --- recursively check nodes */
  int    i, r;                  /* array index, check result */
  ISNODE **chn;                 /* child node array */

  assert(node);                 /* check the function argument */
  if (node->chcnt == 0) return -1; /* do not skip new leaves, */
  if (node->chcnt <= 0) return  0; /* but skip marked subtrees */
  i   = (node->offset < 0) ? node->size : PAD(node->size);
  chn = (ISNODE**)(node->cnts +node->size +i);
  for (r = 0, i = node->chcnt; --i >= 0; )
    if (chn[i]) r |= _needed(chn[i]);
  if (r) return -1;             /* recursively check all children */
  node->chcnt |= F_SKIP;        /* set the skip flag if possible */
  return 0;                     /* return 'subtree can be skipped' */
}  /* _needed() */

/*--------------------------------------------------------------------*/

static int _used (ISNODE *node, int *marks, int supp)
{                               /* --- recursively check item usage */
  int    i, k, r = 0;           /* array index, map size, result */
  int    *map;                  /* item identifier map */
  ISNODE **chn;                 /* child node array */

  assert(node && marks);        /* check the function arguments */
  if (node->offset >= 0) {      /* if a pure array is used */
    if (node->chcnt == 0) {     /* if this is a new node (leaf) */
      k = node->offset;         /* get the index offset */
      for (i = node->size; --i >= 0; ) {
        if (node->cnts[i] >= supp)
          marks[k+i] = r = 1;   /* mark items in set that satisfy */
      } }                       /* the minimum support criterion */
    else if (node->chcnt > 0) { /* if there are child nodes */
      chn = (ISNODE**)(node->cnts +node->size +PAD(node->size));
      for (i = node->chcnt; --i >= 0; )
        if (chn[i]) r |= _used(chn[i], marks, supp);
    } }                         /* recursively process all children */
  else {                        /* if an identifer map is used */
    if (node->chcnt == 0) {     /* if this is a new node */
      map = node->cnts +node->size;
      for (i = node->size; --i >= 0; ) {
        if (node->cnts[i] >= supp)
          marks[map[i]] = r = 1;/* mark items in set that satisfies */
      } }                       /* the minimum support criterion */
    else if (node->chcnt > 0) { /* if there are child nodes */
      chn = (ISNODE**)(node->cnts +node->size +node->size);
      for (i = node->chcnt; --i >= 0; )
        r |= _used(chn[i], marks, supp);
    }                           /* get the child node array and */
  }                             /* recursively process all children */
  if ((r != 0) && node->parent) /* if the check succeeded, mark */
    marks[ITEM(node)] = 1;      /* the item associated with the node */
  return r;                     /* return the check result */
}  /* _used() */

/*--------------------------------------------------------------------*/

static void _mark (ISNODE *node, int *items, int n, int supp)
{                               /* --- mark an item set */
  int    i, k;                  /* array index, map size */
  int    *map;                  /* item identifier map */
  ISNODE **chn;                 /* child node array */

  assert(node                   /* check the function arguments */
     && (n >= 0) && (items || (n <= 0)));
  while (--n > 0) {             /* follow the set/path from the node */
    if (node->offset >= 0) {    /* if a pure array is used */
      chn = (ISNODE**)(node->cnts +node->size +PAD(node->size));
      i   = *items++ -ITEM(chn[0]); }
    else {                      /* if an identifier map is used */
      chn = (ISNODE**)(node->cnts +node->size +node->size);
      i   = _search(*items++, chn, CHILDCNT(node));
    }                           /* get the proper child array index */
    node = chn[i];              /* go to the corresponding child */
  }
  if (node->offset >= 0)        /* if a pure array is used, */
    i   = *items -node->offset; /* compute the counter index */
  else {                        /* if an identifier map is used */
    map = node->cnts +(k = node->size);
    i   = int_bsearch(*items, map, k);
  }                             /* search for the proper index */
  if ((supp < 0)                /* if to clear unconditionally */
  ||  (node->cnts[i] == supp))  /* or the support is the same */
    node->cnts[i] |= F_SKIP;    /* mark the item set by the sign bit */
}  /* _mark() */

/*--------------------------------------------------------------------*/

static void _marksub (ISTREE *ist, ISNODE *node, int index, int supp)
{                               /* --- mark all n-1 subsets */
  int i;                        /* next item, loop variable */
  int *items;                   /* (partial) item set */

  if (node->offset >= 0) i = node->offset +index;
  else                   i = node->cnts[node->size +index];
  items = ist->buf +ist->maxht; /* get and store the first two items */
  *--items = i;          _mark(node->parent, items, 1, supp);
  *--items = ITEM(node); _mark(node->parent, items, 1, supp);
  i = 2;                        /* mark counters in parent node */
  for (node = node->parent; node->parent; node = node->parent) {
    _mark(node->parent, items, i, supp);
    *--items = ITEM(node); i++; /* climb up the tree and mark */
  }                             /* counters for all n-1 subsets */
}  /* _marksub() */

/*--------------------------------------------------------------------*/

static ISNODE* _child (ISTREE *ist, ISNODE *node, int index, int spx)
{                               /* --- create child node (extend set) */
  int    i, k, n;               /* loop variables, counters */
  ISNODE *curr;                 /* to traverse the path to the root */
  int    item, cnt;             /* item identifier, number of items */
  int    *set;                  /* next (partial) item set to check */
  int    body;                  /* enough support for a rule body */
  int    hdonly;                /* whether head only item on path */
  int    app;                   /* appearance flags of an item */
  int    s_set;                 /* support of an item set */

  assert(ist && node            /* check the function arguments */
     && (index >= 0) && (index < node->size));

  /* --- initialize --- */
  s_set = node->cnts[index];    /* get support of item set to extend */
  if ((s_set <  ist->supp)      /* if the support is insufficient */
  ||  (s_set >= spx))           /* or item is a perfect extension, */
    return NULL;                /* abort (do not create a child) */
  if (node->offset >= 0) item = node->offset +index; 
  else                   item = node->cnts[node->size +index];
  app = ib_getapp(ist->base, item);  /* get item id. and app. flag */
  if ((app == APP_NONE)         /* do not extend an item to ignore */
  || ((app == APP_HEAD) && (HDONLY(node))))
    return NULL;                /* do not combine two head only items */
  hdonly = (app == APP_HEAD) || HDONLY(node);
  body = (s_set >= ist->rule)   /* if the set has enough support for */
       ? 1 : 0;                 /* a rule body, set the body flag */
  ist->buf[ist->maxht-2] = item;/* init. set for support checks */

  /* --- check candidates --- */
  for (n = 0, i = index; ++i < node->size; ) {
    if (node->offset >= 0) k = node->offset +i;
    else                   k = node->cnts[node->size +i];
    app = ib_getapp(ist->base, k); /* traverse the candidate items */
    if ((app == APP_NONE) || (hdonly && (app == APP_HEAD)))
      continue;                 /* skip sets with two head only items */
    s_set = node->cnts[i];      /* traverse the candidate items */
    if ((s_set <  ist->supp)    /* if set support is insufficient */
    ||  (s_set >= spx))         /* or item is a perfect extension, */
      continue;                 /* ignore the corresponding candidate */
    body &= 1;                  /* restrict body flags to set support */
    if (s_set >= ist->rule)     /* if set support is sufficient for */
      body |= 2;                /* a rule body, set the body flag */ 
    set    = ist->buf +ist->maxht -(cnt = 2);
    set[1] = k;                 /* add the candidate item to the set */
    for (curr = node; curr->parent; curr = curr->parent) {
      s_set = _getsupp(curr->parent, set, cnt);
      if (s_set <  ist->supp)   /* get the item set support and */
        break;                  /* if it is too low, abort the loop */
      if (s_set >= ist->rule)   /* if some subset has enough support */
        body |= 4;              /* for a rule body, set the body flag */
      *--set = ITEM(curr);      /* add id of current node to the set */
      ++cnt;                    /* and adapt the number of items */
    }
    if (!curr->parent && body)  /* if subset support is high enough */
      ist->map[n++] = k;        /* for a full rule and a rule body, */
  }                             /* note the item identifier */
  if (n <= 0) return NULL;      /* if no child is needed, abort */
  #ifdef BENCH                  /* if benchmark version, */
  ist->scnec += n;              /* sum the necessary counters */
  #endif

  /* --- decide on node structure --- */
  k = ist->map[n-1] -ist->map[0] +1;
  if (n+n >= k) n = k;          /* use a pure array if it is small, */
  else          k = n+n;        /* otherwise use an identifier map */
  #ifdef BENCH                  /* if benchmark version, */
  ist->sccnt += n;              /* sum the number of counters */
  if (n != k) ist->mapsz += n;  /* sum the size of the maps */
  ist->ndcnt++;                 /* count the node to be created */
  #endif

  /* --- create child --- */
  curr = (ISNODE*)malloc(sizeof(ISNODE) +(k+PAD(k)-1) *sizeof(int));
  if (!curr) return (ISNODE*)-1;/* create a child node */
  if (hdonly) item |= F_HDONLY; /* set the head only flag and */
  curr->item  = item;           /* initialize the item identifier */
  curr->chcnt = 0;              /* there are no children yet */
  curr->size  = n;              /* set size of counter array */
  if (n == k)                   /* if to use a pure array, */
    curr->offset = ist->map[0]; /* note the first item as an offset */
  else {                        /* if to use an identifier map, */
    curr->offset = -1;          /* use the offset as an indicator */
    for (set = curr->cnts +n +(i = n); --i >= 0; )
      *--set = ist->map[i];     /* copy the identifier map */
  }                             /* from the buffer to the node */
  for (set = curr->cnts +(i = n); --i >= 0; )
    *--set = 0;                 /* clear all counters of the node */
  return curr;                  /* return pointer to created child */
}  /* _child() */

/*----------------------------------------------------------------------
  In the above function the set S represented by the index-th array
element of the current node is extended only by combining it with
the sets represented by the fields that follow it in the node array,
i.e. by the sets represented by vec[index+1] to vec[size-1]. The sets
that can be formed by combining the set S and the sets represented by
vec[0] to vec[index-1] are processed in the branches for these sets.
  In the 'check candidates' loop it is checked for each set represented
by vec[index+1] to vec[size-1] whether this set and all other subsets
of the same size, which can be formed from the union of this set and
the set S, have enough support, so that a child node is necessary.
  Note that i +offset is the identifier of the item that has to be
added to set S to form the union of the set S and the set T represented
by vec[i], since S and T have the same path with the exception of the
index in the current node. Hence we can speak of candidate items that
are added to S.
  Checking the support of the other subsets of the union of S and T
that have the same size as S and T is done with the aid of a path
variable. The items in this variable combined with the items on the
path to the current node always represent the subset currently tested.
That is, the path variable holds the path to be followed from the
current node to arrive at the support counter for the subset. The path
variable is initialized to [0]: <item>, [1]: <offset+i>, since the
support counters for S and T can be inspected directly. Then this
path is followed from the parent node of the current node, which is
equivalent to checking the subset that can be obtained by removing
from the union of S and T the item that corresponds to the parent node
(in the path to S or T, resp.).
  Iteratively making the parent node the current node, adding its
corresponding item to the path and checking the support counter at the
end of the path variable when starting from its (the new current node's)
parent node tests all other subsets.
  Another criterion is that the extended set must not contain two items
which may appear only in the head of a rule. If two such items are
contained in a set, neither can a rule be formed from its items nor
can it be the antecedent of a rule. Whether a set contains two head
only items is determined from the nodes 'hdonly' flag and the
appearance flags of the items.
----------------------------------------------------------------------*/

static void _cleanup (ISTREE *ist)
{                               /* --- clean up on error */
  ISNODE *node, *t;             /* to traverse the nodes */

  assert(ist);                  /* check the function argument */
  for (node = ist->lvls[ist->height]; node; ) {
    t = node; node = node->succ; free(t); }
  ist->lvls[ist->height] = NULL;/* delete all created nodes */
  for (node = ist->lvls[ist->height -1]; node; node = node->succ)
    node->chcnt = 0;            /* clear the child node counters */
}  /* _cleanup() */             /* of the deepest nodes in the tree */

/*----------------------------------------------------------------------
  Main Functions
----------------------------------------------------------------------*/

ISTREE* ist_create (ITEMBASE *base, int mode,
                    int supp, int smax, double conf)
{                               /* --- create an item set tree */
  int    cnt, n;                /* number of items, buffer */
  ISTREE *ist;                  /* created item set tree */
  ISNODE *root;                 /* root node of the tree */

  assert(base                   /* check the function arguments */
     && (supp >= 0) && (conf >= 0) && (conf <= 1));

  /* --- allocate memory --- */ 
  cnt = ib_cnt(base);           /* get the number of items */
  ist = (ISTREE*)malloc(sizeof(ISTREE));
  if (!ist) return NULL;        /* allocate the tree body */
  ist->lvls = (ISNODE**)malloc(BLKSIZE *sizeof(ISNODE*));
  if (!ist->lvls) {                  free(ist); return NULL; }
  ist->buf  = (int*)    malloc(BLKSIZE *sizeof(int));
  if (!ist->buf)  { free(ist->lvls); free(ist); return NULL; }
  ist->map  = (int*)    malloc(cnt *sizeof(int));
  if (!ist->map)  { free(ist->buf);
                    free(ist->lvls); free(ist); return NULL; }
  n = cnt +PAD(cnt);            /* compute the array size */
  ist->lvls[0] = ist->curr =    /* allocate a root node */
  root = (ISNODE*)calloc(1, sizeof(ISNODE) +(n-1) *sizeof(int));
  if (!root)      { free(ist->map);  free(ist->buf);
                    free(ist->lvls); free(ist); return NULL; }

  /* --- initialize structures --- */
  ist->base   = base;           /* copy parameters to the structure */
  ist->mode   = mode;
  ist->wgt    = ib_getwgt(base);
  ist->maxht  = BLKSIZE;
  ist->height = 1;
  ist->rule   = (supp > 0)         ? supp : 1;
  ist->smax   = (smax > ist->rule) ? smax : ist->rule;
  if (!(mode & APP_HEAD)) supp = (int)ceil(conf *supp);
  ist->supp   = (supp > 0)         ? supp : 1;
  ist->conf   = conf *(1.0-DBL_EPSILON);
  /* Multiplying the minimum confidence with (1.0-DBL_EPSILON) takes */
  /* care of rounding errors. For example, a minimum confidence of   */
  /* 0.8 (or 80%) cannot be coded accurately with a double precision */
  /* floating point number. It is rather stored as a slightly larger */
  /* number, which can lead to missing rules. To prevent this, the   */
  /* confidence is made smaller by the largest possible factor < 1.  */
  #ifdef BENCH                  /* if benchmark version */
  ist->ndcnt  = 1;   ist->ndprn = ist->mapsz = 0;
  ist->sccnt  = ist->scnec = cnt; ist->scprn = 0;
  ist->cpcnt  = ist->cpnec =      ist->cpprn = 0;
  #endif                        /* initialize the benchmark variables */
  ist_setsize(ist, 1, 1, 1);    /* init. the extraction variables */
  ist_seteval(ist, IST_NONE, IST_NONE, 1, -DBL_MAX, INT_MAX);
  ist_init(ist);
  root->parent = root->succ  = NULL;
  root->offset = root->chcnt = root->item = 0;
  root->size   = cnt;           /* initialize the root node */
  while (--cnt >= 0)            /* copy the item frequencies */
    root->cnts[cnt] = ib_getfrq(base, cnt); 
  return ist;                   /* return created item set tree */
}  /* ist_create() */

/*--------------------------------------------------------------------*/

void ist_delete (ISTREE *ist)
{                               /* --- delete an item set tree */
  int    i;                     /* loop variables */
  ISNODE *node, *t;             /* to traverse the nodes */

  assert(ist);                  /* check the function argument */
  for (i = ist->height; --i >= 0; ) {
    for (node = ist->lvls[i]; node; ) {
      t = node; node = node->succ; free(t); }
  }                             /* delete all nodes, */
  free(ist->lvls);              /* the level array, */
  free(ist->map);               /* the identifier map, */
  free(ist->buf);               /* the path buffer, */
  free(ist);                    /* and the tree body */
}  /* ist_delete() */

/*--------------------------------------------------------------------*/

void ist_count (ISTREE *ist, const int *items, int n, int wgt)
{                               /* --- count a transaction */
  assert(ist                    /* check the function arguments */
     && (n >= 0) && (items || (n <= 0)));
  if (n >= ist->height)         /* recursively count the transaction */
    _count(ist->lvls[0], items, n, wgt, ist->height);
}  /* ist_count() */

/*--------------------------------------------------------------------*/

void ist_countt (ISTREE *ist, const TRACT *t)
{                               /* --- count a transaction */
  int k;                        /* number of items */

  assert(ist && t);             /* check the function arguments */
  k = ta_size(t);               /* get the transaction size and */
  if (k >= ist->height)         /* count the transaction recursively */
    _count(ist->lvls[0], ta_items(t), k, ta_wgt(t), ist->height);
}  /* ist_countt() */

/*--------------------------------------------------------------------*/

void ist_countb (ISTREE *ist, const TABAG *bag)
{                               /* --- count a transaction bag */
  int   i, k;                   /* loop variable, number of items */
  TRACT *t;                     /* to traverse the transactions */

  assert(ist && bag);           /* check the function arguments */
  if (!tbg_max(bag) >= ist->height)
    return;                     /* check for suff. long transactions */
  for (i = tbg_cnt(bag); --i >= 0; ) {
    t = tbg_tract(bag, i);      /* traverse the transactions */
    k = ta_size(t);             /* get the transaction size and */
    if (k >= ist->height)       /* count the transaction recursively */
      _count(ist->lvls[0], ta_items(t), k, ta_wgt(t), ist->height);
  }
}  /* ist_countb() */

/*--------------------------------------------------------------------*/

void ist_countx (ISTREE *ist, const TATREE *tree)
{                               /* --- count transaction in tree */
  assert(ist && tree);          /* check the function arguments */
  _countx(ist->lvls[0], tat_root(tree), ist->height);
}  /* ist_countx() */           /* recursively count the trans. tree */

/*--------------------------------------------------------------------*/

void ist_prune (ISTREE *ist)
{                               /* --- prune counters and pointers */
  int    i, k, n;               /* loop variables */
  int    *c, *map;              /* counter array, item identifier map */
  ISNODE **np, *node;           /* to traverse the nodes */
  ISNODE **chn;                 /* child node array */

  assert(ist);                  /* check the function argument */
  if (ist->height <= 1)         /* if there is only the root node, */
    return;                     /* there is nothing to prune */

  /* -- prune counters for infrequent items -- */
  for (node = ist->lvls[ist->height-1]; node; node = node->succ) {
    c = node->cnts;             /* traverse the deepest level */
    if (node->offset >= 0) {    /* if a pure array is used */
      for (n = node->size; --n >= 0; ) /* find the last */
        if (c[n] >= ist->supp) break;  /* frequent item */
      for (i = 0; i < n; i++)          /* find the first */
        if (c[i] >= ist->supp) break;  /* frequent item  */
      node->size = ++n-i;       /* set the new node size */
      #ifdef BENCH              /* if benchmark version */
      k = node->size -(n-i);    /* get the number of pruned counters */
      ist->sccnt -= k;          /* update the number of counters */
      ist->scprn += k;          /* and of pruned counters */
      #endif                    /* update the memory usage */
      if (i > 0) {              /* if there are leading infreq. items */
        node->offset += i;      /* set the new item offset */
        for (k = 0; i < n; i++) /* copy the frequent items */
          c[k++] = c[i];        /* to the front of the array */
      } }
    else {                      /* if an identifier map is used */
      map = c +node->size;      /* get the item identifier map */
      for (i = n = 0; i < node->size; i++) {
        if (c[i] >= ist->supp) {
          c[n] = c[i]; map[n++] = map[i]; }
      }                         /* remove infrequent items */
      k = node->size -n;        /* get the number of pruned counters */
      if (k <= 0) continue;     /* if no items were pruned, continue */
      #ifdef BENCH              /* if benchmark version, */
      ist->sccnt -= k;          /* update the number of counters */
      ist->scprn += k;          /* and of pruned counters */
      ist->mapsz -= k;          /* update the total item map size */
      #endif
      node->size = n; c += n;   /* set the new node size */
      for (i = 0; i < n; i++)   /* move the item identifier map */
        c[i] = map[i];          /* so that it starts directly */
    }                           /* after the support counters */
  }

  /* -- prune pointers to empty children -- */
  for (node = ist->lvls[ist->height-2]; node; node = node->succ) {
    n = CHILDCNT(node);         /* traverse the parent nodes */
    if (n <= 0) continue;       /* skip childless nodes */
    if (node->offset >= 0) {    /* if a pure array is used */
      chn = (ISNODE**)(node->cnts +node->size +PAD(node->size));
      while (--n >= 0)          /* find the last  non-empty child */
        if (chn[n] && (chn[n]->size > 0)) break;                
      for (i = 0; i < n; i++)   /* find the first non-empty child */
        if (chn[i] && (chn[i]->size > 0)) break;             
      node->chcnt = ++n-i;      /* set the new number of children */
      #ifdef BENCH              /* if benchmark version, */
      k = node->chcnt -(n-i);   /* get the number of pruned pointers */
      ist->cpcnt -= k;          /* update the number of pointers */
      ist->cpprn += k;          /* and of pruned pointers */
      #endif
      for (k = 0; i < n; i++)   /* remove all empty children */
        chn[k++] = (chn[i] && (chn[i]->size > 0)) ? chn[i] : NULL; }
    else {                      /* if an item identifier map is used */
      chn = (ISNODE**)(node->cnts +node->size +node->size);
      for (i = k = 0; i < n; i++)
        if (chn[i]->size > 0)   /* collect the child nodes */
          chn[k++] = chn[i];    /* that are not empty */
      node->chcnt = k;          /* set the new number of children */
      #ifdef BENCH              /* if benchmark version, */
      n -= k;                   /* get the number of pruned pointers */
      ist->cpcnt -= n;          /* update the number of pointers */
      ist->cpprn += n;          /* and of pruned pointers */
      #endif
    }                 
    if (node->chcnt <= 0)       /* if all children were removed, */
      node->chcnt |= F_SKIP;    /* set the skip flag, so that */
  }                             /* no recounting takes place */

  /* -- remove empty children -- */
  for (np = ist->lvls +ist->height-1; *np; ) {
    node = *np;                 /* traverse the deepest level again */
    if (node->size > 0) { np = &node->succ; continue; }
    *np = node->succ; free(node); /* remove empty nodes */
    #ifdef BENCH                /* if benchmark version */
    ist->ndcnt--; ist->ndprn++; /* update the number nodes */
    #endif                      /* and of pruned nodes */
  }
}  /* ist_prune() */

/*--------------------------------------------------------------------*/

int ist_check (ISTREE *ist, int *marks)
{                               /* --- check item usage */
  int i, n;                     /* loop variable, number of items */

  assert(ist);                  /* check the function argument */
  for (i = ist->lvls[0]->size; --i >= 0; )
    marks[i] = 0;               /* clear the marker array */
  _used(ist->lvls[0], marks, ist->supp);
  for (n = 0, i = ist->lvls[0]->size; --i >= 0; )
    if (marks[i]) n++;          /* count used items */
  return n;                     /* and return this number */
}  /* ist_check() */

/*--------------------------------------------------------------------*/

int ist_addlvl (ISTREE *ist)
{                               /* --- add a level to item set tree */
  int    i, n;                  /* loop variable, node counter */
  int    spx;                   /* support for a perfect extension */
  ISNODE **np;                  /* to traverse the nodes */
  ISNODE *node;                 /* current node in deepest level */
  ISNODE *par;                  /* parent of current node */
  ISNODE *cur;                  /* current node in new level (child) */
  ISNODE **frst;                /* first child of current node */
  ISNODE *last;                 /* last  child of current node */
  ISNODE **end;                 /* end of node list of new level */
  ISNODE **chn;                 /* child node array */
  void   *t;                    /* temporary buffer for reallocation */

  assert(ist);                  /* check the function arguments */

  /* --- enlarge level array --- */
  if (ist->height >= ist->maxht) {
    n = ist->maxht +BLKSIZE;    /* if the level array is full */
    t = realloc(ist->lvls, n *sizeof(ISNODE*));
    if (!t) return -1;          /* enlarge the level array */
    ist->lvls = (ISNODE**)t;    /* and set the new array */
    t = realloc(ist->buf,  n *sizeof(int));
    if (!t) return -1;          /* enlarge the buffer array */
    ist->buf   = (int*)t;       /* and set the new array */
    ist->maxht = n;             /* set the new array size */
  }                             /* (applies to buf and levels) */
  end  = ist->lvls +ist->height;
  *end = NULL;                  /* start a new tree level */

  /* --- prune with evaluation --- */
  if ((ist->eval   >  IST_NONE) /* if to prune with evaluation */
  &&  (ist->height >= ist->prune)) {
    for (node = ist->lvls[ist->height-1]; node; node = node->succ) {
      for (i = node->size; --i >= 0; )
        if (ist->dir *_evaluate(ist, node, i) < ist->thresh)
          node->cnts[i] |= F_SKIP;
    }                           /* compute (aggregated) evaluation to */
  }                             /* check whether item set qualifies */

  /* --- add tree level --- */
  for (np = ist->lvls +ist->height -1; *np; np = &(*np)->succ) {
    node = ist->node = *np;     /* traverse the deepest nodes */
    frst = end; last = NULL;    /* note start of the child node list */
    if (!(ist->mode & IST_PERFECT)) spx = INT_MAX;
    else if (!node->parent)         spx = ist->wgt;
    else spx = _getsupp(node->parent, &node->item, 1);
    spx = COUNT(spx);           /* get support for perfect extension */
    for (i = n = 0; i < node->size; i++) {
      cur = _child(ist,node,i,spx);   /* traverse the counter array */
      if (!cur) continue;       /* create a child node if necessary */
      if (cur == (void*)-1) { *end = NULL; _cleanup(ist); return -1; }
      *end = last = cur;        /* add node at the end of the list */
      end  = &cur->succ; n++;   /* that contains the new level */
    }                           /* and advance the end pointer */
    if (n <= 0) {               /* if no child node was created, */
      node->chcnt = F_SKIP; continue; }         /* skip the node */
    *end = NULL;                /* terminate the child node list */
    #ifdef BENCH                /* if benchmark version, */
    ist->cpnec += n;            /* sum the number of */
    #endif                      /* necessary child pointers */
    chn = np; par = node->parent;
    if (par) {                  /* if there is a parent node */
      if (par->offset >= 0) {   /* if a pure array is used */
        chn = (ISNODE**)(par->cnts +par->size +PAD(par->size));
        chn += ITEM(node) -ITEM(chn[0]); }
      else {                    /* if an identifier map is used */
        chn = (ISNODE**)(par->cnts +par->size +    par->size);
        chn += _search(ITEM(node), chn, CHILDCNT(par));
      }                         /* find the child node pointer */
    }                           /* in the parent node */
    if (node->offset >= 0) {    /* if a pure counter array is used */
      i = (node->size +PAD(node->size) -1) *sizeof(int);
      n = ITEM(last) -ITEM(*frst) +1; }  /* add a pure child array */
    else {                      /* if an identifier map is used */
      i = (node->size +    node->size  -1) *sizeof(int);
    }                           /* add a compact child array */
    node = (ISNODE*)realloc(node, sizeof(ISNODE) +i +n*sizeof(ISNODE*));
    if (!node) { _cleanup(ist); return -1; }
    node->chcnt = n;            /* add a child array to the node */
    #ifdef BENCH                /* if benchmark version, */
    ist->cpcnt += n;            /* sum the number of child pointers */
    #endif
    *chn = *np = node;          /* set the new (reallocated) node */
    if (node->offset >= 0) {    /* if a pure array is used */
      chn = (ISNODE**)(node->cnts +node->size +PAD(node->size));
      while (--n >= 0) chn[n] = NULL;
      i   = ITEM(*frst);        /* get the child node array */
      for (cur = *frst; cur; cur = cur->succ) {
        chn[ITEM(cur)-i] = cur; /* set the child node pointer */
        cur->parent = node;     /* and the parent pointer */
      } }                       /* in the new node */
    else {                      /* if an identifier map is used */
      chn = (ISNODE**)(node->cnts +node->size +node->size);
      i   = 0;                  /* get the child node array */
      for (cur = *frst; cur; cur = cur->succ) {
        chn[i++]    = cur;      /* set the child node pointer */
        cur->parent = node;     /* and the parent pointer */
      }                         /* in the new node */
    }                           /* (store pointers to children */
  }                             /*  in the current node) */
  if (!ist->lvls[ist->height])  /* if no child has been added, */
    return 1;                   /* abort the function, otherwise */
  ist->height++;                /* increment the level counter */
  _needed(ist->lvls[0]);        /* mark unnecessary subtrees */
  return 0;                     /* return 'ok' */
}  /* ist_addlvl() */

/*--------------------------------------------------------------------*/

void ist_up (ISTREE *ist, int root)
{                               /* --- go up in item set tree */
  assert(ist && ist->curr);     /* check the function argument */
  if      (root)                /* if root flag set, */
    ist->curr = ist->lvls[0];   /* go to the root node */
  else if (ist->curr->parent)   /* if it exists, go to the parent */
    ist->curr = ist->curr->parent;
}  /* ist_up() */

/*--------------------------------------------------------------------*/

int ist_down (ISTREE *ist, int item)
{                               /* --- go down in item set tree */
  ISNODE *node;                 /* current node */
  ISNODE **chn;                 /* child node array */
  int    cnt;                   /* number of children */

  assert(ist && ist->curr);     /* check the function argument */
  node = ist->curr;             /* get the current node */
  cnt  = CHILDCNT(node);        /* if there are no child nodes, */
  if (cnt <= 0) return -1;      /* abort the function */
  if (node->offset >= 0) {      /* if a pure array is used */
    chn   = (ISNODE**)(node->cnts +node->size +PAD(node->size));
    item -= ITEM(chn[0]);       /* compute index in child node array */
    if ((item >= cnt) || !chn[item]) return -1; }
  else {                        /* if an identifier map is used */
    chn   = (ISNODE**)(node->cnts +node->size +    node->size);
    item  = _search(item, chn, cnt);
  }                             /* search for the proper index */
  if (item < 0) return -1;      /* if index is out of range, abort */
  ist->curr = chn[item];        /* otherwise go to the child node */
  return 0;                     /* return 'ok' */
}  /* ist_down() */

/*--------------------------------------------------------------------*/

int ist_next (ISTREE *ist, int item)
{                               /* --- get next item with a counter */
  int    i, n;                  /* array index, map size */
  int    *map;                  /* item identifier map */
  ISNODE *node;                 /* current node in tree */

  assert(ist && ist->curr);     /* check the function argument */
  node = ist->curr;             /* get the current node */
  if (node->offset >= 0) {      /* if a pure array is used, */
    i = item -node->offset;     /* compute the array index */
    if (i <  0) return node->offset;
    if (i >= node->size) return -1;
    return item +1; }           /* return the next item identifier */
  else {                        /* if an identifier map is used */
    map = node->cnts +(n = node->size);
    i = int_bsearch(item, map, n);
    i = (i < 0) ? -1-i : i+1;   /* try to find the item in the map */
    return (i < n) ? map[i] : -1;
  }                             /* return the following item */
}  /* ist_next() */

/*--------------------------------------------------------------------*/

int ist_supp (ISTREE *ist, int item)
{                               /* --- get support for an item */
  ISNODE *node;                 /* current node in tree */

  assert(ist && ist->curr);     /* check the function argument */
  node = ist->curr;             /* get the current node */
  if (node->offset >= 0) {      /* if pure arrays are used, */
    item -= node->offset;       /* get index in counter array */
    if (item >= node->size) return 0; }
  else                          /* if an identifier map is used */
    item = int_bsearch(item, node->cnts +node->size, node->size);
  if (item < 0) return 0;       /* abort if index is out of range */
  return COUNT(node->cnts[item]);
}  /* ist_supp() */             /* return the item set support */

/*--------------------------------------------------------------------*/

int ist_suppx (ISTREE *ist, int *items, int n)
{                               /* --- get support of an item set */
  assert(ist                    /* check the function arguments */
     && (n >= 0) && (items || (n <= 0)));
  if (n <= 0)                   /* if the item set is empty, */
    return COUNT(ist->wgt);     /* return the total trans. weight */
  return COUNT(_getsupp(ist->lvls[0], items, n));
}  /* ist_suppx() */            /* return the item set support */

/*--------------------------------------------------------------------*/

void ist_mark (ISTREE *ist, int mode)
{                               /* --- mark frequent item sets */
  int    i, k;                  /* loop variables */
  ISNODE *node;                 /* to traverse the nodes */
  int    supp;                  /* support of an item set */

  assert(ist);                  /* check the function argument */
  if ((mode & ~IST_EVAL) == IST_CLEAR) {
    ist->wgt &= ~F_SKIP;        /* if to clear all skip flags */
    for (k = 0; ++k < ist->height; )
      for (node = ist->lvls[k]; node; node = node->succ)
        for (i = node->size; --i >= 0; )
          node->cnts[i] &= ~F_SKIP;
    return;                     /* clear skip flags of all sets */
  }                             /* and abort the function */
  if ((mode      & IST_EVAL)    /* if maximal sets w.r.t. evaluation */
  &&  (ist->eval > IST_NONE)) { /* and evaluation measure is given */
    supp = -1;                  /* set default support filter */
    for (k = ist->height; --k > 0; ) {
      for (node = ist->lvls[k]; node; node = node->succ) {
        for (i = node->size; --i >= 0; ) {
          if ((node->cnts[i] >= 0)
          && ((node->cnts[i] < ist->supp)
          ||  (ist->dir *_evaluate(ist, node, i) < ist->thresh))) {
            node->cnts[i] |= F_SKIP; continue; }
          if (mode & IST_CLOSED) supp = node->cnts[i];
          _marksub(ist, node, i, supp);
	}                       /* traverse the tree bottom up */
      }                         /* (that is, from leaves to root) */
    }                           /* and mark relevant n-1 subsets */
    supp = (mode & IST_CLOSED) ? ist->wgt : ist->supp;
    node = ist->lvls[0];        /* traverse the root node elements */
    for (i = node->size; --i >= 0; ) {
      if ((node->cnts[i] >= supp) || (node->cnts[i] < 0)) {
        ist->wgt |= F_SKIP; break; }
    } }                         /* mark the empty set if necessary */
  else {                        /* if to use only the support */
    supp = (mode & IST_CLOSED) ? ist->wgt : ist->supp;
    node = ist->lvls[0];        /* traverse the root node elements */
    for (i = node->size; --i >= 0; ) {
      if (node->cnts[i] >= supp) {
        ist->wgt |= F_SKIP; break; }
    }                           /* mark the empty set if necessary */
    supp = -1;                  /* set default support filter */
    for (k = 0; ++k < ist->height; ) {
      for (node = ist->lvls[k]; node; node = node->succ) {
        for (i = node->size; --i >= 0; ) {
          if (node->cnts[i] < ist->supp) {
            node->cnts[i] |= F_SKIP; continue; }
          if (mode == IST_CLOSED) supp = node->cnts[i];
          _marksub(ist, node, i, supp);
        }                       /* traverse the tree top down */
      }                         /* (that is, from root to leaves) */
    }                           /* and mark all n-1 subsets */
  }                             /* of the current item set */
}  /* ist_mark() */

/*--------------------------------------------------------------------*/

void ist_setsize (ISTREE *ist, int min, int max, int order)
{                               /* --- set the set/rule size range */
  assert(ist);                  /* check the function arguments */
  ist->maxsz = (max   < 0) ? -1 : max;  /* store the size range */
  ist->minsz = (min   < 0) ?  0 : min;  /* (min. and max. size) and */
  ist->order = (order < 0) ? -1 : 1;    /* the traversal direction */
}  /* ist_setsize() */

/*--------------------------------------------------------------------*/

void ist_seteval (ISTREE *ist, int eval, int agg,
                  double thresh, double minimp, int prune)
{                               /* --- set additional evaluation */
  assert(ist);                  /* check the function arguments */
  ist->invbxs = eval & IST_INVBXS; eval &= ~IST_INVBXS;
  ist->eval   = ((eval > IST_NONE) && (eval <= IST_LDRATIO))
              ? eval : IST_NONE;/* check and note the eval. measure */
  ist->agg    = ((agg  > IST_NONE) && (agg  <= IST_AVG))
              ? agg  : IST_NONE;/* check and note the agg. mode */
  ist->dir    = (ist->eval == IST_LDRATIO) ? +1 : re_dir(ist->eval);
  ist->thresh = ist->dir *thresh;
  ist->minimp = minimp;         /* note the evaluation parameters */
  ist->prune  = (prune > 0) ? prune : INT_MAX;
}  /* ist_seteval() */

/*--------------------------------------------------------------------*/

void ist_init (ISTREE *ist)
{                               /* --- initialize (rule) extraction */
  assert(ist);                  /* check the function argument */
  if (ist->maxsz > ist->height)    ist->maxsz = ist->height;
  ist->size  = (ist->order >= 0) ? ist->minsz : ist->maxsz;
  ist->node  = ist->lvls[(ist->size > 0) ? ist->size -1 : 0];
  ist->index = ist->item = -1;  /* initialize the */
  ist->head  = NULL;            /* extraction variables */
}  /* ist_init() */

/*--------------------------------------------------------------------*/

static int _emptyset (ISTREE *ist, int *supp, double *eval)
{                               /* --- whether to report empty set */
  assert(ist);                  /* check the function argument */
  ist->size += ist->order;      /* immediately go the next level */
  if ((ist->wgt >= ist->supp)   /* if the empty set qualifies */
  &&  (ist->wgt <= ist->smax)   /* (w.r.t. support and evaluation) */
  && ((ist->eval == IST_NONE) || (0 >= ist->thresh))) {
    if (supp) *supp = COUNT(ist->wgt);
    if (eval) *eval = 0;        /* store the empty item set support */
    return -1;                  /* the add. evaluation is always 0 */
  }                             /* return 'report the empty set' */
  return 0;                     /* return 'do not report' */
}  /* _empty() */

/*--------------------------------------------------------------------*/

int ist_set (ISTREE *ist, int *set, int *supp, double *eval)
{                               /* --- extract next frequent item set */
  int    i;                     /* loop variable, buffer */
  int    item;                  /* an item identifier */
  ISNODE *node;                 /* current item set node */
  int    s_set;                 /* support of the current set */
  double val;                   /* value of evaluation measure */

  assert(ist && set);           /* check the function arguments */
  if ((ist->size < ist->minsz)  /* if below the minimal size */
  ||  (ist->size > ist->maxsz)) /* or above the maximal size, */
    return -1;                  /* abort the function */
  if ((ist->size == 0)          /* if to report the empty item set */
  &&  _emptyset(ist, supp, eval))
    return  0;                  /* check whether it qualifies */

  /* --- find frequent item set --- */
  node = ist->node;             /* get the current item set node */
  while (1) {                   /* search for a frequent item set */
    if (++ist->index >= node->size) { /* if all subsets have been */
      node = node->succ;        /* processed, go to the successor */
      if (!node) {              /* if at the end of a level, */
        ist->size += ist->order;/* go to the next level */
        if ((ist->size < ist->minsz)
        ||  (ist->size > ist->maxsz))
          return -1;            /* if outside size range, abort */
        if ((ist->size == 0)    /* if to report the empty item set */
        &&  _emptyset(ist, supp, eval))
          return  0;            /* check whether it qualifies */
        node = ist->lvls[ist->size -1];
      }                         /* get the 1st node of the new level */
      ist->node  = node;        /* note the new item set node */
      ist->index = 0;           /* start with the first item set */
    }                           /* of the new item set node */
    if (node->offset >= 0) item = node->offset +ist->index;
    else                   item = node->cnts[node->size +ist->index];
    if (ib_getapp(ist->base, item) == APP_NONE)
      continue;                 /* skip items to ignore */
    s_set = node->cnts[ist->index];
    if ((s_set < ist->supp)     /* if the support is not sufficient */
    ||  (s_set > ist->smax))    /* or larger than the maximum, */
      continue;                 /* go to the next item set */
    /* Note that this check automatically skips all item sets that */
    /* are marked with the flag F_SKIP, because s_set is negative  */
    /* with this flag and thus necessarily smaller than ist->supp. */
    if (ist->eval <= IST_NONE){ /* if no add. eval. measure given */
      val = 0; break; }         /* abort the loop (select the set) */
    val = _evaluate(ist, node, ist->index);
    if (ist->dir *val >= ist->thresh)
      break;                    /* if the evaluation is high enough, */
  }  /* while (1) */            /* abort the loop (select the set) */
  if (supp) *supp = s_set;      /* store the item set support and */
  if (eval) *eval = val;        /* the value of the add. measure */

  /* --- build frequent item set --- */
  i        = ist->size;         /* get the current item set size */
  set[--i] = item;              /* and store the first item */
  while (node->parent) {        /* while not at the root node */
    set[--i] = ITEM(node);      /* add item to the item set */
    node = node->parent;        /* and go to the parent node */
  }
  return ist->size;             /* return the item set size */
}  /* ist_set() */

/*--------------------------------------------------------------------*/

int ist_rule (ISTREE *ist, int *rule,
              int *supp, int *body, int *head, double *eval)
{                               /* --- extract next association rule */
  int       i;                  /* loop variable */
  int       item;               /* an item identifier */
  ISNODE    *node;              /* current item set node */
  ISNODE    *parent;            /* parent of the item set node */
  int       *map, n;            /* identifier map and its size */
  int       s_set;              /* support of set  (body & head) */
  int       s_body;             /* support of body (antecedent) */
  int       s_head;             /* support of head (consequent) */
  int       s_base;             /* base support (number of trans.) */
  double    val;                /* value of evaluation measure */
  int       app;                /* appearance flag of head item */
  RULEVALFN *refn;              /* rule evaluation function */

  assert(ist && rule);          /* check the function arguments */
  if (ist->size == 0)           /* if at the empty item set, */
    ist->size += ist->order;    /* go to the next item set size */
  if ((ist->size < ist->minsz)  /* if the item set is too small */
  ||  (ist->size > ist->maxsz)) /* or too large (number of items), */
    return -1;                  /* abort the function */

  /* --- find rule --- */
  s_base = COUNT(ist->wgt);     /* get the base support and */
  node   = ist->node;           /* the current item set node */
  refn   = ((ist->eval > IST_NONE) && (ist->eval < IST_LDRATIO))
         ? re_function(ist->eval) : (RULEVALFN*)0;
  while (1) {                   /* search for a rule */
    if (ist->item >= 0) {       /* --- select next item subset */
      *--ist->path = ist->item; /* add previous head to the path */
      ist->item = ITEM(ist->head); /* and get the next head item */
      ist->head = ist->head->parent;
      if (!ist->head)           /* if all subsets have been processed */
        ist->item = -1;         /* clear the head item to trigger the */
    }                           /* selection of a new item set */
    if (ist->item < 0) {        /* --- select next item set */
      if (++ist->index >= node->size){/* if all subsets have been */
        node = node->succ;      /* processed, go to the successor */
        if (!node) {            /* if at the end of a level, */
          ist->size += ist->order;  /* go to the next level */
          if ((ist->size < ist->minsz) || (ist->size <= 0)
          ||  (ist->size > ist->maxsz))
            return -1;          /* if outside the size range, abort */
          node = ist->lvls[ist->size -1];
        }                       /* get the 1st node of the new level */
        ist->node = node;       /* note the new item set node and */
        ist->index  = 0;        /* start with the first item set */
      }                         /* of the new item set node */
      if (node->offset >= 0) item = node->offset +ist->index;
      else                   item = node->cnts[node->size +ist->index];
      app = ib_getapp(ist->base, item);
      if ((app == APP_NONE) || ((app == APP_HEAD) && HDONLY(node)))
        continue;               /* skip sets with two head only items */
      ist->item   = item;       /* set the head item identifier */
      ist->hdonly = (app == APP_HEAD) || HDONLY(node);
      ist->head   = node;       /* set the new head item node */
      ist->path   = ist->buf +ist->maxht;
    }                           /* clear the path (reinitialize it) */
    app = ib_getapp(ist->base, ist->item);
    if (!(app &  APP_HEAD)      /* get head item appearance indicator */
    ||  ((app != APP_HEAD) && ist->hdonly))
      continue;                 /* if rule is not allowed, skip it */
    s_set = COUNT(node->cnts[ist->index]);
    if ((s_set < ist->supp)     /* if the support is not sufficient */
    ||  (s_set > ist->smax)) {  /* or larger than the maximum, */
      ist->item = -1; continue; }   /* go to the next item set */
    parent = node->parent;      /* get the parent node */
    n = (int)(ist->buf +ist->maxht -ist->path);
    if (n > 0)                  /* if there is a path, use it */
      s_body = COUNT(_getsupp(ist->head, ist->path, n));
    else if (!parent)           /* if there is no parent (root node), */
      s_body = COUNT(ist->wgt); /* get the total trans. weight */
    else if (parent->offset >= 0)  /* if a pure array is used */
      s_body = COUNT(parent->cnts[ITEM(node) -parent->offset]);
    else {                      /* if an identifier map is used */
      map = parent->cnts +(n = parent->size);
      s_body = COUNT(parent->cnts[int_bsearch(ITEM(node), map, n)]);
    }                           /* find array index and get support */
    if ((s_body < ist->rule)    /* if the body support is too low */
    ||  (s_set  < s_body *ist->conf))       /* or the confidence, */
      continue;                 /* go to the next item (sub)set */
    s_head = COUNT(ist->lvls[0]->cnts[ist->item]);
    if (!refn) {                /* if no add. eval. measure given, */
      val = 0; break; }         /* abort the loop (select the rule) */
    val =  (ist->invbxs         /* compute add. evaluation measure */
        && (s_head*(double)s_body >= s_set*(double)s_base))
        ? 0 : refn(s_set, s_body, s_head, s_base);
    if (ist->dir *val >= ist->thresh)
      break;                    /* if the evaluation is high enough, */
  }  /* while (1) */            /* abort the loop (select the rule) */
  if (supp) *supp = s_set;      /* store the rule support values */
  if (body) *body = s_body;     /* (whole rule and only body) */
  if (head) *head = s_head;     /* store the head item support, */
  if (eval) *eval = val;        /* the value of the add. measure */

  /* --- build rule --- */
  if (node->offset >= 0) item = node->offset +ist->index;
  else                   item = node->cnts[node->size +ist->index];
  i = ist->size;                /* get the current item and */
  if (item != ist->item)        /* if this item is not the head, */
    rule[--i] = item;           /* add it to the rule body */
  while (node->parent) {        /* traverse the path to the root */
    if (ITEM(node) != ist->item)/* and add all items on this */
      rule[--i] = ITEM(node);   /* path to the rule body */
    node = node->parent;        /* (except the head of the rule) */
  }
  rule[0] = ist->item;          /* set the head of the rule, */
  return ist->size;             /* return the rule size */
}  /* ist_rule() */

/*--------------------------------------------------------------------*/

static void _report (ISTREE *ist, ISREPORT *rep, ISNODE *node, int supp)
{                               /* --- recursive item set reporting */
  int    i, k, c;               /* loop variables, buffers */
  int    spx;                   /* support for perfect extension */
  int    off;                   /* item offset */
  int    *map;                  /* item identifier map */
  ISNODE **chn;                 /* child node array */

  assert(ist && rep);           /* check the function arguments */
  if (!(ist->mode & IST_PERFECT))  /* if no perfext extension pruning */
    spx = INT_MAX;              /* clear perfect extension support */
  else {                        /* if perfect extensions pruning */
    spx = supp;                 /* note the parent set support */
    for (i = 0; i < node->size; i++) {
      if (COUNT(node->cnts[i]) < spx) continue;
      if (node->offset >= 0) k = i +node->offset;
      else k = node->cnts[node->size +i];
      isr_addpex(rep, k);       /* traverse the node's items and */
    }                           /* collect the perfect extensions */
  }                             /* in the item set reporter */
  if ((supp >= 0)               /* if current item set is not marked */
  &&  (supp <= ist->smax))      /* and does not exceed max. support, */
    isr_report(rep);            /* report the current item set */
  if (node->offset >= 0) {      /* if a pure array is used */
    chn = (ISNODE**)(node->cnts +node->size +PAD(node->size));
    c   = CHILDCNT(node);       /* get the child node array */
    off = (c > 0) ? ITEM(chn[0]) : 0;
    for (i = 0; i < node->size; i++) {
      supp = COUNT(node->cnts[i]);
      if ((supp <  ist->supp)   /* traverse the node's items and */
      ||  (supp >= spx))        /* check against minimum support */
        continue;               /* and the parent set support */
      ist->node  = node;        /* store the node and the index */
      ist->index = i;           /* in the node for evaluation */
      k = node->offset +i;      /* compute the item identifier */
      isr_add(rep, k, supp);    /* add the item to the reporter */
      supp = node->cnts[i];     /* get the item support (with flag) */
      k -= off;                 /* compute the child node index */
      if ((k >= 0)              /* if the corresp. child node exists, */
      &&  (k <  c) && chn[k])   /* recursively report the subtree */
        _report(ist, rep, chn[k], supp);
      else if ((supp >= 0)      /* if the item set is not marked */
      &&       (supp <= ist->smax))
        isr_report(rep);        /* report the current item set */
      isr_remove(rep, 1);       /* remove the last item */
    } }                         /* from the current item set */
  else {                        /* if an identifier map is used */
    map = node->cnts +(k = node->size);
    chn = (ISNODE**)(map +k);   /* get the item id map */
    c   = CHILDCNT(node);       /* and the child node array  */
    c   = (c > 0) ? ITEM(chn[c-1]) : -1;
    for (i = 0; i < node->size; i++) {
      supp = COUNT(node->cnts[i]);
      if ((supp <  ist->supp)   /* traverse the node's items and */
      ||  (supp >= spx))        /* check against minimum support */
        continue;               /* and the parent set support */
      ist->node  = node;        /* store the node and the index */
      ist->index = i;           /* in the node for evaluation */
      k = map[i];               /* retrieve the item identifier */
      isr_add(rep, k, supp);    /* add the item to the reporter */
      supp = node->cnts[i];     /* get the item support (with flag) */
      if (k <= c)               /* if there may be a child node, */
        while (k > ITEM(*chn)) chn++;/* skip the preceding items */
      if ((k <= c)              /* if the corresp. child node exists, */
      &&  (k == ITEM(*chn)))    /* recursively report the subtree */
        _report(ist, rep, *chn, supp);
      else if ((supp >= 0)      /* if the item set is not marked */
      &&       (supp <= ist->smax))
        isr_report(rep);        /* report the current item set */
      isr_remove(rep, 1);       /* remove the last item */
    }                           /* from the current item set */
  }
}  /* _report() */

/*--------------------------------------------------------------------*/

int ist_report (ISTREE *ist, ISREPORT *rep)
{                               /* --- recursive item set reporting */
  assert(ist && rep);           /* check the function arguments */
  _report(ist, rep, ist->lvls[0], ist->wgt);
  return isr_repcnt(rep);       /* recursively report item sets */
}  /* ist_report() */

/*--------------------------------------------------------------------*/

double ist_eval (ISTREE *ist)
{                               /* --- evaluate current item set */
  assert(ist);                  /* check the function argument */
  return _evaluate(ist, ist->node, ist->index);
}  /* ist_eval() */

/*--------------------------------------------------------------------*/

double ist_evalx (ISREPORT *rep, void *data)
{                               /* --- evaluate current item set */
  ISTREE *ist;                  /* item set tree to work on */

  assert(rep && data);          /* check the function arguments */
  ist = (ISTREE*)data;          /* type the user data */
  return _evaluate(ist, ist->node, ist->index);
}  /* ist_evalx() */

/*--------------------------------------------------------------------*/
#ifdef BENCH

void ist_stats (ISTREE *ist)
{                               /* --- show search statistics */
  printf("number of created nodes    : %d\n", ist->ndcnt);
  printf("number of pruned  nodes    : %d\n", ist->ndprn);
  printf("number of item map elements: %d\n", ist->mapsz);
  printf("number of support counters : %d\n", ist->sccnt);
  printf("necessary support counters : %d\n", ist->scnec);
  printf("pruned    support counters : %d\n", ist->scprn);
  printf("number of child pointers   : %d\n", ist->cpcnt);
  printf("necessary child pointers   : %d\n", ist->cpnec);
  printf("pruned    child pointers   : %d\n", ist->cpprn);
}  /* ist_stats() */

#endif
/*--------------------------------------------------------------------*/
#ifndef NDEBUG

static void _showtree (ISNODE *node, ITEMBASE *base, int level)
{                               /* --- show subtree */
  int    i, k, cnt;             /* loop variables, number of children */
  int    ins;                   /* flag for integer names */
  ISNODE **chn;                 /* child node array */

  assert(node && (level >= 0)); /* check the function arguments */
  ins = ib_mode(base) & IB_INTNAMES;
  i   = (node->offset < 0) ? node->size : PAD(node->size);
  chn = (ISNODE**)(node->cnts +node->size +i);
  cnt = CHILDCNT(node);         /* get the child node array */
  for (i = 0; i < node->size; i++) {
    for (k = level; --k >= 0; ) /* indent and print */
      printf("   ");            /* item identifier and counter */
    if (node->offset >= 0) k = node->offset +i;
    else                   k = node->cnts[node->size +i];
    if (ins) printf("%d", ib_int (base, k));
    else     printf("%s", ib_name(base, k));
    printf(": %d", COUNT(node->cnts[i]));
    if (node->cnts[i] & F_SKIP) printf("*");
    printf("\n");               /* print a skip flag indicator */
    if (cnt <= 0) continue;     /* check whether there are children */
    if (node->offset >= 0) k -= ITEM(chn[0]);
    else                   k  = _search(k, chn, cnt);
    if ((k >= 0) && (k < cnt) && chn[k])
      _showtree(chn[k], base, level +1);
  }                             /* show subtree recursively */
}  /* _showtree() */

/*--------------------------------------------------------------------*/

void ist_show (ISTREE *ist)
{                               /* --- show an item set tree */
  assert(ist);                  /* check the function argument */
  _showtree(ist->lvls[0], ist->base, 0);
  printf("total: %d\n", COUNT(ist->wgt));
}  /* ist_show() */             /* show the nodes recursively */

#endif
