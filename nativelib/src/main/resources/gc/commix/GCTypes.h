#ifndef IMMIX_GC_TYPES_H
#define IMMIX_GC_TYPES_H

#include <stdint.h>

#define NOINLINE __attribute__((noinline))
#define INLINE __attribute__((always_inline))

#define UNLIKELY(b) __builtin_expect((b), 0)
#define LIKELY(b) __builtin_expect((b), 1)

typedef uintptr_t word_t;
typedef uint8_t ubyte_t;

#endif // IMMIX_GC_TYPES_H
