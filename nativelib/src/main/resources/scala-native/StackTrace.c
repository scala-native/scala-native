
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include "platform/unwind.h"
#include "gc/shared/ThreadUtil.h"

static mutex_t *getStackTraceLock() {
    static mutex_t lock;
    static bool initialized = false;
    if (!initialized) {
        mutex_init(&lock);
        initialized = true;
    }
    return &lock;
}

void StackTrace_PrintStackTrace() {
    void *cursor = malloc(scalanative_unwind_sizeof_cursor());
    void *context = malloc(scalanative_unwind_sizeof_context());
    scalanative_unwind_get_context(context);
    scalanative_unwind_init_local(cursor, context);

    mutex_t *lock = getStackTraceLock();
    mutex_lock(lock);
    {
        int frames = 0;
        const int MaxFrames = 128;
        while (scalanative_unwind_step(cursor) > 0) {
            size_t offset, pc;
            scalanative_unwind_get_reg(cursor, scalanative_unw_reg_ip(), &pc);
            if (pc == 0) {
                break;
            }

            char sym[256];
            memset(&sym, 0, sizeof(sym));
            if (++frames < MaxFrames &&
                scalanative_unwind_get_proc_name(cursor, sym, sizeof(sym),
                                                 &offset) == 0) {
                printf("\tat %s\n", sym);
            }
        }
        if (frames > MaxFrames)
            printf("\t... and %d more\n", frames - MaxFrames);
    }
    mutex_unlock(lock);
    free(cursor);
    free(context);
}
