#include "delimcc.h"
#include <stddef.h>
#include <stdio.h>
#include <assert.h>
#include <threads.h>
#include <stdlib.h>
#include <string.h>

// setjmp, longjmp
#define ASM_JMPBUF_SIZE 192
#define __externc extern
#define __noreturn __attribute__((noreturn))
#define __returnstwice __attribute__((returns_twice))
// define the lh_jmp_buf in terms of `void*` elements to have natural alignment
typedef void *lh_jmp_buf[ASM_JMPBUF_SIZE / sizeof(void *)];
__externc __returnstwice int _lh_setjmp(lh_jmp_buf buf);
__externc void *_lh_longjmp(lh_jmp_buf buf, void *arg);
__externc __noreturn void __lh_longjmp(lh_jmp_buf buf, int arg);

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
    fprintf(stderr, "Pushing handler with label %lu\n", h->id);
    Handlers *hs = malloc(sizeof(Handlers));
    hs->h = h;
    hs->next = (Handlers *)__handlers;
    __handlers = hs;
}

static void handler_pop() {
    assert(__handlers != NULL);
    fprintf(stderr, "Popping handler with label %lu\n", __handlers->h->id);
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

__attribute__((noinline)) static void **get(void **btm) { return btm; }

static const unsigned BOUNDARY_IMPL_INTERNAL_STACK_SIZE =
    /* self */ 1 + /*args*/ 2 + /*stored registers*/ 4;

static const ptrdiff_t BOUNDARY_LR_OFFSET = 32 - 8 /* bytes_from_end */;

__attribute__((noinline, optnone, returns_twice,
               no_stack_protector)) static void *
boundaryImpl(ContFn *f, void *arg) __attribute__((disable_tail_calls)) {
    void *inner_btm = NULL;
    void **ptr = get(&inner_btm);
    void **btm = ptr + BOUNDARY_IMPL_INTERNAL_STACK_SIZE;
    // check the stack
    // assert(ptr < stack_btm);
    // for (unsigned i = 0; i <= 10; ++i) {
    //     fprintf(stderr, "  i = %u, ptr = %p, v = %lx\n", i, ptr + i,
    //             ((unsigned long *)(ptr))[i]);
    // }
    /* Output:
  i = 0, ptr = 0xffffffffa6b0, v = 0 <- ptr
  i = 1, ptr = 0xffffffffa6b8, v = a79cfcd675dbbb00 <- gibberish
  i = 2, ptr = 0xffffffffa6c0, v = ffffffffa6f0 <- sp
  i = 3, ptr = 0xffffffffa6c8, v = 4009ac <- return addr
  i = 4, ptr = 0xffffffffa6d0, v = ffffffffa708 <- arg
  i = 5, ptr = 0xffffffffa6d8, v = 4007fc <- arg
  i = 6, ptr = 0xffffffffa6e0, v = 0
    */

    // allocate handlers and such
    void *result = NULL;
    // ContResult local_r = {.cont = NULL, .in = NULL};
    Handler h = {
        .id = ++label_count,
        .stack_btm = btm,
        .result = &result,
    };
    fprintf(stderr, "Setting up result slot at %p\n", &result);
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
// void cont_boundary(ContFn *f, void *arg, ContResult *r)
//     __attribute__((disable_tail_calls)) {
//     boundaryImpl(f, arg, r);
// }

void *cont_boundary(ContFn *f, void *arg) __attribute__((disable_tail_calls)) {
    return boundaryImpl(f, arg);
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

__attribute__((noinline, optnone, no_stack_protector)) static void **
get_stack_top() {
    void *v = NULL;
    return get(&v);
}

// suspend[T, R] : BoundaryLabel[T] -> T -> R
__attribute__((noinline, optnone)) void *cont_suspend(ContBoundaryLabel b,
                                                      SuspendFn *f, void *arg)
    __attribute__((disable_tail_calls)) {
    // set up the continuation
    Continuation *cont = malloc(sizeof(Continuation));
    cont->stack_top = get_stack_top();
    cont->handlers = handler_split_at(b.id);
    Handlers *last_handler = cont->handlers;
    while (last_handler->next != NULL)
        last_handler = last_handler->next;
    assert(last_handler->h->stack_btm != NULL); // not a resume handler
    cont->size = last_handler->h->stack_btm - cont->stack_top;
    cont->stack = malloc(cont->size);
    memcpy(cont->stack, cont->stack_top, cont->size);

    // set up return value slot
    void *ret_val = NULL;
    cont->return_slot = get(&ret_val);

    // assign it to the handler's return value
    *last_handler->h->result = f(cont, arg);
    fprintf(stderr, "Putting result %p to slot %p\n", *last_handler->h->result,
            last_handler->h->result);

    // we will be back...
    if (_lh_setjmp(cont->buf) == 0) {
        __lh_longjmp(last_handler->h->buf, 1);
    } else {
        // We're back, just collect value from return slot.
        return ret_val;
    }
}

#define fixedAddr(X) (void *)(X) + diff
#define fixAddr(X) X = fixedAddr(X);

static void jmpbuf_fix(lh_jmp_buf buf, ptrdiff_t diff) {
    // fp = 11
    fixAddr(buf[11]);
    // sp = 13
    fixAddr(buf[13]);
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
// __attribute__((noinline)) void call_memcpy(Continuation *cont, void *target)
// {
//     memcpy(target, cont->stack, cont->size);
// }

__attribute__((noinline, optnone, no_stack_protector)) static void *
resumeImpl(Continuation *cont, void *out);

__attribute__((noinline, optnone, no_stack_protector)) static void *
indirect(Continuation *cont, void *out) {
    return resumeImpl(cont, out);
}

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
    void *stackTop_ = NULL;
    // get the stack difference
    stackTop = get(&stackTop_) - 1 /* = sp */;
    target = stackTop - cont->size;
    diff = target - cont->stack_top;
    // set up stuff
    return_buf = malloc(ASM_JMPBUF_SIZE);
    call_memcpy(return_buf, cont->buf, ASM_JMPBUF_SIZE);
    stack = malloc(cont->size);
    call_memcpy(stack, cont->stack, cont->size);
    if (diff & 15) {
        // unaligned stack, try to allocate more
        return indirect(cont, out);
    }
    assert((diff & 15) == 0);
    fprintf(stderr,
            "diff is %ld, stack (size = %ld) goes %p -> %p | on heap = %p\n",
            diff, cont->size, cont->stack_top, target, stack);
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
    result = _lh_longjmp(return_buf, target + cont->size - BOUNDARY_LR_OFFSET);
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
        __lh_longjmp(h.buf, 1);
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
