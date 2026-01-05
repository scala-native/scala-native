#ifndef GC_TIME_H
#define GC_TIME_H

// Monotonic time utilities for GC
// Uses monotonic clocks that are immune to system time adjustments
// Suitable for measuring elapsed time, timeouts, etc.

long long Time_current_millis(void);
long long Time_current_nanos(void);

#endif // GC_TIME_H
