#ifndef MUTATOR_THREAD_IMMIX_H
#define MUTATOR_THREAD_IMMIX_H

#include "immix_commix/MutatorThread.h"

void MutatorThreads_lock(void);
void MutatorThreads_unlock(void);
// The synchronizer holds the thread list's lock while it stops and scans the
// threads, Commix's as a read lock; Immix has one lock for both
static inline void MutatorThreads_lockRead(void) { MutatorThreads_lock(); }
static inline void MutatorThreads_unlockRead(void) { MutatorThreads_unlock(); }

#endif // MUTATOR_THREAD_IMMIX_H
