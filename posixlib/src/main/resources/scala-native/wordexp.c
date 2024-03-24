#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_POSIX_WORDEXP)
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))
#ifndef __OpenBSD__

#include <stddef.h>
#include <wordexp.h>

struct scalanative_wordexp_t {
    size_t we_wordc; //  Count of words matched by 'words'.
    char **we_wordv; // Pointer to list of expanded words.
    size_t we_offs;  // Slots to reserve at the beginning of we_wordv.

    /* Permitted but not requited by POSIX 2018.
     * Used here to allow direct overlay calling on FreeBSD in addition
     * to Linux & macOS.
     */
    char *we_strings;  // storage for wordv strings
    size_t *we_nbytes; // size of we_strings
};

#if !(defined __STDC_VERSION__) || (__STDC_VERSION__ < 201112L)
#ifndef SCALANATIVE_SUPPRESS_STRUCT_CHECK_WARNING
#warning "Size and order of C structures are not checked when -std < c11."
#endif
#else

_Static_assert(sizeof(struct scalanative_wordexp_t) >= sizeof(wordexp_t),
               "size mismatch: wordexp_t");

_Static_assert(offsetof(struct scalanative_wordexp_t, we_wordc) ==
                   offsetof(wordexp_t, we_wordc),
               "offset mismatch: wordexp_t we_wordc");

_Static_assert(offsetof(struct scalanative_wordexp_t, we_wordv) ==
                   offsetof(wordexp_t, we_wordv),
               "offset mismatch: wordexp_t we_wordv");

_Static_assert(offsetof(struct scalanative_wordexp_t, we_offs) ==
                   offsetof(wordexp_t, we_offs),
               "offset mismatch: wordexp_t we_offs");

#endif // __STDC_VERSION__
#endif // !OpenBSD
#endif // Unix or Mac OS

// flags
#if !defined(__OpenBSD__)
int scalanative_wrde_append() { return WRDE_APPEND; };

int scalanative_wrde_dooffs() { return WRDE_DOOFFS; };

int scalanative_wrde_nocmd() { return WRDE_NOCMD; };

int scalanative_wrde_reuse() { return WRDE_REUSE; };

int scalanative_wrde_showerr() { return WRDE_SHOWERR; };

int scalanative_wrde_undef() { return WRDE_UNDEF; };

// error returns
int scalanative_wrde_badchar() { return WRDE_BADCHAR; };

int scalanative_wrde_badval() { return WRDE_BADVAL; };

int scalanative_wrde_cmdsub() { return WRDE_CMDSUB; };

int scalanative_wrde_nospace() { return WRDE_NOSPACE; };

int scalanative_wrde_syntax() { return WRDE_SYNTAX; };

#endif // not bsd

#endif // __SCALANATIVE_POSIX_WORDEXP