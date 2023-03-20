#ifndef IMMIX_LOG_H
#define IMMIX_LOG_H

// #define DEBUG_PRINT
#define DEBUG_ASSERT
#ifndef DEBUG_ASSERT

#ifndef NDEBUG
#define NDEBUG
#endif // NDEBUG

#endif // DEBUG_ASSERT

#include <assert.h>
#include <inttypes.h>


#endif // IMMIX_LOG_H
