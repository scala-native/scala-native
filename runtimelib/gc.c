#include <stdlib.h>
#include "gc.h"

sn_ptr sn_alloc(sn_size size, sn_tag tag) {
    return malloc(size);
}
