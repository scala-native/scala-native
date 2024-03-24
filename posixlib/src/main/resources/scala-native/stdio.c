#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_POSIX_STDIO)
#include <stdio.h>

#if !defined(L_ctermid)
#if defined(_WIN32)
// Windows MAX_PATH is 260, plus 1 for terminating NUL/NULL/"\u0000".
#define L_ctermid 260 + 1
#else
#error "L_ctermid is not defined in stdio.h"
#endif
#endif

// This file contains functions that wrap posixlib
// built-in macros. We need this because Scala Native
// can not expand C macros, and that's the easiest way to
// get the values out of those in a portable manner.

// CX extension
int scalanative_l_ctermid() { return L_ctermid; }
#endif