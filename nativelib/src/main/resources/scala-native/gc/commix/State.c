#if defined(SCALANATIVE_GC_COMMIX)

#include "State.h"

Heap heap = {};
BlockAllocator blockAllocator = {};
MutatorThreads mutatorThreads = NULL;
atomic_int_fast32_t mutatorThreadsCount = 0;
SN_ThreadLocal MutatorThread *currentMutatorThread = NULL;
GC_Roots *customRoots = NULL;

#ifdef SCALANATIVE_MULTITHREADING_ENABLED
struct GCWeakRefsHandlerThread *weakRefsHandlerThread = NULL;
#endif

#endif
