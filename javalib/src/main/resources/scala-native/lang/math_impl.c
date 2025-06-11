/* Derived, with thanks & appreciation, from
 * "plokhotnyuk/jsoniter-scala multiply_high.c",
 * URL: https://github.com/plokhotnyuk/jsoniter-scala
 *
 * commit: d69ebc6 dated: 2025-06-10
 *
 * Used under the original's permissive MIT license as reproduced in
 * this projects LICENSE.md.
 */

#if defined(SCALANATIVE_COMPILE_ALWAYS) ||                                     \
    defined(__SCALANATIVE_JAVALIB_LANG_MATHIMPL_H)

#include <stdint.h>

#if defined(_WIN64)

#include <intrin.h>

int64_t scalanative_javalib_multiply_high(int64_t x, int64_t y) {
    return __mulh(x, y);
}

uint64_t scalanative_javalib_unsigned_multiply_high(uint64_t x, uint64_t y) {
    return __umulh(x, y);
}

#elif defined(__SIZEOF_INT128__)

int64_t scalanative_javalib_multiply_high(int64_t x, int64_t y) {
    return (x * (__int128)y) >> 64;
}

uint64_t scalanative_javalib_unsigned_multiply_high(uint64_t x, uint64_t y) {
    return (x * (unsigned __int128)y) >> 64;
}

#else
#if !defined(__clang__) && !defined(__GNUC__)

/* The C standard specifies the right shift operator as implementation
 * defined.
 *
 * clang and gcc both specify an arithmetic shift (sign bit)
 * is used to fill vacated bits if the the possibly promoted left hand
 * operand is signed. If the left operand is unsigned, a logical shift
 * will be used and vacated bits are filled with zeros.
 *
 * Scala Native specifies that it is built using Clang.
 * If a compiler is used which is not known to
 * use the clang right shift definition, use the gentlest
 * available method to bring attention to the possibly incorrect
 * runtime behavior.
 *
 * In the general case, use the #warning directive. Not all compilers
 * have that directive. Those which do not will get
 * a compilation error here, which serves to direct attention to this issue.
 */

#warning "C '>>' is implementation defined and not known to be correct here."

#endif // __clang__

// Based on `int mulhs(int u, int v)` function from
// Hacker’s Delight 2nd Ed. by Henry S. Warren, Jr.
int64_t scalanative_javalib_multiply_high(int64_t x, int64_t y) {
    int64_t x0 = x & 0xFFFFFFFFL;
    int64_t x1 = x >> 32;
    int64_t y0 = y & 0xFFFFFFFFL;
    int64_t y1 = y >> 32;
    int64_t t = x1 * y0 + ((uint64_t)(x0 * y0) >> 32);
    return x1 * y1 + (t >> 32) + (((t & 0xFFFFFFFFL) + x0 * y1) >> 32);
}

// Based on `unsigned mulhu(unsigned u, unsigned v)` function from
// Hacker’s Delight 2nd Ed. by Henry S. Warren, Jr.
uint64_t scalanative_javalib_unsigned_multiply_high(uint64_t x, uint64_t y) {
    uint64_t x0 = x & 0xFFFFFFFFL;
    uint64_t x1 = x >> 32;
    uint64_t y0 = y & 0xFFFFFFFFL;
    uint64_t y1 = y >> 32;
    uint64_t t = x1 * y0 + ((x0 * y0) >> 32);
    return x1 * y1 + (t >> 32) + (((t & 0xFFFFFFFFL) + x0 * y1) >> 32);
}

#endif

#endif // defined(SCALANATIVE_COMPILE_ALWAYS) ||
       // defined(__SCALANATIVE_JAVALIB_LANG_MATHIMPL_H)
