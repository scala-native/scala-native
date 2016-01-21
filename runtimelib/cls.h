#include "obj.h"

#ifndef SN_CLS
#define SN_CLS

sn_bool sn_cls_equals    (sn_obj cls1, sn_obj cls2);
sn_int  sn_cls_hash_code (sn_obj cls);
sn_obj  sn_cls_to_string (sn_obj cls);
sn_size sn_cls_size      (sn_obj cls);
sn_obj  sn_cls_vtable    (sn_obj cls);
sn_obj  sn_cls_is_subtype(sn_obj cls1, sn_obj cls2);

#endif
