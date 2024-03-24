#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_C_LOCALE)
#ifdef _WIN32
// No Windows support
#else
#if !(defined __STDC_VERSION__) || (__STDC_VERSION__ < 201112L)
#ifndef SCALANATIVE_SUPPRESS_STRUCT_CHECK_WARNING
#warning "Size and order of C structures are not checked when -std < c11."
#endif
#else // POSIX
#include <stddef.h>
#include <locale.h>

/* _Static_assert statements below verify that this layout is valid
 * on Linux & mac_OS. Read the documentation but do not believe it too much.
 * Trust the _Static_assert statements.
 *
 * This is the Linux physical layout. macOS swaps/exchanges the
 * int_p_sep_by_space & int_n_cs_precedes fields.
 *
 * macOS "man localeconv" describes this layout.
 *
 * Linux "man lconv" describes a layout with int_curr_symbol after
 * n_sign_position.  _Static_assert below refutes that.
 *
 * POSIX 2018 describes its usual "which shall include at least the
 * following members". As is its right, it then gives a list which
 * has no correspondence this layout.
 */

struct scalanative_lconv {
    char *decimal_point;
    char *thousands_sep;
    char *grouping;
    char *int_curr_symbol;
    char *currency_symbol;

    char *mon_decimal_point;
    char *mon_thousands_sep;
    char *mon_grouping;
    char *positive_sign;
    char *negative_sign;

    char int_frac_digits;
    char frac_digits;
    char p_cs_precedes;
    char p_sep_by_space;
    char n_cs_precedes;

    char n_sep_by_space;
    char p_sign_posn;
    char n_sign_posn;
    char int_p_cs_precedes;
    char int_p_sep_by_space; // Linux, overlays macOS int_n_cs_precedes

    char int_n_cs_precedes; // Linux, overlays macOS int_p_sep_by_space
    char int_n_sep_by_space;
    char int_p_sign_posn;
    char int_n_sign_posn;
};

_Static_assert(sizeof(struct scalanative_lconv) <= sizeof(struct lconv),
               "Unexpected size: os lconv");

_Static_assert(offsetof(struct scalanative_lconv, decimal_point) ==
                   offsetof(struct lconv, decimal_point),
               "Unexpected offset: scalanative_lconv.decimal_point");

_Static_assert(offsetof(struct scalanative_lconv, thousands_sep) ==
                   offsetof(struct lconv, thousands_sep),
               "Unexpected offset: scalanative_lconv.thousands_sep");

_Static_assert(offsetof(struct scalanative_lconv, grouping) ==
                   offsetof(struct lconv, grouping),
               "Unexpected offset: scalanative_lconv.grouping");

_Static_assert(offsetof(struct scalanative_lconv, int_curr_symbol) ==
                   offsetof(struct lconv, int_curr_symbol),
               "Unexpected offset: scalanative_lconv.int_curr_symbol");

_Static_assert(offsetof(struct scalanative_lconv, currency_symbol) ==
                   offsetof(struct lconv, currency_symbol),
               "Unexpected offset: scalanative_lconv.currency_symbol");

_Static_assert(offsetof(struct scalanative_lconv, mon_decimal_point) ==
                   offsetof(struct lconv, mon_decimal_point),
               "Unexpected offset: scalanative_lconv.mon_decimal_point");

_Static_assert(offsetof(struct scalanative_lconv, mon_grouping) ==
                   offsetof(struct lconv, mon_grouping),
               "Unexpected offset: scalanative_lconv.mon_grouping");

_Static_assert(offsetof(struct scalanative_lconv, mon_thousands_sep) ==
                   offsetof(struct lconv, mon_thousands_sep),
               "Unexpected offset: scalanative_lconv.mon_thousands_sep");

_Static_assert(offsetof(struct scalanative_lconv, positive_sign) ==
                   offsetof(struct lconv, positive_sign),
               "Unexpected offset: scalanative_lconv.positive_sign");

_Static_assert(offsetof(struct scalanative_lconv, negative_sign) ==
                   offsetof(struct lconv, negative_sign),
               "Unexpected offset: scalanative_lconv.negative_sign");

_Static_assert(offsetof(struct scalanative_lconv, int_frac_digits) ==
                   offsetof(struct lconv, int_frac_digits),
               "Unexpected offset: scalanative_lconv.int_frac_digits");

_Static_assert(offsetof(struct scalanative_lconv, frac_digits) ==
                   offsetof(struct lconv, frac_digits),
               "Unexpected offset: scalanative_lconv,frac_digits");

_Static_assert(offsetof(struct scalanative_lconv, p_cs_precedes) ==
                   offsetof(struct lconv, p_cs_precedes),
               "Unexpected offset: scalanative_lconv.p_cs_precedes.");

_Static_assert(offsetof(struct scalanative_lconv, p_sep_by_space) ==
                   offsetof(struct lconv, p_sep_by_space),
               "Unexpected offset: scalanative_lconv.p_sep_by_space");

_Static_assert(offsetof(struct scalanative_lconv, n_cs_precedes) ==
                   offsetof(struct lconv, n_cs_precedes),
               "Unexpected offset: scalanative_lconv.n_cs_precedes");

_Static_assert(offsetof(struct scalanative_lconv, n_sep_by_space) ==
                   offsetof(struct lconv, n_sep_by_space),
               "Unexpected offset: scalanative_lconv.n_sep_by_space");

_Static_assert(offsetof(struct scalanative_lconv, p_sign_posn) ==
                   offsetof(struct lconv, p_sign_posn),
               "Unexpected offset: scalanative_lconv.p_sign_posn");

_Static_assert(offsetof(struct scalanative_lconv, n_sign_posn) ==
                   offsetof(struct lconv, n_sign_posn),
               "Unexpected offset: scalanative_lconv.n_sign_posn");

_Static_assert(offsetof(struct scalanative_lconv, int_p_cs_precedes) ==
                   offsetof(struct lconv, int_p_cs_precedes),
               "Unexpected offset: scalanative_lconv.int_p_cs_precedes");

#if defined(__linux__) || defined(__OpenBSD__)
_Static_assert(offsetof(struct scalanative_lconv, int_n_cs_precedes) ==
                   offsetof(struct lconv, int_n_cs_precedes),
               "Unexpected offset: scalanative_lconv.int_n_cs_precedes");
_Static_assert(offsetof(struct scalanative_lconv, int_p_sep_by_space) ==
                   offsetof(struct lconv, int_p_sep_by_space),
               "Unexpected offset: scalanative_lconv.int_p_sep_by_space");
#else  // __APPLE__, etc.
// Be aware of the trickery with field names being swapped/exchanged.
_Static_assert(offsetof(struct scalanative_lconv, int_n_cs_precedes) ==
                   offsetof(struct lconv, int_p_sep_by_space),
               "Unexpected offset: scalanative_lconv.int_p_sep_by_space");

_Static_assert(offsetof(struct scalanative_lconv, int_p_sep_by_space) ==
                   offsetof(struct lconv, int_n_cs_precedes),
               "Unexpected offset: scalanative_lconv.int_n_cs_precedes");
#endif // __APPLE__

_Static_assert(offsetof(struct scalanative_lconv, int_n_sep_by_space) ==
                   offsetof(struct lconv, int_n_sep_by_space),
               "Unexpected offset: scalanative_lconv.int_n_sep_by_space");

_Static_assert(offsetof(struct scalanative_lconv, int_p_sign_posn) ==
                   offsetof(struct lconv, int_p_sign_posn),
               "Unexpected offset: scalanative_lconv.int_p_sign_posn");

_Static_assert(offsetof(struct scalanative_lconv, int_n_sign_posn) ==
                   offsetof(struct lconv, int_n_sign_posn),
               "Unexpected offset: scalanative_lconv.int_n_sign_posn");

// Symbolic constants

int scalanative_lc_all() { return LC_ALL; }

int scalanative_lc_collate() { return LC_COLLATE; }

int scalanative_lc_ctype() { return LC_CTYPE; }

int scalanative_lc_monetary() { return LC_MONETARY; }

int scalanative_lc_numeric() { return LC_NUMERIC; }

int scalanative_lc_time() { return LC_TIME; }

#endif // POSIX
#endif // ! _WIN32
#endif