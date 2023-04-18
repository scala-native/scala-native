#include "delimcc.h"
#include <stddef.h>
#include <stdio.h>
#include <assert.h>
#include <threads.h>
#include <stdlib.h>
#include <string.h>

// Defined symbols here:
// - ASM_JMPBUF_SIZE: The size of the jmpbuf, should be a constant defined in
// `setjmp.S`.
// - JMPBUF_STACK_POINTER_OFFSET: The offset within the jmpbuf where
//   the stack pointer is located. Should be defined in `setjmp.S`.
#if defined(__aarch64__)
#define ASM_JMPBUF_SIZE 192
#define JMPBUF_STACK_POINTER_OFFSET (104 / 8)
#elif defined(__x86_64__) && defined(__linux__)
#define ASM_JMPBUF_SIZE 72
#define JMPBUF_STACK_POINTER_OFFSET (16 / 8)
#endif

// The return address is always stored in stack_btm - BOUNDARY_LR_OFFSET.
// See `_lh_boundary_entry`.
#define BOUNDARY_LR_OFFSET 8

#define __externc extern
#define __noreturn __attribute__((noreturn))
#define __returnstwice __attribute__((returns_twice))
// define the lh_jmp_buf in terms of `void*` elements to have natural alignment
typedef void *lh_jmp_buf[ASM_JMPBUF_SIZE / sizeof(void *)];
// Non-standard setjmp.
__externc __returnstwice int _lh_setjmp(lh_jmp_buf buf);
// Jumps to the given setjmp'd buffer after storing the return address to *lr.
// Always returns 1 to setjmp.
__externc void *_lh_store_lr_longjmp(lh_jmp_buf buf, void *lr);
// Jumps to the given setjmp'd buffer, returning arg as the value.
// arg must be non-zero.
__externc __noreturn void _lh_longjmp(lh_jmp_buf buf, int arg);
// Stores the return address in sp+8, then calls __cont_boundary_impl.
__externc __returnstwice void *_lh_boundary_entry(ContFn *f, void *arg);
// Returns the stack pointer of the calling function.
__externc void *_lh_get_sp();

// Label counter
volatile static ContLabel label_count = 0;

typedef struct Handler {
    ContLabel id;
    void *stack_btm; // where the bottom is, should be changed
    void **result;   // where the result is stored, should be changed
    lh_jmp_buf buf;  // jmp buf
} Handler;

// handler chain handling functions
typedef struct Handlers {
    Handler *h;
    struct Handlers *next;
} Handlers;

volatile static thread_local Handlers *__handlers = NULL;

static void handler_push(Handler *h) {
    // fprintf(stderr, "Pushing handler with label %lu\n", h->id);
    Handlers *hs = malloc(sizeof(Handlers));
    hs->h = h;
    hs->next = (Handlers *)__handlers;
    __handlers = hs;
}

static void handler_pop() {
    assert(__handlers != NULL);
    // fprintf(stderr, "Popping handler with label %lu\n", __handlers->h->id);
    Handlers *old = (Handlers *)__handlers;
    __handlers = __handlers->next;
    free(old);
}

static void handler_install(Handlers *hs) {
    assert(hs != NULL);
    Handlers *tail = hs;
    while (tail->next != NULL)
        tail = tail->next;
    tail->next = (Handlers *)__handlers;
    __handlers = hs;
}

Handlers *handler_split_at(ContLabel l) {
    Handlers *ret = (Handlers *)__handlers, **cur = &ret;
    while ((*cur)->h->id != l)
        cur = (&(*cur)->next);
    __handlers = (*cur)->next;
    (*cur)->next = NULL;
    return ret;
}

__attribute__((noinline, optnone, returns_twice, no_stack_protector)) void *
__cont_boundary_impl(void **btm, ContFn *f, void *arg)
    __attribute__((disable_tail_calls)) {
    fprintf(stderr, "Boundary btm is %p\n", btm);

    // allocate handlers and such
    void *result = NULL;
    // ContResult local_r = {.cont = NULL, .in = NULL};
    Handler h = {
        .id = ++label_count,
        .stack_btm = btm,
        .result = &result,
    };
    // fprintf(stderr, "Setting up result slot at %p\n", &result);
    ContBoundaryLabel l = {.id = h.id};
    handler_push(&h);

    // setjmp and call
    if (_lh_setjmp(h.buf) == 0) {
        result = f(l, arg);
        handler_pop();
    } else {
    }
    return result;
}

// boundary : BoundaryFn -> Result
// Result MUST BE HEAP ALLOCATED
__attribute__((noinline, optnone, returns_twice, no_stack_protector)) void *
cont_boundary(ContFn *f, void *arg) __attribute__((disable_tail_calls)) {
    return _lh_boundary_entry(f, arg);
}

// ========== SUSPENDING ===========

struct Continuation {
    void *stack;
    void *stack_top;
    ptrdiff_t size;

    Handlers *handlers;

    void **return_slot;
    lh_jmp_buf buf;
};

// suspend[T, R] : BoundaryLabel[T] -> T -> R
__attribute__((noinline, optnone)) void *cont_suspend(ContBoundaryLabel b,
                                                      SuspendFn *f, void *arg)
    __attribute__((disable_tail_calls)) {
    // set up the continuation
    Continuation *cont = malloc(sizeof(Continuation));
    cont->stack_top = _lh_get_sp();
    cont->handlers = handler_split_at(b.id);
    Handlers *last_handler = cont->handlers;
    while (last_handler->next != NULL)
        last_handler = last_handler->next;
    assert(last_handler->h->stack_btm != NULL); // not a resume handler
    cont->size = last_handler->h->stack_btm - cont->stack_top;
    // make the continuation size a multiple of 16
    cont->stack = malloc(cont->size);
    memcpy(cont->stack, cont->stack_top, cont->size);

    // set up return value slot
    void *ret_val = NULL;
    cont->return_slot = &ret_val;

    // assign it to the handler's return value
    *last_handler->h->result = f(cont, arg);
    // fprintf(stderr, "Putting result %p to slot %p\n",
    // *last_handler->h->result,
    //         last_handler->h->result);

    // we will be back...
    if (_lh_setjmp(cont->buf) == 0) {
        _lh_longjmp(last_handler->h->buf, 1);
    } else {
        // We're back, just collect value from return slot.
        return ret_val;
    }
}

#define fixedAddr(X) (void *)(X) + diff
#define fixAddr(X) X = fixedAddr(X);

static void jmpbuf_fix(lh_jmp_buf buf, ptrdiff_t diff) {
    // fprintf(stderr, "> fixing sp at %p from %p", buf,
    //         buf[JMPBUF_STACK_POINTER_OFFSET]);
    fixAddr(buf[JMPBUF_STACK_POINTER_OFFSET]);
    // fprintf(stderr, " to %p\n", buf[JMPBUF_STACK_POINTER_OFFSET]);
}

__attribute__((noinline)) static Handlers *handler_clone_fix(Handlers *other,
                                                             ptrdiff_t diff,
                                                             Continuation *cont,
                                                             void *stack) {
    Handlers *nw = NULL, **cur = &nw;
    while (other != NULL) {
        *cur = malloc(sizeof(Handlers));
        (*cur)->h = (Handler *)((void *)other->h + diff);
        // fix the handlers in cont->stack
        {
            Handler *nh_in_mem =
                (Handler *)((void *)other->h - cont->stack_top + stack);
            fixAddr(nh_in_mem->result);
            if (nh_in_mem->stack_btm != NULL)
                fixAddr(nh_in_mem->stack_btm);
            jmpbuf_fix(nh_in_mem->buf, diff);
        }
        cur = &(*cur)->next;
        other = other->next;
    }
    *cur = NULL;
    return nw;
}

__attribute__((noinline)) static void call_memcpy(void *dest, void *src,
                                                  ptrdiff_t size) {
    memcpy(dest, src, size);
}

__attribute__((noinline, optnone, no_stack_protector)) static void *
resumeImpl(Continuation *cont, void *out);

// resume[T, R] : Continuation[T, R] -> R -> Result
// Consumes the Continuation.
__attribute__((noinline, optnone, no_stack_protector)) static void *
resumeImpl(Continuation *cont, void *out) {
    // Allocate all values up front so we know how many to deal with.
    Handlers *nw; // new handler chain
    ptrdiff_t i;
    ptrdiff_t diff;         // pointer difference and stack size
    void *target;           // our target stack
    void **new_return_slot; // new return slot
    void *return_buf;

    void *result;
    void *stack;

    void *stackTop; // our stack top
    // get the stack difference
    stackTop = _lh_get_sp();
    target = stackTop - cont->size;
    diff = target - cont->stack_top;
    // set up stuff
    return_buf = malloc(ASM_JMPBUF_SIZE);
    call_memcpy(return_buf, cont->buf, ASM_JMPBUF_SIZE);
    stack = malloc(cont->size);
    call_memcpy(stack, cont->stack, cont->size);
    assert((diff & 15) == 0);
    fprintf(
        stderr,
        "diff is %ld, stack (size = %ld) goes %p~%p -> %p~%p | on heap = %p\n",
        diff, cont->size, cont->stack_top, cont->stack_top + cont->size, target,
        stackTop, stack);
    // clone the handler chain, with fixes.
    nw = handler_clone_fix(cont->handlers, diff, cont, stack);
    // install the handlers and fix the return buf
    handler_install(nw);
    jmpbuf_fix(return_buf, diff);
    // copy and fix the remaining information in the continuation
    new_return_slot = fixedAddr(cont->return_slot);
    // install the memory
    for (i = 0; i < cont->size; ++i)
        ((char *)target)[i] = ((char *)stack)[i];
    // set return value for the return slot
    // fprintf(stderr, "return slot is %p\n", new_return_slot);
    *new_return_slot = out;
    // fire!
    result = _lh_store_lr_longjmp(return_buf,
                                  target + cont->size - BOUNDARY_LR_OFFSET);
    // clean up copied stack
    free(stack);
    free(return_buf);
    return result;
}

__attribute__((noinline, optnone, no_stack_protector)) void *
cont_resume(Continuation *cont, void *out) __attribute__((disable_tail_calls)) {
    void *result = NULL;
    Handler h = {.id = ++label_count, .result = &result, .stack_btm = NULL};
    handler_push(&h);
    if (_lh_setjmp(h.buf) == 0) {
        result = resumeImpl(cont, out);
        _lh_longjmp(h.buf, 1);
    }
    handler_pop();
    return result;
}

static void handler_free(Handlers *hs) {
    while (hs != NULL) {
        Handlers *old = hs;
        hs = hs->next;
        free(old);
    }
}

void cont_free(Continuation *cont) {
    handler_free(cont->handlers);
    free(cont->stack);
    free(cont);
}
