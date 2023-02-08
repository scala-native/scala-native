#ifndef ZONE_UTIL_H
#define ZONE_UTIL_H

#include <stdlib.h>
#include "MemoryPage.h"

size_t Util_pad(size_t addr, size_t alignment);

size_t Util_debug_pages_length(MemoryPage *head);

void Util_debug_print_pages(MemoryPage *head);

#endif // ZONE_UTIL_H