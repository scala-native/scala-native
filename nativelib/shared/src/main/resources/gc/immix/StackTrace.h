#ifndef IMMIX_STACKTRACE_H
#define IMMIX_STACKTRACE_H

#include <libunwind.h>
#include <stdio.h>

void StackTrace_PrintStackTrace() {
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
}

#endif // IMMIX_STACKTRACE_H
