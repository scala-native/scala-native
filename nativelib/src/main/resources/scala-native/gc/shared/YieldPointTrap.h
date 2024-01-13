#ifndef YieldPointTrap_H
#define YieldPointTrap_H

typedef void **safepoint_t;
safepoint_t YieldPointTrap_init();
void YieldPointTrap_arm(safepoint_t ref);
void YieldPointTrap_disarm(safepoint_t ref);

#endif