// Unwind implementation used only on Posix compliant systems
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))

// _GNU_SOURCE needed for dladdr/Dl_info on Linux (must be before any includes)
#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif

#include "../unwind.h"
#include "libunwind/libunwind.h"
#include <dlfcn.h>
#include <string.h>

// The unwinding on NetBSD is unstable, they don't provide CFI
// annotations for most of libc and other places, nor for the signal
// trampoline. So it can't work properly, and probably leads to
// segmentation errors. To minimize the impact, I allow to get context
// and initialize unwind, but cursor returns nothing.

int scalanative_unwind_get_context(void *context) {
#ifdef __NetBSD__
    return 0;
#else
    return unw_getcontext((unw_context_t *)context);
#endif
}

int scalanative_unwind_init_local(void *cursor, void *context) {
#ifdef __NetBSD__
    return 0;
#else
    return unw_init_local((unw_cursor_t *)cursor, (unw_context_t *)context);
#endif
}

int scalanative_unwind_step(void *cursor) {
#ifdef __NetBSD__
    return 0;
#else
    return unw_step((unw_cursor_t *)cursor);
#endif
}

int scalanative_unwind_get_proc_name(void *cursor, char *buffer, size_t length,
                                     void *offset) {
#ifdef __NetBSD__
    return UNW_EUNSPEC;
#else
    return unw_get_proc_name((unw_cursor_t *)cursor, buffer, length,
                             (unw_word_t *)offset);
#endif
}

int scalanative_unwind_get_reg(void *cursor, int regnum, size_t *valp) {
#ifdef __NetBSD__
    return UNW_EUNSPEC;
#else
    return unw_get_reg((unw_cursor_t *)cursor, regnum, (unw_word_t *)valp);
#endif
}

int scalanative_unw_reg_ip() { return UNW_REG_IP; }

size_t scalanative_unwind_sizeof_context() { return sizeof(unw_context_t); }
size_t scalanative_unwind_sizeof_cursor() { return sizeof(unw_cursor_t); }

// Look up procedure name by instruction pointer address using dladdr.
// This works without needing the cursor to be at the correct stack frame.
// Returns 0 on success, negative value on error.
int scalanative_unwind_get_proc_name_by_ip(size_t ip, char *buffer,
                                           size_t length, size_t *offset) {
#ifdef __NetBSD__
    return UNW_EUNSPEC;
#else
    if (buffer == NULL || length == 0) {
        return UNW_EINVAL;
    }

    Dl_info info;
    if (dladdr((void *)ip, &info) == 0) {
        // dladdr failed - no symbol found
        buffer[0] = '\0';
        return UNW_ENOINFO;
    }

    if (info.dli_sname == NULL) {
        // Symbol address found but name not available
        buffer[0] = '\0';
        return UNW_ENOINFO;
    }

    // Copy symbol name to buffer
    size_t name_len = strlen(info.dli_sname);
    if (name_len >= length) {
        // Name truncated
        memcpy(buffer, info.dli_sname, length - 1);
        buffer[length - 1] = '\0';
    } else {
        memcpy(buffer, info.dli_sname, name_len + 1);
    }

    // Calculate offset from symbol start
    if (offset != NULL) {
        *offset = ip - (size_t)info.dli_saddr;
    }

    return 0;
#endif
}

#endif // Unix or Mac OS
