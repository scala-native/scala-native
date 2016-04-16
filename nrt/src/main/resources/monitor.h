#ifndef NRT_MONITOR
#define NRT_MONITOR

#include "defs.h"

nrt_unit nrt_Monitor_enter(nrt_monitor* mon);
nrt_unit nrt_Monitor_exit(nrt_monitor* mon);
nrt_unit nrt_Monitor_notify(nrt_monitor* mon);
nrt_unit nrt_Monitor_notifyAll(nrt_monitor* mon);
nrt_unit nrt_Monitor_wait(nrt_monitor* mon, nrt_i64 timeout, nrt_i32 nanos);

#endif
