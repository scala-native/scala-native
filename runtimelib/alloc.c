#include <stdlib.h>
#include "alloc.h"
#include "cls.h"
#include "util.h"

sn_obj sn_alloc_obj(sn_obj cls) {
    sn_header_t* p = (sn_header_t*)(malloc(sn_cls_size(cls)));
    p->rc = 0;
    p->vtable = sn_cls_vtable(cls);
    return (sn_obj) p;
}
sn_obj sn_alloc_arr(sn_obj cls, sn_int length) {
    return 0;
}
