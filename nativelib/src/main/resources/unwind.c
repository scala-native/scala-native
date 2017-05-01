#ifndef _WIN32
    #include <libunwind.h>
#endif

int scalanative_unwind_get_context(void *context) {
#ifndef _WIN32
    return unw_getcontext((unw_context_t*) context);
#else
    context = 0;
    return 0;
#endif
}

int scalanative_unwind_init_local(void *cursor, void *context) {
#ifndef _WIN32
    return unw_init_local((unw_cursor_t*) cursor, (unw_context_t*) context);
#else
    cursor = 0;
    return 0;
#endif
}

int scalanative_unwind_step(void *cursor) {
#ifndef _WIN32
    return unw_step((unw_cursor_t*) cursor);
#else
    return 0;
#endif
}

int scalanative_unwind_get_proc_name(void *cursor, char *buffer,
       size_t length, void *offset) {
#ifndef _WIN32
    return unw_get_proc_name((unw_cursor_t*) cursor, buffer, length,
            (unw_word_t*) offset);
#else
    return 0;
#endif
}

#ifdef _WIN32
void* __cxa_begin_catch(void* address)
{
    return 0;
}
void __cxa_end_catch()
{

}
int __gxx_personality_v0(void* address)
{
   return 0;
}
#endif
