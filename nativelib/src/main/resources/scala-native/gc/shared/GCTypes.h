#ifndef GC_TYPES_H
#define GC_TYPES_H

#include <stdint.h>

#define NO_OPTIMIZE __attribute__((optnone))
#define NOINLINE __attribute__((noinline))
#define INLINE __attribute__((always_inline))

#if defined(__has_feature)
#if __has_feature(address_sanitizer)
// NO_SANITIZE annotation might be skipped if function is inlined, prohibit
// inlining instead
#define NO_SANITIZE_ADDRESS __attribute__((no_sanitize("address"))) NOINLINE
#endif
#if __has_feature(thread_sanitizer)
#define NO_SANITIZE_THREAD __attribute__((no_sanitize("thread"))) NOINLINE
#endif
#if defined(NO_SANITIZE_ADDRESS) || defined(NO_SANITIZE_THREAD)
#define NO_SANITIZE __attribute__((disable_sanitizer_instrumentation)) NOINLINE
#endif
#endif // has_feature

#ifndef NO_SANITIZE
#define NO_SANITIZE
#define NO_SANITIZE_ADDRESS
#define NO_SANITIZE_THREAD
#endif

#define UNLIKELY(b) __builtin_expect((b), 0)
#define LIKELY(b) __builtin_expect((b), 1)

typedef uintptr_t word_t;
typedef uint8_t ubyte_t;

/* Convenient internal macro to test version of gcc.    */
#if defined(__GNUC__) && defined(__GNUC_MINOR__)
#define GNUC_PREREQ(major, minor)                                              \
    ((__GNUC__ << 8) + __GNUC_MINOR__ >= ((major) << 8) + (minor))
#else
#define GNUC_PREREQ(major, minor) 0 /* FALSE */
#endif

#endif // GC_TYPES_H
