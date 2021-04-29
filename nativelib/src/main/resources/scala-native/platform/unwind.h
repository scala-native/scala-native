#ifndef UNWIND_H
#define UNWIND_H

#include <stddef.h>

int scalanative_unwind_get_context(void *context);
int scalanative_unwind_init_local(void *cursor, void *context);
int scalanative_unwind_step(void *cursor);
int scalanative_unwind_get_proc_name(void *cursor, char *buffer, size_t length,
                                     void *offset);
int scalanative_unwind_get_reg(void *cursor, int regnum,
                               unsigned long long *valp);
int scalanative_unw_reg_ip();

#endif
