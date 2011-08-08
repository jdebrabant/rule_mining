/*----------------------------------------------------------------------
  File    : arrays.h
  Contents: some basic array operations, especially for pointer arrays
  Author  : Christian Borgelt
  History : 1996.09.16 file created as arrays.h
            1999.02.04 long int changed to int
            2001.06.03 functions ptr_shuffle() added
            2002.01.02 functions for basic data types added
            2002.03.03 functions #_reverse() added
            2003.08.21 functions #_heapsort() added
            2007.01.16 shuffle functions for basic data types added
            2008.08.01 renamed to arrays.h, some functions added
            2008.10.05 functions #_select() added
            2010.07.31 index array sorting functions added
----------------------------------------------------------------------*/
#ifndef __ARRAYS__
#define __ARRAYS__
#include <string.h>
#include "fntypes.h"

/*----------------------------------------------------------------------
  Functions for Arrays of Basic Data Types
----------------------------------------------------------------------*/
extern void sht_clear    (short  *array, int n);
extern void sht_copy     (short  *dst,   short *src, int n);
extern void sht_move     (short  *array, int off, int n, int pos);
extern void sht_select   (short  *array, int n, int k, RANDFN *randfn);
extern void sht_shuffle  (short  *array, int n,        RANDFN *randfn);
extern void sht_reverse  (short  *array, int n);
extern void sht_qsort    (short  *array, int n);
extern void sht_heapsort (short  *array, int n);
extern int  sht_unique   (short  *array, int n);
extern int  sht_bsearch  (short  key, short *array, int n);

/*--------------------------------------------------------------------*/

extern void int_clear    (int    *array, int n);
extern void int_copy     (int    *dst,   int *src, int n);
extern void int_move     (int    *array, int off, int n, int pos);
extern void int_select   (int    *array, int n, int k, RANDFN *randfn);
extern void int_shuffle  (int    *array, int n,        RANDFN *randfn);
extern void int_reverse  (int    *array, int n);
extern void int_qsort    (int    *array, int n);
extern void int_heapsort (int    *array, int n);
extern int  int_unique   (int    *array, int n);
extern int  int_bsearch  (int    key, int *array, int n);

/*--------------------------------------------------------------------*/

extern void flt_clear    (float  *array, int n);
extern void flt_copy     (float  *dst,   float *src, int n);
extern void flt_move     (float  *array, int off, int n, int pos);
extern void flt_select   (float  *array, int n, int k, RANDFN *randfn);
extern void flt_shuffle  (float  *array, int n,        RANDFN *randfn);
extern void flt_reverse  (float  *array, int n);
extern void flt_qsort    (float  *array, int n);
extern void flt_heapsort (float  *array, int n);
extern int  flt_unique   (float  *array, int n);
extern int  flt_bsearch  (float  key, float *array, int n);

/*--------------------------------------------------------------------*/

extern void dbl_clear    (double *array, int n);
extern void dbl_copy     (double *dst,   double *src, int n);
extern void dbl_move     (double *array, int off, int n, int pos);
extern void dbl_select   (double *array, int n, int k, RANDFN *randfn);
extern void dbl_shuffle  (double *array, int n,        RANDFN *randfn);
extern void dbl_reverse  (double *array, int n);
extern void dbl_qsort    (double *array, int n);
extern void dbl_heapsort (double *array, int n);
extern int  dbl_unique   (double *array, int n);
extern int  dbl_bsearch  (double key, double *array, int n);

/*----------------------------------------------------------------------
  Functions for Pointer Arrays
----------------------------------------------------------------------*/
extern void ptr_clear    (void *array, int n);
extern void ptr_copy     (void *dst, void *src, int n);
extern void ptr_move     (void *array, int off, int n, int pos);
extern void ptr_select   (void *array, int n, int k, RANDFN *randfn);
extern void ptr_shuffle  (void *array, int n,        RANDFN *randfn);
extern void ptr_reverse  (void *array, int n);
extern void ptr_qsort    (void *array, int n, CMPFN *cmpfn, void *data);
extern void ptr_heapsort (void *array, int n, CMPFN *cmpfn, void *data);
extern int  ptr_bsearch  (const void *key,
                          void *array, int n, CMPFN *cmpfn, void *data);
extern int  ptr_unique   (void *array, int n, CMPFN *cmpfn, void *data,
                          OBJFN *delfn);

/*----------------------------------------------------------------------
  Functions for Index Arrays
----------------------------------------------------------------------*/
extern void i2s_qsort    (int *index, int n, const short  *array);
extern void i2s_heapsort (int *index, int n, const short  *array);
extern void i2i_qsort    (int *index, int n, const int    *array);
extern void i2i_heapsort (int *index, int n, const int    *array);
extern void i2f_qsort    (int *index, int n, const float  *array);
extern void i2f_heapsort (int *index, int n, const float  *array);
extern void i2d_qsort    (int *index, int n, const double *array);
extern void i2d_heapsort (int *index, int n, const double *array);
extern void i2p_qsort    (int *index, int n, const void  **array,
                          CMPFN *cmpfn, void *data);
extern void i2p_heapsort (int *index, int n, const void  **array,
                          CMPFN *cmpfn, void *data);
extern void i2x_qsort    (int *index, int n, ICMPFN *cmpfn, void *data);
extern void i2x_heapsort (int *index, int n, ICMPFN *cmpfn, void *data);

/*----------------------------------------------------------------------
  Preprocessor Definitions
----------------------------------------------------------------------*/
#define sht_clear(a,n)   memset(a, 0, (n)*sizeof(short))
#define int_clear(a,n)   memset(a, 0, (n)*sizeof(int))
#define flt_clear(a,n)   memset(a, 0, (n)*sizeof(float))
#define dbl_clear(a,n)   memset(a, 0, (n)*sizeof(double))
#define ptr_clear(a,n)   memset(a, 0, (n)*sizeof(void*))

#define sht_copy(d,s,n)  memmove(d, s, (n)*sizeof(short))
#define int_copy(d,s,n)  memmove(d, s, (n)*sizeof(int))
#define flt_copy(d,s,n)  memmove(d, s, (n)*sizeof(float))
#define dbl_copy(d,s,n)  memmove(d, s, (n)*sizeof(double))
#define ptr_copy(d,s,n)  memmove(d, s, (n)*sizeof(void*))

#endif
