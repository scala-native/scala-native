#include <stdint.h>
#include <stdbool.h>
#include "obj.h"

#ifndef SN_PRIM
#define SN_PRIM

sn_obj sn_box_bool  (sn_bool   value);
sn_obj sn_box_char  (sn_char   value);
sn_obj sn_box_byte  (sn_byte   value);
sn_obj sn_box_short (sn_short  value);
sn_obj sn_box_int   (sn_int    value);
sn_obj sn_box_long  (sn_long   value);
sn_obj sn_box_float (sn_float  value);
sn_obj sn_box_double(sn_double value);

sn_bool   sn_unbox_bool  (sn_obj obj);
sn_char   sn_unbox_char  (sn_obj obj);
sn_byte   sn_unbox_byte  (sn_obj obj);
sn_short  sn_unbox_short (sn_obj obj);
sn_int    sn_unbox_int   (sn_obj obj);
sn_long   sn_unbox_long  (sn_obj obj);
sn_float  sn_unbox_float (sn_obj obj);
sn_double sn_unbox_double(sn_obj obj);

sn_bool   sn_parse_bool     (sn_obj str);
sn_byte   sn_parse_byte     (sn_obj str);
sn_byte   sn_parse_byte_rdx (sn_obj str, sn_int rdx);
sn_short  sn_parse_short    (sn_obj str);
sn_short  sn_parse_short_rdx(sn_obj str, sn_int rdx);
sn_int    sn_parse_int      (sn_obj str);
sn_int    sn_parse_int_rdx  (sn_obj str, sn_int rdx);
sn_long   sn_parse_long     (sn_obj str);
sn_long   sn_parse_long_rdx (sn_obj str, sn_int rdx);
sn_float  sn_parse_float    (sn_obj str);
sn_double sn_parse_double   (sn_obj str);

sn_int sn_bool_equals  (sn_bool   value, sn_obj obj);
sn_int sn_char_equals  (sn_char   value, sn_obj obj);
sn_int sn_byte_equals  (sn_byte   value, sn_obj obj);
sn_int sn_short_equals (sn_short  value, sn_obj obj);
sn_int sn_int_equals   (sn_int    value, sn_obj obj);
sn_int sn_long_equals  (sn_long   value, sn_obj obj);
sn_int sn_float_equals (sn_float  value, sn_obj obj);
sn_int sn_double_equals(sn_double value, sn_obj obj);

sn_int sn_bool_hash_code  (sn_bool   value);
sn_int sn_char_hash_code  (sn_char   value);
sn_int sn_byte_hash_code  (sn_byte   value);
sn_int sn_short_hash_code (sn_short  value);
sn_int sn_int_hash_code   (sn_int    value);
sn_int sn_long_hash_code  (sn_long   value);
sn_int sn_float_hash_code (sn_float  value);
sn_int sn_double_hash_code(sn_double value);

sn_obj sn_bool_to_string  (sn_bool   value);
sn_obj sn_char_to_string  (sn_char   value);
sn_obj sn_byte_to_string  (sn_byte   value);
sn_obj sn_short_to_string (sn_short  value);
sn_obj sn_int_to_string   (sn_int    value);
sn_obj sn_long_to_string  (sn_long   value);
sn_obj sn_float_to_string (sn_float  value);
sn_obj sn_double_to_string(sn_double value);

#endif
