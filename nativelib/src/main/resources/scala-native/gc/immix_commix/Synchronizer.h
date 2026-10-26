#ifndef SYNCHRONIZER_H
#define SYNCHRONIZER_H

#include <stdbool.h>
#include <stdatomic.h>
#include <stdint.h>

extern atomic_bool Synchronizer_stopThreads;
#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
#include "shared/ThreadUtil.h"
extern SN_ThreadLocal void **scalanative_GC_yieldpoint_trap;
#endif

void Synchronizer_init();
// Try to acquire ownership of synchronization and stop remaining threads, if
// race for ownership is won returns true, false otherwise
bool Synchronizer_acquire();
#ifdef SCALANATIVE_GC_IMMIX
bool Synchronizer_acquireForAllocation(atomic_uint_fast64_t *completed,
                                       uint64_t generation);
#endif

// Resume remaining threads and release ownersip
void Synchronizer_release();

// Yield execution of calling thead to synchronizer until a call to
// Synchronizer_release is done by other thread
void Synchronizer_yield();

#endif // SYNCHRONIZER_H