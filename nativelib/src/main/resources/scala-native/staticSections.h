#ifndef SCALANATIVE_STATIC_SECTIONS_H
#define SCALANATIVE_STATIC_SECTIONS_H

/* A half-open range of static storage in the current linked image. */
typedef struct {
    void *start;
    void *end;
} SNStaticSection;

#ifdef _WIN32
#define SN_SECTION_LOCAL
#else
#define SN_SECTION_LOCAL __attribute__((visibility("hidden")))
#endif

/* Callers decide whether to register, inspect, or scan each section. */
#ifdef __cplusplus
extern "C" {
#endif
SN_SECTION_LOCAL const SNStaticSection *
scalanative_static_strings_section(void);
SN_SECTION_LOCAL const SNStaticSection *
scalanative_static_classinfo_section(void);
SN_SECTION_LOCAL const SNStaticSection *
scalanative_static_globals_section(void);
#ifdef __cplusplus
}
#endif

#endif
