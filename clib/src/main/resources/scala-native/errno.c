#include <errno.h>

int scalanative_errno() { return errno; }

void scalanative_set_errno(int value) { errno = value; }

int scalanative_edom() { return EDOM; }

int scalanative_eilseq() { return EILSEQ; }

int scalanative_erange() { return ERANGE; }
