// Unwind API implementation used only on Windows
#if defined(_WIN32)

#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
#include <DbgHelp.h>
#include <stdio.h>
#include "../unwind.h"

#define MAX_LENGTH_OF_CALLSTACK 255

typedef struct _UnwindContext {
    void **stack;
    unsigned short frames;
    HANDLE process;
    DWORD64 cursor;
    SYMBOL_INFOW symbol;
} UnwindContext;

int scalanative_unwind_get_context(void *context) { return 0; }

int scalanative_unwind_init_local(void *cursor, void *context) {
    static int symInitialized = 0;
    UnwindContext *ucontext = (UnwindContext *)cursor;
    memset(ucontext, 0, sizeof(UnwindContext));
    ucontext->stack = (void **)context;
    ucontext->process = GetCurrentProcess();
    if (!symInitialized) {
        if (SymInitialize(ucontext->process, NULL, TRUE) == FALSE) {
            return 1;
        }
        symInitialized = 1;
    }
    ucontext->frames = CaptureStackBackTrace(0, MAX_LENGTH_OF_CALLSTACK,
                                             ucontext->stack, NULL);
    ucontext->cursor = 0;
    ucontext->symbol.MaxNameLen = 255;
    ucontext->symbol.SizeOfStruct = sizeof(SYMBOL_INFOW);
    return 0;
}

int scalanative_unwind_step(void *cursor) {
    UnwindContext *ucontext = (UnwindContext *)cursor;
    return ucontext->frames - (++ucontext->cursor);
}

int scalanative_unwind_get_proc_name(void *cursor, char *buffer, size_t length,
                                     void *offset) {
    DWORD displacement = 0;
    IMAGEHLP_LINE line;
    int fileNameLen = 0;
    UnwindContext *ucontext = (UnwindContext *)cursor;
    if (ucontext->cursor < ucontext->frames) {
        void *address = ucontext->stack[ucontext->cursor];
        PSYMBOL_INFOW symbol = &ucontext->symbol;
        SymFromAddrW(ucontext->process, (DWORD64)address, 0, symbol);
        snprintf(buffer, length, "%ws", symbol->Name);
        memcpy(offset, &(symbol->Address), sizeof(void *));
        if (SymGetLineFromAddr(ucontext->process, (DWORD64)address,
                               &displacement, &line)) {
            fileNameLen = strlen(line.FileName);
            if (fileNameLen > 0) {
                snprintf(buffer + symbol->NameLen, length - symbol->NameLen,
                         ":(%s:%lu)", line.FileName, line.LineNumber);
            }
        }
    }
    return 0;
}

int scalanative_unwind_get_reg(void *cursor, int regnum,
                               unsigned long long *valp) {
    UnwindContext *ucontext = (UnwindContext *)cursor;
    *valp = (unsigned long long)(ucontext->stack[ucontext->cursor]);
    return 0;
}

// Only usage results in passing to get_reg as `regnum`, where it's not actually
// used
int scalanative_unw_reg_ip() { return -1; }

#endif // _WIN32
