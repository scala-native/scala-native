#ifndef SCALANATIVE_STATIC_ROOTS_H
#define SCALANATIVE_STATIC_ROOTS_H

#include "staticSections.h"

#ifdef __cplusplus
extern "C" {
#endif

/* Registers the global objects section as GC roots. Called after GC
 * initialization, before Scala initializers.
 */
SN_SECTION_LOCAL void scalanative_register_static_roots(void);

/* Called when a monitor is inflated on `object`. A static object in the class
 * or string sections then references the heap through its lock word, which
 * becomes a GC root. Other objects are ignored.
 */
SN_SECTION_LOCAL void scalanative_register_object_roots(void *object);

/* Calls `visit` with the address of every inflated lock word of a static
 * object in the class or string sections: these point to heap allocated
 * monitors, the only references such objects hold to the heap. Garbage
 * collectors call it while marking roots. Objects have no lock word without
 * multithreading.
 */
typedef void (*SNLockWordVisitor)(void **lockWord, void *context);
SN_SECTION_LOCAL void
scalanative_foreach_static_lock_word(SNLockWordVisitor visit, void *context);

#ifdef __cplusplus
}
#endif

#endif
