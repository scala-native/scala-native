#ifndef NRT_EXCEPTION
#define NRT_EXCEPTION

#include "defs.h"

nrt_nothing nrt_throw(nrt_obj* obj);
nrt_unit    nrt_begin_catch(void* exc);
nrt_unit    nrt_end_catch();

#endif
