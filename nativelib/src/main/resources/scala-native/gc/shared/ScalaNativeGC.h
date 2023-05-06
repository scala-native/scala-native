#ifndef SCALA_NATIVE_GC_H
#define SCALA_NATIVE_GC_H
#include <stdlib.h>
#include <stdbool.h>
#include "GCTypes.h"

#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
// Boehm on Windows needs User32.lib linked
#pragma comment(lib, "User32.lib")
#pragma comment(lib, "Kernel32.lib")
#include <windows.h>
typedef DWORD ThreadRoutineReturnType;
#else
#include <pthread.h>
typedef void *ThreadRoutineReturnType;
#endif

typedef ThreadRoutineReturnType (*ThreadStartRoutine)(void *);
typedef void *RoutineArgs;

void scalanative_init();
void *scalanative_alloc(void *info, size_t size);
void *scalanative_alloc_small(void *info, size_t size);
void *scalanative_alloc_large(void *info, size_t size);
void *scalanative_alloc_atomic(void *info, size_t size);
void scalanative_collect();
void scalanative_register_weak_reference_handler(void *handler);
size_t scalanative_get_init_heapsize();
size_t scalanative_get_max_heapsize();

// Functions used to create a new thread supporting multithreading support in
// the garbage collector. Would execute a proxy startup routine to register
// newly created thread upon startup and unregister it from the GC upon
// termination.
#ifdef _WIN32
HANDLE scalanative_CreateThread(LPSECURITY_ATTRIBUTES threadAttributes,
                                SIZE_T stackSize, ThreadStartRoutine routine,
                                RoutineArgs args, DWORD creationFlags,
                                DWORD *threadId);
#else
int scalanative_pthread_create(pthread_t *thread, pthread_attr_t *attr,
                               ThreadStartRoutine routine, RoutineArgs args);
#endif

// Current type of execution by given threadin foreign scope be included in the
// stop-the-world mechanism, as they're assumed to not modify the state of the
// GC. Upon conversion from Managed to Unmanged state calling thread shall dump
// the contents of the register to the stack and save the top address of the
// stack.
typedef enum scalanative_MutatorThreadState {
    /*  Thread executes Scala Native code using GC following cooperative mode -
     *  it periodically polls for synchronization events.
     */
    MutatorThreadState_Managed = 0,
    /*  Thread executes foreign code (syscalls, C functions) and is not able to
     *  modify the state of the GC. Upon synchronization event garbage collector
     *  would ignore this thread. Upon returning from foreign execution thread
     *  would stop until synchronization event would finish.
     */
    MutatorThreadState_Unmanaged = 1
} MutatorThreadState;

// Receiver for notifications on entering/exiting potentially blocking extern
// functions. Changes the internal state of current (calling) thread
void scalanative_gc_set_mutator_thread_state(MutatorThreadState);

// Conditionally protected memory address used for STW events polling
typedef void **safepoint_t;
extern safepoint_t scalanative_gc_safepoint;

// Check for StopTheWorld event and wait for its end if needed
// Used internally only in GC. Scala Native safepoints polling would be inlined
// in the code.
void scalanative_gc_safepoint_poll();

#endif // SCALA_NATIVE_GC_H
