#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
// YieldProcessor already defined
#else // Unix
// Only clang defines __has_builtin, so we first test for a GCC define
// before using __has_builtin.

#if defined(__i386__) || defined(__x86_64__)
#if (__GNUC__ > 4 && __GNUC_MINOR > 7) || __has_builtin(__builtin_ia32_pause)
// clang added this intrinsic in 3.8
// gcc added this intrinsic by 4.7.1
#define YieldProcessor __builtin_ia32_pause
#endif // __has_builtin(__builtin_ia32_pause)

// If we don't have intrinsics, we can do some inline asm instead.
#ifndef YieldProcessor
#define YieldProcessor() asm volatile("pause")
#endif // YieldProcessor

#endif // defined(__i386__) || defined(__x86_64__)

#ifdef __aarch64__
#define YieldProcessor() asm volatile("yield")
#endif // __aarch64__

#ifdef __arm__
#define YieldProcessor()
#endif // __arm__

#endif // Unix

void scalanative_yield_processor() { YieldProcessor(); }
