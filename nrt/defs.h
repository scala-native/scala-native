#ifndef NRT_DEFS
#define NRT_DEFS

#include <stddef.h>
#include <stdint.h>
#include <stdbool.h>

typedef struct {
} nrt_obj;

typedef struct {
} nrt_monitor;

typedef struct {
} nrt_type;

typedef void    nrt_unit;
typedef void    nrt_nothing;
typedef size_t  nrt_size;
typedef bool    nrt_bool;
typedef int8_t  nrt_i8;
typedef int16_t nrt_i16;
typedef int32_t nrt_i32;
typedef int64_t nrt_i64;

#define nrt_null  (0)
#define nrt_true  (true)
#define nrt_false (false)

#endif
