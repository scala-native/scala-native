#ifdef SCALANATIVE_MULTITHREADING_ENABLED
#include "stdatomic.h"
#include "gc/shared/ScalaNativeGC.h"

#ifdef _WIN32
#include "windows.h"
#define YieldThread() SwitchToThread()
#else
#include "sched.h"
#define YieldThread() sched_yield()
#endif

#ifdef WIN32
#include <windows.h>
#elif _POSIX_C_SOURCE >= 199309L
#include <time.h> // for nanosleep
#else
#include <unistd.h> // for usleep
#endif

#include <assert.h>

// Thread identity helpers
#ifdef _WIN32
typedef DWORD thread_id;
#else
typedef pthread_t thread_id;
#endif

static thread_id getThreadId() {
#ifdef _WIN32
    return GetCurrentThreadId();
#else
    return pthread_self();
#endif
}
static bool isThreadEqual(thread_id l, thread_id r) {
#ifdef _WIN32
    return l == r;
#else
    return pthread_equal(l, r);
#endif
}

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
    thread_id initThreadId;
    ModuleRef instance;
} InitializationContext;

inline static ModuleRef waitForInitialization(ModuleSlot slot,
                                              void *classInfo) {
    int spin = 0;
    ModuleRef module = atomic_load_explicit(slot, memory_order_acquire);
    assert(module != NULL);
    while (*module != classInfo) {
        InitializationContext *ctx = (InitializationContext *)module;
        // Usage of module in it's constructor, return unitializied instance
        if (isThreadEqual(ctx->initThreadId, getThreadId())) {
            return ctx->instance;
        }
        if (spin++ < 32)
            YieldThread();
        else
            sleep_ms(1);
        scalanative_GC_yield();
        module = atomic_load_explicit(slot, memory_order_acquire);
    }
    return module;
}

ModuleRef __scalanative_loadModule(ModuleSlot slot, void *classInfo,
                                   size_t size, ModuleCtor ctor) {
    ModuleRef module = atomic_load_explicit(slot, memory_order_acquire);

    if (module == NULL) {
        InitializationContext ctx = {};
        void **expected = NULL;
        if (atomic_compare_exchange_strong(slot, &expected, (void **)&ctx)) {
            ModuleRef instance = scalanative_GC_alloc(classInfo, size);
            ctx.initThreadId = getThreadId();
            ctx.instance = instance;
            ctor(instance);
            atomic_store_explicit(slot, instance, memory_order_release);
            return instance;
        } else {
            return waitForInitialization(slot, classInfo);
        }
    }
    if (*module == classInfo)
        return module;
    else
        return waitForInitialization(slot, classInfo);
}

#endif
