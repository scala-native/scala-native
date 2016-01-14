#include "obj.h"

#ifndef SN_GC
#define SN_GC

sn_obj sn_alloc_obj(sn_obj cls);
sn_obj sn_alloc_arr(sn_obj cls, sn_int length);

#endif
