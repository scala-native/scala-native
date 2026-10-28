#ifndef IMMIX_COMMIX_COLLECTOR_STATE_H
#define IMMIX_COMMIX_COLLECTOR_STATE_H

// The code in immix_commix/ is compiled for one collector at a time, Immix or
// Commix, against that collector's heap and global state. Both collectors
// name the parts this code uses alike (see State.h and Heap.h in either
// directory).
#if defined(SCALANATIVE_GC_IMMIX)
#include "immix/State.h"
#elif defined(SCALANATIVE_GC_COMMIX)
#include "commix/State.h"
#endif

#endif // IMMIX_COMMIX_COLLECTOR_STATE_H
