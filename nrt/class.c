#include "class.h"

const nrt_class nrt_null_class   = { nrt_null, 0 };
const nrt_class nrt_object_class = { nrt_null, 0 };
const nrt_class nrt_class_class  = { nrt_null, 0 };
const nrt_class nrt_string_class = { nrt_null, 0 };
const nrt_class nrt_char_class   = { nrt_null, 0 };
const nrt_class nrt_bool_class   = { nrt_null, 0 };
const nrt_class nrt_byte_class   = { nrt_null, 0 };
const nrt_class nrt_short_class  = { nrt_null, 0 };
const nrt_class nrt_int_class    = { nrt_null, 0 };
const nrt_class nrt_long_class   = { nrt_null, 0 };
const nrt_class nrt_float_class  = { nrt_null, 0 };
const nrt_class nrt_double_class = { nrt_null, 0 };

nrt_obj class_get_name(nrt_obj cls) {
    return ((nrt_class*)cls)->name;
}

nrt_i32 class_get_size(nrt_obj cls) {
    return ((nrt_class*)cls)->size;
}
