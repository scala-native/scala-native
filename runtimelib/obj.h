#include "prim.h"

#ifndef SN_OBJ
#define SN_OBJ

typedef void*   sn_obj;
typedef uint8_t sn_tag;

#define OBJ_TAG    0
#define CLASS_TAG  1
#define CHAR_TAG   2
#define BOOL_TAG   3
#define BYTE_TAG   4
#define SHORT_TAG  5
#define INT_TAG    6
#define LONG_TAG   7
#define FLOAT_TAG  8
#define DOUBLE_TAG 9

sn_bool sn_obj_equals   (sn_obj obj1, sn_obj obj2);
sn_int  sn_obj_hash_code(sn_obj obj);
sn_obj  sn_obj_to_string(sn_obj obj);
sn_tag  sn_obj_get_tag  (sn_obj obj);
sn_obj  sn_obj_get_class(sn_obj obj);
sn_obj  sn_obj_is       (sn_obj obj, sn_obj cls);

#endif
