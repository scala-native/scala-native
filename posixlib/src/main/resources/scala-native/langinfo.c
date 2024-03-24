#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_POSIX_LANGINFO)
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))

#include <langinfo.h>

#if defined(__OpenBSD__)
#define ERA -1
#define ERA_D_FMT -1
#define ERA_D_T_FMT -1
#define ERA_T_FMT -1
#define ALT_DIGITS -1
#endif // OpenBSD

int scalanative_codeset() { return CODESET; };

int scalanative_d_t_fmt() { return D_T_FMT; };

int scalanative_d_fmt() { return D_FMT; };

int scalanative_t_fmt() { return T_FMT; };

int scalanative_t_fmt_ampm() { return T_FMT_AMPM; };

int scalanative_am_str() { return AM_STR; };

int scalanative_pm_str() { return PM_STR; };

int scalanative_day_1() { return DAY_1; };

int scalanative_day_2() { return DAY_2; };

int scalanative_day_3() { return DAY_3; };

int scalanative_day_4() { return DAY_4; };

int scalanative_day_5() { return DAY_5; };

int scalanative_day_6() { return DAY_6; };

int scalanative_day_7() { return DAY_7; };

int scalanative_abday_1() { return ABDAY_1; };

int scalanative_abday_2() { return ABDAY_2; };

int scalanative_abday_3() { return ABDAY_3; };

int scalanative_abday_4() { return ABDAY_4; };

int scalanative_abday_5() { return ABDAY_5; };

int scalanative_abday_6() { return ABDAY_6; };

int scalanative_abday_7() { return ABDAY_7; };

int scalanative_mon_1() { return MON_1; };

int scalanative_mon_2() { return MON_2; };

int scalanative_mon_3() { return MON_3; };

int scalanative_mon_4() { return MON_4; };

int scalanative_mon_5() { return MON_5; };

int scalanative_mon_6() { return MON_6; };

int scalanative_mon_7() { return MON_7; };

int scalanative_mon_8() { return MON_8; };

int scalanative_mon_9() { return MON_9; };

int scalanative_mon_10() { return MON_10; };

int scalanative_mon_11() { return MON_11; };

int scalanative_mon_12() { return MON_12; };

int scalanative_abmon_1() { return ABMON_1; };

int scalanative_abmon_2() { return ABMON_2; };

int scalanative_abmon_3() { return ABMON_3; };

int scalanative_abmon_4() { return ABMON_4; };

int scalanative_abmon_5() { return ABMON_5; };

int scalanative_abmon_6() { return ABMON_6; };

int scalanative_abmon_7() { return ABMON_7; };

int scalanative_abmon_8() { return ABMON_8; };

int scalanative_abmon_9() { return ABMON_9; };

int scalanative_abmon_10() { return ABMON_10; };

int scalanative_abmon_11() { return ABMON_11; };

int scalanative_abmon_12() { return ABMON_12; };

int scalanative_era() { return ERA; };

int scalanative_era_d_fmt() { return ERA_D_FMT; };

int scalanative_era_d_t_fmt() { return ERA_D_T_FMT; };

int scalanative_era_t_fmt() { return ERA_T_FMT; };

int scalanative_alt_digits() { return ALT_DIGITS; };

int scalanative_radixchar() { return RADIXCHAR; };

int scalanative_thousep() { return THOUSEP; };

int scalanative_yesexpr() { return YESEXPR; };

int scalanative_noexpr() { return NOEXPR; };

int scalanative_crncystr() { return CRNCYSTR; };

#endif // Unix or Mac OS
#endif