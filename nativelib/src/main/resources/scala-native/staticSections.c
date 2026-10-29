#include "staticSections.h"

/* Keep this list in sync with codegen.GlobalSections.
 * Mach-O needs a shorter alias for sn_global_objects because section names are
 * limited to 16 bytes.
 */
#define STATIC_SECTIONS(SECTION)                                               \
    SECTION(strings, "sn_strings", "__sn_strings", ".snstr")                   \
    SECTION(classinfo, "sn_classinfo", "__sn_classinfo", ".sncls")             \
    SECTION(globals, "sn_global_objects", "__sn_globals", ".snglob")

STATIC_SECTIONS(SN_DECLARE_SECTION)

#define SECTION_INDEX(id, elf, mach, coff) id##_index,
enum { STATIC_SECTIONS(SECTION_INDEX) };

#define DESCRIBE_SECTION(id, elf, mach, coff)                                  \
    SN_SECTION_RANGE(id, elf, mach, coff),

static const SNStaticSection staticSections[] = {
    STATIC_SECTIONS(DESCRIBE_SECTION)};

static void keepStaticSections(void) {
    /* Preserve empty sections under dead stripping without requiring newer
     * compilers' retain attribute. These are volatile reads on ELF and Mach-O.
     */
    STATIC_SECTIONS(SN_KEEP_SECTION);
}

const SNStaticSection *scalanative_static_strings_section(void) {
    keepStaticSections();
    return &staticSections[strings_index];
}

const SNStaticSection *scalanative_static_classinfo_section(void) {
    keepStaticSections();
    return &staticSections[classinfo_index];
}

const SNStaticSection *scalanative_static_globals_section(void) {
    keepStaticSections();
    return &staticSections[globals_index];
}

const SNStaticSection *scalanative_static_sections(size_t *count) {
    keepStaticSections();
    *count = sizeof(staticSections) / sizeof(staticSections[0]);
    return staticSections;
}
