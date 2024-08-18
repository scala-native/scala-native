#ifndef SCALA_NATIVE_GC_H
#define SCALA_NATIVE_GC_H
#include <stdlib.h>
#include <stdbool.h>
#include "shared/GCTypes.h"
#include "immix_commix/headers/ObjectHeader.h"

#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
// Boehm on Windows needs User32.lib linked
#pragma comment(lib, "user32.lib")
#pragma comment(lib, "kernel32.lib")
#include <windows.h>
typedef DWORD ThreadRoutineReturnType;
#else
#include <pthread.h>
typedef void *ThreadRoutineReturnType;
#endif

typedef ThreadRoutineReturnType (*ThreadStartRoutine)(void *);
typedef void *RoutineArgs;

void scalanative_GC_init();
void *scalanative_GC_alloc(Rtti *info, size_t size);
void *scalanative_GC_alloc_small(Rtti *info, size_t size);
void *scalanative_GC_alloc_large(Rtti *info, size_t size);
/* Allocate an array with capacity of `length` elements of element size equal to
 * `stride`. Total ammount of allocated memory should be at least equal to
 * `info->rtti + length * stride`. After successful allocation GC is
 * responsible to assign length and stride to Array header. */
void *scalanative_GC_alloc_array(Rtti *info, size_t length, size_t stride);
void scalanative_GC_collect();

typedef void (*WeakReferencesCollectedCallback)();
void scalanative_GC_set_weak_references_collected_callback(
    WeakReferencesCollectedCallback);

size_t scalanative_GC_get_init_heapsize();
size_t scalanative_GC_get_max_heapsize();
size_t scalanative_GC_get_used_heapsize();

// Functions used to create a new thread supporting multithreading support in
// the garbage collector. Would execute a proxy startup routine to register
// newly created thread upon startup and unregister it from the GC upon
// termination.
#ifdef _WIN32
HANDLE scalanative_GC_CreateThread(LPSECURITY_ATTRIBUTES threadAttributes,
                                   SIZE_T stackSize, ThreadStartRoutine routine,
                                   RoutineArgs args, DWORD creationFlags,
                                   DWORD *threadId);
#else
int scalanative_GC_pthread_create(pthread_t *thread, pthread_attr_t *attr,
                                  ThreadStartRoutine routine, RoutineArgs args);
#endif

// Current type of execution by given threadin foreign scope be included in the
// stop-the-world mechanism, as they're assumed to not modify the state of the
// GC. Upon conversion from Managed to Unmanged state calling thread shall dump
// the contents of the register to the stack and save the top address of the
// stack.
typedef enum scalanative_GC_MutatorThreadState {
    /*  Thread executes Scala Native code using GC following cooperative mode -
     *  it periodically polls for synchronization events.
     */
    GC_MutatorThreadState_Managed = 0,
    /*  Thread executes foreign code (syscalls, C functions) and is not able to
     *  modify the state of the GC. Upon synchronization event garbage collector
     *  would ignore this thread. Upon returning from foreign execution thread
     *  would stop until synchronization event would finish.
     */
    GC_MutatorThreadState_Unmanaged = 1
} GC_MutatorThreadState;

// Receiver for notifications on entering/exiting potentially blocking extern
// functions. Changes the internal state of current (calling) thread
void scalanative_GC_set_mutator_thread_state(GC_MutatorThreadState);

// Check for StopTheWorld event and wait for its end if needed
void scalanative_GC_yield();
#ifdef SCALANATIVE_GC_USE_YIELDPOINT_TRAPS
// Conditionally protected memory address used for STW events polling
// Scala Native compiler would introduce load/store operations to location
// pointed by this field. Under active StopTheWorld event these would trigger
// thread execution suspension via exception handling mechanism
// (signals/exceptionHandler)
extern void **scalanative_GC_yieldpoint_trap;
#endif

void scalanative_GC_add_roots(void *addr_low, void *addr_high);
void scalanative_GC_remove_roots(void *addr_low, void *addr_high);

#endif // SCALA_NATIVE_GC_H
