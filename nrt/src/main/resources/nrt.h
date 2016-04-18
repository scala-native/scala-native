#ifndef NRT_NRT
#define NRT_NRT

#include "defs.h"
#include "exception.h"
#include "monitor.h"
#include "object.h"
#include "type.h"

nrt_obj* nrt_init(nrt_i32 argc, void** argv);
nrt_unit nrt_yield();

#endif
