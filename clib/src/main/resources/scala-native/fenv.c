#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_C_FENV)
#include <fenv.h>

int scalanative_fe_divbyzero() { return FE_DIVBYZERO; }
int scalanative_fe_inexact() { return FE_INEXACT; }
int scalanative_fe_invalid() { return FE_INVALID; }
int scalanative_fe_overflow() { return FE_OVERFLOW; }
int scalanative_fe_underflow() { return FE_UNDERFLOW; }
int scalanative_fe_all_except() { return FE_ALL_EXCEPT; }
int scalanative_fe_downward() { return FE_DOWNWARD; }
int scalanative_fe_tonearest() { return FE_TONEAREST; }
int scalanative_fe_towardzero() { return FE_TOWARDZERO; }
int scalanative_fe_upward() { return FE_UPWARD; }
#endif