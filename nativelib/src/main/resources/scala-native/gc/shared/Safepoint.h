#ifndef SAFEPOINT_H
#define SAFEPOINT_H
#include "shared/ScalaNativeGC.h"

void Safepoint_init(safepoint_t *ref);
void Safepoint_arm(safepoint_t ref);
void Safepoint_disarm(safepoint_t ref);

#endif