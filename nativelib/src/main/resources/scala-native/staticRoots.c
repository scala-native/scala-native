#include "staticSections.h"
#include "gc/shared/ScalaNativeGC.h"

/* Called by runtime startup after GC initialization, before Scala initializers.
 * Section discovery is independent of GC policy. Collectors implement the
 * ordinary add_roots API; a non-collecting GC can leave it as a no-op.
 */
SN_SECTION_LOCAL void scalanative_register_static_roots(void) {
    size_t count;
    const SNStaticSection *sections = scalanative_static_sections(&count);
    for (size_t i = 0; i < count; i++) {
        const SNStaticSection *section = &sections[i];
        if (section->start != section->end)
            scalanative_GC_add_roots(section->start, section->end);
    }
}
