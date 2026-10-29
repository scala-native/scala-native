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

/* Called after GC initialization, before Scala initializers. Strings cannot
 * inflate their monitors and do not need roots. Classes are registered lazily.
 */
SN_SECTION_LOCAL void scalanative_register_static_roots(void) {
    registerSection(scalanative_static_globals_section());
}

static void registerClassSection(void) {
    registerSection(scalanative_static_classinfo_section());
}

/* Finish registration before any caller publishes a managed reference in class
 * storage. Shared by class monitor inflation and future ClassValue storage.
 */
#ifndef SCALANATIVE_MULTITHREADING_ENABLED
static bool classRootsRegistered;

SN_SECTION_LOCAL void scalanative_register_class_roots(void) {
    if (!classRootsRegistered) {
        registerClassSection();
        classRootsRegistered = true;
    }
}
#elif defined(_WIN32)
static INIT_ONCE classRootsOnce = INIT_ONCE_STATIC_INIT;

static BOOL CALLBACK registerClassRoots(PINIT_ONCE once, PVOID arg,
                                        PVOID *context) {
    (void)once;
    (void)arg;
    (void)context;
    registerClassSection();
    return TRUE;
}

SN_SECTION_LOCAL void scalanative_register_class_roots(void) {
    if (!InitOnceExecuteOnce(&classRootsOnce, registerClassRoots, NULL, NULL))
        abort();
}
#else
static pthread_once_t classRootsOnce = PTHREAD_ONCE_INIT;

SN_SECTION_LOCAL void scalanative_register_class_roots(void) {
    if (pthread_once(&classRootsOnce, registerClassSection) != 0)
        abort();
}
#endif
