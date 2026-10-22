#if defined(SCALANATIVE_GC_BOEHM)
// Native hosts can create OS workers even in single-threaded Scala builds.
#define GC_THREADS

#include <gc/gc.h>
#include "shared/ScalaNativeGC.h"
#include <stdlib.h>
#include <string.h>
#include "shared/Parsing.h"
#include "shared/Time.h"
#include "shared/jmx.h"
#include "nativeThreadTLS.h"
#include "externalThreadGC.h"

static SN_ThreadLocal GC_MutatorThreadState executionState =
    GC_MutatorThreadState_Managed;

// At the moment we rely on the conservative
// mode of Boehm GC as our garbage collector.
static WeakReferencesCollectedCallback weakReferencesCollectedCallback = NULL;
static void GC_CALLBACK handleOnCollectionEvent(GC_EventType event);
static void GC_CALLBACK weakRefFinalizer(void *obj, void *client_data);

void scalanative_GC_init() {
    GC_INIT();
    GC_allow_register_threads();
    // Keep this hook enabled at all times to collect Boehm GC statistics.
    GC_set_on_collection_event(handleOnCollectionEvent);
    // Drive Java-side weak reference processing through Boehm finalizers.
    GC_set_java_finalization(1);
}

void *scalanative_GC_alloc(Rtti *info, size_t size) {
    Object *alloc = (Object *)GC_malloc(size);
    alloc->rtti = info;
    return (void *)alloc;
}

void *scalanative_GC_alloc_small(Rtti *info, size_t size) {
    Object *alloc = (Object *)GC_malloc(size);
    alloc->rtti = info;
    return (void *)alloc;
}

void *scalanative_GC_alloc_large(Rtti *info, size_t size) {
    Object *alloc = (Object *)GC_malloc(size);
    alloc->rtti = info;
    return (void *)alloc;
}

void *scalanative_GC_alloc_array(Rtti *info, size_t length, size_t stride) {
    size_t size = info->size + length * stride;
    ArrayHeader *alloc;
    int32_t classId = info->rt.id;
    if (classId == __object_array_id || classId == __blob_array_id)
        alloc = (ArrayHeader *)GC_malloc(size);
    else
        alloc = (ArrayHeader *)GC_malloc_atomic(size);
    memset(alloc, 0, size);
    alloc->rtti = info;
    alloc->length = length;
    alloc->stride = stride;
    return (void *)alloc;
}

size_t scalanative_GC_get_init_heapsize() {
    return Parse_Env_Or_Default("GC_INITIAL_HEAP_SIZE", 0L);
}

size_t scalanative_GC_get_max_heapsize() {
    struct GC_prof_stats_s *stats =
        (struct GC_prof_stats_s *)malloc(sizeof(struct GC_prof_stats_s));
    GC_get_prof_stats(stats, sizeof(struct GC_prof_stats_s));
    size_t heap_sz = stats->heapsize_full;
    free(stats);
    return Parse_Env_Or_Default("GC_MAXIMUM_HEAP_SIZE", heap_sz);
}

size_t scalanative_GC_get_used_heapsize() {
    struct GC_prof_stats_s stats = {};
    GC_get_prof_stats(&stats, sizeof(struct GC_prof_stats_s));
    size_t heap_sz = stats.heapsize_full;
    size_t unmapped_bytes = stats.unmapped_bytes;
    return heap_sz - unmapped_bytes;
}

size_t scalanative_GC_stats_collection_total() {
    return jmx_stats_get_collection_total();
}

size_t scalanative_GC_stats_collection_duration_total() {
    return jmx_stats_get_collection_duration_total();
}

void scalanative_GC_collect() { GC_gcollect(); }

void scalanative_GC_set_weak_references_collected_callback(
    WeakReferencesCollectedCallback callback) {
    weakReferencesCollectedCallback = callback;
}

void *scalanative_GC_weak_ref_slot_create(void *referent) {
    void **slot = (void **)GC_malloc_atomic(sizeof(void *));
    if (slot == NULL)
        return NULL;

    *slot = referent;

    if (referent != NULL) {
        GC_general_register_disappearing_link(slot, referent);
        GC_register_finalizer_no_order(referent, weakRefFinalizer, NULL, NULL,
                                       NULL);
    }
    return (void *)slot;
}

void *scalanative_GC_weak_ref_slot_get(void *slot) {
    if (slot == NULL)
        return NULL;
    return *((void **)slot);
}

void scalanative_GC_weak_ref_slot_clear(void *slot) {
    if (slot == NULL)
        return;

    GC_unregister_disappearing_link((void **)slot);
    *((void **)slot) = NULL;
}

static void GC_CALLBACK handleOnCollectionEvent(GC_EventType event) {
    static volatile size_t gcCollectionStart_ns = -1L;
    switch (event) {
    case GC_EVENT_START:
        gcCollectionStart_ns = (size_t)Time_current_nanos();
        break;

    case GC_EVENT_END: {
        if (gcCollectionStart_ns > 0) {
            size_t end_ns = (size_t)Time_current_nanos();
            jmx_stats_record_collection(gcCollectionStart_ns, end_ns);
            gcCollectionStart_ns = 0;
        }
        break;
    }

    default:
        break;
    }
}

static void GC_CALLBACK weakRefFinalizer(void *obj, void *client_data) {
    (void)obj;
    (void)client_data;
    if (weakReferencesCollectedCallback != NULL &&
        currentThreadInfo.isInitialized) {
        weakReferencesCollectedCallback();
    }
}

#ifdef SCALANATIVE_MULTITHREADING_ENABLED
#ifdef _WIN32
HANDLE scalanative_GC_CreateThread(LPSECURITY_ATTRIBUTES threadAttributes,
                                   SIZE_T stackSize, ThreadStartRoutine routine,
                                   RoutineArgs args, DWORD creationFlags,
                                   DWORD *threadId) {
    return GC_CreateThread(threadAttributes, stackSize, routine, args,
                           creationFlags, threadId);
}
#else
int scalanative_GC_pthread_create(pthread_t *thread, pthread_attr_t *attr,
                                  ThreadStartRoutine routine,
                                  RoutineArgs args) {
    return GC_pthread_create(thread, attr, routine, args);
}
#endif
#endif // SCALANATIVE_MULTITHREADING_ENABLED

// ScalaNativeGC interface stubs. Boehm GC relies on STW using signal handlers
void scalanative_GC_set_mutator_thread_state(GC_MutatorThreadState state) {
    executionState = state;
}

bool scalanative_externalGC_isRegistered(void) {
    return GC_thread_is_registered() != 0;
}
bool scalanative_externalGC_register(void) {
    struct GC_stack_base base;
    if (GC_get_stack_base(&base) != GC_SUCCESS)
        return false;
    base.mem_base = currentThreadInfo.stackBottom;
    int status = GC_register_my_thread(&base);
    executionState = GC_MutatorThreadState_Unmanaged;
    return status == GC_SUCCESS || status == GC_DUPLICATE;
}
bool scalanative_externalGC_rebind(void) {
    // Boehm registration belongs to the OS thread. An existing registration
    // already describes this thread's stack; a new owner registers its own.
    return scalanative_externalGC_isRegistered() ||
           scalanative_externalGC_register();
}
void scalanative_externalGC_unregister(void) { GC_unregister_my_thread(); }
void *scalanative_externalGC_saveContext(ThreadInfo *info) {
    (void)info;
    return (void *)(uintptr_t)1;
}
bool scalanative_externalGC_restoreContext(void *context) {
    (void)context;
    executionState = GC_MutatorThreadState_Unmanaged;
    return scalanative_externalGC_isRegistered() ||
           scalanative_externalGC_register();
}
void scalanative_externalGC_admit(void) {
    executionState = GC_MutatorThreadState_Managed;
}
GC_MutatorThreadState scalanative_externalGC_state(void) {
    return executionState;
}
bool scalanative_externalGC_addRoots(void *low, void *high) {
    GC_add_roots(low, high);
    return true;
}

void scalanative_GC_yield() {}

void scalanative_GC_add_roots(void *addr_low, void *addr_high) {
    GC_add_roots(addr_low, addr_high);
}

void scalanative_GC_remove_roots(void *addr_low, void *addr_high) {
    GC_remove_roots(addr_low, addr_high);
}
#endif
