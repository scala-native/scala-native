#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_DELIMCC)
#include "delimcc.h"
#include <stddef.h>
#include <stdio.h>
#include <assert.h>
#include <stdlib.h>
#include <string.h>
#include <stdatomic.h>
#include <setjmp.h>
#include <stdint.h>
#include "gc/shared/ThreadUtil.h"
#include "nativeThreadTLS.h"

// Defined symbols here:
// - ASM_JMPBUF_SIZE: The size of the jmpbuf, should be a constant defined in
// `setjmp.S`.
// - JMPBUF_STACK_POINTER_OFFSET: The offset within the jmpbuf where
//   the stack pointer is located. Should be defined in `setjmp.S`.
#if defined(__aarch64__) // ARM64
#define ASM_JMPBUF_SIZE 192
#define JMPBUF_STACK_POINTER_OFFSET (104 / 8)
#define JMPBUF_FRAME_POINTER_OFFSET (88 / 8)
#elif defined(__x86_64__) &&                                                   \
    (defined(__linux__) || defined(__APPLE__) || defined(__FreeBSD__) ||       \
     defined(__OpenBSD__) || defined(__NetBSD__))
#define ASM_JMPBUF_SIZE 72
#define JMPBUF_STACK_POINTER_OFFSET (16 / 8)
#define JMPBUF_FRAME_POINTER_OFFSET (24 / 8)
#elif defined(__i386__) &&                                                     \
    (defined(__linux__) || defined(__APPLE__)) // x86 linux and macOS
#define ASM_JMPBUF_SIZE 32
#define JMPBUF_STACK_POINTER_OFFSET (16 / 4)
#define JMPBUF_FRAME_POINTER_OFFSET (0 / 4)
#elif defined(__x86_64__) && defined(_WIN64) // x86-64 Windows
#define ASM_JMPBUF_SIZE 256
#define JMPBUF_STACK_POINTER_OFFSET (16 / 8)
#define JMPBUF_FRAME_POINTER_OFFSET (24 / 8)
#else
#error "Unsupported platform"
#endif

#ifdef SCALANATIVE_DELIMCC_DEBUG
#define debug_printf(...) printf(__VA_ARGS__)
#else
#define debug_printf(...) (void)0
#endif

#define DELIMCC_ERROR(...)                                                     \
    do {                                                                       \
        fprintf(stderr, "[ScalaNative Continuations | Error] ");               \
        fprintf(stderr, __VA_ARGS__);                                          \
        fprintf(stderr, "\n");                                                 \
        fflush(stderr);                                                        \
    } while (0)

#define DELIMCC_ERROR_ABORT(...)                                               \
    do {                                                                       \
        DELIMCC_ERROR(__VA_ARGS__);                                            \
        abort();                                                               \
    } while (0)

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
 *
 * The pointer is declared volatile to prevent the compiler from caching
 * the TLS slot value in a callee-saved register across setjmp/longjmp
 * boundaries.
 */
static SN_ThreadLocal Handler *volatile __handlers = NULL;

/** Force a fresh read of the thread-local handler chain. Required when a
 *  continuation may have been suspended on one carrier and resumed on
 *  another: without this, the compiler could cache the TLS slot address
 *  in a callee-saved register and we would read the wrong carrier's chain
 *  (e.g. "label not found" in handler_split_at). */
__noinline static Handler *handlers_load(void) { return (Handler *)__handlers; }

__noinline static void handlers_store(Handler *h) { __handlers = h; }

static void print_handlers(Handler *hs) {
    while (hs != NULL) {
        debug_printf("[id = %lu, addr = %p] -> ", hs->id, hs);
        hs = hs->next;
    }
    debug_printf("nil\n");
}

__noinline static void handler_push(Handler *h) {
    Handler *cur = handlers_load();
    if (cur != NULL && cur->id == h->id) {
        DELIMCC_ERROR_ABORT("handler_push: duplicate top id=%lu\n",
                            (unsigned long)h->id);
    }
    // debug_printf("Pushing [id = %lu, addr = %p]: ", h->id, h);
    // print_handlers(cur);
    h->next = cur;
    handlers_store(h);
}

__noinline static void handler_pop(ContinuationBoundaryLabel label) {
    Handler *cur = handlers_load();
    // debug_printf("Popping: ");
    // print_handlers(cur);
    if (cur == NULL || label != cur->id) {
        Handler *it = cur;
        // Recover from leaked resume handlers left above the expected frame.
        while (it != NULL && it->id != label && it->stack_btm == NULL)
            it = it->next;
        if (it != NULL && it->id == label) {
            DELIMCC_ERROR("handler_pop: recovered leaked resume handlers "
                          "while popping id=%lu\n",
                          (unsigned long)label);
            handlers_store(it->next);
            return;
        }
        DELIMCC_ERROR_ABORT(
            "handler_pop: expected id=%lu, got head=%p head_id=%lu\n",
            (unsigned long)label, (void *)cur,
            cur == NULL ? 0UL : (unsigned long)cur->id);
    }
    handlers_store(cur->next);
}

__noinline static void handler_install(Handler *head, Handler *tail) {
    if (head == NULL || tail == NULL || tail->next != NULL) {
        DELIMCC_ERROR_ABORT(
            "handler_install: invalid args head=%p tail=%p tail_next=%p\n",
            (void *)head, (void *)tail,
            tail == NULL ? NULL : (void *)tail->next);
    }
    Handler *cur = handlers_load();
    // debug_printf("Installing: ");
    // print_handlers(head);
    // debug_printf("  to : ");
    // print_handlers(cur);
    tail->next = cur;
    handlers_store(head);
}

__noinline static void handler_split_at(ContinuationBoundaryLabel l,
                                        Handler **head, Handler **tail) {
    Handler *hd = handlers_load(), *tl = hd;
    // debug_printf("Splitting [id = %lu]: ", l);
    // print_handlers(hd);
    if (tl == NULL) {
        DELIMCC_ERROR_ABORT(
            "handler_split_at: empty handler chain for label=%lu\n",
            (unsigned long)l);
    }
    while (tl != NULL && tl->id != l)
        tl = tl->next;
    if (tl == NULL) {
        Handler *it = hd;
        int depth = 0;
        DELIMCC_ERROR("handler_split_at: label=%lu not found, chain=",
                      (unsigned long)l);
        while (it != NULL && depth < 16) {
            DELIMCC_ERROR("[id=%lu,btm=%p]->", (unsigned long)it->id,
                          it->stack_btm);
            it = it->next;
            depth++;
        }
        DELIMCC_ERROR("%s\n", it == NULL ? "nil" : "...");
        abort();
    }
    handlers_store(tl->next);
    tl->next = NULL;
    *head = hd;
    *tail = tl;
}

// longjmp to the head handler. Useful for `cont_resume`.
__noinline static void *handler_head_longjmp(int arg) {
    Handler *cur = handlers_load();
    if (cur == NULL) {
        DELIMCC_ERROR_ABORT("handler_head_longjmp: empty chain\n");
    }
    return _lh_longjmp(cur->buf, arg);
}

// =============================
// Continuation exception escape state (shared by eh.c / eh.cpp)

static SN_ThreadLocal struct ContinuationExceptionHandler
    continuation_exception_handler = {NULL, NULL};

void scalanative_continuation_exception_handler_set(
    struct ContinuationExceptionHandler handler) {
    continuation_exception_handler = handler;
}
struct ContinuationExceptionHandler
scalanative_continuation_exception_handler() {
    return continuation_exception_handler;
}

extern void *scalanative_continuation_exception_to_failure(Exception exception);

int scalanative_continuation_exception_escape(Exception exception) {
    Handler *head = handlers_load();
    if (head == NULL)
        return 0;

    void *failure = scalanative_continuation_exception_to_failure(exception);

    if (head->stack_btm == NULL) {
        volatile void **result_slot = (volatile void **)head->result;
        if (result_slot == NULL)
            return 0;
        *result_slot = failure;
    } else {
        head->result = failure;
        // Jumping to a boundary handler bypasses the normal pop path, so we
        // must unlink it first to avoid leaking stale handlers in TLS.
        handlers_store(head->next);
    }

    _lh_longjmp(head->buf, 1);
    __builtin_unreachable();
}

// =============================

// Assigned allocation function. Should not be modified after `init`.
static void *(*continuation_alloc_fn)(unsigned long, void *);

void scalanative_continuation_init(void *(*alloc_f)(unsigned long, void *)) {
    if (alloc_f == NULL)
        DELIMCC_ERROR_ABORT("scalanative_continuation_init: alloc_f is NULL\n");

    continuation_alloc_fn = alloc_f;
    atomic_init(&label_count, 0);
}

void scalanative_continuation_handlers_reset(void) { handlers_store(NULL); }

NO_SANITIZE
__returnstwice void *
__continuation_boundary_impl(void **btm, ContinuationBody *body, void *arg) {
    // debug_printf("Boundary btm is %p\n", btm);
    volatile void *body_arg = arg;

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
        h.result = body(l, (void *)body_arg);
        handler_pop(label);
    }
    return (void *)h.result;
}

// boundary : BoundaryFn -> Result
// Result MUST BE HEAP ALLOCATED
void *scalanative_continuation_boundary(ContinuationBody *body, void *arg)
    __attribute__((disable_tail_calls)) {
    volatile void *boundary_arg = arg;
    return _lh_boundary_entry(body, (void *)boundary_arg);
}

// ========== SUSPENDING ===========

struct Continuation {
    ptrdiff_t size;
    void *stack_top;

    Handler *handlers;

    volatile void **return_slot;
    lh_jmp_buf buf;

    /* When non-NULL, the heap block for this continuation's stack fragment
     * (carrier stack overflow). Layout: [heap_stack_fragment,
     * +HEAP_FRAGMENT_SLACK, +size]; the copied fragment is at block+slack so
     * the resumed code can grow the stack into the slack.
     * Allocated via continuation_alloc_fn(alloc_arg) so GC can reclaim it;
     * no free. heap_fragment_alloc_size is the allocation size (0 if not
     * heap). */
    void *heap_stack_fragment;
    size_t heap_fragment_alloc_size;

    /* alloc_arg passed at suspend; used at resume to allocate
     * heap_stack_fragment via continuation_alloc_fn (non-moving GC). */
    void *alloc_arg;

    char stack[];
};

static inline int
continuation_contains_stack_address(const Continuation *continuation,
                                    const void *p) {
    uintptr_t addr = (uintptr_t)p;
    uintptr_t stack_top = (uintptr_t)continuation->stack_top;
    uintptr_t stack_end = stack_top + (uintptr_t)continuation->size;
    // Pre-prologue spill area: on x86-64 the red zone extends 128 bytes below
    // RSP; on ARM64 there is no red zone but callee-save spills can create
    // pointers slightly below the captured top.
    const uintptr_t below_slack = 256;
    // Frame pointer chain and spilled references: saved FP or callee-saved
    // regs can point several frames above the captured fragment. Under VT
    // contention (many migrations) false negatives cause SIGBUS (C1); use a
    // large margin so stack-derived pointers are rebased after carrier
    // migration.
    const uintptr_t above_slack = 16384;
    return addr >= (stack_top - below_slack) &&
           addr <= (stack_end + above_slack);
}

static inline ptrdiff_t continuation_address_diff(const void *to,
                                                  const void *from) {
    uintptr_t to_addr = (uintptr_t)to;
    uintptr_t from_addr = (uintptr_t)from;
    if (to_addr >= from_addr)
        return (ptrdiff_t)(to_addr - from_addr);
    else
        return -((ptrdiff_t)(from_addr - to_addr));
}

static inline void *continuation_rebase_ptr(const void *p, const void *old_base,
                                            const void *new_base) {
    if (p == NULL)
        return NULL;
    uintptr_t p_addr = (uintptr_t)p;
    uintptr_t old_addr = (uintptr_t)old_base;
    uintptr_t new_addr = (uintptr_t)new_base;
    return (void *)(new_addr + (p_addr - old_addr));
}

/** Clamp pointer to [frag_lo, frag_lo + frag_size]. Used when resuming to a
 * heap-allocated fragment so rebased pointers (e.g. from above_slack) don't
 * point past the end of the fragment. */
static inline void *clamp_ptr_to_fragment(void *p, void *frag_lo,
                                          ptrdiff_t frag_size) {
    if (p == NULL)
        return NULL;
    uintptr_t lo = (uintptr_t)frag_lo;
    uintptr_t hi = lo + (uintptr_t)frag_size;
    uintptr_t a = (uintptr_t)p;
    if (a < lo)
        return (void *)lo;
    if (a > hi)
        return (void *)hi;
    return p;
}

// suspend[T, R] : BoundaryLabel[T] -> T -> R
NO_SANITIZE
void *scalanative_continuation_suspend(ContinuationBoundaryLabel b,
                                       SuspendFn *f, void *arg, void *alloc_arg)
    __attribute__((disable_tail_calls)) {
    volatile void *suspend_arg = arg;
    volatile void *suspend_alloc_arg = alloc_arg;
    void *stack_top = _lh_get_sp();
    Handler *head, *tail;
    handler_split_at(b, &head, &tail);
    if (tail->stack_btm == NULL) {
        DELIMCC_ERROR_ABORT(
            "continuation_suspend: split tail is resume handler "
            "(label=%lu, head=%p, tail=%p)\n",
            (unsigned long)b, (void *)head, (void *)tail);
    }
    uintptr_t stack_top_addr = (uintptr_t)stack_top;
    uintptr_t stack_btm_addr = (uintptr_t)tail->stack_btm;
    if (stack_btm_addr < stack_top_addr) {
        DELIMCC_ERROR_ABORT(
            "continuation_suspend: invalid stack bounds "
            "(label=%lu, top=%p, btm=%p, head=%p, tail=%p, tls_head=%p)\n",
            (unsigned long)b, stack_top, tail->stack_btm, (void *)head,
            (void *)tail, (void *)handlers_load());
        Handler *it = handlers_load();
        int depth = 0;
        while (it != NULL && depth < 16) {
            DELIMCC_ERROR(
                "handler[%d]: id=%lu addr=%p stack_btm=%p result=%p next=%p\n",
                depth, (unsigned long)it->id, (void *)it, it->stack_btm,
                it->result, (void *)it->next);
            it = it->next;
            depth += 1;
        }
    }
    ptrdiff_t stack_size = (ptrdiff_t)(stack_btm_addr - stack_top_addr);
    // set up the continuation
    Continuation *continuation = continuation_alloc_fn(
        sizeof(Continuation) + stack_size, (void *)suspend_alloc_arg);
    continuation->stack_top = stack_top;
    continuation->handlers = head;
    continuation->size = stack_size;
    continuation->heap_stack_fragment = NULL;
    continuation->heap_fragment_alloc_size = 0;
    continuation->alloc_arg = (void *)suspend_alloc_arg;
    memcpy(continuation->stack, continuation->stack_top, continuation->size);

    // set up return value slot
    volatile void *ret_val = NULL;
    continuation->return_slot = &ret_val;

    // we will be back...
    if (_lh_setjmp(continuation->buf) == 0) {
        /*
         * Execute the suspend callback with the captured boundary chain
         * temporarily reinstalled. This allows nested suspend points in the
         * callback to find the boundary label instead of observing only the
         * resume handler.
         */
        handler_install(head, tail);
        // assign it to the handler's return value
        tail->result = f(continuation, (void *)suspend_arg);

        Handler *split_head = NULL;
        Handler *split_tail = NULL;
        handler_split_at(b, &split_head, &split_tail);
        if (split_head != head || split_tail != tail) {
            DELIMCC_ERROR_ABORT(
                "continuation_suspend: unexpected handler chain "
                "after callback (label=%lu, expected head=%p tail=%p, "
                "got head=%p tail=%p)\n",
                (unsigned long)b, (void *)head, (void *)tail,
                (void *)split_head, (void *)split_tail);
        }

        debug_printf("Putting result %p to slot %p, header = %p\n",
                     tail->result, tail->result, tail);
        return _lh_longjmp(tail->buf, 1);
    } else {
        // We're back, ret_val should be populated.
        return (void *)ret_val;
    }
}

/* Extra bytes below the copied fragment when using heap fallback, so the
 * resumed code can push new frames without SIGBUS. */
#define HEAP_FRAGMENT_SLACK ((size_t)(64 * 1024))

// Resumes the continuation to [tail - size, tail).
NO_SANITIZE
void __continuation_resume_impl(void *tail, Continuation *continuation,
                                void *out, void *ret_addr) {
    // Allocate all values up front so we know how many to deal with.
    Handler *h_tail, *h_head; // new handler chain
    ptrdiff_t diff;           // pointer difference and stack size
    void *target;             // our target stack
    void **new_return_slot;   // new return slot
    lh_jmp_buf return_buf;
    int use_heap = 0;

    target = (void *)((uintptr_t)tail - (uintptr_t)continuation->size);
    diff = continuation_address_diff(target, continuation->stack_top);

    /* Avoid placing the continuation fragment in the guard region. If the
     * carrier stack has no room (target < stackBottom), use a heap-allocated
     * fragment (reused from continuation->heap_stack_fragment or newly
     * allocated via continuation_alloc_fn). When we later resume on a
     * carrier with enough stack, set heap_stack_fragment = NULL so the GC
     * can reclaim it (no free; allocator is non-moving GC). */
    {
        ThreadInfo *ti = scalanative_currentThreadInfo();
        use_heap = 0;
        if (ti->stackTop != NULL && ti->stackBottom != NULL) {
            uintptr_t lo = (uintptr_t)ti->stackTop;
            uintptr_t hi = (uintptr_t)ti->stackBottom;
            if (lo > hi) {
                uintptr_t t = lo;
                lo = hi;
                hi = t;
            }
            use_heap = ((uintptr_t)target < lo || (uintptr_t)target >= hi);
        }
        if (use_heap) {
            size_t need = (size_t)continuation->size + HEAP_FRAGMENT_SLACK;
            if (continuation->heap_stack_fragment != NULL &&
                continuation->heap_fragment_alloc_size >= need) {
                target = (char *)continuation->heap_stack_fragment +
                         HEAP_FRAGMENT_SLACK;
            } else {
                /* Drop old fragment (GC will reclaim); alloc via same
                 * allocator as continuation so GC is aware. */
                continuation->heap_stack_fragment = NULL;
                continuation->heap_fragment_alloc_size = 0;
                void *block = continuation_alloc_fn((unsigned long)need,
                                                    continuation->alloc_arg);
                if (block == NULL) {
                    DELIMCC_ERROR_ABORT(
                        "__continuation_resume_impl: continuation_alloc_fn("
                        "%lu) failed for heap fallback; carrier stack "
                        "overflow\n",
                        (unsigned long)need);
                }
                continuation->heap_stack_fragment = block;
                continuation->heap_fragment_alloc_size = need;
                target = (char *)block + HEAP_FRAGMENT_SLACK;
            }
            diff = continuation_address_diff(target, continuation->stack_top);
        } else {
            continuation->heap_stack_fragment = NULL;
            continuation->heap_fragment_alloc_size = 0;
        }
    }

    // set up stuff
    memcpy(return_buf, continuation->buf, ASM_JMPBUF_SIZE);

#if !defined(__i386__) // 32 bit platforms don't have an alignment restriction?
    assert((diff & 15) == 0);
#endif
    debug_printf(
        "diff is %td, stack (size = %td) goes %p~%p -> %p~%p | original "
        "cont = %p [%p]\n",
        diff, continuation->size, continuation->stack_top,
        (void *)((uintptr_t)continuation->stack_top +
                 (uintptr_t)continuation->size),
        target, tail, continuation, continuation->stack);
#define fixed_addr(X)                                                          \
    continuation_rebase_ptr((const void *)(X), continuation->stack_top, target)
/* When resuming to a heap fragment, rebased pointers can land outside the
 * fragment (e.g. continuation_contains_stack_address uses above_slack);
 * clamp so we never store a pointer past the end of the fragment. */
#define fixed_addr_clamped(X)                                                  \
    (use_heap                                                                  \
         ? clamp_ptr_to_fragment(fixed_addr(X), target, continuation->size)    \
         : (fixed_addr(X)))
#define fix_addr(X) X = fixed_addr_clamped(X)
/**
 * Fix stack-derived registers in the saved jmp buffer.
 *
 * Besides SP/FP, some general-purpose callee-saved registers may also hold
 * stack-derived pointers (for example addresses of locals spilled across
 * calls). After stack relocation we must rebase any such values.
 */
#define jmpbuf_fix_slot_if_stack_addr(buf, offset)                             \
    do {                                                                       \
        void *saved_reg = (buf)[(offset)];                                     \
        if (saved_reg != NULL &&                                               \
            continuation_contains_stack_address(continuation, saved_reg)) {    \
            (buf)[(offset)] = fixed_addr_clamped(saved_reg);                   \
        }                                                                      \
    } while (0)

#if defined(__aarch64__)
#if defined(__APPLE__)
/* x18 is Apple's platform register (per-thread TLS base, OS-reserved).
   It must never be saved, restored, or rebased across carrier threads.
   setjmp.S keeps slot 0 reserved for layout compatibility, but leaves x18
   untouched on Apple and stores x19 at slot 1. */
#define jmpbuf_fix_general_registers(buf)                                      \
    do {                                                                       \
        /* slot 0 = x18: intentionally skipped on Apple */                     \
        jmpbuf_fix_slot_if_stack_addr((buf), 1);  /* x19 */                    \
        jmpbuf_fix_slot_if_stack_addr((buf), 2);  /* x20 */                    \
        jmpbuf_fix_slot_if_stack_addr((buf), 3);  /* x21 */                    \
        jmpbuf_fix_slot_if_stack_addr((buf), 4);  /* x22 */                    \
        jmpbuf_fix_slot_if_stack_addr((buf), 5);  /* x23 */                    \
        jmpbuf_fix_slot_if_stack_addr((buf), 6);  /* x24 */                    \
        jmpbuf_fix_slot_if_stack_addr((buf), 7);  /* x25 */                    \
        jmpbuf_fix_slot_if_stack_addr((buf), 8);  /* x26 */                    \
        jmpbuf_fix_slot_if_stack_addr((buf), 9);  /* x27 */                    \
        jmpbuf_fix_slot_if_stack_addr((buf), 10); /* x28 */                    \
    } while (0)
#else
/* Non-Apple ARM64: x18 may be a general-purpose callee-saved register. */
#define jmpbuf_fix_general_registers(buf)                                      \
    do {                                                                       \
        jmpbuf_fix_slot_if_stack_addr((buf), 0);  /* x18 */                    \
        jmpbuf_fix_slot_if_stack_addr((buf), 1);  /* x19 */                    \
        jmpbuf_fix_slot_if_stack_addr((buf), 2);  /* x20 */                    \
        jmpbuf_fix_slot_if_stack_addr((buf), 3);  /* x21 */                    \
        jmpbuf_fix_slot_if_stack_addr((buf), 4);  /* x22 */                    \
        jmpbuf_fix_slot_if_stack_addr((buf), 5);  /* x23 */                    \
        jmpbuf_fix_slot_if_stack_addr((buf), 6);  /* x24 */                    \
        jmpbuf_fix_slot_if_stack_addr((buf), 7);  /* x25 */                    \
        jmpbuf_fix_slot_if_stack_addr((buf), 8);  /* x26 */                    \
        jmpbuf_fix_slot_if_stack_addr((buf), 9);  /* x27 */                    \
        jmpbuf_fix_slot_if_stack_addr((buf), 10); /* x28 */                    \
    } while (0)
#endif
#elif defined(__x86_64__) &&                                                   \
    (defined(__linux__) || defined(__APPLE__) || defined(__FreeBSD__) ||       \
     defined(__OpenBSD__) || defined(__NetBSD__))
#define jmpbuf_fix_general_registers(buf)                                      \
    do {                                                                       \
        jmpbuf_fix_slot_if_stack_addr((buf), 1); /* rbx */                     \
        jmpbuf_fix_slot_if_stack_addr((buf), 4); /* r12 */                     \
        jmpbuf_fix_slot_if_stack_addr((buf), 5); /* r13 */                     \
        jmpbuf_fix_slot_if_stack_addr((buf), 6); /* r14 */                     \
        jmpbuf_fix_slot_if_stack_addr((buf), 7); /* r15 */                     \
    } while (0)
#elif defined(__i386__) && (defined(__linux__) || defined(__APPLE__))
#define jmpbuf_fix_general_registers(buf)                                      \
    do {                                                                       \
        jmpbuf_fix_slot_if_stack_addr((buf), 1); /* ebx */                     \
        jmpbuf_fix_slot_if_stack_addr((buf), 2); /* edi */                     \
        jmpbuf_fix_slot_if_stack_addr((buf), 3); /* esi */                     \
    } while (0)
#elif defined(__x86_64__) && defined(_WIN64)
#define jmpbuf_fix_general_registers(buf)                                      \
    do {                                                                       \
        jmpbuf_fix_slot_if_stack_addr((buf), 0); /* rdx */                     \
        jmpbuf_fix_slot_if_stack_addr((buf), 1); /* rbx */                     \
        jmpbuf_fix_slot_if_stack_addr((buf), 4); /* rsi */                     \
        jmpbuf_fix_slot_if_stack_addr((buf), 5); /* rdi */                     \
        jmpbuf_fix_slot_if_stack_addr((buf), 6); /* r12 */                     \
        jmpbuf_fix_slot_if_stack_addr((buf), 7); /* r13 */                     \
        jmpbuf_fix_slot_if_stack_addr((buf), 8); /* r14 */                     \
        jmpbuf_fix_slot_if_stack_addr((buf), 9); /* r15 */                     \
    } while (0)
#else
#define jmpbuf_fix_general_registers(buf) ((void)0)
#endif

/* Rebase stack-derived values in the jmp_buf. SP and FP are always adjusted;
 * callee-saved GPRs are rebased only if they fall in the continuation stack
 * range. The LR/PC slot must NEVER be rebased (C3: it is a code address). */
#define jmpbuf_fix(buf)                                                        \
    do {                                                                       \
        fix_addr((buf)[JMPBUF_STACK_POINTER_OFFSET]);                          \
        jmpbuf_fix_slot_if_stack_addr((buf), JMPBUF_FRAME_POINTER_OFFSET);     \
        jmpbuf_fix_general_registers((buf));                                   \
    } while (0)
    // clone the handler chain, with fixes.
    h_head = h_tail = (Handler *)fixed_addr_clamped(continuation->handlers);
    jmpbuf_fix(return_buf);
    // copy and fix the remaining information in the continuation
    new_return_slot = (void **)fixed_addr_clamped(continuation->return_slot);
    // install the memory
    memcpy(target, continuation->stack, continuation->size);
    /* Do not rewrite arbitrary stack words here.
     * Rewriting non-pointer values that happen to look like stack addresses can
     * corrupt frame data under heavy continuation churn. */
    // fix the handlers in cont->stack (continuation_rebase_ptr preserves NULL)
    for (;;) {
        fix_addr(h_tail->result);
        if (h_tail->stack_btm != NULL) {
            if (continuation_contains_stack_address(continuation,
                                                    h_tail->stack_btm)) {
                fix_addr(h_tail->stack_btm);
            } else {
                /* stack_btm was from another stack (e.g. stale from
                 * same-carrier prior run). Rebase would produce a wrong value;
                 * clamp to the high end of the new fragment so it is in-range
                 * and won't cause SIGBUS when the resumed code uses it. */
                h_tail->stack_btm =
                    (char *)target + (uintptr_t)continuation->size;
            }
        }
        jmpbuf_fix(h_tail->buf);
        if (h_tail->next != NULL) {
            h_tail->next = (Handler *)fixed_addr_clamped(h_tail->next);
            h_tail = h_tail->next;
        } else
            break;
    }
    // install the handlers and fix the return buf
    handler_install(h_head, h_tail);

    // set return value for the return slot (must be non-null; suspend always
    // sets continuation->return_slot)
    if (new_return_slot == NULL) {
        DELIMCC_ERROR_ABORT("__continuation_resume_impl: return_slot is null "
                            "(continuation=%p)\n",
                            (void *)continuation);
    }
    *new_return_slot = out;
    // fix the return address of the bottom of our new stack fragment.
    *(void **)((char *)target + continuation->size - BOUNDARY_LR_OFFSET) =
        ret_addr;
    _lh_longjmp(return_buf, 1);
#undef fixed_addr
#undef fixed_addr_clamped
#undef fix_addr
#undef jmpbuf_fix_slot_if_stack_addr
#undef jmpbuf_fix_general_registers
#undef jmpbuf_fix
}

#if defined(SCALANATIVE_USING_CPP_EXCEPTIONS)
extern void scalanative_continuation_exception_terminate_handler_install(void);
#endif

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
     *
     * Exception escape: when the resumed body throws and unwinding returns
     * _URC_END_OF_STACK, eh.c/eh.cpp longjmps here. Handlers can nest across
     * nested resume calls, so we must restore the previous handler (instead of
     * blindly clearing TLS) on both normal and exceptional paths.
     */
    volatile Exception caught = NULL;
    jmp_buf exception_env;
    volatile Handler *saved_handlers = handlers_load();
    volatile ContinuationBoundaryLabel label = 0;
    volatile int resume_handler_pushed = 0;
    ContinuationExceptionHandler previous_exception_handler =
        scalanative_continuation_exception_handler();
    if (setjmp(exception_env) != 0) {
        if (resume_handler_pushed) {
            handlers_store((Handler *)saved_handlers);
            resume_handler_pushed = 0;
        }
        scalanative_continuation_exception_handler_set(
            previous_exception_handler);
        return scalanative_continuation_exception_to_failure(caught);
    }
#if defined(SCALANATIVE_USING_CPP_EXCEPTIONS)
    scalanative_continuation_exception_terminate_handler_install();
#endif
    ContinuationExceptionHandler exception_handler = {
        .env = &exception_env, .exception_slot = (Exception *)&caught};
    scalanative_continuation_exception_handler_set(exception_handler);

    volatile void *resume_out = out;
    volatile void *result = NULL; // we need to force the compiler to re-read
    // this from stack every time.
    label = next_label_count();
    Handler h = {.id = label, .result = &result, .stack_btm = NULL};
    handler_push(&h);
    resume_handler_pushed = 1;
    if (_lh_setjmp(h.buf) == 0) {
        result = _lh_resume_entry(continuation->size, continuation,
                                  (void *)resume_out);
        handler_head_longjmp(1); // top handler is always ours, avoid
                                 // refering to non-volatile `h`
    }
    handlers_store((Handler *)saved_handlers);
    resume_handler_pushed = 0;
    scalanative_continuation_exception_handler_set(
        previous_exception_handler); /* normal return path */

    return (void *)result;
}

#endif
