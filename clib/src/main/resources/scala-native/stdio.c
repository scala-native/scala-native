#include <stdio.h>

// This file contains functions that wrap libc
// built-in macros. We need this because Scala Native
// can not expand C macros, and that's the easiest way to
// get the values out of those in a portable manner.

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
