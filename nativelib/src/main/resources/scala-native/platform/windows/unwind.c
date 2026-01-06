// Unwind API implementation used only on Windows
#if defined(_WIN32)

#define WIN32_LEAN_AND_MEAN
// clang-format off
// windows.h must be included before dbghelp.h
#include <windows.h>
#include <dbghelp.h>
#include <stdio.h>
#include "platform/unwind.h"
// clang-format on

#define MAX_LENGTH_OF_CALLSTACK 255
#define MAX_LENGHT_OF_NAME 255

// Ensure debug symbols are initialized. Must be called before using
// SymFromAddrW. SymInitialize must only be called once per process.
// GetCurrentProcess() returns a pseudo-handle that's always valid, so we don't
// need to store it.
static int ensureSymInitialized(void) {
    static int symInitialized = 0;
    if (!symInitialized) {
        // Disable SYMOPT_UNDNAME to get raw mangled symbol names.
        // Scala Native needs mangled names to extract class/method information.
        // Default options include SYMOPT_UNDNAME which demangles names.
        DWORD options = SymGetOptions();
        options &= ~SYMOPT_UNDNAME;
        SymSetOptions(options);

        if (SymInitialize(GetCurrentProcess(), NULL, TRUE) == FALSE) {
            return -1;
        }
        symInitialized = 1;
    }
    return 0;
}

typedef struct _UnwindContext {
    void *stack[MAX_LENGTH_OF_CALLSTACK];
    unsigned short frames;
    HANDLE process;
    DWORD64 cursor;
    struct {
        SYMBOL_INFOW info;
        wchar_t nameBuffer[MAX_LENGHT_OF_NAME + 1];
    } symbol;
} UnwindContext;

int scalanative_unwind_get_context(void *context) { return 0; }

int scalanative_unwind_init_local(void *cursor, void *context) {
    UnwindContext *ucontext = (UnwindContext *)context;
    UnwindContext **ucontextRef = (UnwindContext **)cursor;
    *ucontextRef = ucontext;
    memset(ucontext, 0, sizeof(UnwindContext));
    if (ensureSymInitialized() != 0) {
        return 1;
    }
    ucontext->process = GetCurrentProcess();
    ucontext->frames = CaptureStackBackTrace(0, MAX_LENGTH_OF_CALLSTACK,
                                             ucontext->stack, NULL);
    ucontext->cursor = 0;
    ucontext->symbol.info.MaxNameLen = MAX_LENGHT_OF_NAME;
    ucontext->symbol.info.SizeOfStruct = sizeof(ucontext->symbol.info);
    return 0;
}

int scalanative_unwind_step(void *cursor) {
    UnwindContext *ucontext = *(UnwindContext **)cursor;
    return ucontext->frames - (++ucontext->cursor);
}

int scalanative_unwind_get_proc_name(void *cursor, char *buffer, size_t length,
                                     void *offset) {
    DWORD displacement = 0;
    IMAGEHLP_LINE line;
    int fileNameLen = 0;
    UnwindContext *ucontext = *(UnwindContext **)cursor;
    if (ucontext->cursor < ucontext->frames) {
        void *address = ucontext->stack[ucontext->cursor];
        PSYMBOL_INFOW symbol = &ucontext->symbol.info;
        SymFromAddrW(ucontext->process, (DWORD64)address, 0, symbol);
        snprintf(buffer, length, "%S", symbol->Name);
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

int scalanative_unwind_get_reg(void *cursor, int regnum, size_t *valp) {
    UnwindContext *ucontext = *(UnwindContext **)cursor;
    *valp = (size_t)(ucontext->stack[ucontext->cursor]);
    return 0;
}

// Only usage results in passing to get_reg as `regnum`, where it's not actually
// used
int scalanative_unw_reg_ip() { return -1; }

size_t scalanative_unwind_sizeof_context() { return sizeof(UnwindContext); }
size_t scalanative_unwind_sizeof_cursor() { return sizeof(UnwindContext *); }

// Look up procedure name by instruction pointer address.
// On Windows, this uses SymFromAddrW which already does address-based lookup.
// Returns 0 on success, negative value on error.
int scalanative_unwind_get_proc_name_by_ip(size_t ip, char *buffer,
                                           size_t length, size_t *offset) {
    if (ensureSymInitialized() != 0) {
        buffer[0] = '\0';
        return -1;
    }

    struct {
        SYMBOL_INFOW info;
        wchar_t nameBuffer[MAX_LENGHT_OF_NAME + 1];
    } symbol;
    symbol.info.MaxNameLen = MAX_LENGHT_OF_NAME;
    symbol.info.SizeOfStruct = sizeof(symbol.info);

    if (SymFromAddrW(GetCurrentProcess(), (DWORD64)ip, 0, &symbol.info) ==
        FALSE) {
        buffer[0] = '\0';
        return -1;
    }

    snprintf(buffer, length, "%S", symbol.info.Name);
    if (offset != NULL) {
        *offset = ip - (size_t)symbol.info.Address;
    }

    return 0;
}

#endif // _WIN32
