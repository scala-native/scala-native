#include "staticSections.h"
#include <stdint.h>

#define SN_SECTION_STORAGE(name)                                               \
    __attribute__((section(name), used, aligned(sizeof(uintptr_t))))

#if defined(_WIN32)

/* COFF sorts $A / $M / $Z contributions into one output section. */
#define SN_DECLARE_SECTION(id, elf, mach, coff)                                \
    static uintptr_t id##_start SN_SECTION_STORAGE(coff "$A") = 0;             \
    static uintptr_t id##_end SN_SECTION_STORAGE(coff "$Z") = 0;
#define SN_SECTION_RANGE(id, elf, mach, coff) {&id##_start + 1, &id##_end}
#define SN_KEEP_SECTION(id)

#elif defined(__APPLE__)

#define SN_DECLARE_SECTION(id, elf, mach, coff)                                \
    extern uintptr_t id##_start[] __asm("section$start$__DATA$" mach);         \
    extern uintptr_t id##_end[] __asm("section$end$__DATA$" mach);             \
    static volatile uintptr_t id##_anchor SN_SECTION_STORAGE("__DATA," mach) = \
        0;
#define SN_SECTION_RANGE(id, elf, mach, coff) {id##_start, id##_end}
#define SN_KEEP_SECTION(id) (void)id##_anchor;

#else /* ELF */

#define SN_DECLARE_SECTION(id, elf, mach, coff)                                \
    extern uintptr_t id##_start[] __asm("__start_" elf) SN_SECTION_LOCAL;      \
    extern uintptr_t id##_end[] __asm("__stop_" elf) SN_SECTION_LOCAL;         \
    static volatile uintptr_t id##_anchor SN_SECTION_STORAGE(elf) = 0;
#define SN_SECTION_RANGE(id, elf, mach, coff) {id##_start, id##_end}
#define SN_KEEP_SECTION(id) (void)id##_anchor;

#endif

/* Each accessor keeps its own empty section alive under dead stripping, without
 * requiring newer compilers' retain attribute or retaining unrelated sections.
 */
#define SN_DEFINE_SECTION(id, elf, mach, coff)                                 \
    SN_DECLARE_SECTION(id, elf, mach, coff)                                    \
    const SNStaticSection *scalanative_static_##id##_section(void) {           \
        static const SNStaticSection section =                                 \
            SN_SECTION_RANGE(id, elf, mach, coff);                             \
        SN_KEEP_SECTION(id)                                                    \
        return &section;                                                       \
    }

/* Keep these names in sync with codegen.GlobalSections. Mach-O section names
 * are limited to 16 bytes, so sn_global_objects needs a shorter alias.
 */
SN_DEFINE_SECTION(strings, "sn_strings", "__sn_strings", ".snstr")
SN_DEFINE_SECTION(classinfo, "sn_classinfo", "__sn_classinfo", ".sncls")
SN_DEFINE_SECTION(globals, "sn_global_objects", "__sn_globals", ".snglob")
