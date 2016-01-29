#ifndef NRT_ALLOC
#define NRT_ALLOC

#include "object.h"

nrt_obj nrt_alloc_object(nrt_obj cls, nrt_size size);
nrt_obj nrt_alloc_array(nrt_obj cls, nrt_i32 length);

#endif
