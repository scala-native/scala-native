#ifndef IMMIX_STACKTRACE_H
#define IMMIX_STACKTRACE_H

#ifndef _WIN32
#include <libunwind.h>
#else
int scalanative_unwind_get_context(void *context);
int scalanative_unwind_init_local(void *cursor, void *context);
int scalanative_unwind_step(void *cursor);
int scalanative_unwind_get_proc_name(void *cursor, char *buffer, size_t length,
                                     void *offset);
#endif
#include <stdio.h>

void StackTrace_PrintStackTrace() {
#ifndef _WIN32
    unw_cursor_t cursor;
    unw_context_t context;
    unw_getcontext(&context);
    unw_init_local(&cursor, &context);

    while (unw_step(&cursor) > 0) {
        unw_word_t offset, pc;
        unw_get_reg(&cursor, UNW_REG_IP, &pc);
        if (pc == 0) {
            break;
        }

        char sym[256];
        if (unw_get_proc_name(&cursor, sym, sizeof(sym), &offset) == 0) {
            printf("\tat %s\n", sym);
        }
    }
#else
    char cursor[2048];
    char context[2048];
    char offset[8];
    char name[256];
    scalanative_unwind_get_context(context);
    scalanative_unwind_init_local(cursor, context);

    while (scalanative_unwind_step(cursor) > 0) {
        /*unw_word_t offset, pc;
        unw_get_reg(&cursor, UNW_REG_IP, &pc);
        if (pc == 0) {
            break;
        }*/

        if (scalanative_unwind_get_proc_name(cursor, name, sizeof(name),
                                             offset) == 0) {
            printf("\tat %s\n", name);
        }
    }
#endif
}

#endif // IMMIX_STACKTRACE_H
