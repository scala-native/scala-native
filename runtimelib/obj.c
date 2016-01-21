#include "obj.h"
#include "cls.h"
#include "prim.h"
#include "util.h"

sn_bool sn_obj_equals(sn_obj obj1, sn_obj obj2) {
    switch(sn_obj_tag(obj1)) {
        case OBJ_TAG:    return ((sn_header_t*)obj1)->vtable->equals(obj1, obj2);
        case CLASS_TAG:  return sn_cls_equals(obj1, obj2);
        case CHAR_TAG:   return sn_char_equals(sn_unbox_char(obj1), obj2);
        case BOOL_TAG:   return sn_bool_equals(sn_unbox_bool(obj1), obj2);
        case BYTE_TAG:   return sn_byte_equals(sn_unbox_byte(obj1), obj2);
        case SHORT_TAG:  return sn_short_equals(sn_unbox_short(obj1), obj2);
        case INT_TAG:    return sn_int_equals(sn_unbox_int(obj1), obj2);
        case LONG_TAG:   return sn_long_equals(sn_unbox_long(obj1), obj2);
        case FLOAT_TAG:  return sn_float_equals(sn_unbox_float(obj1), obj2);
        case DOUBLE_TAG: return sn_double_equals(sn_unbox_double(obj1), obj2);
        default:         unreachable;
    }
}

sn_int sn_obj_hash_code(sn_obj obj) {
    switch(sn_obj_tag(obj)) {
        case OBJ_TAG:    return ((sn_header_t*)obj)->vtable->hash_code(obj);
        case CLASS_TAG:  return sn_cls_hash_code(obj);
        case CHAR_TAG:   return sn_char_hash_code(sn_unbox_char(obj));
        case BOOL_TAG:   return sn_bool_hash_code(sn_unbox_bool(obj));
        case BYTE_TAG:   return sn_byte_hash_code(sn_unbox_byte(obj));
        case SHORT_TAG:  return sn_short_hash_code(sn_unbox_short(obj));
        case INT_TAG:    return sn_int_hash_code(sn_unbox_int(obj));
        case LONG_TAG:   return sn_long_hash_code(sn_unbox_long(obj));
        case FLOAT_TAG:  return sn_float_hash_code(sn_unbox_float(obj));
        case DOUBLE_TAG: return sn_double_hash_code(sn_unbox_double(obj));
        default:         unreachable;
    }
}

sn_obj sn_obj_to_string(sn_obj obj) {
    switch(sn_obj_tag(obj)) {
        case OBJ_TAG:    return ((sn_header_t*)obj)->vtable->to_string(obj);
        case CLASS_TAG:  return sn_cls_to_string(obj);
        case CHAR_TAG:   return sn_char_to_string(sn_unbox_char(obj));
        case BOOL_TAG:   return sn_bool_to_string(sn_unbox_bool(obj));
        case BYTE_TAG:   return sn_byte_to_string(sn_unbox_byte(obj));
        case SHORT_TAG:  return sn_short_to_string(sn_unbox_short(obj));
        case INT_TAG:    return sn_int_to_string(sn_unbox_int(obj));
        case LONG_TAG:   return sn_long_to_string(sn_unbox_long(obj));
        case FLOAT_TAG:  return sn_float_to_string(sn_unbox_float(obj));
        case DOUBLE_TAG: return sn_double_to_string(sn_unbox_double(obj));
        default:         unreachable;
    }
}

sn_tag sn_obj_tag(sn_obj obj) {
    return 0;
}

sn_obj sn_obj_class(sn_obj obj) {
    return ((sn_header_t*)obj)->vtable->cls;
}

sn_obj sn_obj_is(sn_obj obj, sn_obj cls) {
    return sn_cls_is_subtype(sn_obj_class(obj), cls);
}
