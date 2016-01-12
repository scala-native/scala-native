#include "box.h"

sn_ptr sn_box_char  (sn_char   value) { return 0; }
sn_ptr sn_box_byte  (sn_byte   value) { return 0; }
sn_ptr sn_box_short (sn_short  value) { return 0; }
sn_ptr sn_box_int   (sn_int    value) { return 0; }
sn_ptr sn_box_long  (sn_long   value) { return 0; }
sn_ptr sn_box_float (sn_float  value) { return 0; }
sn_ptr sn_box_double(sn_double value) { return 0; }

sn_char   sn_unbox_char  (sn_ptr ptr) { return 0; }
sn_byte   sn_unbox_byte  (sn_ptr ptr) { return 0; }
sn_short  sn_unbox_short (sn_ptr ptr) { return 0; }
sn_int    sn_unbox_int   (sn_ptr ptr) { return 0; }
sn_long   sn_unbox_long  (sn_ptr ptr) { return 0; }
sn_float  sn_unbox_float (sn_ptr ptr) { return 0; }
sn_double sn_unbox_double(sn_ptr ptr) { return 0; }
