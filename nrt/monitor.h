#ifndef NRT_MONITOR
#define NRT_MONITOR

void nrt_monitor_enter(nrt_obj obj);
void nrt_monitor_exit(nrt_obj obj);
void nrt_monitor_notify(nrt_obj obj);
void nrt_monitor_notify_all(nrt_obj obj);
void nrt_monitor_wait(nrt_obj obj, nrt_i64 timeout, nrt_i32 nanos);

#endif
