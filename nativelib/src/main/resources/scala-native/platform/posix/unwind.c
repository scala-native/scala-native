// Unwind implementation used only on Posix compliant systems
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))
#include "../unwind.h"
#include "libunwind/libunwind.h"

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

#endif // Unix or Mac OS
