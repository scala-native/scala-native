#if defined(SCALANATIVE_GC_COMMIX)
#include <shared/ScalaNativeGC.h>
#include <stdlib.h>
#include <memory.h>
#include "shared/GCTypes.h"
#include "Heap.h"
#include "Allocator.h"
#include "LargeAllocator.h"
#include "Marker.h"
#include "shared/Log.h"
#include "immix_commix/Object.h"
#include "State.h"
#include "immix_commix/utils/MathUtils.h"
#include "Constants.h"
#include "Settings.h"
#include "GCThread.h"
#include "WeakReferences.h"
#include "Sweeper.h"
#include "immix_commix/Synchronizer.h"
#include "shared/jmx.h"

#include "shared/Parsing.h"

#include "MutatorThread.h"
#include "stackOverflowGuards.h"
#include <stdatomic.h>

void scalanative_afterexit() {
#ifdef ENABLE_GC_STATS
    Stats_OnExit(heap.stats);

    int gcThreadCount = heap.gcThreads.count;
    GCThread *gcThreads = (GCThread *)heap.gcThreads.all;
    for (int i = 0; i < gcThreadCount; i++) {
        Stats_OnExit(gcThreads[i].stats);
    }
#endif
}

NOINLINE void scalanative_GC_init() {
    GC_Log_Init();
    Settings_Init();
    Heap_Init(&heap, Settings_MinHeapSize(), Settings_MaxHeapSize());
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    Synchronizer_init();
#endif
    MutatorThreads_init();
    ThreadInfo threadStack;
    if (!scalanative_detectExternalThreadStack(&threadStack))
        threadStack.stackBottom = alignToNextPage(&threadStack);
    if (!MutatorThread_init((word_t **)threadStack.stackBottom))
        abort();
    customRoots = GC_Roots_Init();
    if (customRoots == NULL)
        abort();
#ifdef ENABLE_GC_STATS
    atexit(scalanative_afterexit);
#endif
}

INLINE void *scalanative_GC_alloc_small(Rtti *info, size_t size) {
    size = MathUtils_RoundToNextMultiple(size, ALLOCATION_ALIGNMENT);

    Object *alloc = (Object *)Allocator_Alloc(&heap, size);
    alloc->rtti = info;
    return (void *)alloc;
}

INLINE void scalanative_GC_collect() { Heap_Collect(&heap, false); }

#endif // defined(SCALANATIVE_GC_COMMIX)
