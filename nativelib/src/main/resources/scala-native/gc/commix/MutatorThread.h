#ifndef MUTATOR_THREAD_COMMIX_H
#define MUTATOR_THREAD_COMMIX_H

#include "immix_commix/MutatorThread.h"

void MutatorThread_setThreadInfo(MutatorThread *self, ThreadInfo *info);
void MutatorThreads_lockRead(void);
bool MutatorThreads_tryLockRead(void);
void MutatorThreads_unlockRead(void);

#endif // MUTATOR_THREAD_COMMIX_H
