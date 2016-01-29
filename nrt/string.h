#ifndef NRT_STRING
#define NRT_STRING

#include "object.h"

nrt_obj string_concat(nrt_obj s1, nrt_obj s2);
nrt_obj string_from_ptr(nrt_i32 length, char* data);

#endif
