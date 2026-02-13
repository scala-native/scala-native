#include "backtrace_wrapper.h"

#include <stdlib.h>
#include <string.h>

#if defined(__APPLE__) || defined(__linux__)

/* ---- Mac/Linux: libbacktrace ---- */

#include "platform/posix/libbacktrace/backtrace.h"

static struct backtrace_state *bt_state = NULL;

static void error_cb(void *data, const char *msg, int errnum) {}

static int pcinfo_cb(void *data, uintptr_t pc, const char *filename, int lineno,
                     const char *function) {
    scalanative_pcinfo_result *result = (scalanative_pcinfo_result *)data;
    result->filename = filename;
    result->lineno = lineno;
    return 0;
}

typedef struct {
    scalanative_pcinfo_result *result;
    uintptr_t func_start_addr;
} syminfo_data;

static void syminfo_cb(void *data, uintptr_t pc, const char *symname,
                       uintptr_t symval, uintptr_t symsize) {
    syminfo_data *sd = (syminfo_data *)data;
    sd->result->symname = symname;
    sd->func_start_addr = symval;
}

int scalanative_backtrace_init(const char *filename, int threaded) {
    /* backtrace_create_state stores the pointer for lazy use, strdup */
    const char *fn_copy = filename ? strdup(filename) : NULL;
    bt_state = backtrace_create_state(fn_copy, threaded, error_cb, NULL);
    return bt_state != NULL ? 0 : -1;
}

int scalanative_backtrace_pcinfo(uintptr_t pc,
                                 scalanative_pcinfo_result *result) {
    if (bt_state == NULL)
        return -1;

    memset(result, 0, sizeof(*result));

    // Resolve filename + line from DWARF
    backtrace_pcinfo(bt_state, pc, pcinfo_cb, error_cb, result);

    // Resolve symbol name + start address from symbol table.
    syminfo_data sd = {result, 0};
    backtrace_syminfo(bt_state, pc, syminfo_cb, error_cb, &sd);

    /* If the Throwable constructor was inlined, DWARF may point to
       Throwables.scala. Re-resolve at the function start address to get
       the actual caller's file/line. */
    if (result->filename != NULL && sd.func_start_addr != 0 &&
        sd.func_start_addr != pc &&
        strstr(result->filename, "Throwable") != NULL) {
        result->filename = NULL;
        result->lineno = 0;
        backtrace_pcinfo(bt_state, sd.func_start_addr, pcinfo_cb, error_cb,
                         result);
    }

    return 0;
}

typedef struct {
    uintptr_t *buffer;
    int max_frames;
    int count;
} collect_data;

static int collect_cb(void *vdata, uintptr_t pc) {
    collect_data *d = (collect_data *)vdata;
    if (d->count >= d->max_frames)
        return 1;
    d->buffer[d->count++] = pc;
    return 0;
}

/* Collect raw PC addresses via backtrace_simple (no symbol lookup).
   Returns frame count, or -1 if not initialized. */
int scalanative_backtrace_collect(int skip, uintptr_t *buffer, int max_frames) {
    if (bt_state == NULL)
        return -1;
    collect_data data = {buffer, max_frames, 0};
    backtrace_simple(bt_state, skip, collect_cb, error_cb, &data);
    return data.count;
}

#endif /* __APPLE__ || __linux__ */
