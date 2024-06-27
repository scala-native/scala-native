#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_DELIMCC)
#include "delimcc.h"
#include <stddef.h>
#include <stdio.h>
#include <assert.h>
#include <stdlib.h>
#include <string.h>
#include <stdatomic.h>
#include "gc/shared/ThreadUtil.h"

// Defined symbols here:
// - ASM_JMPBUF_SIZE: The size of the jmpbuf, should be a constant defined in
// `setjmp.S`.
// - JMPBUF_STACK_POINTER_OFFSET: The offset within the jmpbuf where
//   the stack pointer is located. Should be defined in `setjmp.S`.
#if defined(__aarch64__) // ARM64
#define ASM_JMPBUF_SIZE 192
#define JMPBUF_STACK_POINTER_OFFSET (104 / 8)
#elif defined(__x86_64__) &&                                                   \
    (defined(__linux__) || defined(__APPLE__) || defined(__FreeBSD__) ||       \
     defined(__OpenBSD__) || defined(__NetBSD__))
#define ASM_JMPBUF_SIZE 72
#define JMPBUF_STACK_POINTER_OFFSET (16 / 8)
#elif defined(__i386__) &&                                                     \
    (defined(__linux__) || defined(__APPLE__)) // x86 linux and macOS
#define ASM_JMPBUF_SIZE 32
#define JMPBUF_STACK_POINTER_OFFSET (16 / 4)
#elif defined(__x86_64__) && defined(_WIN64) // x86-64 Windows
#define ASM_JMPBUF_SIZE 256
#define JMPBUF_STACK_POINTER_OFFSET (16 / 8)
#else
#error "Unsupported platform"
#endif

#ifdef SCALANATIVE_DELIMCC_DEBUG
#define debug_printf(...) printf(__VA_ARGS__)
#else
#define debug_printf(...) (void)0
#endif

// The return address is always stored in stack_btm - BOUNDARY_LR_OFFSET.
// See `_lh_boundary_entry`.
#if defined(__i386__)
#define BOUNDARY_LR_OFFSET 12
#else
#define BOUNDARY_LR_OFFSET 8
#endif

// Apple platforms mangle the names of some symbols in assembly. We override the
// names here.
#if defined(__APPLE__)
#define _lh_setjmp lh_setjmp
#define _lh_longjmp lh_longjmp
#define _lh_boundary_entry lh_boundary_entry
#define _lh_resume_entry lh_resume_entry
#define _lh_get_sp lh_get_sp

#define __continuation_boundary_impl _continuation_boundary_impl
#define __continuation_resume_impl _continuation_resume_impl
#endif

#define __externc extern
#define __noreturn __attribute__((noreturn))
#define __returnstwice __attribute__((returns_twice))
#ifndef __noinline
#define __noinline __attribute__((noinline))
#endif
// define the lh_jmp_buf in terms of `void*` elements to have natural alignment
typedef void *lh_jmp_buf[ASM_JMPBUF_SIZE / sizeof(void *)];
// Non-standard setjmp.
__externc __returnstwice int _lh_setjmp(lh_jmp_buf buf);
// Jumps to the given setjmp'd buffer, returning arg as the value.
// arg must be non-zero.
__externc void *_lh_longjmp(lh_jmp_buf buf, int arg);
// Stores the return address in sp+BOUNDARY_LR_OFFSET, then calls
// __cont_boundary_impl.
__externc __returnstwice void *_lh_boundary_entry(ContinuationBody *f,
                                                  void *arg);
// Allocate enough stack for the resumption, and then call __cont_resume_impl.
__externc void *_lh_resume_entry(ptrdiff_t cont_size, Continuation *c,
                                 void *arg);
// Returns the stack pointer of the calling function.
__externc void *_lh_get_sp();

// Label counter
volatile static atomic_ulong label_count;
static ContinuationBoundaryLabel next_label_count() { return ++label_count; }

// The handler structure that is stored and directly accessed on the stack.
typedef struct Handler {
    ContinuationBoundaryLabel id;
    void *stack_btm;       // where the bottom is, should be changed
    volatile void *result; // where the result is stored, should be changed
    struct Handler *next;  // the next handler in the chain
    lh_jmp_buf buf;        // jmp buf
} Handler;

/**
 * Handler chain, thread local.
 *
 * All handler chain handling functions should be __noinline,
 * to make sure that the handlers' address is looked up every time. If a
 * function is suspended and resumed on different threads, a cached thread-local
 * address might wreck havoc on its users.
 */
static SN_ThreadLocal Handler *__handlers = NULL;

static void print_handlers(Handler *hs) {
    while (hs != NULL) {
        debug_printf("[id = %lu, addr = %p] -> ", hs->id, hs);
        hs = hs->next;
    }
    debug_printf("nil\n");
}

__noinline static void handler_push(Handler *h) {
    assert(__handlers == NULL || __handlers->id != h->id);
    // debug_printf("Pushing [id = %lu, addr = %p]: ", h->id, h);
    // print_handlers((Handler *)__handlers);
    h->next = (Handler *)__handlers;
    __handlers = h;
}

__noinline static void handler_pop(ContinuationBoundaryLabel label) {
    // debug_printf("Popping: ");
    // print_handlers((Handler *)__handlers);
    assert(__handlers != NULL && label == __handlers->id);
    __handlers = __handlers->next;
}

__noinline static void handler_install(Handler *head, Handler *tail) {
    assert(head != NULL && tail != NULL && tail->next == NULL);
    // debug_printf("Installing: ");
    // print_handlers(head);
    // debug_printf("  to : ");
    // print_handlers((Handler *)__handlers);
    tail->next = (Handler *)__handlers;
    __handlers = head;
}

__noinline static void handler_split_at(ContinuationBoundaryLabel l,
                                        Handler **head, Handler **tail) {
    // debug_printf("Splitting [id = %lu]: ", l);
    // print_handlers((Handler *)__handlers);
    Handler *hd = (Handler *)__handlers, *tl = hd;
    while (tl->id != l)
        tl = tl->next;
    __handlers = tl->next;
    tl->next = NULL;
    *head = hd;
    *tail = tl;
}

// longjmp to the head handler. Useful for `cont_resume`.
__noinline static void *handler_head_longjmp(int arg) {
    assert(__handlers != NULL);
    return _lh_longjmp(__handlers->buf, arg);
}

// =============================

// Continuation allocation function by `malloc`.
static void *continuation_alloc_by_malloc(unsigned long size, void *arg);
// Assigned allocation function. Should not be modified after `init`.
static void *(*continuation_alloc_fn)(unsigned long, void *);

void scalanative_continuation_init(void *(*alloc_f)(unsigned long, void *)) {
    if (alloc_f != NULL)
        continuation_alloc_fn = alloc_f;
    else
        continuation_alloc_fn = continuation_alloc_by_malloc;

    atomic_init(&label_count, 0);
}

NO_SANITIZE
__returnstwice void *
__continuation_boundary_impl(void **btm, ContinuationBody *body, void *arg) {
    // debug_printf("Boundary btm is %p\n", btm);

    // allocate handlers and such
    volatile ContinuationBoundaryLabel label = next_label_count();
    Handler h = {
        .id = label,
        .stack_btm = btm,
        .result = NULL,
    };
    debug_printf("Setting up result slot at %p, header = %p\n", &h.result, &h);
    ContinuationBoundaryLabel l = h.id;
    handler_push(&h);

    // setjmp and call
    if (_lh_setjmp(h.buf) == 0) {
        h.result = body(l, arg);
        handler_pop(label);
    }
    return (void *)h.result;
}

// boundary : BoundaryFn -> Result
// Result MUST BE HEAP ALLOCATED
void *scalanative_continuation_boundary(ContinuationBody *body, void *arg)
    __attribute__((disable_tail_calls)) {
    return _lh_boundary_entry(body, arg);
}

// ========== SUSPENDING ===========

struct Continuation {
    ptrdiff_t size;
    void *stack_top;

    Handler *handlers;

    volatile void **return_slot;
    lh_jmp_buf buf;

    char stack[];
};

static void *continuation_alloc_by_malloc(unsigned long size, void *arg) {
    (void)arg;
    return malloc(size);
}

// suspend[T, R] : BoundaryLabel[T] -> T -> R
NO_SANITIZE
void *scalanative_continuation_suspend(ContinuationBoundaryLabel b,
                                       SuspendFn *f, void *arg, void *alloc_arg)
    __attribute__((disable_tail_calls)) {
    void *stack_top = _lh_get_sp();
    Handler *head, *tail;
    handler_split_at(b, &head, &tail);
    assert(tail->stack_btm != NULL); // not a resume handler
    ptrdiff_t stack_size = tail->stack_btm - stack_top;
    // set up the continuation
    Continuation *continuation =
        continuation_alloc_fn(sizeof(Continuation) + stack_size, alloc_arg);
    continuation->stack_top = stack_top;
    continuation->handlers = head;
    continuation->size = stack_size;
    memcpy(continuation->stack, continuation->stack_top, continuation->size);

    // set up return value slot
    volatile void *ret_val = NULL;
    continuation->return_slot = &ret_val;

    // we will be back...
    if (_lh_setjmp(continuation->buf) == 0) {
        // assign it to the handler's return value
        tail->result = f(continuation, arg);
        debug_printf("Putting result %p to slot %p, header = %p\n",
                     tail->result, tail->result, tail);
        return _lh_longjmp(tail->buf, 1);
    } else {
        // We're back, ret_val should be populated.
        return (void *)ret_val;
    }
}

// Resumes the continuation to [tail - size, tail).
NO_SANITIZE
void __continuation_resume_impl(void *tail, Continuation *continuation,
                                void *out, void *ret_addr) {
    // Allocate all values up front so we know how many to deal with.
    Handler *h_tail, *h_head; // new handler chain
    ptrdiff_t i;
    ptrdiff_t diff;         // pointer difference and stack size
    void *target;           // our target stack
    void **new_return_slot; // new return slot
    lh_jmp_buf return_buf;

    target = tail - continuation->size;
    diff = target - continuation->stack_top;
    // set up stuff
    memcpy(return_buf, continuation->buf, ASM_JMPBUF_SIZE);

#if !defined(__i386__) // 32 bit platforms don't have an alignment restriction?
    assert((diff & 15) == 0);
#endif
    debug_printf(
        "diff is %td, stack (size = %td) goes %p~%p -> %p~%p | original "
        "cont = %p [%p]\n",
        diff, continuation->size, continuation->stack_top,
        continuation->stack_top + continuation->size, target, tail,
        continuation, continuation->stack);
#define fixed_addr(X) ((void *)(X) + diff)
#define fix_addr(X) X = fixed_addr(X)
/**
 * Fixes the stack pointer offset within a `jmpbuf` by the difference given by
 * `diff`. We need to do this for every jmpbuf that is stored in the handler
 * chain, as well as the suspend jmpbuf.
 */
#define jmpbuf_fix(buf) fix_addr(buf[JMPBUF_STACK_POINTER_OFFSET])
    // clone the handler chain, with fixes.
    h_head = h_tail = (Handler *)fixed_addr(continuation->handlers);
    jmpbuf_fix(return_buf);
    // copy and fix the remaining information in the continuation
    new_return_slot = fixed_addr(continuation->return_slot);
    // install the memory
    memcpy(target, continuation->stack, continuation->size);
    // fix the handlers in cont->stack
    for (;;) {
        fix_addr(h_tail->result);
        if (h_tail->stack_btm != NULL)
            fix_addr(h_tail->stack_btm);
        jmpbuf_fix(h_tail->buf);
        if (h_tail->next != NULL) {
            h_tail->next = (Handler *)fixed_addr(h_tail->next);
            h_tail = h_tail->next;
        } else
            break;
    }
    // install the handlers and fix the return buf
    handler_install(h_head, h_tail);

    // set return value for the return slot
    // debug_printf("return slot is %p\n", new_return_slot);
    *new_return_slot = out;
    // fix the return address of the bottom of our new stack fragment.
    *(void **)(target + continuation->size - BOUNDARY_LR_OFFSET) = ret_addr;
    _lh_longjmp(return_buf, 1);
#undef fixed_addr
#undef fix_addr
#undef jmpbuf_fix
}

void *scalanative_continuation_resume(Continuation *continuation, void *out) {
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
    volatile ContinuationBoundaryLabel label = next_label_count();
    Handler h = {.id = label, .result = &result, .stack_btm = NULL};
    handler_push(&h);
    if (_lh_setjmp(h.buf) == 0) {
        result = _lh_resume_entry(continuation->size, continuation, out);
        handler_head_longjmp(1); // top handler is always ours, avoid
                                 // refering to non-volatile `h`
    }
    handler_pop(label);
    return (void *)result;
}

#ifdef SCALANATIVE_DELIMCC_DEBUG

void scalanative_continuation_free(Continuation *continuation) {
    free(continuation);
}
#endif // SCALANATIVE_DELIMCC_DEBUG

#endif
