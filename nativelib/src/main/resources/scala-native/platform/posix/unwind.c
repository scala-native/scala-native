// Unwind implementation used only on Posix compliant systems
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))
#include "../unwind.h"
#include "libunwind/libunwind.h"

int scalanative_unwind_get_context(void *context) {
    return unw_getcontext((unw_context_t *)context);
}

int scalanative_unwind_init_local(void *cursor, void *context) {
    return unw_init_local((unw_cursor_t *)cursor, (unw_context_t *)context);
}

int scalanative_unwind_step(void *cursor) {
    return unw_step((unw_cursor_t *)cursor);
}

int scalanative_unwind_get_proc_name(void *cursor, char *buffer, size_t length,
                                     void *offset) {
    return unw_get_proc_name((unw_cursor_t *)cursor, buffer, length,
                             (unw_word_t *)offset);
}

int scalanative_unwind_get_reg(void *cursor, int regnum, size_t *valp) {
    return unw_get_reg((unw_cursor_t *)cursor, regnum, (unw_word_t *)valp);
}

int scalanative_unw_reg_ip() { return UNW_REG_IP; }

size_t scalanative_unwind_sizeof_context(){
    return sizeof(unw_context_t);
}
size_t scalanative_unwind_sizeof_cursor(){
    return sizeof(unw_cursor_t);
}

#endif // Unix or Mac OS
