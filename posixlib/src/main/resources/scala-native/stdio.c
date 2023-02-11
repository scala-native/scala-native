#include <stdio.h>

// This file contains functions that wrap posixlib
// built-in macros. We need this because Scala Native
// can not expand C macros, and that's the easiest way to
// get the values out of those in a portable manner.

// CX extension
int scalanative_l_ctermid() { return L_ctermid; }
