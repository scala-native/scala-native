#ifndef IMMIX_LINEHEADER_H
#define IMMIX_LINEHEADER_H

#include <stdint.h>
#include <stdbool.h>

typedef struct {
    int16_t next;
    uint16_t size;
} FreeLineHeader;

typedef enum {
    line_empty = 0x0,
    line_marked = 0x1,
} LineFlag;

typedef uint8_t LineHeader;

static inline bool Line_IsMarked(LineHeader *lineHeader) {
    return *lineHeader == line_marked;
}
static inline void Line_Mark(LineHeader *lineHeader) {
    *lineHeader = line_marked;
}
static inline void Line_Unmark(LineHeader *lineHeader) {
    *lineHeader = line_empty;
}

#endif // IMMIX_LINEHEADER_H
