#ifndef SCALANATIVE_USING_CPP_EXCEPTIONS

#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include "unwind.h"

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

size_t scalanative_Throwable_sizeOfExceptionWrapperr() {
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

uint64_t read_uleb_128(LSDA_ptr *data) {
    uint64_t result = 0;
    int shift = 0;
    uint8_t byte = 0;
    do {
        byte = **data;
        (*data)++;
        result |= (byte & 0b1111111) << shift;
        shift += 7;
    } while (byte & 0b10000000);
    return result;
}

uint64_t read_sleb_128(LSDA_ptr *data) {
    uint64_t result = 0;
    int shift = 0;
    uint8_t byte = 0;
    const uint8_t *p = *data;
    do {
        byte = *p;
        p++;
        result |= (byte & 0b1111111) << shift;
        shift += 7;
    } while (byte & 0b10000000);
    if ((byte & 0x40) && (shift < (sizeof(result) << 3))) {
        result |= (uintptr_t)(~0) << shift;
    }
    return result;
}

typedef struct LSDA_call_site_Header {
    uint8_t encoding;
    uint64_t length;
} LSDA_call_site_Header;

void LSDA_call_site_Header_init(LSDA_call_site_Header *header, LSDA_ptr *lsda) {
    LSDA_ptr read_ptr = *lsda;
    header->encoding = read_ptr[0];
    *lsda += 1;
    header->length = read_uleb_128(lsda);
}

typedef struct Action {
    uint8_t type_index;
    int8_t next_offset;
    LSDA_ptr my_ptr;
} Action;

typedef struct LSDA_call_site {
    uint64_t start;
    uint64_t len;
    uint64_t landing_pad;
    uint64_t action;
} LSDA_call_site;

void LSDA_call_site_init(LSDA_call_site *callSite, LSDA_ptr *lsda) {
    callSite->start = read_uleb_128(lsda);
    callSite->len = read_uleb_128(lsda);
    callSite->landing_pad = read_uleb_128(lsda);
    callSite->action = read_uleb_128(lsda);
}

bool LSDA_call_site_valid_for_throw_ip(const LSDA_call_site *callSite,
                                       _Unwind_Context *context) {
    uintptr_t func_start = _Unwind_GetRegionStart(context);
    uintptr_t try_start = func_start + callSite->start;
    uintptr_t try_end = try_start + callSite->len;
    uintptr_t throw_ip = _Unwind_GetIP(context) - 1;
    if (throw_ip > try_end || throw_ip < try_start) {
        return false;
    }
    return true;
}

typedef struct LSDA {
    uint8_t start_encoding;
    uint8_t type_encoding;
    uint64_t type_table_offset;

    LSDA_ptr lsda;
    LSDA_ptr call_site_table_end;
    LSDA_call_site next_call_site;
    LSDA_ptr next_call_site_ptr;
    LSDA_call_site_Header call_site_header;
    LSDA_ptr action_table_start;
    Action current_action;
    const int *types_table_start;
} LSDA;

void LSDA_init(LSDA *lsda, _Unwind_Context *context) {
    lsda->lsda = (uint8_t *)_Unwind_GetLanguageSpecificData(context);
    lsda->start_encoding = lsda->lsda[0];
    lsda->type_encoding = lsda->lsda[1];
    lsda->lsda += 2;
    if (lsda->type_encoding != 0xff) {
        lsda->type_table_offset = read_uleb_128(&lsda->lsda);
    }
    lsda->types_table_start =
        ((const int *)(lsda->lsda + lsda->type_table_offset));
    LSDA_call_site_Header_init(&lsda->call_site_header, &lsda->lsda);
    lsda->call_site_table_end = lsda->lsda + lsda->call_site_header.length;
    lsda->next_call_site_ptr = lsda->lsda;
    lsda->action_table_start = lsda->call_site_table_end;
}

LSDA_call_site *LSDA_get_next_call_site(LSDA *lsda) {
    if (lsda->next_call_site_ptr > lsda->call_site_table_end) {
        return NULL;
    }
    LSDA_call_site_init(&lsda->next_call_site, &lsda->next_call_site_ptr);
    return &lsda->next_call_site;
}

Action *LSDA_get_first_action(LSDA *lsda, LSDA_call_site *call_site) {
    if (call_site->action == 0) {
        return NULL;
    }
    LSDA_ptr raw_ptr = lsda->action_table_start + call_site->action - 1;
    lsda->current_action.type_index = raw_ptr[0];
    raw_ptr++;
    lsda->current_action.next_offset = read_sleb_128(&raw_ptr);
    lsda->current_action.my_ptr = raw_ptr;
    return &lsda->current_action;
}

Action *LSDA_get_next_action(LSDA *lsda) {
    if (lsda->current_action.next_offset == 0) {
        return NULL;
    }
    LSDA_ptr raw_ptr =
        lsda->current_action.my_ptr + lsda->current_action.next_offset;
    lsda->current_action.type_index = raw_ptr[0];
    raw_ptr++;
    lsda->current_action.next_offset = read_sleb_128(&raw_ptr);
    lsda->current_action.my_ptr = raw_ptr;
    return &lsda->current_action;
}

_Unwind_Reason_Code set_landing_pad(_Unwind_Context *context,
                                    _Unwind_Exception *unwindException,
                                    uintptr_t landing_pad, uint8_t type_index) {
    int r0 = __builtin_eh_return_data_regno(0);
    int r1 = __builtin_eh_return_data_regno(1);

    _Unwind_SetGR(context, r0, (uintptr_t)(unwindException));
    _Unwind_SetGR(context, r1, (uintptr_t)(type_index));

    _Unwind_SetIP(context, landing_pad);

    return _URC_INSTALL_CONTEXT;
}

// A personality function to catch all exceptions
_Unwind_Reason_Code scalanative_personality(int version, _Unwind_Action actions,
                                            uint64_t exception_class,
                                            _Unwind_Exception *unwindException,
                                            _Unwind_Context *context) {
    LSDA header;
    LSDA_init(&header, context);
    bool have_cleanup = false;

    // Loop through each entry in the call_site table
    for (LSDA_call_site *call_site = LSDA_get_next_call_site(&header);
         call_site; call_site = LSDA_get_next_call_site(&header)) {

        if (call_site->landing_pad) {
            uintptr_t func_start = _Unwind_GetRegionStart(context);
            if (!LSDA_call_site_valid_for_throw_ip(call_site, context)) {
                continue;
            }
            ExceptionWrapper *exceptionWrapper =
                GetExceptionWrapper(unwindException);
            if (call_site->action == 0 && actions & _UA_CLEANUP_PHASE) {
                // clean up block?
                return set_landing_pad(context, unwindException,
                                       func_start + call_site->landing_pad, 0);
            }
            for (Action *action = LSDA_get_first_action(&header, call_site);
                 action; action = LSDA_get_next_action(&header)) {
                if (action->type_index == 0) {
                    if (actions & _UA_CLEANUP_PHASE) {
                        set_landing_pad(context, unwindException,
                                        func_start + call_site->landing_pad, 0);
                        have_cleanup = true;
                    }
                } else {
                    if (actions & _UA_SEARCH_PHASE) {
                        return _URC_HANDLER_FOUND;
                    } else if (actions & _UA_CLEANUP_PHASE) {
                        return set_landing_pad(context, unwindException,
                                               func_start +
                                                   call_site->landing_pad,
                                               action->type_index);
                    }
                }
            }
        }
    }

    if ((actions & _UA_CLEANUP_PHASE) && have_cleanup) {
        return _URC_INSTALL_CONTEXT;
    }
    return _URC_CONTINUE_UNWIND;
}

void *scalanative_catch(_Unwind_Exception *unwindException) {
    ExceptionWrapper *exceptionWrapper = GetExceptionWrapper(unwindException);
    void *exception = exceptionWrapper->obj;
    Exception_cleanup(exception);
    return exception;
}

void scalanative_throw(void *obj) {
    ExceptionWrapper *exceptionWrapper =
        scalanative_Throwable_exceptionWrapper(obj);
    exceptionWrapper->unwindException.exception_cleanup =
        generic_exception_cleanup;
    exceptionWrapper->obj = obj;

    _Unwind_Reason_Code code =
        _Unwind_RaiseException(&exceptionWrapper->unwindException);

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
