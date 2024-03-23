#if defined(__SCALANATIVE_MEMORY_SAFEZONE)
#include <stdio.h>
#include <stdlib.h>
#include "Util.h"

size_t Util_pad(size_t addr, size_t alignment) {
    size_t alignment_mask = alignment - 1;
    size_t padding = ((addr & alignment_mask) == 0)
                         ? 0
                         : (alignment - (addr & alignment_mask));
    return addr + padding;
}
#endif
