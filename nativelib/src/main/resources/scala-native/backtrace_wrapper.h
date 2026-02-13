#ifndef SCALANATIVE_BACKTRACE_WRAPPER_H
#define SCALANATIVE_BACKTRACE_WRAPPER_H

#include <stdint.h>

typedef struct {
    const char *filename;
    const char *symname;
    int lineno;
} scalanative_pcinfo_result;

int scalanative_backtrace_init(const char *filename, int threaded);
int scalanative_backtrace_pcinfo(uintptr_t pc,
                                 scalanative_pcinfo_result *result);
int scalanative_backtrace_collect(int skip, uintptr_t *buffer, int max_frames);

#endif /* SCALANATIVE_BACKTRACE_WRAPPER_H */
