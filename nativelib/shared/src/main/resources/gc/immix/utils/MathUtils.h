#ifndef IMMIX_MATHUTILS_H
#define IMMIX_MATHUTILS_H

#include <stddef.h>

static const int MultiplyDeBruijnBitPosition[32] = {
    0, 9,  1,  10, 13, 21, 2,  29, 11, 14, 16, 18, 22, 25, 3, 30,
    8, 12, 20, 28, 15, 17, 24, 7,  19, 27, 23, 6,  26, 5,  4, 31};

static int log2_floor(size_t v) {

    v |= v >> 1; // first round down to one less than a power of 2
    v |= v >> 2;
    v |= v >> 4;
    v |= v >> 8;
    v |= v >> 16;

    return MultiplyDeBruijnBitPosition[(uint32_t)(v * 0x07C4ACDDU) >> 27];
}

static inline int MathUtils_Log2Ceil(size_t value) {
    return log2_floor(2 * value - 1);
}

static inline size_t MathUtils_RoundToNextMultiple(size_t value,
                                                   size_t multiple) {
    return (value + multiple - 1) / multiple * multiple;
}

static inline size_t MathUtils_DivAndRoundUp(size_t value, size_t divider) {
    return (value + divider - 1) / divider;
}

#endif // IMMIX_MATHUTILS_H
