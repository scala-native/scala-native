#ifndef IMMIX_STACKTRACE_H
#define IMMIX_STACKTRACE_H

#ifndef _WIN32
#include "../../platform/posix/libunwind/include-libunwind/libunwind.h"
#endif

void StackTrace_PrintStackTrace();

#endif // IMMIX_STACKTRACE_H
