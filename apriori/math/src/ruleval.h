/*----------------------------------------------------------------------
  File    : ruleval.c
  Contents: rule evaluation measures
  Author  : Christian Borgelt
  History : 2011.07.22 file created
----------------------------------------------------------------------*/
#ifndef __RULEVAL__
#define __RULEVAL__

/*----------------------------------------------------------------------
  Preprocessor Definitions
----------------------------------------------------------------------*/
/* --- rule evaluation function identifiers --- */
#define RE_NONE        0        /* no measure / constant zero */
#define RE_CONF        1        /* rule confidence */
#define RE_CONFDIFF    2        /* confidence diff. to prior */
#define RE_LIFT        3        /* lift value (conf./prior) */
#define RE_LIFTDIFF    4        /* difference of lift value    to 1 */
#define RE_LIFTQUOT    5        /* difference of lift quotient to 1 */
#define RE_CVCT        6        /* conviction */
#define RE_CVCTDIFF    7        /* difference of conviction  to 1 */
#define RE_CVCTQUOT    8        /* difference of conv. quot. to 1 */
#define RE_CERT        9        /* certainty factor */
#define RE_CHI2       10        /* normalized chi^2 measure */
#define RE_CHI2PVAL   11        /* p-value from chi^2 measure */
#define RE_YATES      12        /* normalized chi^2 measure */
#define RE_YATESPVAL  13        /* p-value from chi^2 measure */
#define RE_INFO       14        /* information diff. to prior */
#define RE_INFOPVAL   15        /* p-value from info diff. */
#define RE_FETPROB    16        /* Fisher's exact test (prob.) */
#define RE_FETCHI2    17        /* Fisher's exact test (chi^2) */
#define RE_FETINFO    18        /* Fisher's exact test (info.) */
#define RE_FETSUPP    19        /* Fisher's exact test (supp.) */
#define RE_FNCNT      20        /* number of evaluation functions */

/*----------------------------------------------------------------------
  Type Definitions
----------------------------------------------------------------------*/
typedef double RULEVALFN (int supp, int body, int head, int base);

/*----------------------------------------------------------------------
  Rule Evaluation Functions
----------------------------------------------------------------------*/
extern double     re_none      (int supp, int body, int head, int base);
extern double     re_conf      (int supp, int body, int head, int base);
extern double     re_confdiff  (int supp, int body, int head, int base);
extern double     re_lift      (int supp, int body, int head, int base);
extern double     re_liftdiff  (int supp, int body, int head, int base);
extern double     re_liftquot  (int supp, int body, int head, int base);
extern double     re_cvct      (int supp, int body, int head, int base);
extern double     re_cvctdiff  (int supp, int body, int head, int base);
extern double     re_cvctquot  (int supp, int body, int head, int base);
extern double     re_cert      (int supp, int body, int head, int base);
extern double     re_chi2      (int supp, int body, int head, int base);
extern double     re_chi2pval  (int supp, int body, int head, int base);
extern double     re_yates     (int supp, int body, int head, int base);
extern double     re_yatespval (int supp, int body, int head, int base);
extern double     re_info      (int supp, int body, int head, int base);
extern double     re_infopval  (int supp, int body, int head, int base);
extern double     re_fetprob   (int supp, int body, int head, int base);
extern double     re_fetchi2   (int supp, int body, int head, int base);
extern double     re_fetinfo   (int supp, int body, int head, int base);
extern double     re_fetsupp   (int supp, int body, int head, int base);

extern RULEVALFN* re_function  (int id);
extern int        re_dir       (int id);

#endif
