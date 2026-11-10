#include "staticSections.h"
#include "gc/shared/ScalaNativeGC.h"
#include <stdatomic.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdlib.h>

#ifdef SCALANATIVE_MULTITHREADING_ENABLED
#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#else
#include <pthread.h>
#endif
#endif

static void registerSection(const SNStaticSection *section) {
    if (section->start != section->end)
        scalanative_GC_add_roots(section->start, section->end);
}

/* Called after GC initialization, before Scala initializers. Class and string
 * sections are registered lazily when their monitors first inflate.
 */
SN_SECTION_LOCAL void scalanative_register_static_roots(void) {
    registerSection(scalanative_static_globals_section());
}

/* Class monitors are found through the class list, see
 * scalanative_foreach_class_lock_word, instead of scanning every class. */
static atomic_bool classMonitorsInflated;

static void enableClassMonitorRoots(void) {
    atomic_store_explicit(&classMonitorsInflated, true, memory_order_release);
}

SN_SECTION_LOCAL void
scalanative_foreach_class_lock_word(SNLockWordVisitor visit, void *context) {
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    if (!atomic_load_explicit(&classMonitorsInflated, memory_order_acquire))
        return;
    const SNStaticSection *classes = scalanative_static_classlist_section();
    for (void **entry = classes->start; entry < (void **)classes->end;
         entry++) {
        void **rtti = (void **)*entry;
        if (rtti == NULL)
            continue;
        // Object header: the RTTI of java.lang.Class, then the lock word.
        // Only inflated lock words point to a monitor, thin locks hold none.
        void **lockWord = &rtti[1];
        if (((word_t)*lockWord & MONITOR_INFLATION_MARK_MASK) != 0)
            visit(lockWord, context);
    }
#else
    (void)visit;
    (void)context;
#endif
}

static void registerStringSection(void) {
    registerSection(scalanative_static_strings_section());
}

/* Finish registration before any caller publishes a managed reference in static
 * storage.
 */
#ifndef SCALANATIVE_MULTITHREADING_ENABLED
static bool classRootsRegistered;
static bool stringRootsRegistered;

static void registerClassRoots(void) {
    if (!classRootsRegistered) {
        enableClassMonitorRoots();
        classRootsRegistered = true;
    }
}
static void registerStringRoots(void) {
    if (!stringRootsRegistered) {
        registerStringSection();
        stringRootsRegistered = true;
    }
}
#elif defined(_WIN32)
static INIT_ONCE classRootsOnce = INIT_ONCE_STATIC_INIT;
static INIT_ONCE stringRootsOnce = INIT_ONCE_STATIC_INIT;

static BOOL CALLBACK registerClassRootsOnce(PINIT_ONCE once, PVOID arg,
                                            PVOID *context) {
    (void)once;
    (void)arg;
    (void)context;
    enableClassMonitorRoots();
    return TRUE;
}

static BOOL CALLBACK registerStringRootsOnce(PINIT_ONCE once, PVOID arg,
                                             PVOID *context) {
    (void)once;
    (void)arg;
    (void)context;
    registerStringSection();
    return TRUE;
}

static void registerClassRoots(void) {
    if (!InitOnceExecuteOnce(&classRootsOnce, registerClassRootsOnce, NULL,
                             NULL))
        abort();
}
static void registerStringRoots(void) {
    if (!InitOnceExecuteOnce(&stringRootsOnce, registerStringRootsOnce, NULL,
                             NULL))
        abort();
}
#else
static pthread_once_t classRootsOnce = PTHREAD_ONCE_INIT;
static pthread_once_t stringRootsOnce = PTHREAD_ONCE_INIT;

static void registerClassRoots(void) {
    if (pthread_once(&classRootsOnce, enableClassMonitorRoots) != 0)
        abort();
}

static void registerStringRoots(void) {
    if (pthread_once(&stringRootsOnce, registerStringSection) != 0)
        abort();
}
#endif

static bool containsObject(const SNStaticSection *section, void *object) {
    uintptr_t address = (uintptr_t)object;
    return address >= (uintptr_t)section->start &&
           address < (uintptr_t)section->end;
}

/* Heap objects and unrelated static storage do not activate section scanning.
 * This works independently of the collector's heap layout or allocation API.
 */
SN_SECTION_LOCAL void scalanative_register_object_roots(void *object) {
    if (containsObject(scalanative_static_classinfo_section(), object))
        registerClassRoots();
    else if (containsObject(scalanative_static_strings_section(), object))
        registerStringRoots();
}
