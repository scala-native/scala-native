#include <stdint.h>
#include "obj.h"

#ifndef SN_PRIM
#define SN_PRIM

typedef uint16_t sn_char;
typedef uint8_t  sn_bool;
typedef int8_t   sn_byte;
typedef int16_t  sn_short;
typedef int32_t  sn_int;
typedef int64_t  sn_long;
typedef float    sn_float;
typedef double   sn_double;

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

sn_int sn_hash_code_bool  (sn_bool   value);
sn_int sn_hash_code_char  (sn_char   value);
sn_int sn_hash_code_byte  (sn_byte   value);
sn_int sn_hash_code_short (sn_short  value);
sn_int sn_hash_code_int   (sn_int    value);
sn_int sn_hash_code_long  (sn_long   value);
sn_int sn_hash_code_float (sn_float  value);
sn_int sn_hash_code_double(sn_double value);

sn_obj sn_to_string_bool  (sn_bool   value);
sn_obj sn_to_string_char  (sn_char   value);
sn_obj sn_to_string_byte  (sn_byte   value);
sn_obj sn_to_string_short (sn_short  value);
sn_obj sn_to_string_int   (sn_int    value);
sn_obj sn_to_string_long  (sn_long   value);
sn_obj sn_to_string_float (sn_float  value);
sn_obj sn_to_string_double(sn_double value);

#endif
