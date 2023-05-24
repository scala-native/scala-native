#include "delimcc.h"
#include <stddef.h>
#include <stdio.h>
#include <assert.h>
#include <stdlib.h>
#include <string.h>
#include <stdatomic.h>

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

// Apple platforms mangle the names of some symbols in assembly. We override the
// names here.
#if defined(__APPLE__)
#define _lh_setjmp lh_setjmp
#define _lh_longjmp lh_longjmp
#define _lh_boundary_entry lh_boundary_entry
#define _lh_resume_entry lh_resume_entry
#define _lh_get_sp lh_get_sp

#define __cont_boundary_impl _cont_boundary_impl
#define __cont_resume_impl _cont_resume_impl
#endif

// The return address is always stored in stack_btm - BOUNDARY_LR_OFFSET.
// See `_lh_boundary_entry`.
#define BOUNDARY_LR_OFFSET 8

#define __externc extern
#define __noreturn __attribute__((noreturn))
#define __returnstwice __attribute__((returns_twice))
#define __noinline __attribute__((noinline))
// define the lh_jmp_buf in terms of `void*` elements to have natural alignment
typedef void *lh_jmp_buf[ASM_JMPBUF_SIZE / sizeof(void *)];
// Non-standard setjmp.
__externc __returnstwice int _lh_setjmp(lh_jmp_buf buf);
// Jumps to the given setjmp'd buffer, returning arg as the value.
// arg must be non-zero.
__externc void *_lh_longjmp(lh_jmp_buf buf, int arg);
// Stores the return address in sp+8, then calls __cont_boundary_impl.
__externc __returnstwice void *_lh_boundary_entry(ContFn *f, void *arg);
// Allocate enough stack for the resumption, and then call __cont_resume_impl.
__externc void *_lh_resume_entry(ptrdiff_t cont_size, Continuation *c,
                                 void *arg);
// Returns the stack pointer of the calling function.
__externc void *_lh_get_sp();

// Label counter
volatile static atomic_ulong label_count;
static ContLabel next_label_count() { return ++label_count; }

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

/**
 * Handler chain, thread local.
 *
 * All handler chain handling functions should be __noinline,
 * to make sure that the handlers' address is looked up every time. If a
 * function is suspended and resumed on different threads, a cached thread-local
 * address might wreck havoc on its users.
 */
static _Thread_local Handlers *__handlers = NULL;

static void print_handlers(Handlers *hs) {
    while (hs != NULL) {
        fprintf(stderr, "[id = %lu, addr = %p | %p] -> ", hs->h->id, hs->h, hs);
        hs = hs->next;
    }
    fprintf(stderr, "nil\n");
}

__noinline static void handler_push(Handler *h) {
    assert(__handlers == NULL || __handlers->h->id != h->id);
    // fprintf(stderr, "Pushing [id = %lu, addr = %p]: ", h->id, h);
    // print_handlers((Handlers *)__handlers);
    Handlers *hs = malloc(sizeof(Handlers));
    hs->h = h;
    hs->next = (Handlers *)__handlers;
    __handlers = hs;
}

__noinline static void handler_pop(ContLabel label) {
    // fprintf(stderr, "Popping: ");
    // print_handlers((Handlers *)__handlers);
    assert(__handlers != NULL && label == __handlers->h->id);
    Handlers *old = (Handlers *)__handlers;
    __handlers = __handlers->next;
    free(old);
}

__noinline static void handler_install(Handlers *hs) {
    assert(hs != NULL);
    Handlers *tail = hs;
    // fprintf(stderr, "Installing: ");
    // print_handlers(hs);
    // fprintf(stderr, "  to : ");
    // print_handlers((Handlers *)__handlers);
    while (tail->next != NULL) {
        tail = tail->next;
    }
    tail->next = (Handlers *)__handlers;
    __handlers = hs;
}

__noinline static Handlers *handler_split_at(ContLabel l) {
    // fprintf(stderr, "Splitting [id = %lu]: ", l);
    // print_handlers((Handlers *)__handlers);
    Handlers *ret = (Handlers *)__handlers, *cur = ret;
    while (cur->h->id != l)
        cur = cur->next;
    __handlers = cur->next;
    cur->next = NULL;
    return ret;
}

// longjmp to the head handler. Useful for `cont_resume`.
__noinline static void *handler_head_longjmp(int arg) {
    assert(__handlers != NULL);
    return _lh_longjmp(__handlers->h->buf, arg);
}

static unsigned int handler_len(Handlers *h) {
    unsigned int ret = 0;
    while (h != NULL) {
        ret++;
        h = h->next;
    }
    return ret;
}

// =============================

static void *cont_malloc(unsigned long size, void *arg);
static void *(*alloc_function)(unsigned long, void *);

void cont_init(void *(*alloc_f)(unsigned long, void *)) {
    if (alloc_f != NULL)
        alloc_function = alloc_f;
    else
        alloc_function = cont_malloc;

    atomic_init(&label_count, 0);
}

__returnstwice void *__cont_boundary_impl(void **btm, ContFn *f, void *arg) {
    // fprintf(stderr, "Boundary btm is %p\n", btm);

    // allocate handlers and such
    volatile void *result = NULL; // we need to force the compiler to re-read
                                  // this from stack every time.
    // ContResult local_r = {.cont = NULL, .in = NULL};
    volatile ContLabel label = next_label_count();
    Handler h = {
        .id = label,
        .stack_btm = btm,
        .result = &result,
    };
    // fprintf(stderr, "Setting up result slot at %p\n", &result);
    ContBoundaryLabel l = {.id = h.id};
    handler_push(&h);

    // setjmp and call
    if (_lh_setjmp(h.buf) == 0) {
        result = f(l, arg);
        handler_pop(label);
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

static void *cont_malloc(unsigned long size, void *arg) {
    (void)arg;
    return malloc(size);
}

// suspend[T, R] : BoundaryLabel[T] -> T -> R
void *cont_suspend(ContBoundaryLabel b, SuspendFn *f, void *arg,
                   void *alloc_arg) __attribute__((disable_tail_calls)) {
    // set up the continuation
    Continuation *cont = alloc_function(sizeof(Continuation), alloc_arg);
    cont->stack_top = _lh_get_sp();
    cont->handlers = handler_split_at(b.id);
    cont->handlers_len = handler_len(cont->handlers);
    Handlers *last_handler = cont->handlers;
    while (last_handler->next != NULL)
        last_handler = last_handler->next;
    assert(last_handler->h->stack_btm != NULL); // not a resume handler
    cont->size = last_handler->h->stack_btm - cont->stack_top;
    // make the continuation size a multiple of 16
    cont->stack = alloc_function(cont->size, alloc_arg);
    memcpy(cont->stack, cont->stack_top, cont->size);

    // set up return value slot
    volatile void *ret_val = NULL;
    cont->return_slot = &ret_val;

    // fprintf(stderr, "Putting result %p to slot %p\n",
    // *last_handler->h->result,
    //         last_handler->h->result);

    // we will be back...
    if (_lh_setjmp(cont->buf) == 0) {
        // assign it to the handler's return value
        *last_handler->h->result = f(cont, arg);
        return _lh_longjmp(last_handler->h->buf, 1);
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
    Handlers *nw, *to_install; // new handler chain
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
    fprintf(stderr,
            "diff is %ld, stack (size = %ld) goes %p~%p -> %p~%p | original "
            "cont = %p [%p]\n",
            diff, cont->size, cont->stack_top, cont->stack_top + cont->size,
            target, tail, cont, cont->stack);
    // clone the handler chain, with fixes.
    to_install = nw = handler_clone_fix(cont->handlers, diff);
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
    // install the handlers and fix the return buf
    handler_install(to_install);

    // set return value for the return slot
    // fprintf(stderr, "return slot is %p\n", new_return_slot);
    *new_return_slot = out;
    // fix the return address of the bottom of our new stack fragment.
    *(void **)(target + cont->size - BOUNDARY_LR_OFFSET) = ret_addr;
    _lh_longjmp(return_buf, 1);
}

void *cont_resume(Continuation *cont, void *out) {
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
    volatile ContLabel label = next_label_count();
    Handler h = {.id = label, .result = &result, .stack_btm = NULL};
    handler_push(&h);
    if (_lh_setjmp(h.buf) == 0) {
        result = _lh_resume_entry(cont->size, cont, out);
        handler_head_longjmp(1); // top handler is always ours, avoid
                                 // refering to non-volatile `h`
    }
    handler_pop(label);
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
