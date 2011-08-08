/*----------------------------------------------------------------------
  File    : arrays.c
  Contents: some basic array operations, especially for pointer arrays
  Author  : Christian Borgelt
  History : 1996.09.16 file created as arrays.c
            1999.02.04 long int changed to int
            2001.06.03 function ptr_shuffle added
            2002.01.02 functions for basic data types added
            2002.03.03 functions ptr_reverse etc. added
            2003.08.21 function ptr_heapsort added
            2007.01.16 shuffle functions for basic data types added
            2007.12.02 bug in reverse functions fixed
            2008.08.01 renamed to arrays.c, some functions added
            2008.08.11 main function added (sortargs)
            2008.08.12 functions ptr_unique etc. added
            2008.08.17 binary search functions improved
            2008.10.05 functions to clear arrays added
            2010.07.31 index array sorting functions added
            2010.12.07 added several explicit type casts
----------------------------------------------------------------------*/
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include "arrays.h"

/*----------------------------------------------------------------------
  Preprocessor Definitions
----------------------------------------------------------------------*/
#define BUFSIZE     1024        /* size of fixed buffer for moving */
#define TH_INSERT   16          /* threshold for insertion sort */

/*----------------------------------------------------------------------
  Functions for Arrays of Basic Data Types
----------------------------------------------------------------------*/

#define MOVE(name,type) \
void name##_move (type *array, int off, int n, int pos) \
{                               /* --- move a number array section */  \
  int  i, end;                  /* loop variable, end index */         \
  type *src, *dst;              /* to traverse the array */            \
  type fxd[BUFSIZE], *buf;      /* buffer for copying */               \
                                                                       \
  assert(array                  /* check the function arguments */     \
     && (off >= 0) && (n >= 0) && (pos >= 0));                         \
  if ((pos >= off) && (pos <= off +n))                                 \
    return;                     /* check whether moving is necessary */\
  if (pos < off) { end = off +n; off = pos; pos = end -n; }            \
  else           { end = pos;               pos = off +n; }            \
  buf = fxd;                    /* normalize the indices */            \
  if (pos -off < end -pos) {    /* if first section is smaller */      \
    while (pos > off) {         /* while there are elements to shift */\
      n = pos -off;             /* get the number of elements */       \
      if (n > BUFSIZE) {        /* if the fixed buffer is too small */ \
        buf = (type*)malloc(n *sizeof(type));                          \
        if (!buf) { buf = fxd; n = BUFSIZE; }                          \
      }                         /* try to allocate a fitting buffer */ \
      src = array + pos -n;     /* get number of elements and */       \
      dst = buf;                /* copy source to the buffer */        \
      for (i = n;        --i >= 0; ) *dst++ = *src++;                  \
      dst = array +pos -n;      /* shift down/left second section */   \
      for (i = end -pos; --i >= 0; ) *dst++ = *src++;                  \
      src = buf;                /* copy buffer to destination */       \
      for (i = n;        --i >= 0; ) *dst++ = *src++;                  \
      pos -= n; end -= n;       /* second section has been shifted */  \
    } }                         /* down/left cnt elements */           \
  else {                        /* if second section is smaller */     \
    while (end > pos) {         /* while there are elements to shift */\
      n = end -pos;             /* get the number of elements */       \
      if (n > BUFSIZE) {        /* if the fixed buffer is too small */ \
        buf = (type*)malloc(n *sizeof(type));                          \
        if (!buf) { buf = fxd; n = BUFSIZE; }                          \
      }                         /* try to allocate a fitting buffer */ \
      src = array +pos +n;      /* get number of elements and */       \
      dst = buf +n;             /* copy source to the buffer */        \
      for (i = n;        --i >= 0; ) *--dst = *--src;                  \
      dst = array +pos +n;      /* shift up/right first section */     \
      for (i = pos -off; --i >= 0; ) *--dst = *--src;                  \
      src = buf +n;             /* copy buffer to destination */       \
      for (i = n;        --i >= 0; ) *--dst = *--src;                  \
      pos += n; off += n;       /* first section has been shifted */   \
    }                           /* up/right cnt elements */            \
  }                                                                    \
  if (buf != fxd) free(buf);    /* delete an allocated buffer */       \
}  /* move() */

/*--------------------------------------------------------------------*/

MOVE(sht, short)
MOVE(int, int)
MOVE(flt, float)
MOVE(dbl, double)

/*--------------------------------------------------------------------*/

#define SELECT(name,type) \
void name##_select (type *array, int n, int k, RANDFN *randfn) \
{                               /* --- shuffle array entries */        \
  int  i;                       /* array index */                      \
  type t;                       /* exchange buffer */                  \
                                                                       \
  assert(array && (n >= k) && (k >= 0));                               \
  if (k >= n) k = n-1;          /* adapt the number of selections */   \
  while (--k >= 0) {            /* shuffle loop (k selections) */      \
    i = (int)(randfn() *n--);   /* compute a random index */           \
    if (i > n) i = n;           /* in the remaining section and */     \
    if (i < 0) i = 0;           /* exchange the array elements */      \
    t = array[i]; array[i] = array[0]; *array++ = t;                   \
  }                                                                    \
}  /* select() */

/*--------------------------------------------------------------------*/

SELECT(sht, short)
SELECT(int, int)
SELECT(flt, float)
SELECT(dbl, double)

/*--------------------------------------------------------------------*/

#define SHUFFLE(name,type) \
void name##_shuffle (type *array, int n, RANDFN *randfn) \
{ name##_select(array, n, n-1, randfn); }

/*--------------------------------------------------------------------*/

SHUFFLE(sht, short)
SHUFFLE(int, int)
SHUFFLE(flt, float)
SHUFFLE(dbl, double)

/*--------------------------------------------------------------------*/

#define REVERSE(name, type) \
void name##_reverse (type *array, int n) \
{                               /* --- reverse a number array */       \
  type t;                       /* exchange buffer */                  \
  while (--n > 0) {             /* reverse the order of the elems. */  \
    t = array[n]; array[n--] = array[0]; *array++ = t; }               \
}  /* reverse */

/*--------------------------------------------------------------------*/

REVERSE(sht, short)
REVERSE(int, int)
REVERSE(flt, float)
REVERSE(dbl, double)

/*--------------------------------------------------------------------*/

#define QSORT(name,type) \
static void _##name##_qrec (type *array, int n) \
{                               /* --- recursive part of sort */       \
  type *l, *r;                  /* pointers to exchange positions */   \
  type x, t;                    /* pivot element and exchange buffer */\
  int  m;                       /* number of elements in sections */   \
                                                                       \
  do {                          /* sections sort loop */               \
    l = array; r = l +n -1;     /* start at left and right boundary */ \
    if (*l > *r) { t = *l; *l = *r; *r = t; }                          \
    x = array[n >> 1];          /* get the middle element as pivot */  \
    if      (x < *l) x = *l;    /* compute median of three */          \
    else if (x > *r) x = *r;    /* to find a better pivot */           \
    while (1) {                 /* split and exchange loop */          \
      while (*++l < x);         /* skip smaller elems. on the left */  \
      while (*--r > x);         /* skip greater elems. on the right */ \
      if (l >= r) {             /* if less than two elements left, */  \
        if (l <= r) { l++; r--; } break; }       /* abort the loop */  \
      t = *l; *l = *r; *r = t;  /* otherwise exchange elements */      \
    }                                                                  \
    m = array +n -l;            /* compute the number of elements */   \
    n = r -array +1;            /* right and left of the split */      \
    if (n > m) {                /* if right section is smaller, */     \
      if (m >= TH_INSERT)       /* but larger than the threshold, */   \
        _##name##_qrec(l, m); } /* sort it by an recursive call */     \
    else {                      /* if the left section is smaller, */  \
      if (n >= TH_INSERT)       /* but larger than the threshold, */   \
        _##name##_qrec(array,n);/* sort it by an recursive call, */    \
      array = l; n = m;         /* then switch to the right section */ \
    }                           /* keeping its size m in variable n */ \
  } while (n >= TH_INSERT);     /* while greater than threshold */     \
}  /* _qrec() */                                                       \
                                                                       \
/*------------------------------------------------------------------*/ \
                                                                       \
void name##_qsort (type *array, int n)                                 \
{                               /* --- sort a number array */          \
  int  k;                       /* size of first section */            \
  type *l, *r;                  /* to traverse the array */            \
  type t;                       /* exchange buffer */                  \
                                                                       \
  assert(array && (n >= 0));    /* check the function arguments */     \
  if (n <= 1) return;           /* do not sort less than two elems. */ \
  if (n < TH_INSERT)            /* if less elements than threshold */  \
    k = n;                      /* for insertion sort, note the */     \
  else {                        /* number of elements, otherwise */    \
    _##name##_qrec(array, n);   /* call the recursive sort function */ \
    k = TH_INSERT -1;           /* and get the number of elements */   \
  }                             /* in the first array section */       \
  for (l = r = array; --k > 0;) /* find position of smallest element */\
    if (*++r < *l) l = r;       /* within the first k elements */      \
  r = array;                    /* swap the smallest element */        \
  t = *l; *l = *r; *r = t;      /* to front as a sentinel */           \
  while (--n > 0) {             /* standard insertion sort */          \
    t = *++r;                   /* note the number to insert */        \
    for (l = r; *--l > t; k--)  /* shift right all numbers that are */ \
      l[1] = *l;                /* greater than the one to insert */   \
    l[1] = t;                   /* and store the number to insert */   \
  }                             /* in the place thus found */          \
}  /* qsort() */

/*--------------------------------------------------------------------*/

QSORT(sht, short)
QSORT(int, int)
QSORT(flt, float)
QSORT(dbl, double)

/*--------------------------------------------------------------------*/

#define HEAPSORT(name,type) \
static void _##name##_sift (type *array, int l, int r)                 \
{                               /* --- let element sift down in heap */\
  type t;                       /* buffer for an array element */      \
  int  i;                       /* index of first successor in heap */ \
                                                                       \
  t = array[l];                 /* note the sift element */            \
  i = l +l +1;                  /* compute index of first successor */ \
  do {                          /* sift loop */                        \
    if ((i < r) && (array[i] < array[i+1]))                            \
      i++;                      /* if second successor is greater */   \
    if (t >= array[i])          /* if the successor is greater */      \
      break;                    /* than the sift element, */           \
    array[l] = array[i];        /* let the successor ascend in heap */ \
    l = i; i += i +1;           /* compute index of first successor */ \
  } while (i <= r);             /* while still within heap */          \
  array[l] = t;                 /* store the sift element */           \
}  /* _sift() */                                                       \
                                                                       \
/*------------------------------------------------------------------*/ \
                                                                       \
void name##_heapsort (type *array, int n)                              \
{                               /* --- heap sort for number arrays */  \
  int  l, r;                    /* boundaries of heap section */       \
  type t;                       /* exchange buffer */                  \
                                                                       \
  assert(array && (n >= 0));    /* check the function arguments */     \
  if (n <= 1) return;           /* do not sort less than 2 elements */ \
  l = n >> 1;                   /* at start, only the second half */   \
  r = n -1;                     /* of the array has heap structure */  \
  while (--l >= 0)              /* while the heap is not complete, */  \
    _##name##_sift(array,l,r);  /* extend it by one element */         \
  while (1) {                   /* heap reduction loop */              \
    t = array[0];               /* swap the greatest element */        \
    array[0] = array[r];        /* to the end of the array */          \
    array[r] = t;                                                      \
    if (--r <= 0) break;        /* if the heap is empty, abort */      \
    _##name##_sift(array,0,r);  /* let the element that has been */    \
  }                             /* swapped to front sift down */       \
}  /* heapsort() */

/*--------------------------------------------------------------------*/

HEAPSORT(sht, short)
HEAPSORT(int, int)
HEAPSORT(flt, float)
HEAPSORT(dbl, double)

/*--------------------------------------------------------------------*/

#define UNIQUE(name,type) \
int name##_unique (type *array, int n) \
{                               /* --- remove duplicate elements */    \
  type *s, *d;                  /* to traverse the array */            \
                                                                       \
  assert(array && (n >= 0));    /* check the function arguments */     \
  if (n <= 1) return n;         /* check for 0 or 1 element */         \
  for (d = s = array; --n > 0;) /* traverse the (sorted) array and */  \
    if (*++s != *d) *++d = *s;  /* collect the unique elements */      \
  return ++d -array;            /* return new number of elements */    \
}  /* unique() */

/*--------------------------------------------------------------------*/

UNIQUE(sht, short)
UNIQUE(int, int)
UNIQUE(flt, float)
UNIQUE(dbl, double)

/*--------------------------------------------------------------------*/

#define BSEARCH(name,type) \
int name##_bsearch (type key, type *array, int n) \
{                               /* --- do a binary search */           \
  int l, r, m;                  /* array indices */                    \
                                                                       \
  assert(array && (n >= 0));    /* check the function arguments */     \
  for (l = 0, r = n; l < r; ) { /* while search range is not empty */  \
    m = (l+r) >> 1;             /* compare the given key */            \
    if (key > array[m]) l = m+1;/* to the middle element */            \
    else                r = m;  /* adapt the search range */           \
  }                             /* according to the result */          \
  return ((l >= n) || (key != array[l])) ? -1-l : l;                   \
}  /* bsearch() */              /* return the (insertion) position */

/*--------------------------------------------------------------------*/

BSEARCH(sht, short)
BSEARCH(int, int)
BSEARCH(flt, float)
BSEARCH(dbl, double)

/*----------------------------------------------------------------------
  Functions for Pointer Arrays
----------------------------------------------------------------------*/

void ptr_move (void *array, int off, int n, int pos)
{                               /* --- move a pointer array section */
  int  i, end;                  /* loop variable, end index */
  void **src, **dst;            /* to traverse the array */
  void *fxd[BUFSIZE], **buf;    /* buffer for copying */

  assert(array                  /* check the function arguments */
     && (off >= 0) && (n >= 0) && (pos >= 0));
  if ((pos >= off) && (pos <= off +n))
    return;                     /* check whether moving is necessary */
  if (pos < off) { end = off +n; off = pos; pos = end -n; }
  else           { end = pos;               pos = off +n; }
  buf = fxd;                    /* normalize the indices */
  if (pos -off < end -pos) {    /* if first section is smaller */
    while (pos > off) {         /* while there are elements to shift */
      n = pos -off;             /* get the number of elements */
      if (n > BUFSIZE) {        /* if the fixed buffer is too small */
        buf = (void**)malloc(n *sizeof(void*));
        if (!buf) { buf = fxd; n = BUFSIZE; }
      }                         /* try to allocate a fitting buffer */
      src = (void**)array+pos-n;/* get number of elements and */
      dst = buf;                /* copy source to the buffer */
      for (i = n;        --i >= 0; ) *dst++ = *src++;
      dst = (void**)array+pos-n;/* shift down/left second section */
      for (i = end -pos; --i >= 0; ) *dst++ = *src++;
      src = buf;                /* copy buffer to destination */
      for (i = n;        --i >= 0; ) *dst++ = *src++;
      pos -= n; end -= n;       /* second section has been shifted */
    } }                         /* down/left cnt elements */
  else {                        /* if second section is smaller */
    while (end > pos) {         /* while there are elements to shift */
      n = end -pos;             /* get the number of elements */
      if (n > BUFSIZE) {        /* if the fixed buffer is too small */
        buf = (void**)malloc(n *sizeof(void*));
        if (!buf) { buf = fxd; n = BUFSIZE; }
      }                         /* try to allocate a fitting buffer */
      src = (void**)array+pos+n;/* get number of elements and */
      dst = buf +n;             /* copy source to the buffer */
      for (i = n;        --i >= 0; ) *--dst = *--src;
      dst = (void**)array+pos+n;/* shift up/right first section */
      for (i = pos -off; --i >= 0; ) *--dst = *--src;
      src = buf +n;             /* copy buffer to destination */
      for (i = n;        --i >= 0; ) *--dst = *--src;
      pos += n; off += n;       /* first section has been shifted */
    }                           /* up/right cnt elements */
  }
  if (buf != fxd) free(buf);    /* delete an allocated buffer */
}  /* ptr_move() */

/*--------------------------------------------------------------------*/

void ptr_select (void *array, int n, int k, RANDFN *randfn)
{                               /* --- select random array entries */
  int  i;                       /* array index */
  void **a = (void**)array;     /* array to sort */
  void *t;                      /* exchange buffer */

  assert(array                  /* check the function arguments */
      && randfn && (n >= k) && (k >= 0));
  if (k >= n) k = n-1;          /* adapt the number of selections */
  while (--k >= 0) {            /* shuffle loop (k selections) */
    i = (int)(randfn() *n--);   /* compute a random index */
    if (i > n) i = n;           /* in the remaining section and */
    if (i < 0) i = 0;           /* clamp it to a valid range */
    t = a[i]; a[i] = a[0]; *a++ = t;
  }                             /* exchange the array elements */
}  /* ptr_select() */

/*--------------------------------------------------------------------*/

void ptr_shuffle (void *array, int n, RANDFN *randfn)
{ ptr_select(array, n, n-1, randfn); }

/*--------------------------------------------------------------------*/

void ptr_reverse (void *array, int n)
{                               /* --- reverse a pointer array */
  void **a = (void**)array;     /* array to sort */
  void *t;                      /* exchange buffer */

  assert(array && (n >= 0));    /* check the function arguments */
  while (--n > 0) {             /* reverse the order of the elements */
    t = a[n]; a[n--] = a[0]; *a++ = t; }
}  /* ptr_reverse() */

/*--------------------------------------------------------------------*/

static void _qrec (void **array, int n, CMPFN *cmpfn, void *data)
{                               /* --- recursive part of quicksort */
  void **l, **r;                /* pointers to exchange positions */
  void *x,  *t;                 /* pivot element and exchange buffer */
  int  m;                       /* number of elements in 2nd section */

  do {                          /* sections sort loop */
    l = array; r = l +n -1;     /* start at left and right boundary */
    if (cmpfn(*l, *r, data) > 0) {  /* bring the first and last */
      t = *l; *l = *r; *r = t; }    /* element into proper order */
    x = array[n >> 1];          /* get the middle element as pivot */
    if      (cmpfn(x, *l, data) < 0) x = *l;  /* try to find a */
    else if (cmpfn(x, *r, data) > 0) x = *r;  /* better pivot */
    while (1) {                 /* split and exchange loop */
      while (cmpfn(*++l, x, data) < 0)    /* skip left  elements that */
        ;                       /* are smaller than the pivot element */
      while (cmpfn(*--r, x, data) > 0)    /* skip right elements that */
        ;                       /* are greater than the pivot element */
      if (l >= r) {             /* if less than two elements left, */
        if (l <= r) { l++; r--; } break; }       /* abort the loop */
      t = *l; *l = *r; *r = t;  /* otherwise exchange elements */
    }
    m = array +n -l;            /* compute the number of elements */
    n = r -array +1;            /* right and left of the split */
    if (n > m) {                /* if right section is smaller, */
      if (m >= TH_INSERT)       /* but larger than the threshold, */
        _qrec(l,m,cmpfn,data);} /* sort it by a recursive call, */
    else {                      /* if the left section is smaller, */
      if (n >= TH_INSERT)       /* but larger than the threshold, */
        _qrec(array,n,cmpfn,data); /* sort it by a recursive call, */
      array = l; n = m;         /* then switch to the right section */
    }                           /* keeping its size m in variable n */
  } while (n >= TH_INSERT);     /* while greater than threshold */
}  /* _qrec() */

/*--------------------------------------------------------------------*/

void ptr_qsort (void *array, int n, CMPFN *cmpfn, void *data)
{                               /* --- quicksort for pointer arrays */
  int  k;                       /* size of first section */
  void **l, **r;                /* to traverse the array */
  void *t;                      /* exchange buffer */

  assert(array && (n >= 0) && cmpfn); /* check the function arguments */
  if (n <= 1) return;           /* do not sort less than two elements */
  if (n < TH_INSERT)            /* if fewer elements than threshold */
    k = n;                      /* for insertion sort, note the */
  else {                        /* number of elements, otherwise */
    _qrec((void**)array, n, cmpfn, data);
    k = TH_INSERT -1;           /* call the recursive function and */
  }                             /* get size of first array section */
  for (l = r = (void**)array; --k > 0; )
    if (cmpfn(*++r, *l, data) < 0)
      l = r;                    /* find smallest of first k elements */
  r = (void**)array;            /* swap the smallest element */
  t = *l; *l = *r; *r = t;      /* to the front as a sentinel */
  while (--n > 0) {             /* insertion sort loop */
    t = *++r;                   /* note the element to insert */
    for (l = r; cmpfn(*--l, t, data) > 0; ) /* shift right elements */
      l[1] = *l;                /* that are greater than the one to */
    l[1] = t;                   /* insert and store the element to */
  }                             /* insert in the place thus found */
}  /* ptr_qsort() */

/*--------------------------------------------------------------------*/

static void _sift (void **array, int l, int r, CMPFN *cmpfn, void *data)
{                               /* --- let element sift down in heap */
  void *t;                      /* buffer for an array element */
  int  i;                       /* index of first successor in heap */

  t = array[l];                 /* note the sift element */
  i = l +l +1;                  /* compute index of first successor */
  do {                          /* sift loop */
    if ((i < r)                 /* if second successor is greater */
    &&  (cmpfn(array[i], array[i+1], data) < 0))
      i++;                      /* go to the second successor */
    if (cmpfn(t, array[i], data) >= 0) /* if the successor is greater */
      break;                           /* than the sift element, */
    array[l] = array[i];        /* let the successor ascend in heap */
    l = i; i += i +1;           /* compute index of first successor */
  } while (i <= r);             /* while still within heap */
  array[l] = t;                 /* store the sift element */
}  /* _sift() */

/*--------------------------------------------------------------------*/

void ptr_heapsort (void *array, int n, CMPFN *cmpfn, void *data)
{                               /* --- heap sort for pointer arrays */
  int  l, r;                    /* boundaries of heap section */
  void *t, **a;                 /* exchange buffer, array */

  assert(array && (n >= 0) && cmpfn); /* check the function arguments */
  if (n <= 1) return;           /* do not sort less than two elements */
  l = n >> 1;                   /* at start, only the second half */
  r = n -1;                     /* of the array has heap structure */
  a = (void**)array;            /* type the array pointer */
  while (--l >= 0)              /* while the heap is not complete, */
    _sift(a, l, r, cmpfn, data);/* extend it by one element */
  while (1) {                   /* heap reduction loop */
    t = a[0]; a[0] = a[r];      /* swap the greatest element */
    a[r] = t;                   /* to the end of the array */
    if (--r <= 0) break;        /* if the heap is empty, abort */
    _sift(a, 0, r, cmpfn, data);
  }                             /* let the element that has been */
}  /* ptr_heapsort() */         /* swapped to front sift down */

/*--------------------------------------------------------------------*/

int ptr_bsearch (const void *key,
                 void *array, int n, CMPFN *cmpfn, void *data)
{                               /* --- do a binary search */
  int  l, r, m, c, x = -1;      /* array indices, comparison result */
  void **a;                     /* typed array */

  assert(key && array && (n >= 0) && cmpfn);    /* check arguments */
  a = (void**)array;            /* type the array pointer */
  for (l = 0, r = n; l < r; ) { /* while search range is not empty */
    m = (l+r) >> 1;             /* compare the given key */
    c = cmpfn(key, a[m], data); /* to the middle element */
    if (c > 0)  l = m+1;        /* adapt the search range */
    else x = c, r = m;          /* according to the result */
  }
  return (x) ? -1-l : l;        /* return the (insertion) position */
}  /* ptr_bsearch() */

/*--------------------------------------------------------------------*/

int ptr_unique (void *array, int n, CMPFN *cmpfn, void *data,
                OBJFN *delfn)
{                               /* --- remove duplicate elements */
  void **s, **d;                /* to traverse the pointer array */

  assert(array && (n >= 0) && cmpfn); /* check the function arguments */
  if (n <= 1) return n;         /* check for 0 or 1 element */
  for (d = s = (void**)array; --n > 0; ) {
    if (cmpfn(*++s, *d, data) != 0) *++d = *s;
    else if (delfn) delfn(*s);  /* traverse the (sorted) array */
  }                             /* and collect unique elements */
  return ++d -(void**)array;    /* return the new number of elements */
}  /* ptr_unique() */

/*----------------------------------------------------------------------
  Functions for Index Arrays
----------------------------------------------------------------------*/

#define IDX_QSORT(name,type) \
static void _##name##_qrec (int *index, int n, const type *array)      \
{                               /* --- recursive part of sort */       \
  int  *l, *r;                  /* pointers to exchange positions */   \
  int  x, t;                    /* pivot element and exchange buffer */\
  int  m;                       /* number of elements in sections */   \
  type p, a, z;                 /* buffers for array elements */       \
                                                                       \
  do {                          /* sections sort loop */               \
    l = index; r = l +n -1;     /* start at left and right boundary */ \
    a = array[*l];              /* get the first and last elements */  \
    z = array[*r];              /* and bring them into right order */  \
    if (a > z) { t = *l; *l = *r; *r = t; }                            \
    x = index[n >> 1];          /* get the middle element as pivot */  \
    p = array[x];               /* and array element referred to */    \
    if      (p < a) { p = a; x = *l; }  /* compute median of three */  \
    else if (p > z) { p = z; x = *r; }  /* to find a better pivot */   \
    while (1) {                 /* split and exchange loop */          \
      while (array[*++l] < p);  /* skip smaller elems. on the left */  \
      while (array[*--r] > p);  /* skip greater elems. on the right */ \
      if (l >= r) {             /* if less than two elements left, */  \
        if (l <= r) { l++; r--; } break; }       /* abort the loop */  \
      t = *l; *l = *r; *r = t;  /* otherwise exchange elements */      \
    }                                                                  \
    m = index +n -l;            /* compute the number of elements */   \
    n = r -index +1;            /* right and left of the split */      \
    if (n > m) {                /* if right section is smaller, */     \
      if (m >= TH_INSERT)       /* but larger than the threshold, */   \
        _##name##_qrec(l,     m, array);} /* sort it recursively, */   \
    else {                      /* if the left section is smaller, */  \
      if (n >= TH_INSERT)       /* but larger than the threshold, */   \
        _##name##_qrec(index, n, array);  /* sort it recursively, */   \
      index = l; n = m;         /* then switch to the right section */ \
    }                           /* keeping its size m in variable n */ \
  } while (n >= TH_INSERT);     /* while greater than threshold */     \
}  /* _qrec() */                                                       \
                                                                       \
/*------------------------------------------------------------------*/ \
                                                                       \
void name##_qsort (int *index, int n, const type *array)               \
{                               /* --- sort a number array */          \
  int  k;                       /* size of first section */            \
  int  *l, *r;                  /* to traverse the array */            \
  int  x;                       /* exchange buffer */                  \
  type t;                       /* buffer for element referred to */   \
                                                                       \
  assert(index && (n >= 0) && array);  /* check function arguments */  \
  if (n <= 1) return;           /* do not sort less than two elems. */ \
  if (n < TH_INSERT)            /* if less elements than threshold */  \
    k = n;                      /* for insertion sort, note the */     \
  else {                        /* number of elements, otherwise */    \
    _##name##_qrec(index, n, array);  /* call recursive function */    \
    k = TH_INSERT -1;           /* and get the number of elements */   \
  }                             /* in the first array section */       \
  for (l = r = index; --k > 0;) /* find the position */                \
    if (array[*++r] < array[*l])/* of the smallest element */          \
      l = r;                    /* within the first k elements */      \
  r = index;                    /* swap the smallest element */        \
  x = *l; *l = *r; *r = x;      /* to front as a sentinel */           \
  while (--n > 0) {             /* standard insertion sort */          \
    t = array[x = *++r];        /* note the number to insert */        \
    for (l = r; array[*--l] > t; k--) /* shift right all that are */   \
      l[1] = *l;                /* greater than the one to insert */   \
    l[1] = x;                   /* and store the number to insert */   \
  }                             /* in the place thus found */          \
}  /* qsort() */

/*--------------------------------------------------------------------*/

IDX_QSORT(i2s, short)
IDX_QSORT(i2i, int)
IDX_QSORT(i2f, float)
IDX_QSORT(i2d, double)

/*--------------------------------------------------------------------*/

#define IDX_HEAPSORT(name,type) \
static void _##name##_sift (int *index, int l, int r,                  \
                            const type *array)                         \
{                               /* --- let element sift down in heap */\
  int  i;                       /* index of first successor in heap */ \
  int  x;                       /* buffer for an index element */      \
  type t;                       /* buffer for an array element */      \
                                                                       \
  t = array[x = index[l]];      /* note the sift element */            \
  i = l +l +1;                  /* compute index of first successor */ \
  do {                          /* sift loop */                        \
    if ((i < r) && (array[index[i]] < array[index[i+1]]))              \
      i++;                      /* if second successor is greater */   \
    if (t >= array[index[i]])   /* if the successor is greater */      \
      break;                    /* than the sift element, */           \
    index[l] = index[i];        /* let the successor ascend in heap */ \
    l = i; i += i +1;           /* compute index of first successor */ \
  } while (i <= r);             /* while still within heap */          \
  index[l] = x;                 /* store the sift element */           \
}  /* _sift() */                                                       \
                                                                       \
/*------------------------------------------------------------------*/ \
                                                                       \
void name##_heapsort (int *index, int n, const type *array)            \
{                               /* --- heap sort for number arrays */  \
  int l, r;                     /* boundaries of heap section */       \
  int t;                        /* exchange buffer */                  \
                                                                       \
  assert(index && (n >= 0) && array);   /* check function arguments */ \
  if (n <= 1) return;           /* do not sort less than 2 elements */ \
  l = n >> 1;                   /* at start, only the second half */   \
  r = n -1;                     /* of the array has heap structure */  \
  while (--l >= 0)              /* while the heap is not complete, */  \
    _##name##_sift(index, l, r, array);/* extend it by one element */  \
  while (1) {                   /* heap reduction loop */              \
    t        = index[0];        /* swap the greatest element */        \
    index[0] = index[r];        /* to the end of the array */          \
    index[r] = t;                                                      \
    if (--r <= 0) break;        /* if the heap is empty, abort */      \
    _##name##_sift(index, 0, r, array);                                \
  }                             /* let the swap element sift down */   \
}  /* heapsort() */

/*--------------------------------------------------------------------*/

IDX_HEAPSORT(i2s, short)
IDX_HEAPSORT(i2i, int)
IDX_HEAPSORT(i2f, float)
IDX_HEAPSORT(i2d, double)

/*--------------------------------------------------------------------*/

static void _i2p_qrec (int *index, int n, const void **array,
                       CMPFN *cmpfn, void *data)
{                               /* --- recursive part of quicksort */
  int *l, *r;                   /* pointers to exchange positions */
  int x,  t;                    /* pivot element and exchange buffer */
  int m;                        /* number of elements in 2nd section */
  const void *p, *a, *z;        /* buffers for array elements */

  do {                          /* sections sort loop */
    l = index; r = l +n -1;     /* start at left and right boundary */
    a = array[*l];              /* get the first and last elements */
    z = array[*r];              /* and bring them into right order */
    if (cmpfn(a, z, data) > 0) { t = *l; *l = *r; *r = t; }
    x = index[n >> 1];          /* get the middle element as pivot */
    p = array[x];               /* compute median of three to improve */
    if      (cmpfn(p, a, data) < 0) { p = a; x = *l; }
    else if (cmpfn(p, z, data) > 0) { p = z; x = *r; }
    while (1) {                 /* split and exchange loop */
      while (cmpfn(array[*++l], p, data) < 0)
        ;                       /* skip elements smaller than pivot */
      while (cmpfn(array[*--r], p, data) > 0)
        ;                       /* skip elements greater than pivot */
      if (l >= r) {             /* if less than two elements left, */
        if (l <= r) { l++; r--; } break; }       /* abort the loop */
      t = *l; *l = *r; *r = t;  /* otherwise exchange elements */
    }
    m = index +n -l;            /* compute the number of elements */
    n = r -index +1;            /* right and left of the split */
    if (n > m) {                /* if right section is smaller, */
      if (m >= TH_INSERT)       /* but larger than the threshold, */
        _i2p_qrec(l,     m, array, cmpfn, data); }    /* sort it, */
    else {                      /* if the left section is smaller, */
      if (n >= TH_INSERT)       /* but larger than the threshold, */
        _i2p_qrec(index, n, array, cmpfn, data);      /* sort it, */
      index = l; n = m;         /* then switch to the right section */
    }                           /* keeping its size m in variable n */
  } while (n >= TH_INSERT);     /* while greater than threshold */
}  /* _i2p_qrec() */

/*--------------------------------------------------------------------*/

void i2p_qsort (int *index, int n, const void **array,
                CMPFN *cmpfn, void *data)
{                               /* --- quick sort for index arrays */
  int k;                        /* size of first section */
  int *l, *r;                   /* to traverse the array */
  int x;                        /* exchange buffer */
  const void *t;                /* buffer for pointer referred to */

  assert(index                  /* check the function arguments */
  &&    (n >= 0) && array && cmpfn);
  if (n <= 1) return;           /* do not sort less than two elements */
  if (n < TH_INSERT)            /* if fewer elements than threshold */
    k = n;                      /* for insertion sort, note the */
  else {                        /* number of elements, otherwise */
    _i2p_qrec(index, n, array, cmpfn, data); /* sort recursively */
    k = TH_INSERT -1;           /* and get the number of elements */
  }                             /* in the first array section */
  for (l = r = index; --k > 0;) /* find the smallest element */
    if (cmpfn(array[*++r], array[*l], data) < 0)
      l = r;                    /* among the first k elements */
  r = index;                    /* swap the smallest element */
  x = *l; *l = *r; *r = x;      /* to the front as a sentinel */
  while (--n > 0) {             /* insertion sort loop */
    t = array[x = *++r];        /* note the element to insert */
    for (l = r; cmpfn(array[*--l], t, data) > 0; )
      l[1] = *l;                /* shift right index elements */
    l[1] = x;                   /* that are greater than the one to */
  }                             /* insert and store the element to */
}  /* i2p_qsort() */            /* insert in the place thus found */

/*--------------------------------------------------------------------*/

static void _i2p_sift (int *index, int l, int r, const void **array,
                       CMPFN *cmpfn, void *data)
{                               /* --- let element sift down in heap */
  int i;                        /* index of first successor in heap */
  int x;                        /* buffer for an index element */
  const void *t;                /* buffer for an array element */

  t = array[x = index[l]];      /* note the sift element */
  i = l +l +1;                  /* compute index of first successor */
  do {                          /* sift loop */
    if ((i < r)                 /* if second successor is greater */
    &&  (cmpfn(array[index[i]], array[index[i+1]], data) < 0))
      i++;                      /* go to the second successor */
    if (cmpfn(t, array[i], data) >= 0) /* if the successor is greater */
      break;                           /* than the sift element, */
    index[l] = index[i];        /* let the successor ascend in heap */
    l = i; i += i +1;           /* compute index of first successor */
  } while (i <= r);             /* while still within heap */
  index[l] = x;                 /* store the sift element */
}  /* _i2p_sift() */

/*--------------------------------------------------------------------*/

void i2p_heapsort (int *index, int n, const void **array,
                   CMPFN *cmpfn, void *data)
{                               /* --- heap sort for index arrays */
  int l, r;                     /* boundaries of heap section */
  int t;                        /* exchange buffer */

  assert(index                  /* check the function arguments */
  &&    (n >= 0) && array && cmpfn);
  if (n <= 1) return;           /* do not sort less than two elements */
  l = n >> 1;                   /* at start, only the second half */
  r = n -1;                     /* of the array has heap structure */
  while (--l >= 0)              /* while the heap is not complete, */
    _i2p_sift(index, l, r, array, cmpfn, data);       /* extend it */
  while (1) {                   /* heap reduction loop */
    t        = index[0];        /* swap the greatest element */
    index[0] = index[r];        /* to the end of the array */
    index[r] = t;
    if (--r <= 0) break;        /* if the heap is empty, abort */
    _i2p_sift(index, 0, r, array, cmpfn, data);
  }                             /* let the element that has been */
}  /* i2p_heapsort() */         /* swapped to front sift down */

/*--------------------------------------------------------------------*/

static void _i2x_qrec (int *index, int n, ICMPFN *cmpfn, void *data)
{                               /* --- recursive part of quicksort */
  int *l, *r;                   /* pointers to exchange positions */
  int x,  t;                    /* pivot element and exchange buffer */
  int m;                        /* number of elements in 2nd section */

  do {                          /* sections sort loop */
    l = index; r = l +n -1;     /* start at left and right boundary */
    if (cmpfn(*l, *r, data) > 0) {  /* bring the first and last */
      t = *l; *l = *r; *r = t; }    /* element into proper order */
    x = index[n >> 1];          /* get the middle element as pivot */
    if      (cmpfn(x, *l, data) < 0) x = *l;  /* try to find a */
    else if (cmpfn(x, *r, data) > 0) x = *r;  /* better pivot */
    while (1) {                 /* split and exchange loop */
      while (cmpfn(*++l, x, data) < 0)    /* skip left  elements that */
        ;                       /* are smaller than the pivot element */
      while (cmpfn(*--r, x, data) > 0)    /* skip right elements that */
        ;                       /* are greater than the pivot element */
      if (l >= r) {             /* if less than two elements left, */
        if (l <= r) { l++; r--; } break; }       /* abort the loop */
      t = *l; *l = *r; *r = t;  /* otherwise exchange elements */
    }
    m = index +n -l;            /* compute the number of elements */
    n = r -index +1;            /* right and left of the split */
    if (n > m) {                /* if right section is smaller, */
      if (m >= TH_INSERT)       /* but larger than the threshold, */
        _i2x_qrec(l,     m, cmpfn, data);} /* sort it recursively, */
    else {                      /* if the left section is smaller, */
      if (n >= TH_INSERT)       /* but larger than the threshold, */
        _i2x_qrec(index, n, cmpfn, data);  /* sort it recursively, */
      index = l; n = m;         /* then switch to the right section */
    }                           /* keeping its size m in variable n */
  } while (n >= TH_INSERT);     /* while greater than threshold */
}  /* _i2x_qrec() */

/*--------------------------------------------------------------------*/

void i2x_qsort (int *index, int n, ICMPFN *cmpfn, void *data)
{                               /* --- quicksort for index arrays */
  int k;                        /* size of first section */
  int *l, *r;                   /* to traverse the array */
  int t;                        /* exchange buffer */

  assert(index && (n >= 0) && cmpfn); /* check the function arguments */
  if (n <= 1) return;           /* do not sort less than two elements */
  if (n < TH_INSERT)            /* if fewer elements than threshold */
    k = n;                      /* for insertion sort, note the */
  else {                        /* number of elements, otherwise */
    _i2x_qrec(index, n, cmpfn, data); /* call recursice function */
    k = TH_INSERT -1;           /* and get the number of elements */
  }                             /* in the first array section */
  for (l = r = index; --k > 0;) /* find the smallest element within */
    if (cmpfn(*++r, *l, data) < 0) l = r;   /* the first k elements */
  r = index;                    /* swap the smallest element */
  t = *l; *l = *r; *r = t;      /* to the front as a sentinel */
  while (--n > 0) {             /* insertion sort loop */
    t = *++r;                   /* note the element to insert */
    for (l = r; cmpfn(*--l, t, data) > 0; ) /* shift right elements */
      l[1] = *l;                /* that are greater than the one to */
    l[1] = t;                   /* insert and store the element to */
  }                             /* insert in the place thus found */
}  /* i2x_qsort() */

/*--------------------------------------------------------------------*/

static void _i2x_sift (int *index, int l, int r,
                       ICMPFN *cmpfn, void *data)
{                               /* --- let element sift down in heap */
  int i;                        /* index of first successor in heap */
  int t;                        /* buffer for an array element */

  t = index[l];                 /* note the sift element */
  i = l +l +1;                  /* compute index of first successor */
  do {                          /* sift loop */
    if ((i < r)                 /* if second successor is greater */
    &&  (cmpfn(index[i], index[i+1], data) < 0))
      i++;                      /* go to the second successor */
    if (cmpfn(t, index[i], data) >= 0) /* if the successor is greater */
      break;                           /* than the sift element, */
    index[l] = index[i];        /* let the successor ascend in heap */
    l = i; i += i +1;           /* compute index of first successor */
  } while (i <= r);             /* while still within heap */
  index[l] = t;                 /* store the sift element */
}  /* _sift() */

/*--------------------------------------------------------------------*/

void i2x_heapsort (int *index, int n, ICMPFN *cmpfn, void *data)
{                               /* --- heap sort for index arrays */
  int l, r;                     /* boundaries of heap section */
  int t;                        /* exchange buffer */

  assert(index && (n >= 0) && cmpfn); /* check the function arguments */
  if (n <= 1) return;           /* do not sort less than two elements */
  l = n >> 1;                   /* at start, only the second half */
  r = n -1;                     /* of the index has heap structure */
  while (--l >= 0)              /* while the heap is not complete, */
    _i2x_sift(index, l, r, cmpfn, data); /* extend it by one element */
  while (1) {                   /* heap reduction loop */
    t        = index[0];        /* swap the greatest element */
    index[0] = index[r];        /* to the end of the index */
    index[r] = t;
    if (--r <= 0) break;        /* if the heap is empty, abort */
    _i2x_sift(index, 0, r, cmpfn, data);
  }                             /* let the element that has been */
}  /* i2x_heapsort() */         /* swapped to front sift down */

/*----------------------------------------------------------------------
  Main Function for Testing
----------------------------------------------------------------------*/
#ifdef ARRAYS_MAIN

static int lexcmp (const void *p1, const void *p2, void *data)
{                               /* --- compare lexicographically */
  const char *s1 = (const char*)p1;  /* type the two pointers */
  const char *s2 = (const char*)p2;  /* to strings, */
  while (1) {                   /* then traverse the strings */
    if (*s1 <  *s2)  return -1; /* if one string is smaller than */
    if (*s1 >  *s2)  return +1; /* the other, return the result */
    if (*s1 == '\0') return  0; /* if at string end, return 'equal' */
    s1++; s2++;                 /* go to the next character */
  }
}  /* lexcmp() */

/*--------------------------------------------------------------------*/

static int numcmp (const void *p1, const void *p2, void *data)
{                               /* --- compare strings numerically */
  double d1 = strtod((const char*)p1, NULL);
  double d2 = strtod((const char*)p2, NULL);
  if (d1 < d2) return -1;       /* convert to numbers and */
  if (d1 > d2) return +1;       /* compare numerically */
  return lexcmp(p1, p2, NULL);  /* if the numbers are equal, */
}  /* numcmp() */               /* compare strings lexicographically */

/*--------------------------------------------------------------------*/

int main (int argc, char *argv[])
{                               /* --- sort program arguments */
  int  i, n;                    /* loop variables */
  int  numeric = 0;             /* flag for numeric comparison */
  char *s;                      /* to traverse the arguments */

  if (argc < 2) {               /* if no arguments are given */
    printf("usage: %s [arg [arg ...]]\n", argv[0]);
    printf("sort the list of program arguments\n");
    return 0;                   /* print a usage message */
  }                             /* and abort the program */
  for (i = n = 0; ++i < argc; ) {
    s = argv[i];                /* traverse the arguments */
    if (*s != '-') { argv[n++] = s; continue; }
    s++;                        /* store the arguments to sort */
    while (*s) {                /* traverse the options */
      switch (*s++) {           /* evaluate the options */
        case 'n': numeric = -1; break;
        default : printf("unknown option -%c\n", *--s); return -1;
      }                         /* set the option variables */
    }                           /* and check for known options */
  }
  ptr_qsort(argv, n, (numeric) ? numcmp : lexcmp, NULL);
                                /* sort the program arguments */
  for (i = 0; i < n; i++) {     /* print the sorted arguments */
    fputs(argv[i], stdout); fputc('\n', stdout); }
  return 0;                     /* return 'ok' */
}  /* main() */

#endif
