#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <math.h>
#include <errno.h>

extern char **environ;

// This file contains functions that wrap posix
// built-in macros. We need this because Scala Native
// can not expand C macros, and that's the easiest way to
// get the values out of those in a portable manner.

int scalanative_eintr() { return EINTR; }

char **scalanative_environ() { return environ; }
