#ifndef _WIN32
    #include <libunwind.h>
#else
    #define WIN32_LEAN_AND_MEAN
    #include <Windows.h>
    #include <DbgHelp.h>
    #include <stdio.h>
    #define MAX_LENGTH_OF_CALLSTACK 255
    typedef struct _UnwindContext
    {
        void** stack;
        unsigned short frames;        
        HANDLE process;
        DWORD64 cursor;
        SYMBOL_INFO symbol;
    } UnwindContext;
#endif

int scalanative_unwind_get_context(void *context) {
#ifndef _WIN32
    return unw_getcontext((unw_context_t*) context);
#else
    return 0;
#endif
}

int scalanative_unwind_init_local(void *cursor, void *context) {
#ifndef _WIN32
    return unw_init_local((unw_cursor_t*) cursor, (unw_context_t*) context);
#else
    UnwindContext* ucontext = (UnwindContext*)cursor;
    memset(ucontext, 0, sizeof(UnwindContext));
    ucontext->stack = (void**)context;
    ucontext->process = GetCurrentProcess();
    if (SymInitialize(ucontext->process, NULL, TRUE) == FALSE)
    {
        return 1;
    }
    ucontext->frames = CaptureStackBackTrace(0, MAX_LENGTH_OF_CALLSTACK, ucontext->stack, NULL);
    ucontext->cursor = 0;
    ucontext->symbol.MaxNameLen = 255; //todo: review
    ucontext->symbol.SizeOfStruct = sizeof(SYMBOL_INFO);
    return 0;
#endif
}

int scalanative_unwind_step(void *cursor) {
#ifndef _WIN32
    return unw_step((unw_cursor_t*) cursor);
#else
    UnwindContext* ucontext = (UnwindContext*)cursor;
    return ucontext->frames - ucontext->cursor - 1;
#endif
}

int scalanative_unwind_get_proc_name(void *cursor, char *buffer,
       size_t length, void *offset) {
#ifndef _WIN32
    return unw_get_proc_name((unw_cursor_t*) cursor, buffer, length,
            (unw_word_t*) offset);
#else
    DWORD displacement = 0;
    IMAGEHLP_LINE line;
    int fileNameLen = 0;
    UnwindContext* ucontext = (UnwindContext*)cursor;
    if (ucontext->cursor < ucontext->frames)
    {
        DWORD64 address = (DWORD64)(ucontext->stack[ucontext->cursor]);
        PSYMBOL_INFO symbol = &ucontext->symbol;
        SymFromAddr(ucontext->process, address, 0, symbol);
        ucontext->cursor++;
        memcpy(buffer, symbol->Name, symbol->NameLen);
        buffer[symbol->NameLen] = 0;
        memcpy(offset, &(symbol->Address), sizeof(void*));
        if (SymGetLineFromAddr(ucontext->process, address, &displacement, &line))
        {
            fileNameLen = strlen(line.FileName);
            if (fileNameLen > 0)
            {
                snprintf(buffer + symbol->NameLen, length - symbol->NameLen + 1, "(%s:%lu)", line.FileName, line.LineNumber);
            }
            else
            {
                snprintf(buffer + symbol->NameLen, length - symbol->NameLen + 1, "(%lu)", line.LineNumber);
            }
        }
    }
    return 0;
#endif
}