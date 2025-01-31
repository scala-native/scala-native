#ifndef SCALANATIVE_USING_CPP_EXCEPTIONS

#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>

// Refer to embeded unwind.h to ensure _Unwind_GetIPInfo is defined
#include "platform/posix/libunwind/unwind.h"

// gets the ExceptionWrapper from the _Unwind_Exception which is at the end of
// it. +1 goes to the end of the struct since it adds with the size of
// _Unwind_Exception, then we cast to ExceptionWrapper and we do - 1 to
// go back of sizeof ExceptionWrapper
#define GetExceptionWrapper(unwindException)                                   \
    ((ExceptionWrapper *)(unwindException + 1) - 1)

typedef void *Exception;
typedef void (*OnCatchHandler)(Exception);
typedef struct ExceptionWrapper {
    Exception obj;
    _Unwind_Exception unwindException;
} ExceptionWrapper;

extern OnCatchHandler scalanative_Throwable_onCatchHandler(Exception exception);
extern void scalanative_Throwable_showStackTrace(Exception exception);
extern ExceptionWrapper *
scalanative_Throwable_exceptionWrapper(Exception exception);

size_t scalanative_Throwable_sizeOfExceptionWrapper() {
    return sizeof(ExceptionWrapper);
}

static void Exception_cleanup(Exception *self) {
    OnCatchHandler handler = scalanative_Throwable_onCatchHandler(self);
    if (handler)
        handler(self);
}

// Cleanup function for the exception
void generic_exception_cleanup(_Unwind_Reason_Code code,
                               _Unwind_Exception *exception) {
    ExceptionWrapper *exceptionWrapper = GetExceptionWrapper(exception);
    Exception_cleanup(exceptionWrapper->obj);
}

typedef const uint8_t *LSDA_ptr;

// Read a ULEB128 encoded value and advance pointer
static size_t readULEB128(LSDA_ptr *data) {
    size_t result = 0;
    size_t shift = 0;
    unsigned char byte;
    const uint8_t *p = *data;
    do {
        byte = *p++;
        result |= (byte & 0x7f) << shift;
        shift += 7;
    } while (byte & 0x80);
    *data = p;
    return result;
}

// Read a SLEB128 encoded value and advance pointer
static ssize_t readSLEB128(LSDA_ptr *data) {
    ssize_t result = 0;
    size_t shift = 0;
    unsigned char byte;
    const uint8_t *p = *data;
    do {
        byte = *p++;
        result |= (byte & 0x7f) << shift;
        shift += 7;
    } while (byte & 0x80);

    // Sign extension if the value is negative
    if ((byte & 0x40) && shift < (sizeof(ssize_t) * 8)) {
        result |= -(1LL << shift);
    }
    *data = p;
    return result;
}

// DWARF Exception Header Encoding docummented at
// https://refspecs.linuxbase.org/LSB_5.0.0/LSB-Core-generic/LSB-Core-generic/dwarfext.html
enum {
    // DWARF Exception Header value format
    DW_EH_PE_absptr = 0x00,
    DW_EH_PE_uleb128 = 0x01,
    DW_EH_PE_udata2 = 0x02,
    DW_EH_PE_udata4 = 0x03,
    DW_EH_PE_udata8 = 0x04,
    DW_EH_PE_sleb128 = 0x09,
    DW_EH_PE_sdata2 = 0x0A,
    DW_EH_PE_sdata4 = 0x0B,
    DW_EH_PE_sdata8 = 0x0C,
    // DWARF Exception Header application
    DW_EH_PE_iprel = 0x10,
    DW_EH_PE_textrel = 0x20,
    DW_EH_PE_datarel = 0x30,
    DW_EH_PE_funcrel = 0x40,
    DW_EH_PE_aligned = 0x50,
    // special
    DW_EH_PE_indirect = 0x80, // gcc extension
    DW_EH_PE_omit = 0xff,     // no data follows
};

// read a pointer encoded value and advance pointer
static uintptr_t readDWARFEncodedPointer(LSDA_ptr *data, uint8_t encoding) {
    const uint8_t *p = *data;
    uintptr_t result = 0;

    if (encoding == DW_EH_PE_omit)
        return 0;

    // first get value
    switch (encoding & 0x0F) {
    case DW_EH_PE_absptr:
        result = *((const uintptr_t *)p);
        p += sizeof(uintptr_t);
        break;
    case DW_EH_PE_uleb128:
        result = readULEB128(&p);
        break;
    case DW_EH_PE_udata2:
        result = *((const uint16_t *)p);
        p += sizeof(uint16_t);
        break;
    case DW_EH_PE_udata4:
        result = *((const uint32_t *)p);
        p += sizeof(uint32_t);
        break;
    case DW_EH_PE_udata8:
        result = *((const uint64_t *)p);
        p += sizeof(uint64_t);
        break;
    case DW_EH_PE_sdata2:
        result = *((const int16_t *)p);
        p += sizeof(int16_t);
        break;
    case DW_EH_PE_sdata4:
        result = *((const int32_t *)p);
        p += sizeof(int32_t);
        break;
    case DW_EH_PE_sdata8:
        result = *((const int64_t *)p);
        p += sizeof(int64_t);
        break;
    case DW_EH_PE_sleb128:
        result = readSLEB128(&p);
        break;
    default:
        // not supported
        abort();
        break;
    }

    // then add relative offset
    switch (encoding & 0x70) {
    case DW_EH_PE_absptr:
        // do nothing
        break;
    case DW_EH_PE_iprel:
        result += (uintptr_t)(*data);
        break;
    case DW_EH_PE_textrel:
    case DW_EH_PE_datarel:
    case DW_EH_PE_funcrel:
    case DW_EH_PE_aligned:
    default:
        abort(); // not supported
        break;
    }

    // then apply indirection
    if (encoding & DW_EH_PE_indirect) {
        result = *((const uintptr_t *)result);
    }

    *data = p;
    return result;
}

#ifdef DEBUG_PERSONALITY
#include <dlfcn.h>
#include <unwind.h>
static const char *get_function_name(uintptr_t address) {
    Dl_info info;
    if (dladdr((void *)address, &info)) {
        return info.dli_sname;
    }
    return NULL; // Return nullptr if the symbol is not found
}
#endif

// A personality function to catch all exceptions
_Unwind_Reason_Code scalanative_personality(int version, _Unwind_Action actions,
                                            uint64_t exception_class,
                                            _Unwind_Exception *unwindException,
                                            _Unwind_Context *context) {
    // There is nothing to do if there is no LSDA for this frame.
    LSDA_ptr lsda = (LSDA_ptr)_Unwind_GetLanguageSpecificData(context);
    if (lsda == (uint8_t *)0)
        return _URC_CONTINUE_UNWIND;

    int ipBeforeInst = false;
    uintptr_t ip = (uintptr_t)_Unwind_GetIPInfo(context, &ipBeforeInst);
    // Normalize the position so we're always refering from callsite
    if (!ipBeforeInst)
        ip -= 1;
    uintptr_t funcStart = (uintptr_t)_Unwind_GetRegionStart(context);
    uintptr_t ipOffset = ip - funcStart;

    // Parse LSDA header.
    uint8_t lpStartEncoding = *lsda++;
    uintptr_t lpStart = 0; // unused
    if (lpStartEncoding != DW_EH_PE_omit) {
        lpStart = readDWARFEncodedPointer(&lsda, lpStartEncoding);
    }
    uint8_t ttypeEncoding = *lsda++;
    size_t ttype = 0; // unused
    if (ttypeEncoding != DW_EH_PE_omit) {
        ttype = readULEB128(&lsda);
    }

    // Walk call-site table looking for range that includes current IP.
    uint8_t callSiteEncoding = *lsda++;
    size_t callSiteTableLength = readULEB128(&lsda);
    LSDA_ptr callSiteTableStart = lsda;
    LSDA_ptr callSiteTableEnd = callSiteTableStart + callSiteTableLength;
    LSDA_ptr cs = callSiteTableStart;

#ifdef DEBUG_PERSONALITY
    if (actions & _UA_CLEANUP_PHASE) {
        printf("unwinding IP=%p\toffset=%zu\tfunctionStart=%p\t%s\n",
               (void *)ip, ipOffset, (void *)funcStart,
               get_function_name(funcStart));
    }
#endif

    while (cs < callSiteTableEnd) {
        uintptr_t start = readDWARFEncodedPointer(&cs, callSiteEncoding);
        size_t length = readDWARFEncodedPointer(&cs, callSiteEncoding);
        size_t landingPad = readDWARFEncodedPointer(&cs, callSiteEncoding);
        size_t action = readULEB128(&cs); // unused

#ifdef DEBUG_PERSONALITY
        if (actions & _UA_CLEANUP_PHASE) {
            printf("\tcallsite: "
                   "start=%lu\tlength=%zu\tlandingPad=%zu\taction=%zu\n",
                   start, length, landingPad, action);
        }
#endif
        if (landingPad == 0)
            continue; // no landing pad for this entry

        // Check if in callsite range or it IP is currently at beginning of next
        // landing pad. The special case can happen after inlining by LTO
        // resulting in throw directly followed by catched
        if (((start <= ipOffset) && (ipOffset < (start + length)) ||
             (ipOffset + 1) == landingPad)) {
            // Found valid entry, decide next step
            if (actions & _UA_SEARCH_PHASE) {
                return _URC_HANDLER_FOUND;
            } else {
                // Found landing pad for the IP.
                // Set Instruction Pointer to so we re-enter function
                // at landing pad. The landing pad is created by the compiler
                // to take two parameters in registers.
                _Unwind_SetGR(context, __builtin_eh_return_data_regno(0),
                              (uintptr_t)unwindException);
                _Unwind_SetGR(context, __builtin_eh_return_data_regno(1), 0);
                _Unwind_SetIP(context, (funcStart + landingPad));
#ifdef DEBUG_PERSONALITY
                printf("\nunwinding to handler in %s at %p\n",
                       get_function_name(funcStart),
                       (void *)(funcStart + landingPad));
#endif
                return _URC_INSTALL_CONTEXT;
            }
        }
    }
    // No landing pad found, continue unwinding.
    return _URC_CONTINUE_UNWIND;
}

Exception scalanative_catch(_Unwind_Exception *unwindException) {
    ExceptionWrapper *exceptionWrapper = GetExceptionWrapper(unwindException);
    Exception exception = exceptionWrapper->obj;
    Exception_cleanup(exception);
    return exception;
}

__attribute__((noreturn)) void scalanative_throw(Exception obj) {
    ExceptionWrapper *exceptionWrapper =
        scalanative_Throwable_exceptionWrapper(obj);
    exceptionWrapper->unwindException.exception_cleanup =
        generic_exception_cleanup;
    exceptionWrapper->obj = obj;
    _Unwind_Exception *unwindException = &exceptionWrapper->unwindException;
    _Unwind_Reason_Code code = _Unwind_RaiseException(unwindException);

    if (code == _URC_END_OF_STACK) {
        generic_exception_cleanup(code, &exceptionWrapper->unwindException);
        fprintf(stderr,
                "ScalaNative Fatal Error: Failed to throw exception, not found "
                "a valid catch handler for exception when unwinding execution "
                "stack.\n");
        scalanative_Throwable_showStackTrace(obj);
        abort();
    }
    scalanative_Throwable_showStackTrace(obj);
    fprintf(stderr,
            "Scala Native Fatal Error: Unhandled exception: "
            "_Unwind_RaiseException returned %d\n",
            code);
    abort();
}
#endif
