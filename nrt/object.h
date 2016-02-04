#ifndef NRT_OBJECT
#define NRT_OBJECT

#include <stdint.h>
#include <stdbool.h>

#define OBJECT_TAG 0
#define CLASS_TAG  1
#define CHAR_TAG   2
#define BOOL_TAG   3
#define BYTE_TAG   4
#define SHORT_TAG  5
#define INT_TAG    6
#define LONG_TAG   7
#define FLOAT_TAG  8
#define DOUBLE_TAG 9

typedef void*    nrt_obj;
typedef uint8_t  nrt_tag;
typedef uint64_t nrt_size;
typedef bool     nrt_bool;
typedef int8_t   nrt_i8;
typedef int16_t  nrt_i16;
typedef int32_t  nrt_i32;
typedef int64_t  nrt_i64;
typedef float    nrt_f32;
typedef double   nrt_f64;

#define nrt_null 0

typedef nrt_obj  nrt_to_string_f(nrt_obj);
typedef nrt_i32  nrt_hash_code_f(nrt_obj);
typedef nrt_bool nrt_equals_f(nrt_obj, nrt_obj);

nrt_obj nrt_object_equals(nrt_obj obj1, nrt_obj obj2);
nrt_obj nrt_object_to_string(nrt_obj obj);
nrt_obj nrt_object_hash_code(nrt_obj obj);
nrt_obj nrt_object_get_class(nrt_obj obj);

#endif
