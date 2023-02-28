#include <math.h>

float scalanative_huge_valf() { return HUGE_VALF; }

double scalanative_huge_val() { return HUGE_VAL; }

float scalanative_infinity() { return INFINITY; }

float scalanative_nan() { return NAN; }

#if defined(math_errhandling)
int scalanative_math_errhandling() { return math_errhandling; }
#endif

#if defined(MATH_ERRNO)
int scalanative_math_errno() { return MATH_ERRNO; }
#endif

#if defined(MATH_ERREXCEPT)
int scalanative_math_errexcept() { return MATH_ERREXCEPT; }
#endif
