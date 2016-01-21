#include <stdint.h>
#include <stdbool.h>

#ifndef SN_OBJ
#define SN_OBJ

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

typedef void*    sn_obj;
typedef uint8_t  sn_tag;
typedef uint64_t sn_size;
typedef uint16_t sn_char;
typedef bool     sn_bool;
typedef int8_t   sn_byte;
typedef int16_t  sn_short;
typedef int32_t  sn_int;
typedef int64_t  sn_long;
typedef float    sn_float;
typedef double   sn_double;

typedef sn_obj  to_string_t(sn_obj);
typedef sn_int  hash_code_t(sn_obj);
typedef sn_bool equals_t(sn_obj, sn_obj);

typedef struct {
    sn_obj       cls;
    to_string_t* to_string;
    hash_code_t* hash_code;
    equals_t*    equals;
} sn_vtable_t;

typedef struct {
    uint64_t     rc;
    sn_vtable_t* vtable;
} sn_header_t;

typedef struct {
    sn_header_t header;
    int32_t     size;
} sn_array_header_t;

sn_bool sn_obj_equals   (sn_obj obj1, sn_obj obj2);
sn_int  sn_obj_hash_code(sn_obj obj);
sn_obj  sn_obj_to_string(sn_obj obj);
sn_tag  sn_obj_tag      (sn_obj obj);
sn_obj  sn_obj_class    (sn_obj obj);
sn_obj  sn_obj_is       (sn_obj obj, sn_obj cls);

#endif
