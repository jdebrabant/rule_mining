/*----------------------------------------------------------------------
  File    : tract.h
  Contents: item and transaction management
  Author  : Christian Borgelt
  History : 2001.11.18 file created from file apriori.c
            2001.12.28 first version completed
            2001.01.02 function t_sort mapped to function int_qsort
            2002.02.19 transaction tree functions added (tat_...)
            2003.07.17 function tbg_filter() added (remove unused items)
            2003.08.21 parameter 'heap' added to function tbg_sort()
            2003.09.12 function tbg_wgt() added (total trans. weight)
            2003.09.20 empty transactions in input made possible
            2004.12.11 access functions for extended frequency added
            2004.12.15 function ib_trunc added (remove irrelevant items)
            2006.11.26 item set formatter and evaluator added
            2007.02.13 adapted to modified module tabread
            2008.08.12 considerable redesign, transaction weight added
            2008.08.14 function tbg_filter() extended (minimal size)
            2008.10.13 functions t_reverse and tbg_reverse() added
            2008.11.19 transaction tree and tree node separated
            2009.05.28 bug in function tbg_filter() fixed (minimal size)
            2009.08.27 fixed prototypes of trans. tree node functions
            2010.03.16 handling of extended transactions added
            2010.06.23 function tbg_extent() added (total item insts.)
            2010.07.02 transaction size comparison functions added
            2010.08.05 function tbg_itemcnt() added for convenience
            2010.08.10 function tbg_trim() added (for sequences)
            2010.08.11 parameter of ib_read() changed to general mode
            2010.08.13 function tbg_addib() added (add from item base)
            2010.08.19 function ib_readsel() added (item selectors)
            2010.08.22 adapted to modified module tabread
            2010.09.13 functions tbg_reverse() and tbg_mirror() added
            2010.10.25 parameter max added to function ib_recode()
            2010.12.15 functions tbg_read() added (read database)
            2010.12.20 functions tbg_icnts() and tbg_ifrqs() added
            2011.05.06 parameter wgt added to tbg_filter(), tbg_trim()
            2011.07.09 interface for transaction bag recoding modified
            2011.07.12 adapted to modified symbol table/idmap interface
            2011.07.18 alternative transaction tree implementation added
----------------------------------------------------------------------*/
#ifndef __TRACT__
#define __TRACT__
#ifndef IDMAPFN
#define IDMAPFN
#endif
#include "arrays.h"
#include "symtab.h"
#include "tabread.h"

/*----------------------------------------------------------------------
  Preprocessor Definitions
----------------------------------------------------------------------*/
/* --- item appearance flags --- */
#define APP_NONE    0x00        /* item should be ignored */
#define APP_BODY    0x01        /* item may appear in rule body */
#define APP_HEAD    0x02        /* item may appear in rule head */
#define APP_BOTH    (APP_HEAD|APP_BODY)  /* item may apper in both */

/* --- item base modes --- */
#define IB_WEIGHTS  0x01        /* items have t.a.-specific weights */
#define IB_INTNAMES 0x02        /* item names are integer numbers */

/* --- transaction read modes --- */
#define TA_WEIGHT   0x01        /* integer weight in last field */
#define TA_DUPLICS  0x02        /* allow duplicates of items */
#define TA_DUPERR   0x04        /* consider duplicates as errors */
#define TA_WGTSEP   TRD_OTHER   /* item weight separator */

/* --- error codes --- */
#define E_NONE         0        /* no error */
#define E_NOMEM      (-1)       /* not enough memory */
#define E_FOPEN      (-2)       /* cannot open file */
#define E_FREAD      (-3)       /* read error on file */
#define E_FWRITE     (-4)       /* write error on file */

#define E_NOITEMS   (-15)       /* no frequent items found */
#define E_ITEMEXP   (-16)       /* item expected */
#define E_ITEMWGT   (-17)       /* invalid item weight */
#define E_DUPITEM   (-18)       /* duplicate item */
#define E_INVITEM   (-19)       /* invalid item (not integer) */
#define E_WGTEXP    (-20)       /* transaction weight expected */
#define E_TAWGT     (-21)       /* invalid transaction weight */
#define E_FLDCNT    (-22)       /* too many fields */
#define E_APPEXP    (-23)       /* appearance indicator expected */
#define E_UNKAPP    (-24)       /* unknown appearance indicator */
#define E_PENEXP    (-25)       /* insertion penalty expected */
#define E_PENALTY   (-26)       /* invalid insertion penalty */

/* --- special macros --- */
#if defined TATREEFN && !defined TATCOMPACT
#ifdef ALIGN8
#define TAN_CHILDREN(n) ((TANODE**)((n)->items +(n)->size \
                                + (((n)->items +(n)->size \
                                   - (int*)(n)) & 1)))
#else
#define TAN_CHILDREN(n) ((TANODE**)((n)->items +(n)->size))
#endif
#endif
/* On certain 64 bit architectures, pointers have to be stored at  */
/* addresses divisible by 8, which is ensured by the above macros. */

/*----------------------------------------------------------------------
  Type Definitions
----------------------------------------------------------------------*/
typedef struct {                /* --- information about an item --- */
  int      id;                  /* item identifier */
  int      app;                 /* appearance indicator */
  double   pen;                 /* insertion penalty */
  int      frq;                 /* standard frequency (trans. weight) */
  int      xfq;                 /* extended frequency (trans. sizes) */
  int      idx;                 /* index of last transaction */
} ITEM;                         /* (item information) */

typedef struct {                /* --- an item base --- */
  IDMAP    *idmap;              /* key/name to identifier map */
  int      mode;                /* mode (IB_INTNAME, IB_WEIGHT) */
  int      wgt;                 /* total weight of transactions */
  int      app;                 /* default appearance indicator */
  double   pen;                 /* default insertion penalty */
  int      idx;                 /* index of current transaction */
  int      size;                /* size of the transaction buffer */
  void     *tract;              /* buffer for a transaction */
  int      err;                 /* error code (file reading) */
  TABREAD  *trd;                /* table/transaction reader */
} ITEMBASE;                     /* (item base) */

/* typedef int IIT; */          /* --- an item instance --- */

typedef struct {                /* --- a transaction --- */
  int      wgt;                 /* weight (number of occurrences) */
  int      size;                /* size   (number of items) */
  int      items[1];            /* items in the transaction */
} TRACT;                        /* (transaction) */

typedef struct {                /* --- a weighted item instance --- */
  int      id;                  /* item identifier */
  float    wgt;                 /* item weight (transaction-specific) */
} WIIT;                         /* (weighted item instance) */

typedef struct {                /* --- a trans. w/ weighted items --- */
  int      wgt;                 /* weight (number of occurrences) */
  int      size;                /* size   (number of items) */
  WIIT     items[1];            /* items in the transaction */
} WTRACT;                       /* (transaction with weighted items) */

typedef struct {                /* --- a transaction bag/multiset --- */
  ITEMBASE *base;               /* underlying item base */
  int      mode;                /* flag for extended transactions */
  int      max;                 /* number of items in largest trans. */
  int      wgt;                 /* total weight of transactions */
  int      extent;              /* total number of item instances */
  int      size;                /* size of the transaction array */
  int      cnt;                 /* number of transactions */
  void     **tracts;            /* array  of transactions */
  int      *icnts;              /* number of transactions per item */
  int      *ifrqs;              /* frequency of the items (weight) */
} TABAG;                        /* (transaction bag/multiset) */

#ifdef TATREEFN
#ifdef TATCOMPACT

typedef struct _tanode {        /* --- a transaction tree node --- */
  int      item;                /* next item in transaction */
  int      wgt;                 /* weight of trans. with this prefix */
  int      max;                 /* number of items in largest trans. */
  void     *data;               /* data depending on node type */
} TANODE;                       /* (transaction tree node) */

typedef struct {                /* --- a transaction tree --- */
  TABAG    *bag;                /* underlying transaction bag */
  TANODE   root;                /* root of the transaction tree */
  int      suffix[1];           /* empty transaction suffix */
} TATREE;                       /* (transaction tree) */

#else

typedef struct {                /* --- a transaction tree node --- */
  int      wgt;                 /* weight (number of transactions) */
  int      max;                 /* number of items in largest trans. */
  int      size;                /* node size (number of children) */
  int      items[1];            /* next items in rep. transactions */
} TANODE;                       /* (transaction tree node) */

typedef struct {                /* --- a transaction tree --- */
  TABAG    *bag;                /* underlying transaction bag */
  TANODE   *root;               /* root of the transaction tree */
  TANODE   empty;               /* empty transaction node */
} TATREE;                       /* (transaction tree) */

#endif
#endif
/*----------------------------------------------------------------------
  Item Base Functions
----------------------------------------------------------------------*/
extern ITEMBASE*   ib_create   (int mode, int size);
extern void        ib_delete   (ITEMBASE *base);

extern int         ib_mode     (ITEMBASE *base);
extern int         ib_cnt      (ITEMBASE *base);
extern int         ib_add      (ITEMBASE *base, const void *name);
extern int         ib_item     (ITEMBASE *base, const void *name);
extern int         ib_int      (ITEMBASE *base, int item);
extern const char* ib_name     (ITEMBASE *base, int item);
extern const char* ib_xname    (ITEMBASE *base, int item);
extern int         ib_clear    (ITEMBASE *base);
extern int         ib_add2ta   (ITEMBASE *base, const void *name);
extern void        ib_finta    (ITEMBASE *base, int wgt);

extern int         ib_getwgt   (ITEMBASE *base);
extern int         ib_setwgt   (ITEMBASE *base, int n);
extern int         ib_incwgt   (ITEMBASE *base, int n);

extern int         ib_getapp   (ITEMBASE *base, int item);
extern int         ib_setapp   (ITEMBASE *base, int item, int app);
extern int         ib_getfrq   (ITEMBASE *base, int item);
extern int         ib_setfrq   (ITEMBASE *base, int item, int frq);
extern int         ib_incfrq   (ITEMBASE *base, int item, int frq);
extern int         ib_getxfq   (ITEMBASE *base, int item);
extern int         ib_setxfq   (ITEMBASE *base, int item, int xfq);
extern int         ib_incxfq   (ITEMBASE *base, int item, int xfq);
extern double      ib_getpen   (ITEMBASE *base, int item);
extern double      ib_setpen   (ITEMBASE *base, int item, double pen);

extern int         ib_readsel  (ITEMBASE *base, TABREAD *trd);
extern int         ib_readapp  (ITEMBASE *base, TABREAD *trd);
extern int         ib_readpen  (ITEMBASE *base, TABREAD *trd);
extern int         ib_read     (ITEMBASE *base, TABREAD *trd, int mode);
extern const char* ib_errmsg   (ITEMBASE *base, char *buf, size_t size);

extern int         ib_recode   (ITEMBASE *base, int min, int max,
                                int dir, int *map);
extern void        ib_trunc    (ITEMBASE *base, int n);

extern TRACT*      ib_tract    (ITEMBASE *base);
extern WTRACT*     ib_wtract   (ITEMBASE *base);

/*----------------------------------------------------------------------
  Transaction Functions
----------------------------------------------------------------------*/
extern TRACT*      ta_create   (const int *items, int n, int wgt);
extern void        ta_delete   (TRACT *t);
extern TRACT*      ta_clone    (const TRACT *t);

extern const int*  ta_items    (const TRACT *t);
extern int         ta_size     (const TRACT *t);
extern int         ta_wgt      (const TRACT *t);

extern void        ta_sort     (TRACT *t);
extern void        ta_reverse  (TRACT *t);
extern int         ta_unique   (TRACT *t);

extern int         ta_cmp      (const void *p1,
                                const void *p2, void *data);
extern int         ta_cmpx     (const TRACT *t, const int *items,int n);
extern int         ta_cmpsz    (const void *p1,
                                const void *p2, void *data);
#ifndef NDEBUG
extern void        ta_show     (TRACT *t, ITEMBASE *base);
#endif

/*----------------------------------------------------------------------
  Weighted Item Instance Functions
----------------------------------------------------------------------*/
extern int         wi_cmp      (const WIIT *a, const WIIT *b);
extern void        wi_sort     (WIIT *wia, int n);
extern void        wi_reverse  (WIIT *wia, int n);
extern int         wi_unique   (WIIT *wia, int n);

/*----------------------------------------------------------------------
  Extended Transaction Functions
----------------------------------------------------------------------*/
extern WTRACT*     wta_create  (int size, int wgt);
extern void        wta_delete  (WTRACT *t);
extern WTRACT*     wta_clone   (const WTRACT *t);

extern void        wta_add     (WTRACT *t, int item, float wgt);
extern const WIIT* wta_items   (const WTRACT *t);
extern int         wta_size    (const WTRACT *t);
extern int         wta_wgt     (const WTRACT *t);

extern void        wta_sort    (WTRACT *t);
extern void        wta_reverse (WTRACT *t);
extern int         wta_unique  (WTRACT *t);

extern int         wta_cmp     (const void *p1,
                                const void *p2, void *data);

/*----------------------------------------------------------------------
  Transaction Bag/Multiset Functions
----------------------------------------------------------------------*/
extern TABAG*      tbg_create  (ITEMBASE *base);
extern void        tbg_delete  (TABAG *bag, int delis);
extern ITEMBASE*   tbg_base    (TABAG *bag);

extern int         tbg_mode    (const TABAG *bag);
extern int         tbg_itemcnt (const TABAG *bag);
extern int         tbg_cnt     (const TABAG *bag);
extern int         tbg_wgt     (const TABAG *bag);
extern int         tbg_max     (const TABAG *bag);
extern int         tbg_extent  (const TABAG *bag);
extern const int*  tbg_icnts   (TABAG *bag);
extern const int*  tbg_ifrqs   (TABAG *bag);

extern int         tbg_add     (TABAG *bag,  TRACT *t);
extern int         tbg_addw    (TABAG *bag, WTRACT *t);
extern int         tbg_addib   (TABAG *bag);
extern TRACT*      tbg_tract   (TABAG *bag, int index);
extern WTRACT*     tbg_wtract  (TABAG *bag, int index);
extern int         tbg_read    (TABAG *bag, TABREAD *trd, int mode);
extern const char* tbg_errmsg  (TABAG *bag, char *buf, size_t size);

extern int         tbg_recode  (TABAG *bag, int min, int max, int dir);
extern void        tbg_filter  (TABAG *bag, int min,
                                const int *marks, double wgt);
extern void        tbg_trim    (TABAG *bag, int min,
                                const int *marks, double wgt);
extern void        tbg_itsort  (TABAG *bag, int dir, int heap);
extern void        tbg_mirror  (TABAG *bag);
extern void        tbg_sort    (TABAG *bag, int dir, int heap);
extern void        tbg_sortsz  (TABAG *bag, int dir, int heap);
extern void        tbg_reverse (TABAG *bag);
extern int         tbg_reduce  (TABAG *bag);
extern int         tbg_occur   (TABAG *bag, const int *items, int n);

#ifndef NDEBUG
extern void        tbg_show    (TABAG *bag);
#endif

/*----------------------------------------------------------------------
  Transaction Node Functions
----------------------------------------------------------------------*/
#ifdef TATREEFN
#ifdef TATCOMPACT
extern int         tan_item    (const TANODE *node);
extern int         tan_wgt     (const TANODE *node);
extern int         tan_max     (const TANODE *node);
extern TANODE*     tan_sibling (const TANODE *node);
extern TANODE*     tan_children(const TANODE *node);
extern const int*  tan_suffix  (const TANODE *node);
#else
extern int         tan_wgt     (const TANODE *node);
extern int         tan_max     (const TANODE *node);
extern int         tan_size    (const TANODE *node);
extern int*        tan_items   (TANODE *node);
extern int         tan_item    (const TANODE *node, int index);
extern TANODE*     tan_child   (const TANODE *node, int index);
#endif
#endif
/*----------------------------------------------------------------------
  Transaction Tree Functions
----------------------------------------------------------------------*/
#ifdef TATREEFN
#ifdef TATCOMPACT
extern TATREE*     tat_create  (TABAG *bag);
extern void        tat_delete  (TATREE *tree, int del);
extern TABAG*      tat_tabag   (const TATREE *tree);
extern TANODE*     tat_root    (const TATREE *tree);
extern int         tat_size    (const TATREE *tree);
#else
extern TATREE*     tat_create  (TABAG *bag);
extern void        tat_delete  (TATREE *tree, int del);
extern TABAG*      tat_tabag   (const TATREE *tree);
extern TANODE*     tat_root    (const TATREE *tree);
extern int         tat_size    (const TATREE *tree);
extern int         tat_filter  (TATREE *tree, int min,
                                const int *marks, int heap);
#endif
#ifndef NDEBUG
extern void        tat_show    (TATREE *tree);
#endif
#endif

/*----------------------------------------------------------------------
  Preprocessor Definitions
----------------------------------------------------------------------*/
#define ib_mode(s)        ((s)->mode)
#define ib_cnt(s)         idm_cnt((s)->idmap)
#define ib_item(s,n)      idm_getid((s)->idmap, n)
#define ib_name(s,i)      idm_name(idm_byid((s)->idmap, i))
#define ib_int(s,i)       (*(int*)idm_key(idm_byid((s)->idmap, i)))
#define ib_clear(s)       (((TRACT*)s->tract)->size = 0)

#define ib_getwgt(s)      ((s)->wgt)
#define ib_setwgt(s,n)    ((s)->wgt  = (n))
#define ib_incwgt(s,n)    ((s)->wgt += (n))

#define ib_getapp(s,i)    (((ITEM*)idm_byid((s)->idmap, i))->app)
#define ib_setapp(s,i,a)  (((ITEM*)idm_byid((s)->idmap, i))->app  = (a))
#define ib_getfrq(s,i)    (((ITEM*)idm_byid((s)->idmap, i))->frq)
#define ib_setfrq(s,i,n)  (((ITEM*)idm_byid((s)->idmap, i))->frq  = (n))
#define ib_incfrq(s,i,n)  (((ITEM*)idm_byid((s)->idmap, i))->frq += (n))
#define ib_getxfq(s,i)    (((ITEM*)idm_byid((s)->idmap, i))->xfq)
#define ib_setxfq(s,i,n)  (((ITEM*)idm_byid((s)->idmap, i))->xfq  = (n))
#define ib_incxfq(s,i,n)  (((ITEM*)idm_byid((s)->idmap, i))->xfq += (n))
#define ib_getpen(s,i)    (((ITEM*)idm_byid((s)->idmap, i))->pen)
#define ib_setpen(s,i,p)  (((ITEM*)idm_byid((s)->idmap, i))->pen  = (p))

#define ib_tract(s)       ((TRACT*) (s)->tract)
#define ib_wtract(s)      ((WTRACT*)(s)->tract)

/*--------------------------------------------------------------------*/
#define ta_delete(t)      free(t)
#define ta_sort(t)        int_qsort  ((t)->items, (t)->size)
#define ta_reverse(t)     int_reverse((t)->items, (t)->size)
#define ta_unique(t)      int_unique ((t)->items, (t)->size)
#define ta_items(t)       ((const int*)(t)->items)
#define ta_size(t)        ((t)->size)
#define ta_wgt(t)         ((t)->wgt)

/*--------------------------------------------------------------------*/
#define wta_delete(t)     free(t)
#define wta_items(t)      ((t)->items)
#define wta_size(t)       ((t)->size)
#define wta_wgt(t)        ((t)->wgt)

/*--------------------------------------------------------------------*/
#define tbg_base(b)       ((b)->base)

#define tbg_mode(b)       ((b)->mode)
#define tbg_itemcnt(b)    ib_cnt((b)->base)
#define tbg_cnt(b)        ((b)->cnt)
#define tbg_wgt(b)        ((b)->wgt)
#define tbg_max(b)        ((b)->max)
#define tbg_extent(b)     ((b)->extent)

#define tbg_tract(b,i)    ((TRACT*) (b)->tracts[i])
#define tbg_wtract(b,i)   ((WTRACT*)(b)->tracts[i])
#define tbg_errmsg(b,s,n) ib_errmsg((b)->base, s, n)
#define tbg_reverse(b)    ptr_reverse((b)->tracts, (b)->cnt)

/*--------------------------------------------------------------------*/
#ifdef TATREEFN
#ifdef TATCOMPACT

#define tan_item(n)       ((n)->item)
#define tan_wgt(n)        ((n)->wgt)
#define tan_max(n)        ((n)->max)
#define tan_sibling(n)    (((n)[1].item >= 0) ? (n)+1 : NULL)
#define tan_children(n)   ((TANODE*)(n)->data)
#define tan_suffix(n)     ((const int*)(n)->data)

#define tat_tabag(t)      ((t)->bag)
#define tat_root(t)       (&(t)->root)

#else

#define tan_wgt(n)        ((n)->wgt)
#define tan_max(n)        ((n)->max)
#define tan_size(n)       ((n)->size)
#define tan_item(n,i)     ((n)->items[i])
#define tan_items(n)      ((n)->items)
#define tan_child(n,i)    (TAN_CHILDREN(n)[i])

#define tat_tabag(t)      ((t)->bag)
#define tat_root(t)       ((t)->root)

#endif
#endif

#endif
