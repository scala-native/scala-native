#include <fenv.h>

int scalanative_fenv_fe_divbyzero() { return FE_DIVBYZERO; }
int scalanative_fenv_fe_inexact() { return FE_INEXACT; }
int scalanative_fenv_fe_invalid() { return FE_INVALID; }
int scalanative_fenv_fe_overflow() { return FE_OVERFLOW; }
int scalanative_fenv_fe_underflow() { return FE_UNDERFLOW; }
int scalanative_fenv_fe_all_except() { return FE_ALL_EXCEPT; }
int scalanative_fenv_fe_downward() { return FE_DOWNWARD; }
int scalanative_fenv_fe_tonearest() { return FE_TONEAREST; }
int scalanative_fenv_fe_towardzero() { return FE_TOWARDZERO; }
int scalanative_fenv_fe_upward() { return FE_UPWARD; }
