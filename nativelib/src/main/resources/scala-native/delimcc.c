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
// Jumps to the given setjmp'd buffer, returning arg as the value.
// arg must be non-zero.
__externc __noreturn void _lh_longjmp(lh_jmp_buf buf, int arg);
// Stores the return address in sp+8, then calls __cont_boundary_impl.
__externc __returnstwice void *_lh_boundary_entry(ContFn *f, void *arg);
// Allocate enough stack for the resumption, and then call __cont_resume_impl.
__externc void *_lh_resume_entry(ptrdiff_t cont_size, Continuation *c,
                                 void *arg);
// Returns the stack pointer of the calling function.
__externc void *_lh_get_sp();

// Label counter
volatile static ContLabel label_count = 0;

typedef struct Handler {
    ContLabel id;
    void *stack_btm;        // where the bottom is, should be changed
    volatile void **result; // where the result is stored, should be changed
    lh_jmp_buf buf;         // jmp buf
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

static Handlers *handler_split_at(ContLabel l) {
    Handlers *ret = (Handlers *)__handlers, **cur = &ret;
    while ((*cur)->h->id != l)
        cur = (&(*cur)->next);
    __handlers = (*cur)->next;
    (*cur)->next = NULL;
    return ret;
}

static unsigned int handler_len(Handlers *h) {
    unsigned int ret = 0;
    while (h != NULL) {
        ret++;
        h = h->next;
    }
    return ret;
}

__returnstwice void *__cont_boundary_impl(void **btm, ContFn *f, void *arg) {
    // fprintf(stderr, "Boundary btm is %p\n", btm);

    // allocate handlers and such
    volatile void *result = NULL; // we need to force the compiler to re-read
                                  // this from stack every time.
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
    }
    return (void *)result;
}

// boundary : BoundaryFn -> Result
// Result MUST BE HEAP ALLOCATED
void *cont_boundary(ContFn *f, void *arg) __attribute__((disable_tail_calls)) {
    return _lh_boundary_entry(f, arg);
}

// ========== SUSPENDING ===========

struct Continuation {
    ptrdiff_t size;
    void *stack;
    void *stack_top;

    Handlers *handlers;
    unsigned int handlers_len;

    volatile void **return_slot;
    lh_jmp_buf buf;
};

static void *(*alloc_function)(unsigned long) = malloc;

void cont_set_alloc(void *(*f)(unsigned long)) { alloc_function = f; }

// suspend[T, R] : BoundaryLabel[T] -> T -> R
void *cont_suspend(ContBoundaryLabel b, SuspendFn *f, void *arg)
    __attribute__((disable_tail_calls)) {
    // set up the continuation
    Continuation *cont = alloc_function(sizeof(Continuation));
    cont->stack_top = _lh_get_sp();
    cont->handlers = handler_split_at(b.id);
    cont->handlers_len = handler_len(cont->handlers);
    Handlers *last_handler = cont->handlers;
    while (last_handler->next != NULL)
        last_handler = last_handler->next;
    assert(last_handler->h->stack_btm != NULL); // not a resume handler
    cont->size = last_handler->h->stack_btm - cont->stack_top;
    // make the continuation size a multiple of 16
    cont->stack = alloc_function(cont->size);
    memcpy(cont->stack, cont->stack_top, cont->size);

    // set up return value slot
    volatile void *ret_val = NULL;
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
        // We're back, ret_val should be populated.
        return (void *)ret_val;
    }
}

#define fixed_addr(X) (void *)(X) + diff
#define fix_addr(X) X = fixed_addr(X)

#define jmpbuf_fix(buf) fix_addr(buf[JMPBUF_STACK_POINTER_OFFSET])

static Handlers *handler_clone_fix(Handlers *other, ptrdiff_t diff) {
    Handlers *nw = NULL, **cur = &nw;
    while (other != NULL) {
        *cur = malloc(sizeof(Handlers));
        (*cur)->h = (Handler *)((void *)other->h + diff);
        cur = &(*cur)->next;
        other = other->next;
    }
    *cur = NULL;
    return nw;
}

// Resumes the continuation to [tail - size, tail).
void __cont_resume_impl(void *tail, Continuation *cont, void *out,
                        void *ret_addr) {
    // Allocate all values up front so we know how many to deal with.
    Handlers *nw; // new handler chain
    ptrdiff_t i;
    ptrdiff_t diff;         // pointer difference and stack size
    void *target;           // our target stack
    void **new_return_slot; // new return slot
    lh_jmp_buf return_buf;

    target = tail - cont->size;
    diff = target - cont->stack_top;
    // set up stuff
    memcpy(return_buf, cont->buf, ASM_JMPBUF_SIZE);

    assert((diff & 15) == 0);
    // fprintf(stderr,
    //         "diff is %ld, stack (size = %ld) goes %p~%p -> %p~%p | original "
    //         "cont = %p [%p]\n",
    //         diff, cont->size, cont->stack_top, cont->stack_top + cont->size,
    //         target, tail, cont, cont->stack);
    // clone the handler chain, with fixes.
    nw = handler_clone_fix(cont->handlers, diff);
    // install the handlers and fix the return buf
    handler_install(nw);
    jmpbuf_fix(return_buf);
    // copy and fix the remaining information in the continuation
    new_return_slot = fixed_addr(cont->return_slot);
    // install the memory
    memcpy(target, cont->stack, cont->size);
    // fix the handlers in cont->stack
    for (i = 0; i < cont->handlers_len; ++i, nw = nw->next) {
        fix_addr(nw->h->result);
        if (nw->h->stack_btm != NULL)
            fix_addr(nw->h->stack_btm);
        jmpbuf_fix(nw->h->buf);
    }

    // set return value for the return slot
    // fprintf(stderr, "return slot is %p\n", new_return_slot);
    *new_return_slot = out;
    // fix the return address of the bottom of our new stack fragment.
    *(void **)(target + cont->size - BOUNDARY_LR_OFFSET) = ret_addr;
    _lh_longjmp(return_buf, 1);
}

__attribute__((optnone)) void *cont_resume(Continuation *cont, void *out) {
    /*
     * Why we need a setjmp/longjmp.
     *
     * `resume` stack doesn't know which registers are changed, and so we
     * basically need to save all of them. setjmp/longjmp-ing to the same place
     * is the easiest way to do so.
     *
     * Why we need a Handler.
     *
     * Resumed computation might suspend on a parent, and mess up the setjmp
     * buffer that way.
     * */
    volatile void *result = NULL; // we need to force the compiler to re-read
                                  // this from stack every time.
    Handler h = {.id = ++label_count, .result = &result, .stack_btm = NULL};
    handler_push(&h);
    if (_lh_setjmp(h.buf) == 0) {
        result = _lh_resume_entry(cont->size, cont, out);
        _lh_longjmp(h.buf, 1); // top handler is always ours, avoid
                               // refering to non-volatile `h`
    }
    handler_pop();
    return (void *)result;
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
