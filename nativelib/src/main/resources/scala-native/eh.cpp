#include "unwind.h"
#include <stdlib.h>
#include <stdio.h>

extern "C" {

// gets the GenericException from the _Unwind_Exception which is at the end of
// it. +1 goes to the end of the struct since since it adds with the size of
// _Unwind_Exception, then we cast to GenericException and we do - 1 to
// go back of sizeof GenericException
#define GetGenericException(unwind_exception)                                  \
    ((struct GenericException *)(unwind_exception + 1) - 1)

// Define an exception object with a void * obj
struct GenericException {
    void *obj;                                 // The exception payload (void *)
    struct _Unwind_Exception unwind_exception; // Base exception structure
};

// Cleanup function for the exception
void generic_exception_cleanup(_Unwind_Reason_Code code,
                               struct _Unwind_Exception *exception) {
    struct GenericException *generic_exception = GetGenericException(exception);
    free(generic_exception); // Free the allocated memory
}

typedef const uint8_t *LSDA_ptr;

uint64_t read_uleb_128(const uint8_t **data) {
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

int64_t read_sleb_128(const uint8_t **data) {
    uint64_t result = 0;
    int shift = 0;
    uint8_t byte = 0;
    auto p = *data;
    do {
        byte = *p;
        p++;
        result |= (byte & 0b1111111) << shift;
        shift += 7;
    } while (byte & 0b10000000);
    if ((byte & 0x40) && (shift < (sizeof(result) << 3))) {
        result |= static_cast<uintptr_t>(~0) << shift;
    }
    return result;
}

struct LSDA_call_site_Header {
    LSDA_call_site_Header(LSDA_ptr *lsda) {
        LSDA_ptr read_ptr = *lsda;
        encoding = read_ptr[0];
        *lsda += 1;
        length = read_uleb_128(lsda);
    }

    LSDA_call_site_Header() = default;

    uint8_t encoding;
    uint64_t length;
};

struct Action {
    uint8_t type_index;
    int8_t next_offset;
    LSDA_ptr my_ptr;
};

struct LSDA_call_site {
    explicit LSDA_call_site(LSDA_ptr *lsda) {
        LSDA_ptr read_ptr = *lsda;
        start = read_uleb_128(lsda);
        len = read_uleb_128(lsda);
        landing_pad = read_uleb_128(lsda);
        action = read_uleb_128(lsda);
    }

    LSDA_call_site() = default;

    bool has_landing_pad() const { return landing_pad; }

    bool valid_for_throw_ip(_Unwind_Context *context) const {
        uintptr_t func_start = _Unwind_GetRegionStart(context);
        uintptr_t try_start = func_start + start;
        uintptr_t try_end = try_start + len;
        uintptr_t throw_ip = _Unwind_GetIP(context) - 1;
        if (throw_ip > try_end || throw_ip < try_start) {
            return false;
        }
        return true;
    }

    uint64_t start;
    uint64_t len;
    uint64_t landing_pad;
    uint64_t action;
};

struct LSDA {
    explicit LSDA(_Unwind_Context *context) {
        lsda = (uint8_t *)_Unwind_GetLanguageSpecificData(context);
        start_encoding = lsda[0];
        type_encoding = lsda[1];
        lsda += 2;
        if (type_encoding != 0xff) { // TODO: think
            type_table_offset = read_uleb_128(&lsda);
        }
        types_table_start = ((const int *)(lsda + type_table_offset));
        call_site_header = LSDA_call_site_Header(&lsda);
        call_site_table_end = lsda + call_site_header.length;
        next_call_site_ptr = lsda;
        action_table_start = call_site_table_end;
    }

    LSDA_call_site *get_next_call_site() {
        if (next_call_site_ptr > call_site_table_end) {
            return nullptr;
        }
        next_call_site = LSDA_call_site(&next_call_site_ptr);
        return &next_call_site;
    }

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

    Action *get_first_action(LSDA_call_site *call_site) {
        if (call_site->action == 0) {
            return nullptr;
        }
        LSDA_ptr raw_ptr = action_table_start + call_site->action - 1;
        current_action.type_index = raw_ptr[0];
        raw_ptr++;
        current_action.next_offset = read_sleb_128(&raw_ptr);
        current_action.my_ptr = raw_ptr;
        return &current_action;
    }

    Action *get_next_action() {
        if (current_action.next_offset == 0) {
            return nullptr;
        }
        LSDA_ptr raw_ptr = current_action.my_ptr + current_action.next_offset;
        current_action.type_index = raw_ptr[0];
        raw_ptr++;
        current_action.next_offset = read_sleb_128(&raw_ptr);
        current_action.my_ptr = raw_ptr;
        return &current_action;
    }
};

_Unwind_Reason_Code set_landing_pad(_Unwind_Context *context,
                                    _Unwind_Exception *unwind_exception,
                                    uintptr_t landing_pad, uint8_t type_index) {
    int r0 = __builtin_eh_return_data_regno(0);
    int r1 = __builtin_eh_return_data_regno(1);

    _Unwind_SetGR(context, r0, (uintptr_t)(unwind_exception));
    _Unwind_SetGR(context, r1, (uintptr_t)(type_index));

    _Unwind_SetIP(context, landing_pad);

    return _URC_INSTALL_CONTEXT;
}

// A personality function to catch all exceptions
_Unwind_Reason_Code
scalanative_personality(int version, _Unwind_Action actions,
                        uint64_t exception_class,
                        struct _Unwind_Exception *unwind_exception,
                        struct _Unwind_Context *context) {
    LSDA header(context);
    bool have_cleanup = false;

    // Loop through each entry in the call_site table
    for (LSDA_call_site *call_site = header.get_next_call_site(); call_site;
         call_site = header.get_next_call_site()) {

        if (call_site->has_landing_pad()) {
            uintptr_t func_start = _Unwind_GetRegionStart(context);
            if (!call_site->valid_for_throw_ip(context)) {
                continue;
            }
            struct GenericException *generic_exception =
                GetGenericException(unwind_exception);
            if (call_site->action == 0 && actions & _UA_CLEANUP_PHASE) {
                // clean up block?
                return set_landing_pad(context, unwind_exception,
                                       func_start + call_site->landing_pad, 0);
            }
            for (Action *action = header.get_first_action(call_site); action;
                 action = header.get_next_action()) {
                if (action->type_index == 0) {
                    if (actions & _UA_CLEANUP_PHASE) {
                        set_landing_pad(context, unwind_exception,
                                        func_start + call_site->landing_pad, 0);
                        have_cleanup = true;
                    }
                } else {
                    if (actions & _UA_SEARCH_PHASE) {
                        return _URC_HANDLER_FOUND;
                    } else if (actions & _UA_CLEANUP_PHASE) {
                        return set_landing_pad(context, unwind_exception,
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
    GenericException *genericException = GetGenericException(unwindException);
    void *exception = genericException->obj;

    free(genericException);

    return exception;
}

// Throw function to raise a GenericException
void scalanative_throw(void *obj) {
    // Allocate and initialize the exception object
    // TODO: We could add space inside java.lang.Throwable to store
    // _UnwindException so we don't need to malloc at all
    struct GenericException *exception =
        (struct GenericException *)malloc(sizeof(struct GenericException));
    if (!exception) {
        perror("Failed to allocate memory for exception");
        abort();
    }

    exception->unwind_exception.exception_cleanup = generic_exception_cleanup;
    exception->obj = obj;

    _Unwind_Reason_Code code =
        _Unwind_RaiseException(&exception->unwind_exception);

    if (code == _URC_END_OF_STACK) {
        printf("No handler found for exception. Exiting.\n");
        generic_exception_cleanup(code, &exception->unwind_exception);
        abort();
    } else {
        printf("Unhandled exception: _Unwind_RaiseException returned %d\n",
               code);
        abort();
    }
}

_Unwind_Reason_Code __gxx_personality_v0(int, _Unwind_Action actions,
                                         uint64_t exceptionClass,
                                         _Unwind_Exception *unwind_exception,
                                         _Unwind_Context *context) {
    printf("__gxx_personality_v0 Provided as a stub. Should never be called\n");
    abort();
}
}
