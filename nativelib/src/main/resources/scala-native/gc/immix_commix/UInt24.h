#ifndef UINT24_H
#define UINT24_H
#include <stddef.h>

typedef struct {
    uint8_t bytes[3];
} UInt24;

typedef union {
    UInt24 value;
    uint32_t bits : 24;
} UInt24Bits;

static inline UInt24 UInt24_fromUInt32(uint32_t value) {
    return ((UInt24Bits)(value)).value;
}
static inline uint32_t UInt24_toUInt32(UInt24 v) {
    return ((UInt24Bits)(v)).bits;
}

static inline UInt24 UInt24_plus(UInt24 value, int32_t arg) {
    uint32_t v = UInt24_toUInt32(value);
    return UInt24_fromUInt32(v + arg);
}

static inline bool UInt24_equals(UInt24 v1, UInt24 v2) {
    return UInt24_toUInt32(v1) == UInt24_toUInt32(v2);
}

#endif // UINT24_H
