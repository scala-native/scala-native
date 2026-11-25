#ifndef SCALANATIVE_STATIC_ROOTS_H
#define SCALANATIVE_STATIC_ROOTS_H

#include "staticSections.h"
#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

/* Registers the global objects section, and the inflated lock words of
 * static objects, as GC roots. Called after GC initialization, before Scala
 * initializers.
 */
SN_SECTION_LOCAL void scalanative_register_static_roots(void);

/* Called when a monitor is inflated on `object`. A static object in the class
 * or string sections then references the heap only through its lock word,
 * which becomes a GC root. Other objects are ignored. Objects have no lock
 * word without multithreading.
 */
SN_SECTION_LOCAL void scalanative_register_object_roots(void *object);

/* Zeroed, never freed memory for a runtime class; NULL when out of memory. */
SN_SECTION_LOCAL void *scalanative_alloc_runtime_class(size_t size);

SN_SECTION_LOCAL size_t scalanative_object_array_class_size(void);

#ifdef __cplusplus
}
#endif

#endif
