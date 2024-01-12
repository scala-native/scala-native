#ifndef IMMIX_LOG_H
#define IMMIX_LOG_H

#ifdef GC_ASSERTIONS
#undef NDEBUG
#else
#ifndef NDEBUG
#define NDEBUG
#endif // NDEBUG
#endif // GC_ASSERTIONS

#include <assert.h>
#include <inttypes.h>

// #define DEBUG_PRINT

#endif // IMMIX_LOG_H
