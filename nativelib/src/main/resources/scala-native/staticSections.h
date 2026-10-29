#ifndef SCALANATIVE_STATIC_SECTIONS_H
#define SCALANATIVE_STATIC_SECTIONS_H

#include <stddef.h>
#include <stdint.h>

/* A named, half-open range of static storage in the current linked image. */
typedef struct {
    const char *name;
    void *start;
    void *end;
} SNStaticSection;

#define SN_SECTION_STORAGE(name)                                               \
    __attribute__((section(name), used, aligned(sizeof(uintptr_t))))

#if defined(_WIN32)

/* COFF sorts $A / $M / $Z contributions into one output section. */
#define SN_DECLARE_SECTION(id, elf, mach, coff)                                \
    static uintptr_t id##_start SN_SECTION_STORAGE(coff "$A") = 0;             \
    static uintptr_t id##_end SN_SECTION_STORAGE(coff "$Z") = 0;
#define SN_SECTION_RANGE(id, elf, mach, coff) {elf, &id##_start + 1, &id##_end}
#define SN_KEEP_SECTION(id, elf, mach, coff)
#define SN_SECTION_LOCAL

#elif defined(__APPLE__)

#define SN_DECLARE_SECTION(id, elf, mach, coff)                                \
    extern uintptr_t id##_start[] __asm("section$start$__DATA$" mach);         \
    extern uintptr_t id##_end[] __asm("section$end$__DATA$" mach);             \
    static volatile uintptr_t id##_anchor SN_SECTION_STORAGE("__DATA," mach) = \
        0;
#define SN_SECTION_RANGE(id, elf, mach, coff) {elf, id##_start, id##_end}
#define SN_KEEP_SECTION(id, elf, mach, coff) (void)id##_anchor;
#define SN_SECTION_LOCAL __attribute__((visibility("hidden")))

#else /* ELF */

#define SN_SECTION_LOCAL __attribute__((visibility("hidden")))
#define SN_DECLARE_SECTION(id, elf, mach, coff)                                \
    extern uintptr_t id##_start[] __asm("__start_" elf) SN_SECTION_LOCAL;      \
    extern uintptr_t id##_end[] __asm("__stop_" elf) SN_SECTION_LOCAL;         \
    static volatile uintptr_t id##_anchor SN_SECTION_STORAGE(elf) = 0;
#define SN_SECTION_RANGE(id, elf, mach, coff) {elf, id##_start, id##_end}
#define SN_KEEP_SECTION(id, elf, mach, coff) (void)id##_anchor;

#endif

/* Returns this image's section descriptors and writes their count. This only
 * discovers storage; callers decide whether to register, inspect, or scan it.
 */
#ifdef __cplusplus
extern "C" {
#endif
SN_SECTION_LOCAL const SNStaticSection *
scalanative_static_sections(size_t *count);
#ifdef __cplusplus
}
#endif

#endif
