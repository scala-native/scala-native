#include "type.h"

const nrt_type nrt_Type_type    = {};
const nrt_type nrt_Object_type  = {};
const nrt_type nrt_Monitor_type = {};
const nrt_type nrt_Null_type    = {};
const nrt_type nrt_Nothing_type = {};

nrt_i32  nrt_Type_getId(nrt_type* ty) {
    return 0;
}

nrt_obj* nrt_Type_getName(nrt_type* ty) {
    return nrt_null;
}
