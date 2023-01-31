#ifdef SCALANATIVE_MULTITHREADING_ENABLED
#include "stdatomic.h"
#include "nativeThreadTLS.h"
#include "gc/shared/ScalaNativeGC.h"

#ifdef _WIN32
#include "windows.h"
#define YieldThread() SwitchToThread()
#else
#include "sched.h"
#define YieldThread() sched_yield()
#endif

typedef _Atomic(void **) ModuleRef;
typedef ModuleRef *ModuleSlot;
typedef void (*ModuleCtor)(ModuleRef);
typedef struct InitializationContext {
    NativeThread thread;
    ModuleRef instance;
} InitializationContext;

ModuleRef __scalanative_loadModule(ModuleSlot slot, void *classInfo,
                                   size_t size, ModuleCtor ctor) {
    ModuleRef module = atomic_load_explicit(slot, memory_order_acquire);

    if (module == NULL) {
        InitializationContext ctx = {};
        void **expected = NULL;
        if (atomic_compare_exchange_strong(slot, &expected, (void **)&ctx)) {
            ModuleRef instance = scalanative_alloc(classInfo, size);
            ctx.thread = currentNativeThread;
            ctx.instance = instance;
            ctor(instance);
            atomic_store_explicit(slot, instance, memory_order_release);
            return instance;
        }
    }

    // Wait in loop until initialization is finished
    while (*module != classInfo) {
        InitializationContext *ctx = (InitializationContext *)module;
        // Usage of module in it's constructor, return unitializied instance
        // thread=null can happen only in the main thread initialization
        if (ctx->thread == currentNativeThread || ctx->thread == NULL) {
            return ctx->instance;
        }
        YieldThread();
        scalanative_gcYield();
        module = atomic_load_explicit(slot, memory_order_acquire);
    }
    return module;
}
#endif