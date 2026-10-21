#include "externalThreads.h"
#include "externalThreadGC.h"
#include <stdatomic.h>
#include <stdio.h>
#include <stdlib.h>

/* These Scala hooks are bootstrap entry points, exempt from automatic guards.
 * They are called only with collector admission and runtime ownership. */
extern int scalanative_externalThread_adopt(void);
extern int scalanative_externalThread_release(NativeThread thread);

typedef struct ExternalThread {
    void *identities[2]; // Java Thread, NativeThread; a contiguous GC root.
    ThreadInfo info;
    void *collectorContext;
    uintptr_t depth;
    bool internal;
    bool attached;
    bool bound;
    bool rooted;
    struct ExternalThread *next;
} ExternalThread;

static ExternalThread *allocateRecord(void) {
    ExternalThread *record = calloc(1, sizeof(ExternalThread));
    return record;
}

static void freeRecord(ExternalThread *record) { free(record); }

enum { RuntimeUninitialized, RuntimeInitializing, RuntimeReady, RuntimeFailed };
static atomic_int runtimeState;

static void onThreadExit(void *raw);
void scalanative_threads_init_failed(void);

#ifdef _WIN32
static INIT_ONCE storageOnce = INIT_ONCE_STATIC_INIT;
static DWORD storageKey = FLS_OUT_OF_INDEXES;
static void CALLBACK onWindowsThreadExit(void *raw) { onThreadExit(raw); }
static BOOL CALLBACK createStorage(PINIT_ONCE once, PVOID arg, PVOID *context) {
    (void)once;
    (void)arg;
    (void)context;
    storageKey = FlsAlloc(onWindowsThreadExit);
    return storageKey != FLS_OUT_OF_INDEXES;
}
static bool initializeStorage(void) {
    return InitOnceExecuteOnce(&storageOnce, createStorage, NULL, NULL) != 0;
}
static ExternalThread *currentRecord(void) {
    return (ExternalThread *)FlsGetValue(storageKey);
}
static bool setRecord(ExternalThread *record) {
    return FlsSetValue(storageKey, record) != 0;
}
#else
static pthread_once_t storageOnce = PTHREAD_ONCE_INIT;
static pthread_key_t storageKey;
static bool storageAvailable;
static void createStorage(void) {
    storageAvailable = pthread_key_create(&storageKey, onThreadExit) == 0;
}
static bool initializeStorage(void) {
    return pthread_once(&storageOnce, createStorage) == 0 && storageAvailable;
}
static ExternalThread *currentRecord(void) {
    return (ExternalThread *)pthread_getspecific(storageKey);
}
static bool setRecord(ExternalThread *record) {
    return pthread_setspecific(storageKey, record) == 0;
}
#endif

_Noreturn static void fatal(const char *reason) {
    fprintf(stderr, "Scala Native external thread: %s\n", reason);
    fflush(stderr);
    abort();
}

static void clearRecord(void) {
    if (!setRecord(NULL))
        fatal("thread storage cleanup failure");
}

#ifndef SCALANATIVE_MULTITHREADING_ENABLED
/* This synchronization is real even when SN_ThreadLocal is erased. The owner
 * is a persistent record, not a recyclable OS-thread identifier. */
#ifdef _WIN32
static SRWLOCK ownershipLock = SRWLOCK_INIT;
static CONDITION_VARIABLE ownershipChanged = CONDITION_VARIABLE_INIT;
static void lockOwnership(void) { AcquireSRWLockExclusive(&ownershipLock); }
static void unlockOwnership(void) { ReleaseSRWLockExclusive(&ownershipLock); }
static bool waitOwnership(void) {
    return SleepConditionVariableSRW(&ownershipChanged, &ownershipLock,
                                     INFINITE, 0) != 0;
}
static void notifyOwnership(void) {
    WakeAllConditionVariable(&ownershipChanged);
}
#else
static pthread_mutex_t ownershipLock = PTHREAD_MUTEX_INITIALIZER;
static pthread_cond_t ownershipChanged = PTHREAD_COND_INITIALIZER;
static void lockOwnership(void) {
    if (pthread_mutex_lock(&ownershipLock) != 0)
        fatal("ownership lock failure");
}
static void unlockOwnership(void) {
    if (pthread_mutex_unlock(&ownershipLock) != 0)
        fatal("ownership unlock failure");
}
static bool waitOwnership(void) {
    return pthread_cond_wait(&ownershipChanged, &ownershipLock) == 0;
}
static void notifyOwnership(void) { pthread_cond_broadcast(&ownershipChanged); }
#endif
static ExternalThread *owner;
#endif
static _Atomic(ExternalThread *) retired;

static void retireIdentity(ExternalThread *record) {
    ExternalThread *pending =
        atomic_load_explicit(&retired, memory_order_relaxed);
    do {
        record->next = pending;
    } while (!atomic_compare_exchange_weak_explicit(&retired, &pending, record,
                                                    memory_order_release,
                                                    memory_order_relaxed));
}

static int acquireOwnership(ExternalThread *record) {
#ifndef SCALANATIVE_MULTITHREADING_ENABLED
    lockOwnership();
    while (owner != NULL && owner != record) {
        if (!waitOwnership()) {
            unlockOwnership();
            return SCALANATIVE_THREAD_RESOURCE_FAILURE;
        }
    }
    owner = record;
    unlockOwnership();
#else
    (void)record;
#endif
    return SCALANATIVE_THREAD_SUCCESS;
}

static void releaseOwnership(ExternalThread *record) {
#ifndef SCALANATIVE_MULTITHREADING_ENABLED
    lockOwnership();
    if (owner != record)
        fatal("invalid ownership release");
    owner = NULL;
    notifyOwnership();
    unlockOwnership();
#else
    (void)record;
#endif
}

static void disposeIdentity(ExternalThread *record) {
    if (record->identities[1] != NULL &&
        scalanative_externalThread_release(record->identities[1]) != 0)
        fatal("identity cleanup failed");
    if (record->rooted)
        scalanative_GC_remove_roots(record->identities, record->identities + 2);
    freeRecord(record);
}

static void drainRetired(void) {
    ExternalThread *pending =
        atomic_exchange_explicit(&retired, NULL, memory_order_acquire);
    while (pending != NULL) {
        ExternalThread *next = pending->next;
        disposeIdentity(pending);
        pending = next;
    }
}

static void unbind(ExternalThread *record) {
    /* Both callers have already captured their stack and become unmanaged. */
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    (void)record;
#endif
#ifndef SCALANATIVE_MULTITHREADING_ENABLED
    /* Boehm's registration belongs to the OS thread. Immix/Commix keep the
     * singleton allocator while ownership moves; none has no tracing state. */
    scalanative_externalGC_unregister();
    record->bound = false;
    scalanative_assignCurrentThread(NULL, NULL);
    currentThreadInfo.isInitialized = false;
    releaseOwnership(record);
#endif
}

static int ensureRecord(ExternalThread **result) {
    ExternalThread *record = currentRecord();
    if (record == NULL) {
        record = allocateRecord();
        if (record == NULL)
            return SCALANATIVE_THREAD_RESOURCE_FAILURE;
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
        record->internal = scalanative_currentNativeThread() != NULL;
        if (record->internal) {
            record->bound = true;
            record->attached = true;
        }
#endif
        if (!record->internal &&
            !scalanative_detectExternalThreadStack(&record->info)) {
            freeRecord(record);
            return SCALANATIVE_THREAD_UNSUPPORTED_STACK;
        }
        if (!setRecord(record)) {
            freeRecord(record);
            return SCALANATIVE_THREAD_RESOURCE_FAILURE;
        }
    }
    *result = record;
    return SCALANATIVE_THREAD_SUCCESS;
}

static int bind(ExternalThread *record) {
    if (record->bound)
        return SCALANATIVE_THREAD_SUCCESS;
    currentThreadInfo = record->info;
    scalanative_assignCurrentThread(record->identities[0],
                                    record->identities[1]);
    bool registered = scalanative_externalGC_isRegistered();
    if (registered ? !scalanative_externalGC_rebind()
                   : !scalanative_externalGC_register()) {
        scalanative_assignCurrentThread(NULL, NULL);
        currentThreadInfo.isInitialized = false;
        return SCALANATIVE_THREAD_RESOURCE_FAILURE;
    }
    record->bound = true;
    record->collectorContext =
        scalanative_externalGC_saveContext(&record->info);
    if (!record->rooted) {
        if (!scalanative_externalGC_addRoots(record->identities,
                                             record->identities + 2))
            goto failure;
        record->rooted = true;
    }
    scalanative_externalGC_admit();
    if (record->identities[1] == NULL) {
        if (scalanative_externalThread_adopt() != 0)
            goto failure;
        record->identities[0] = scalanative_currentThread();
        record->identities[1] = scalanative_currentNativeThread();
    }
    currentThreadInfo.isInitialized = true;
    record->attached = true;
    drainRetired();
    scalanative_GC_set_mutator_thread_state(GC_MutatorThreadState_Unmanaged);
    return SCALANATIVE_THREAD_SUCCESS;

failure:
    if (!record->attached && record->identities[1] != NULL) {
        if (scalanative_externalThread_release(record->identities[1]) != 0)
            fatal("identity cleanup failed");
        record->identities[0] = record->identities[1] = NULL;
    }
    if (record->rooted && !record->attached) {
        scalanative_GC_remove_roots(record->identities, record->identities + 2);
        record->rooted = false;
    }
    scalanative_GC_set_mutator_thread_state(GC_MutatorThreadState_Unmanaged);
    scalanative_externalGC_unregister();
    record->bound = false;
    scalanative_assignCurrentThread(NULL, NULL);
    currentThreadInfo.isInitialized = false;
    return SCALANATIVE_THREAD_RESOURCE_FAILURE;
}

static int attach(ExternalThread **result) {
    if (atomic_load_explicit(&runtimeState, memory_order_acquire) !=
        RuntimeReady)
        return SCALANATIVE_THREAD_NOT_INITIALIZED;
    ExternalThread *record;
    int status = ensureRecord(&record);
    if (status != SCALANATIVE_THREAD_SUCCESS)
        return status;
    /* A bound record already owns the single-threaded runtime. Nested entry
     * needs neither ownership locking nor rebinding. */
    if (!record->bound) {
        status = acquireOwnership(record);
        if (status == SCALANATIVE_THREAD_SUCCESS) {
            status = bind(record);
            if (status != SCALANATIVE_THREAD_SUCCESS)
                releaseOwnership(record);
        }
    }
    if (status != SCALANATIVE_THREAD_SUCCESS) {
        if (!record->attached && !record->rooted) {
            clearRecord();
            freeRecord(record);
        }
        return status;
    }
    record->attached = true;
    *result = record;
    return SCALANATIVE_THREAD_SUCCESS;
}

static void destroyBound(ExternalThread *record) {
    scalanative_externalGC_admit();
    drainRetired();
    if (record->identities[1] != NULL &&
        scalanative_externalThread_release(record->identities[1]) != 0)
        fatal("identity cleanup failed");
    scalanative_GC_set_mutator_thread_state(GC_MutatorThreadState_Unmanaged);
    if (record->rooted)
        scalanative_GC_remove_roots(record->identities, record->identities + 2);
    scalanative_externalGC_unregister();
    scalanative_assignCurrentThread(NULL, NULL);
    currentThreadInfo.isInitialized = false;
    clearRecord();
    releaseOwnership(record);
    freeRecord(record);
}

scalanative_thread_scope_t scalanative_enter_managed_automatic(void) {
    ExternalThread *record;
    int status = attach(&record);
    if (status != SCALANATIVE_THREAD_SUCCESS) {
        switch (status) {
        case SCALANATIVE_THREAD_NOT_INITIALIZED:
            fatal("not initialized");
        case SCALANATIVE_THREAD_UNSUPPORTED_STACK:
            fatal("unsupported stack bounds");
        default:
            fatal("resource failure");
        }
    }
    if (record->depth == (UINTPTR_MAX >> 1))
        fatal("scope depth exhausted");
    GC_MutatorThreadState previous = scalanative_externalGC_state();
    scalanative_externalGC_admit();
    if (record->depth == 0)
        drainRetired();
    record->depth++;
    return (record->depth << 1) | (uintptr_t)previous;
}

void scalanative_leave_managed(scalanative_thread_scope_t scope) {
    ExternalThread *record = currentRecord();
    if (record == NULL || record->depth == 0 || (scope >> 1) != record->depth)
        fatal("invalid managed scope");
    if (record->depth == 1)
        drainRetired();
    GC_MutatorThreadState previous = (GC_MutatorThreadState)(scope & 1);
    if (previous == GC_MutatorThreadState_Managed)
        scalanative_externalGC_admit();
    else
        scalanative_GC_set_mutator_thread_state(previous);
    record->depth--;
    if (record->depth == 0 && !record->internal &&
        previous == GC_MutatorThreadState_Unmanaged)
        unbind(record);
}

static void onThreadExit(void *raw) {
    ExternalThread *record = raw;
    if (record == NULL)
        return;
    if (record->depth != 0)
        fatal("abandoned managed scopes");
    if (record->internal || !record->rooted) {
        freeRecord(record);
        return;
    }
    /* POSIX clears its key before invoking the destructor. Restore it while
     * cleanup runs so any nested runtime operation observes the same record. */
    if (!setRecord(record))
        fatal("thread exit storage failure");
    if (record->bound) {
        // Some platforms destroy compiler TLS before POSIX/FLS destructors.
        // Reinstall the retained context, never register a second mutator.
        currentThreadInfo = record->info;
        scalanative_assignCurrentThread(record->identities[0],
                                        record->identities[1]);
        if (!scalanative_externalGC_restoreContext(record->collectorContext))
            fatal("thread exit context restoration failed");
    }
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    /* A POSIX thread may no longer accept GC wakeup signals once its TLS
     * destructors run. Stay unmanaged and unregister its stack here; retire
     * Scala identities on the next admitted thread instead of executing Scala
     * on an exiting OS thread. Roots remain published until retirement. */
    scalanative_externalGC_unregister();
    scalanative_assignCurrentThread(NULL, NULL);
    currentThreadInfo.isInitialized = false;
    clearRecord();
    retireIdentity(record);
    return;
#else
    lockOwnership();
    if (owner != NULL && owner != record) {
        /* Never make an idle thread's join wait for another owner's Scala
         * frames. Keep its identity rooted until that owner can dispose it. */
        retireIdentity(record);
        unlockOwnership();
        clearRecord();
        return;
    }
    owner = record;
    unlockOwnership();
#endif
    if (bind(record) != SCALANATIVE_THREAD_SUCCESS)
        fatal("thread exit registration failure");
    destroyBound(record);
}

/* Called by generated bootstrap code, not by a host. */
int scalanative_threads_init_begin(bool library) {
    int expected = RuntimeUninitialized;
    if (!atomic_compare_exchange_strong(&runtimeState, &expected,
                                        RuntimeInitializing))
        return SCALANATIVE_THREAD_INVALID_OPERATION;
    ExternalThread *record = NULL;
    bool stored = false;
    if (!initializeStorage())
        goto failure;
    record = allocateRecord();
    if (record == NULL)
        goto failure;
    /* Executables set up their main-thread stack through runtime.init, which
     * also supports platforms without exact host-stack discovery. Only a
     * library must discover its loading thread's stack here. */
    if ((library && !scalanative_detectExternalThreadStack(&record->info)) ||
        !setRecord(record)) {
        goto failure;
    }
    stored = true;
    record->internal = !library;
    record->bound = true;
    record->attached = !library;
    currentThreadInfo = record->info;
    if (acquireOwnership(record) != SCALANATIVE_THREAD_SUCCESS)
        goto failure;
    return SCALANATIVE_THREAD_SUCCESS;
failure:
    if (stored)
        clearRecord();
    freeRecord(record);
    currentThreadInfo.isInitialized = false;
    atomic_store_explicit(&runtimeState, RuntimeFailed, memory_order_release);
    return SCALANATIVE_THREAD_RESOURCE_FAILURE;
}

int scalanative_threads_init_complete(bool library) {
    ExternalThread *record = currentRecord();
    if (record == NULL || atomic_load(&runtimeState) != RuntimeInitializing)
        return 1;
    if (library) {
        record->info = currentThreadInfo;
        /* Keep identities in the captured stack until the persistent roots
         * are published. No managed code may run after this transition: a
         * collector can hold its lifecycle lock while waiting for us. */
        void *volatile identities[2] = {scalanative_currentThread(),
                                        scalanative_currentNativeThread()};
        scalanative_GC_set_mutator_thread_state(
            GC_MutatorThreadState_Unmanaged);
        record->collectorContext =
            scalanative_externalGC_saveContext(&record->info);
        record->identities[0] = identities[0];
        record->identities[1] = identities[1];
        if (!scalanative_externalGC_addRoots(record->identities,
                                             record->identities + 2)) {
            scalanative_threads_init_failed();
            return 1;
        }
        record->rooted = true;
        unbind(record);
    }
    atomic_store_explicit(&runtimeState, RuntimeReady, memory_order_release);
    return 0;
}

void scalanative_threads_init_failed(void) {
    int previous = atomic_exchange_explicit(&runtimeState, RuntimeFailed,
                                            memory_order_acq_rel);
    if (previous != RuntimeInitializing)
        return;
    ExternalThread *record = currentRecord();
    if (record == NULL)
        return;
    if (scalanative_externalGC_isRegistered()) {
        scalanative_GC_set_mutator_thread_state(
            GC_MutatorThreadState_Unmanaged);
        scalanative_externalGC_unregister();
    }
    if (record->rooted)
        scalanative_GC_remove_roots(record->identities, record->identities + 2);
    scalanative_assignCurrentThread(NULL, NULL);
    currentThreadInfo.isInitialized = false;
    clearRecord();
    releaseOwnership(record);
    freeRecord(record);
}
