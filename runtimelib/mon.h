#include "prim.h"

#ifndef SN_MONITOR
#define SN_MONITOR

void sn_mon_enter     (sn_ptr obj);
void sn_mon_exit      (sn_ptr obj);
void sn_mon_notify    (sn_ptr obj);
void sn_mon_notify_all(sn_ptr obj);
void sn_mon_wait      (sn_ptr obj, sn_long timeout, sn_int nanos);

#endif
