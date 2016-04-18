#ifndef NRT_TYPE
#define NRT_TYPE

#include "defs.h"

extern const nrt_type nrt_Type_type;
extern const nrt_type nrt_Object_type;
extern const nrt_type nrt_Monitor_type;
extern const nrt_type nrt_Null_type;
extern const nrt_type nrt_Nothing_type;

nrt_i32  nrt_Type_getId(nrt_type* ty);
nrt_obj* nrt_Type_getName(nrt_type* ty);

#endif
