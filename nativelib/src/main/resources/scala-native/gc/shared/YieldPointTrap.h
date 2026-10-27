#ifndef YieldPointTrap_H
#define YieldPointTrap_H

#include <stdbool.h>

typedef void **safepoint_t;
/* Return NULL when the OS cannot allocate a trap page. */
safepoint_t YieldPointTrap_init(void);
/** macOS: clear task EXC_BAD_ACCESS Mach exception ports so faults reach
 *  SIGBUS/SIGSEGV; no-op elsewhere. Safe to call before each GC suspend. */
void YieldPointTrap_resetTaskMachBadAccessPorts(void);
void YieldPointTrap_arm(safepoint_t ref);
void YieldPointTrap_disarm(safepoint_t ref);
void YieldPointTrap_free(safepoint_t ref);
/** Async-signal-safe: true if `faultAddress` is on a trap page created by
 *  YieldPointTrap_init. Trap pages are never unmapped. */
bool YieldPointTrap_isRegisteredPage(const void *faultAddress);

#endif
