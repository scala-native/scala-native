// Unwind API implementation used only on Windows
#if defined(_WIN32)

#define WIN32_LEAN_AND_MEAN
// windows.h must be included before dbghelp.h
#include <windows.h>
#include <dbghelp.h>
#include <stdio.h>
#include <string.h>
#include "platform/unwind.h"
// clang-format on

#define MAX_LENGTH_OF_CALLSTACK 255
#define MAX_LENGHT_OF_NAME 511

#if defined(_M_ARM64) || defined(__aarch64__)
#define SN_CONTEXT_PC(ctx) ((ctx)->Pc)
#elif defined(_M_X64) || defined(__x86_64__) || defined(_M_AMD64)
#define SN_CONTEXT_PC(ctx) ((ctx)->Rip)
#else
#error "Unsupported Windows architecture for stack unwinding"
#endif

static int symLookupByIp(HANDLE process, DWORD64 ip, PSYMBOL_INFOW symbol,
                         char *buffer, size_t length, size_t *offset) {
    DWORD64 displacement = 0;
    DWORD lineDisplacement = 0;
    IMAGEHLP_LINE line;
    DWORD64 addrs[3];
    int i;

    addrs[0] = ip;
    addrs[1] = (ip > 0) ? (ip - 1) : 0;
    addrs[2] = (ip > 1) ? (ip - 2) : 0;

    for (i = 0; i < 3; i++) {
        if (addrs[i] == 0) {
            continue;
        }
        displacement = 0;
        if (SymFromAddrW(process, addrs[i], &displacement, symbol) == FALSE) {
            continue;
        }
        int nameLen = snprintf(buffer, length, "%S", symbol->Name);
        if (nameLen < 0) {
            continue;
        }
        line.SizeOfStruct = sizeof(line);
        lineDisplacement = 0;
        if (SymGetLineFromAddr(process, addrs[i], &lineDisplacement, &line)) {
            int fileNameLen = (int)strlen(line.FileName);
            if (fileNameLen > 0 && nameLen < (int)length) {
                snprintf(buffer + nameLen, length - (size_t)nameLen,
                         ":(%s:%lu)", line.FileName, line.LineNumber);
            }
        }
        if (offset != NULL) {
            *offset = ip - (size_t)symbol->Address;
        }
        return 0;
    }

    buffer[0] = '\0';
    return -1;
}

static int ensureSymInitialized(void) {
    static int symInitialized = 0;
    if (!symInitialized) {
        DWORD options = SymGetOptions();
        options &= ~SYMOPT_UNDNAME;
        options |= SYMOPT_LOAD_LINES;
        SymSetOptions(options);

        if (SymInitialize(GetCurrentProcess(), NULL, TRUE) == FALSE) {
            return -1;
        }
        SymRefreshModuleList(GetCurrentProcess());
        {
            WCHAR path[MAX_PATH];
            DWORD pathLen = GetModuleFileNameW(NULL, path, MAX_PATH);
            if (pathLen > 0 && pathLen < MAX_PATH) {
                SymLoadModuleExW(GetCurrentProcess(), NULL, path, NULL, 0, 0,
                                 NULL, 0);
            }
        }
        symInitialized = 1;
    }
    return 0;
}

typedef struct _UnwindContext {
    CONTEXT context;
    UNWIND_HISTORY_TABLE history;
    void *stack[MAX_LENGTH_OF_CALLSTACK];
    unsigned short frames;
    HANDLE process;
    DWORD64 cursor;
    struct {
        SYMBOL_INFOW info;
        wchar_t nameBuffer[MAX_LENGHT_OF_NAME + 1];
    } symbol;
} UnwindContext;

static unsigned short capture_stack_rtl(UnwindContext *ucontext) {
    CONTEXT ctx = ucontext->context;
    memset(&ucontext->history, 0, sizeof(ucontext->history));

    unsigned short count = 0;
    DWORD64 previousPc = 0;

    while (count < MAX_LENGTH_OF_CALLSTACK) {
        DWORD64 pc = SN_CONTEXT_PC(&ctx);
        if (pc == 0 || pc == previousPc) {
            break;
        }
        previousPc = pc;
        ucontext->stack[count++] = (void *)pc;

        DWORD64 imageBase = 0;
        PRUNTIME_FUNCTION runtimeFn =
            RtlLookupFunctionEntry(pc, &imageBase, &ucontext->history);

        if (runtimeFn == NULL) {
#if defined(_M_ARM64) || defined(__aarch64__)
            DWORD64 lr = ctx.Lr;
            if (lr == 0 || lr == pc) {
                break;
            }
            ctx.Pc = lr;
            ctx.Lr = 0;
#else
            DWORD64 sp = ctx.Rsp;
            if (sp == 0) {
                break;
            }
            DWORD64 returnAddress = *(DWORD64 *)sp;
            if (returnAddress == 0 || returnAddress == pc) {
                break;
            }
            ctx.Rip = returnAddress;
            ctx.Rsp = sp + 8;
#endif
            continue;
        }

        PVOID handlerData = NULL;
        DWORD64 establisherFrame = 0;
#if defined(_M_ARM64) || defined(__aarch64__)
        RtlVirtualUnwind(UNW_FLAG_NHANDLER, imageBase, pc, runtimeFn, &ctx,
                         &handlerData, &establisherFrame, NULL);
#else
        RtlVirtualUnwind(UNW_FLAG_NHANDLER, imageBase, pc, runtimeFn, &ctx,
                         &handlerData, &establisherFrame, &ucontext->history);
#endif
    }

    return count;
}

int scalanative_unwind_get_context(void *context) {
    UnwindContext *ucontext = (UnwindContext *)context;
    RtlCaptureContext(&ucontext->context);
    return 0;
}

int scalanative_unwind_init_local(void *cursor, void *context) {
    UnwindContext *ucontext = (UnwindContext *)context;
    UnwindContext **ucontextRef = (UnwindContext **)cursor;
    *ucontextRef = ucontext;
    memset(&ucontext->history, 0, sizeof(ucontext->history));
    ucontext->process = GetCurrentProcess();
    ucontext->cursor = 0;
    ucontext->frames = capture_stack_rtl(ucontext);
    if (ucontext->frames == 0) {
        return -1;
    }
    if (ensureSymInitialized() == 0) {
        ucontext->symbol.info.MaxNameLen = MAX_LENGHT_OF_NAME;
        ucontext->symbol.info.SizeOfStruct = sizeof(ucontext->symbol.info);
    }
    return 0;
}

int scalanative_unwind_step(void *cursor) {
    UnwindContext *ucontext = *(UnwindContext **)cursor;
    return ucontext->frames - (++ucontext->cursor);
}

int scalanative_unwind_get_proc_name(void *cursor, char *buffer, size_t length,
                                     void *offset) {
    UnwindContext *ucontext = *(UnwindContext **)cursor;
    if (ucontext->cursor < ucontext->frames) {
        void *address = ucontext->stack[ucontext->cursor];
        PSYMBOL_INFOW symbol = &ucontext->symbol.info;
        size_t unused = 0;
        return symLookupByIp(ucontext->process, (DWORD64)address, symbol, buffer,
                             length, offset != NULL ? &unused : NULL);
    }
    buffer[0] = '\0';
    return -1;
}

int scalanative_unwind_get_reg(void *cursor, int regnum, size_t *valp) {
    UnwindContext *ucontext = *(UnwindContext **)cursor;
    *valp = (size_t)(ucontext->stack[ucontext->cursor]);
    return 0;
}

int scalanative_unw_reg_ip() { return -1; }

size_t scalanative_unwind_sizeof_context() { return sizeof(UnwindContext); }
size_t scalanative_unwind_sizeof_cursor() { return sizeof(UnwindContext *); }

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

    return symLookupByIp(GetCurrentProcess(), (DWORD64)ip, &symbol.info, buffer,
                         length, offset);
}

#endif // _WIN32
