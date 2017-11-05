#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <math.h>
#include <errno.h>

// This file contains functions that wrap libc
// built-in macros. We need this because Scala Native
// can not expand C macros, and that's the easiest way to
// get the values out of those in a portable manner.

#ifdef _WIN32
int os_win_libc_remove(const char *fname);
#endif

int scalanative_libc_remove(const char *fname) {
#ifndef _WIN32
    return remove(fname);
#else
    return os_win_libc_remove(fname);
#endif
}

void *scalanative_libc_stdin() { return stdin; }

void *scalanative_libc_stdout() { return stdout; }

void *scalanative_libc_stderr() { return stderr; }

int scalanative_libc_eof() { return EOF; }

unsigned int scalanative_libc_fopen_max() { return FOPEN_MAX; }

unsigned int scalanative_libc_filename_max() { return FILENAME_MAX; }

unsigned int scalanative_libc_bufsiz() { return BUFSIZ; }

int scalanative_libc_iofbf() { return _IOFBF; }

int scalanative_libc_iolbf() { return _IOLBF; }

int scalanative_libc_ionbf() { return _IONBF; }

int scalanative_libc_seek_set() { return SEEK_SET; }

int scalanative_libc_seek_cur() { return SEEK_CUR; }

int scalanative_libc_seek_end() { return SEEK_END; }

unsigned int scalanative_libc_tmp_max() { return TMP_MAX; }

unsigned int scalanative_libc_l_tmpnam() { return L_tmpnam; }

int scalanative_libc_exit_success() { return EXIT_SUCCESS; }

int scalanative_libc_exit_failure() { return EXIT_FAILURE; }

typedef void (*sig_handler_t)(int);

sig_handler_t scalanative_libc_sig_dfl() { return SIG_DFL; }

sig_handler_t scalanative_libc_sig_ign() { return SIG_IGN; }

sig_handler_t scalanative_libc_sig_err() { return SIG_ERR; }

int scalanative_libc_sigabrt() { return SIGABRT; }

int scalanative_libc_sigfpe() { return SIGFPE; }

int scalanative_libc_sigill() { return SIGILL; }

int scalanative_libc_sigint() { return SIGINT; }

int scalanative_libc_sigsegv() { return SIGSEGV; }

int scalanative_libc_sigterm() { return SIGTERM; }

int scalanative_libc_rand_max() { return RAND_MAX; }

float scalanative_libc_huge_valf() { return HUGE_VALF; }

double scalanative_libc_huge_val() { return HUGE_VAL; }

float scanative_libc_infinity() { return INFINITY; }

float scanative_libc_nan() { return NAN; }

int scalanative_libc_math_errhandling() { return math_errhandling; }

int scalanative_math_errno() { return MATH_ERRNO; }

int scalanative_math_errexcept() { return MATH_ERREXCEPT; }

int scalanative_errno() { return errno; }

void scalanative_set_errno(int value) { errno = value; }

int scalanative_edom() { return EDOM; }

int scalanative_eilseq() { return EILSEQ; }

int scalanative_erange() { return ERANGE; }
