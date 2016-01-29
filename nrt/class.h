#ifndef NRT_CLASS
#define NRT_CLASS

#include "object.h"

typedef struct {
    nrt_obj  name;
    nrt_size size;
} nrt_class;

extern const nrt_class nrt_null_class;
extern const nrt_class nrt_object_class;
extern const nrt_class nrt_class_class;
extern const nrt_class nrt_string_class;
extern const nrt_class nrt_char_class;
extern const nrt_class nrt_bool_class;
extern const nrt_class nrt_byte_class;
extern const nrt_class nrt_short_class;
extern const nrt_class nrt_int_class;
extern const nrt_class nrt_long_class;
extern const nrt_class nrt_float_class;
extern const nrt_class nrt_double_class;

nrt_obj class_get_name(nrt_obj cls);
nrt_i32 class_get_size(nrt_obj cls);

#endif
