#ifndef MEMORY_PAGE_H
#define MEMORY_PAGE_H

#include <stdlib.h>

typedef struct _MemoryPage {
    void *start;
    size_t offset;
    size_t size;
    struct _MemoryPage *next;
} MemoryPage;

#endif // MEMORY_PAGE_H