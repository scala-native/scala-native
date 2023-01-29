#ifndef GC_TYPES_H
#define GC_TYPES_H

#include <stdint.h>

#define NOINLINE __attribute__((noinline))
#define INLINE __attribute__((always_inline))

#if defined(__has_feature)
#if __has_feature(address_sanitizer)
#define NO_SANITIZE __attribute__((no_sanitize("address")))
#endif
#endif

#ifndef NO_SANITIZE
#define NO_SANITIZE
#endif

#define UNLIKELY(b) __builtin_expect((b), 0)
#define LIKELY(b) __builtin_expect((b), 1)

typedef uintptr_t word_t;
typedef uint8_t ubyte_t;

#endif // GC_TYPES_H
