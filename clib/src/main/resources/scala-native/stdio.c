#include <stdio.h>

// This file contains functions that wrap libc
// built-in macros. We need this because Scala Native
// can not expand C macros, and that's the easiest way to
// get the values out of those in a portable manner.

void *scalanative_stdin() { return stdin; }

void *scalanative_stdout() { return stdout; }

void *scalanative_stderr() { return stderr; }

int scalanative_eof() { return EOF; }

unsigned int scalanative_fopen_max() { return FOPEN_MAX; }

unsigned int scalanative_filename_max() { return FILENAME_MAX; }

unsigned int scalanative_bufsiz() { return BUFSIZ; }

int scalanative_iofbf() { return _IOFBF; }

int scalanative_iolbf() { return _IOLBF; }

int scalanative_ionbf() { return _IONBF; }

// SEEK_SET, SEEK_CUR, and SEEK_END also used by posixlib/unistd.scala
int scalanative_seek_set() { return SEEK_SET; }

int scalanative_seek_cur() { return SEEK_CUR; }

int scalanative_seek_end() { return SEEK_END; }

unsigned int scalanative_tmp_max() { return TMP_MAX; }

unsigned int scalanative_l_tmpnam() { return L_tmpnam; }
