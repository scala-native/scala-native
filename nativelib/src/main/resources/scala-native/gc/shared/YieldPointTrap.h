#ifndef YieldPointTrap_H
#define YieldPointTrap_H
#include <stdbool.h>

typedef void **safepoint_t;
safepoint_t YieldPointTrap_init(void);
/** macOS: clear task EXC_BAD_ACCESS Mach exception ports so faults reach
 *  SIGBUS/SIGSEGV; no-op elsewhere. Safe to call before each GC suspend. */
void YieldPointTrap_resetTaskMachBadAccessPorts(void);
void YieldPointTrap_arm(safepoint_t ref);
void YieldPointTrap_disarm(safepoint_t ref);
void YieldPointTrap_free(safepoint_t ref);
/* Signal-safe lookup. Registry entries and mappings live until process exit. */
bool YieldPointTrap_contains(const void *address);

#endif
