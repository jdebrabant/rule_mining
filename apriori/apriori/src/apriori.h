/*----------------------------------------------------------------------
  File    : apriori.h
  Contents: apriori algorithm for finding frequent item sets
  Author  : Christian Borgelt
  History : 2011.07.18 file created
----------------------------------------------------------------------*/
#ifndef __APRIORI__
#define __APRIORI__
#include "istree.h"

/*----------------------------------------------------------------------
  Preprocessor Definitions
----------------------------------------------------------------------*/
#define AV_PLAIN      0
#define AV_TATREE     ((IST_PERFECT >> 1) & ~IST_PERFECT)

/*----------------------------------------------------------------------
  Functions
----------------------------------------------------------------------*/
#ifdef NOMAIN
extern int apriori (TABAG *tabag, int mode, int supp,
                    int eval, int aggm, double minval, double mininc,
                    int prune, double filter, ISREPORT *rep);
#endif
#endif
