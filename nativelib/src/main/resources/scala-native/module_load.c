#ifdef SCALANATIVE_MULTITHREADING_ENABLED
#include "stdatomic.h"
#include <stdint.h>
#include "gc/shared/ScalaNativeGC.h"
#include "gc/shared/ThreadUtil.h"

#ifdef WIN32
#include <windows.h>
#elif _POSIX_C_SOURCE >= 199309L
#include <time.h> // for nanosleep
#else
#include <unistd.h> // for usleep
#endif

#include <assert.h>

// cross-platform sleep function
static void sleep_ms(int milliseconds) {
#ifdef WIN32
    Sleep(milliseconds);
#elif _POSIX_C_SOURCE >= 199309L
    struct timespec ts;
    ts.tv_sec = milliseconds / 1000;
    ts.tv_nsec = (milliseconds % 1000) * 1000000;
    nanosleep(&ts, NULL);
#else
    if (milliseconds >= 1000)
        sleep(milliseconds / 1000);
    usleep((milliseconds % 1000) * 1000);
#endif
}

typedef _Atomic(void **) ModuleRef;
typedef ModuleRef *ModuleSlot;
typedef void (*ModuleCtor)(ModuleRef);
typedef struct InitializationContext {
    ModuleRef instance;
    thread_id initThreadId;
} InitializationContext;

// Module instances are aligned pointers. Tag the otherwise stack-local
// context so readers never need to dereference it to distinguish it from an
// initialized module.
#define INITIALIZATION_CONTEXT_TAG ((uintptr_t)1)

static void **initializationContextRef(InitializationContext *ctx) {
    return (void **)((uintptr_t)ctx | INITIALIZATION_CONTEXT_TAG);
}

static bool isInitializationContext(ModuleRef module) {
    return ((uintptr_t)module & INITIALIZATION_CONTEXT_TAG) != 0;
}

static InitializationContext *initializationContext(ModuleRef module) {
    return (InitializationContext *)((uintptr_t)module &
                                     ~INITIALIZATION_CONTEXT_TAG);
}

extern ModuleRef scalanative_initializeModule(ModuleCtor ctor,
                                              ModuleRef instance,
                                              ModuleSlot slot, void *classInfo);
extern ModuleRef scalanative_awaitForInitialization(ModuleSlot slot,
                                                    void *classInfo);

// This is called while holding classInfo's monitor. If another thread owns the
// initialization, it cannot return from startAndWait... (and invalidate ctx)
// until it has published the initialized module and released that monitor.
bool scalanative_isModuleInitializationContext(ModuleRef module) {
    return isInitializationContext(module);
}

ModuleRef
scalanative_moduleInitializationInstanceForCurrentThread(ModuleRef module) {
    InitializationContext *ctx = initializationContext(module);
    return thread_equals(ctx->initThreadId, thread_getid()) ? ctx->instance
                                                            : NULL;
}

NOINLINE static ModuleRef
__scalanative_waitForModuleInitialization(ModuleSlot slot, void *classInfo) {
    // Do not dereference a published stack-local InitializationContext here.
    // A competing initializer can replace the slot and return before this
    // thread gets to inspect it. awaitForInitialization rechecks the slot
    // under the class monitor, where only the reentrant owner may use ctx.
    ModuleRef module = atomic_load_explicit(slot, memory_order_acquire);
    ModuleRef instance =
        scalanative_moduleInitializationInstanceForCurrentThread(module);
    if (instance != NULL)
        return instance;
    return scalanative_awaitForInitialization(slot, classInfo);
}

NOINLINE static ModuleRef __scalanative_startAndWaitForModuleInitialization(
    ModuleSlot slot, void *classInfo, size_t size, ModuleCtor ctor) {
    InitializationContext ctx = {};
    // Populate the context before publishing &ctx into the slot. The winning
    // atomic_compare_exchange_strong is a release operation, so any thread that
    // later acquire-loads the slot and observes &ctx also observes the fully
    // written fields. Writing initThreadId and instance after the publish (the
    // previous ordering) left a window in which another thread could read them
    // before they were set, a data race on those fields.
    ctx.initThreadId = thread_getid();
    ModuleRef instance = scalanative_GC_alloc(classInfo, size);
    ctx.instance = instance;
    void **expected = NULL;
    if (atomic_compare_exchange_strong(slot, &expected,
                                       initializationContextRef(&ctx))) {
        return scalanative_initializeModule(ctor, instance, slot, classInfo);
    } else {
        return __scalanative_waitForModuleInitialization(slot, classInfo);
    }
}

/* Load module, if required.  The fast path is inlined into the caller. While
 * the slow path not-inlined to avoid pressure on the instruction cache.
 */
INLINE ModuleRef __scalanative_loadModule(ModuleSlot slot, void *classInfo,
                                          size_t size, ModuleCtor ctor) {
    ModuleRef module = atomic_load_explicit(slot, memory_order_acquire);

    if (module == NULL)
        return __scalanative_startAndWaitForModuleInitialization(
            slot, classInfo, size, ctor);

    if (!isInitializationContext(module) && *module == classInfo)
        return module;
    else
        return __scalanative_waitForModuleInitialization(slot, classInfo);
}

#endif
