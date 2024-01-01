#ifndef IMMIX_LOG_H
#define IMMIX_LOG_H

#ifdef DEBUG_ASSERT
#undef NDEBUG
#else
#ifndef NDEBUG
#define NDEBUG
#endif // NDEBUG
#endif // DEBUG_ASSERT

#include <assert.h>
#include <inttypes.h>

// #define DEBUG_PRINT

#endif // IMMIX_LOG_H
