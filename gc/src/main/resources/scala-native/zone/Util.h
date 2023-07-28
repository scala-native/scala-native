#ifndef ZONE_UTIL_H
#define ZONE_UTIL_H

#include <stdlib.h>
#include "MemoryPage.h"

size_t Util_pad(size_t addr, size_t alignment);

#endif // ZONE_UTIL_H