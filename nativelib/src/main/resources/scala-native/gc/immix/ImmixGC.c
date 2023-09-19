#include <stdio.h>
#include <stdlib.h>
#include <memory.h>
#include "GCTypes.h"
#include "Heap.h"
#include "datastructures/Stack.h"
#include "Marker.h"
#include "Log.h"
#include "Object.h"
#include "State.h"
#include "utils/MathUtils.h"
#include "Constants.h"
#include "Settings.h"
#include "WeakRefStack.h"
#include "Parsing.h"

// Stack boottom of the main thread
extern word_t **__stack_bottom;

void scalanative_collect();

void scalanative_afterexit() { Stats_OnExit(heap.stats); }

NOINLINE void scalanative_init() {
    Heap_Init(&heap, Settings_MinHeapSize(), Settings_MaxHeapSize());
    Stack_Init(&stack, INITIAL_STACK_SIZE);
    Stack_Init(&weakRefStack, INITIAL_STACK_SIZE);
    atexit(scalanative_afterexit);
}

INLINE void *scalanative_alloc(void *info, size_t size) {
    size = MathUtils_RoundToNextMultiple(size, ALLOCATION_ALIGNMENT);

    void **alloc = (void **)Heap_Alloc(&heap, size);
    *alloc = info;
    return (void *)alloc;
}

INLINE void *scalanative_alloc_small(void *info, size_t size) {
    size = MathUtils_RoundToNextMultiple(size, ALLOCATION_ALIGNMENT);

    void **alloc = (void **)Heap_AllocSmall(&heap, size);
    *alloc = info;
    return (void *)alloc;
}

INLINE void *scalanative_alloc_large(void *info, size_t size) {
    size = MathUtils_RoundToNextMultiple(size, ALLOCATION_ALIGNMENT);

    void **alloc = (void **)Heap_AllocLarge(&heap, size);
    *alloc = info;
    return (void *)alloc;
}

INLINE void *scalanative_alloc_atomic(void *info, size_t size) {
    return scalanative_alloc(info, size);
}

INLINE void scalanative_collect() { Heap_Collect(&heap, &stack); }

INLINE void scalanative_register_weak_reference_handler(void *handler) {
    WeakRefStack_SetHandler(handler);
}

/* Get the minimum heap size */
/* If the user has set a minimum heap size using the SCALANATIVE_MIN_SIZE
 * environment variable, */
/* then this size will be returned. */
/* Otherwise, the default minimum heap size will be returned.*/
size_t scalanative_get_init_heapsize() { return Settings_MinHeapSize(); }

/* Get the maximum heap size */
/* If the user has set a maximum heap size using the SCALANATIVE_MAX_SIZE
 * environment variable,*/
/* then this size will be returned.*/
/* Otherwise, the total size of the physical memory (guarded) will be returned*/
size_t scalanative_get_max_heapsize() {
    return Parse_Env_Or_Default("SCALANATIVE_MAX_SIZE", Heap_getMemoryLimit());
}
