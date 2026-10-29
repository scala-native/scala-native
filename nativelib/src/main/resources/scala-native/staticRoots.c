#include "staticSections.h"
#include "gc/shared/ScalaNativeGC.h"
#include <stdbool.h>
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

static void registerClassSection(void) {
    registerSection(scalanative_static_classinfo_section());
}

static void registerStringSection(void) {
    registerSection(scalanative_static_strings_section());
}

/* Finish registration before any caller publishes a managed reference in static
 * storage. ClassValue can also use the class registration entry point.
 */
#ifndef SCALANATIVE_MULTITHREADING_ENABLED
static bool classRootsRegistered;
static bool stringRootsRegistered;

SN_SECTION_LOCAL void scalanative_register_class_roots(void) {
    if (!classRootsRegistered) {
        registerClassSection();
        classRootsRegistered = true;
    }
}
SN_SECTION_LOCAL void scalanative_register_string_roots(void) {
    if (!stringRootsRegistered) {
        registerStringSection();
        stringRootsRegistered = true;
    }
}
#elif defined(_WIN32)
static INIT_ONCE classRootsOnce = INIT_ONCE_STATIC_INIT;
static INIT_ONCE stringRootsOnce = INIT_ONCE_STATIC_INIT;

static BOOL CALLBACK registerClassRoots(PINIT_ONCE once, PVOID arg,
                                        PVOID *context) {
    (void)once;
    (void)arg;
    (void)context;
    registerClassSection();
    return TRUE;
}

static BOOL CALLBACK registerStringRoots(PINIT_ONCE once, PVOID arg,
                                         PVOID *context) {
    (void)once;
    (void)arg;
    (void)context;
    registerStringSection();
    return TRUE;
}

SN_SECTION_LOCAL void scalanative_register_class_roots(void) {
    if (!InitOnceExecuteOnce(&classRootsOnce, registerClassRoots, NULL, NULL))
        abort();
}
SN_SECTION_LOCAL void scalanative_register_string_roots(void) {
    if (!InitOnceExecuteOnce(&stringRootsOnce, registerStringRoots, NULL, NULL))
        abort();
}
#else
static pthread_once_t classRootsOnce = PTHREAD_ONCE_INIT;
static pthread_once_t stringRootsOnce = PTHREAD_ONCE_INIT;

SN_SECTION_LOCAL void scalanative_register_class_roots(void) {
    if (pthread_once(&classRootsOnce, registerClassSection) != 0)
        abort();
}

SN_SECTION_LOCAL void scalanative_register_string_roots(void) {
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
        scalanative_register_class_roots();
    else if (containsObject(scalanative_static_strings_section(), object))
        scalanative_register_string_roots();
}
