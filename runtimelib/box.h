#include "types.h"

#ifndef SN_BOX
#define SN_BOX

sn_ptr sn_box_char  (sn_char   value);
sn_ptr sn_box_byte  (sn_byte   value);
sn_ptr sn_box_short (sn_short  value);
sn_ptr sn_box_int   (sn_int    value);
sn_ptr sn_box_long  (sn_long   value);
sn_ptr sn_box_float (sn_float  value);
sn_ptr sn_box_double(sn_double value);

sn_char   sn_unbox_char  (sn_ptr ptr);
sn_byte   sn_unbox_byte  (sn_ptr ptr);
sn_short  sn_unbox_short (sn_ptr ptr);
sn_int    sn_unbox_int   (sn_ptr ptr);
sn_long   sn_unbox_long  (sn_ptr ptr);
sn_float  sn_unbox_float (sn_ptr ptr);
sn_double sn_unbox_double(sn_ptr ptr);

#endif
