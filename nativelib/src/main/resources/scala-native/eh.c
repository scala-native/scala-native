#ifndef SCALANATIVE_USING_CPP_EXCEPTIONS

#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include <string.h>
#include "eh_diagnostics.h"
#include "string_constants.h"
#include "unwind.h"

#if defined(__SCALANATIVE_DELIMCC)
#include "delimcc.h"
#include <setjmp.h>
#endif

#if defined(__SCALANATIVE_DELIMCC)
#include "delimcc.h"
#include <setjmp.h>
#endif

// gets the ExceptionWrapper from the _Unwind_Exception which is at the end of
// it. +1 goes to the end of the struct since it adds with the size of
// _Unwind_Exception, then we cast to ExceptionWrapper and we do - 1 to
// go back of sizeof ExceptionWrapper
#define GetExceptionWrapper(unwindException)                                   \
    ((ExceptionWrapper *)(unwindException + 1) - 1)

typedef void *Exception;
typedef void (*OnCatchHandler)(Exception);

/*
 * Continuation exception escape: when _Unwind_RaiseException returns
 * _URC_END_OF_STACK (no handler found in the resumed stack), we longjmp to
 * the resumer (in delimcc.c) instead of aborting. delimcc.c sets
 * scalanative_continuation_exception_handler before resume and clears it after
 * longjmp or normal return. Local try/catch inside the continuation body still
 * runs (unwinding finds them first); we only escape when no handler was found.
 */
typedef struct ExceptionWrapper {
    Exception obj;
    _Unwind_Exception unwindException;
} ExceptionWrapper;

static _Thread_local ExceptionWrapper fallbackExceptionWrapper;

extern OnCatchHandler scalanative_Throwable_onCatchHandler(Exception exception);
extern void scalanative_Throwable_showStackTrace(Exception exception);
extern ExceptionWrapper *
scalanative_Throwable_exceptionWrapper(Exception exception);
extern int scalanative_unwind_get_proc_name_by_ip(size_t ip, char *buffer,
                                                  size_t length,
                                                  size_t *offset);

#define EH_DIAG_MAX_FRAMES 32
#define EH_DIAG_SITES 8

typedef struct EhDiagSite {
    uint64_t start;
    uint64_t len;
    uint64_t landing_pad;
    uint64_t action;
    uintptr_t try_start;
    uintptr_t try_end;
    int in_range_excl;
    int in_range_incl;
} EhDiagSite;

typedef struct EhDiagFrame {
    int actions;
    uintptr_t ip;
    uintptr_t ipm1;
    uintptr_t func_start;
    uintptr_t cfa;
    const void *lsda;
    uint8_t encoding;
    uint8_t start_encoding;
    uint8_t type_encoding;
    uint64_t call_site_table_len;
    int n_sites;
    int n_landing;
    int n_in_range_excl;
    int n_in_range_incl;
    int n_recorded;
    int truncated;
    EhDiagSite sites[EH_DIAG_SITES];
} EhDiagFrame;

static _Thread_local EhDiagFrame eh_diag_frames[EH_DIAG_MAX_FRAMES];
static _Thread_local int eh_diag_nframes;
static _Thread_local int eh_diag_overflow;

static void eh_diag_reset(void) {
    eh_diag_nframes = 0;
    eh_diag_overflow = 0;
}

static void eh_diag_abort(const char *detail, int unwind_code, Exception obj)
    __attribute__((noreturn));

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

uint64_t read_call_site_value(LSDA_ptr *data, uint8_t encoding) {
    switch (encoding) {
    case 0x01: // DW_EH_PE_uleb128: LLVM's default call-site encoding.
        return read_uleb_128(data);
    case 0x02: { // DW_EH_PE_udata2: compact fixed-width call-site offsets.
        uint16_t result;
        memcpy(&result, *data, sizeof(result));
        *data += sizeof(result);
        return result;
    }
    case 0x03: { // DW_EH_PE_udata4: used by LLVM's RISC-V backend.
        uint32_t result;
        memcpy(&result, *data, sizeof(result));
        *data += sizeof(result);
        return result;
    }
    case 0x04: { // DW_EH_PE_udata8: large fixed-width call-site offsets.
        uint64_t result;
        memcpy(&result, *data, sizeof(result));
        *data += sizeof(result);
        return result;
    }
    default: {
        char detail[96];
        snprintf(detail, sizeof(detail),
                 "Unsupported LSDA call-site encoding during exception "
                 "handling: 0x%02x",
                 encoding);
        eh_diag_abort(detail, (int)encoding, NULL);
    }
    }
}

void LSDA_call_site_init(LSDA_call_site *callSite, LSDA_ptr *lsda,
                         uint8_t encoding) {
    callSite->start = read_call_site_value(lsda, encoding);
    callSite->len = read_call_site_value(lsda, encoding);
    callSite->landing_pad = read_call_site_value(lsda, encoding);
    callSite->action = read_uleb_128(lsda);
}

bool LSDA_call_site_valid_for_throw_ip(const LSDA_call_site *callSite,
                                       _Unwind_Context *context) {
    uintptr_t func_start = _Unwind_GetRegionStart(context);
    uintptr_t try_start = func_start + callSite->start;
    uintptr_t try_end = try_start + callSite->len;
    uintptr_t throw_ip = _Unwind_GetIP(context) - 1;
    if (throw_ip >= try_end || throw_ip < try_start) {
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
    if (lsda->next_call_site_ptr >= lsda->call_site_table_end) {
        return NULL;
    }
    LSDA_call_site_init(&lsda->next_call_site, &lsda->next_call_site_ptr,
                        lsda->call_site_header.encoding);
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

static void eh_diag_record_site(EhDiagFrame *frame, const LSDA_call_site *cs,
                                uintptr_t func_start, uintptr_t ipm1) {
    uintptr_t try_start = func_start + (uintptr_t)cs->start;
    uintptr_t try_end = try_start + (uintptr_t)cs->len;
    int in_excl = ipm1 >= try_start && ipm1 < try_end;
    int in_incl = ipm1 >= try_start && ipm1 <= try_end;
    if (cs->landing_pad)
        frame->n_landing++;
    if (in_excl)
        frame->n_in_range_excl++;
    if (in_incl)
        frame->n_in_range_incl++;

    int record = in_incl || frame->n_recorded < EH_DIAG_SITES;
    if (!record) {
        frame->truncated = 1;
        return;
    }
    int idx = frame->n_recorded;
    if (idx >= EH_DIAG_SITES) {
        /* Prefer in-range sites over the last generic one. */
        if (!in_incl) {
            frame->truncated = 1;
            return;
        }
        idx = EH_DIAG_SITES - 1;
        frame->truncated = 1;
    } else {
        frame->n_recorded++;
    }
    EhDiagSite *site = &frame->sites[idx];
    site->start = cs->start;
    site->len = cs->len;
    site->landing_pad = cs->landing_pad;
    site->action = cs->action;
    site->try_start = try_start;
    site->try_end = try_end;
    site->in_range_excl = in_excl;
    site->in_range_incl = in_incl;
}

static void eh_diag_record_frame(_Unwind_Context *context, int actions) {
    if (!(actions & _UA_SEARCH_PHASE))
        return;
    if (eh_diag_nframes >= EH_DIAG_MAX_FRAMES) {
        eh_diag_overflow++;
        return;
    }
    EhDiagFrame *frame = &eh_diag_frames[eh_diag_nframes++];
    memset(frame, 0, sizeof(*frame));
    frame->actions = actions;
    frame->ip = _Unwind_GetIP(context);
    frame->ipm1 = frame->ip ? frame->ip - 1 : 0;
    frame->func_start = _Unwind_GetRegionStart(context);
    frame->cfa = _Unwind_GetCFA(context);
    frame->lsda = (const void *)_Unwind_GetLanguageSpecificData(context);
    if (frame->lsda == NULL)
        return;

    LSDA header;
    LSDA_init(&header, context);
    frame->encoding = header.call_site_header.encoding;
    frame->start_encoding = header.start_encoding;
    frame->type_encoding = header.type_encoding;
    frame->call_site_table_len = header.call_site_header.length;
    for (LSDA_call_site *cs = LSDA_get_next_call_site(&header); cs;
         cs = LSDA_get_next_call_site(&header)) {
        frame->n_sites++;
        eh_diag_record_site(frame, cs, frame->func_start, frame->ipm1);
    }
}

static void eh_diag_print_symbol(uintptr_t ip) {
    char sym[256];
    memset(sym, 0, sizeof(sym));
    size_t offset = 0;
    scalanative_unwind_get_proc_name_by_ip((size_t)ip, sym, sizeof(sym),
                                           &offset);
    fprintf(stderr, "%s+0x%zx", sym[0] ? sym : "??", offset);
}

static void eh_diag_print_frames(void) {
    fprintf(stderr,
            "LSDA search frames: %d recorded, %d overflowed (search phase "
            "only)\n",
            eh_diag_nframes, eh_diag_overflow);
    for (int i = 0; i < eh_diag_nframes; i++) {
        EhDiagFrame *frame = &eh_diag_frames[i];
        fprintf(stderr,
                "  frame %d actions=0x%x ip=%p ip-1=%p func=%p cfa=%p "
                "lsda=%p enc=0x%02x start_enc=0x%02x type_enc=0x%02x "
                "cs_len=%llu sites=%d landing=%d in_range[excl=%d incl=%d] ",
                i, frame->actions, (void *)frame->ip, (void *)frame->ipm1,
                (void *)frame->func_start, (void *)frame->cfa, frame->lsda,
                frame->encoding, frame->start_encoding, frame->type_encoding,
                (unsigned long long)frame->call_site_table_len, frame->n_sites,
                frame->n_landing, frame->n_in_range_excl,
                frame->n_in_range_incl);
        if (frame->n_in_range_incl != frame->n_in_range_excl)
            fprintf(stderr, " RANGE_INCL_EXCL_MISMATCH");
        fprintf(stderr, " ");
        eh_diag_print_symbol(frame->ip);
        fprintf(stderr, "%s\n", frame->truncated ? " [sites truncated]" : "");
        for (int s = 0; s < frame->n_recorded; s++) {
            EhDiagSite *site = &frame->sites[s];
            fprintf(stderr,
                    "    site[%d] start=%llu len=%llu pad=%llu action=%llu "
                    "try=[%p,%p) excl=%d incl=%d ip-1==end=%d\n",
                    s, (unsigned long long)site->start,
                    (unsigned long long)site->len,
                    (unsigned long long)site->landing_pad,
                    (unsigned long long)site->action, (void *)site->try_start,
                    (void *)site->try_end, site->in_range_excl,
                    site->in_range_incl, frame->ipm1 == site->try_end);
        }
    }
    fflush(stderr);
}

static void eh_diag_abort(const char *detail, int unwind_code, Exception obj) {
    /* Enter first so any throw from dump helpers aborts instead of
     * recursively printing thousands of fatal errors. */
    scalanative_eh_enter_abort_dump();
    fprintf(stderr, "%s %s\n", snFatalErrorPrefix, detail);
    fprintf(stderr, "exception object=%p\n", (void *)obj);
    /* LSDA notes were captured during search and are C-only. Print them
     * before any Scala callback. */
    eh_diag_print_frames();
    scalanative_eh_dump_abort_context(detail, unwind_code);
    if (obj != NULL && scalanative_eh_java_thread_available())
        scalanative_Throwable_showStackTrace(obj);
    fflush(stderr);
    fflush(stdout);
    abort();
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
    eh_diag_record_frame(context, actions);
    if (_Unwind_GetLanguageSpecificData(context) == 0) {
        return _URC_CONTINUE_UNWIND;
    }
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

Exception scalanative_catch(_Unwind_Exception *unwindException) {
    ExceptionWrapper *exceptionWrapper = GetExceptionWrapper(unwindException);
    Exception exception = exceptionWrapper->obj;
    Exception_cleanup(exception);
    return exception;
}

__attribute__((noreturn)) void scalanative_throw(Exception obj) {
    eh_diag_reset();
    ExceptionWrapper *exceptionWrapper =
        scalanative_Throwable_exceptionWrapper(obj);
    if (exceptionWrapper == NULL)
        exceptionWrapper = &fallbackExceptionWrapper;
    exceptionWrapper->unwindException.exception_cleanup =
        generic_exception_cleanup;
    exceptionWrapper->obj = obj;
    _Unwind_Exception *unwindException = &exceptionWrapper->unwindException;
    _Unwind_Reason_Code code = _Unwind_RaiseException(unwindException);

    if (code == _URC_END_OF_STACK) {
#if defined(__SCALANATIVE_DELIMCC)
        /* If we're inside a resumed continuation, escape to the resumer instead
         * of aborting. Unwinding already ran and found no handler (or could not
         * traverse the copied stack); local try/catch in the continuation body
         * would have been found first if present. */
        ContinuationExceptionHandler ceh =
            scalanative_continuation_exception_handler();
        if (ceh.env != NULL && ceh.exception_slot != NULL) {
            jmp_buf *env = ceh.env;
            *ceh.exception_slot = obj;
            scalanative_continuation_exception_handler_clear();
            // Do not run exception cleanup; we're transferring to the resumer.
            longjmp(*env, 1);
            __builtin_unreachable();
        }
        if (scalanative_continuation_exception_escape(obj)) {
            __builtin_unreachable();
        }
#endif
        /* Skip exception cleanup: it can re-enter throw on a dying thread. */
        eh_diag_abort("Failed to throw exception, not found a valid catch "
                      "handler for exception when unwinding execution stack.",
                      (int)code, obj);
    }
    char detail[96];
    snprintf(detail, sizeof(detail),
             "Unhandled exception: _Unwind_RaiseException returned %d",
             (int)code);
    eh_diag_abort(detail, (int)code, obj);
}
#endif
