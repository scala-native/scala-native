#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include "StackTrace.h"

void StackTrace_PrintStackTrace() {
    void *cursor = malloc(scalanative_unwind_sizeof_cursor());
    void *context = malloc(scalanative_unwind_sizeof_context());
    scalanative_unwind_get_context(context);
    scalanative_unwind_init_local(cursor, context);

    while (scalanative_unwind_step(cursor) > 0) {
        size_t offset, pc;
        scalanative_unwind_get_reg(cursor, scalanative_unw_reg_ip(), &pc);
        if (pc == 0) {
            break;
        }

        char sym[256];
        if (scalanative_unwind_get_proc_name(cursor, sym, sizeof(sym),
                                             &offset) == 0) {
            printf("\tat %s\n", sym);
        }
    }
    free(cursor);
    free(context);
}