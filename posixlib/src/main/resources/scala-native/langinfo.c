#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))

#include <langinfo.h>

#endif // Unix or Mac OS

#if defined(_WIN32) // bogus values to keep compiler happy
#define CODESET -1
#define D_T_FMT -1
#define D_FMT -1
#define T_FMT -1
#define T_FMT_AMPM -1
#define AM_STR -1
#define PM_STR -1
#define DAY_1 -1
#define DAY_2 -1
#define DAY_3 -1
#define DAY_4 -1
#define DAY_5 -1
#define DAY_6 -1
#define DAY_7 -1
#define ABDAY_1 -1
#define ABDAY_2 -1
#define ABDAY_3 -1
#define ABDAY_4 -1
#define ABDAY_5 -1
#define ABDAY_6 -1
#define ABDAY_7 -1
#define MON_1 -1
#define MON_2 -1
#define MON_3 -1
#define MON_4 -1
#define MON_5 -1
#define MON_6 -1
#define MON_7 -1
#define MON_8 -1
#define MON_9 -1
#define MON_10 -1
#define MON_11 -1
#define MON_12 -1
#define ABMON_1 -1
#define ABMON_2 -1
#define ABMON_3 -1
#define ABMON_4 -1
#define ABMON_5 -1
#define ABMON_6 -1
#define ABMON_7 -1
#define ABMON_8 -1
#define ABMON_9 -1
#define ABMON_10 -1
#define ABMON_11 -1
#define ABMON_12 -1
#define ERA -1
#define ERA_D_FMT -1
#define ERA_D_T_FMT -1
#define ERA_T_FMT -1
#define ALT_DIGITS -1
#define RADIXCHAR -1
#define THOUSEP -1
#define YESEXPR -1
#define NOEXPR -1
#define CRNCYSTR -1
#endif // _WIN32

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
