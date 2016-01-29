#ifndef NRT
#define NRT

#include "alloc.h"
#include "class.h"
#include "monitor.h"
#include "object.h"
#include "string.h"

nrt_obj nrt_init(int argc, char** argv);
void    nrt_yield();

#endif
