#if !defined(_WIN32) && (defined(SCALANATIVE_COMPILE_ALWAYS) ||                \
                         defined(__SCALANATIVE_DELIMCC_ONESHOT))

/*
 * One-shot delimited continuations.
 *
 * Every `boundary` runs its body on its own stack (a coroutine); suspend and
 * resume switch stacks. Frames never move, so frame pointers and addresses
 * of stack memory stay valid across a suspension, also when the continuation
 * is resumed on another thread.
 *
 * `returnTo` links a coroutine to the context that receives control when it
 * finishes or suspends to its own label: the boundary caller first, then
 * each resumer. Following it from a running coroutine gives its enclosing
 * boundaries.
 *
 * The suspend callback runs in the receiving context, after the suspending
 * coroutine has switched out. Another thread may then resume it right away.
 *
 * GC (see gc/shared/SuspendedStacks.h): ThreadInfo describes the stack that
 * runs. A stack that waits for a coroutine it started or resumed is scanned
 * as a root. The stacks captured by a continuation are not roots: the
 * collector scans them when it marks the SuspendedStacks object of that
 * continuation. A continuation that was not marked in a completed cycle is
 * unreachable, so its stacks are reclaimed.
 */

#include "delimcc.h"
#include "nativeThreadTLS.h"
#include "gc/shared/SuspendedStacks.h"
#include <pthread.h>
#include <stdatomic.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdio.h>
#include <sys/mman.h>
#include <unistd.h>

extern void scalanative_GC_collect(void);
extern void scalanative_StackOverflowGuards_setupSegment(void);

// delimcc/switch.S
extern void scalanative_continuation_entry(void);

#define STACK_SIZE ((size_t)1024 * 1024)
// Free stacks kept per thread, and by all threads together.
#define LOCAL_POOL_CAPACITY 16
#define SHARED_POOL_CAPACITY 1024
// Continuations waiting to be resumed before a collection is requested to
// find the unreachable ones. Their stacks are invisible to the GC heap.
#define MIN_CAPTURED_LIMIT 1024

#define FATAL(...)                                                             \
    do {                                                                       \
        fprintf(stderr, "[ScalaNative Continuations | Error] " __VA_ARGS__);   \
        fputc('\n', stderr);                                                   \
        abort();                                                               \
    } while (0)

// The stack fields of ThreadInfo.
typedef struct StackBounds {
    void *top;    // lowest usable address
    void *bottom; // highest address
    void *guardPage;
    size_t size;
    size_t maxSize;
} StackBounds;

typedef enum {
    THREAD, // the stack of a thread, never finishes
    CREATED,
    RUNNING,
    SUSPENDED,
    LEFT, // broke out to `label`, never resumed
    FINISHED
} CoroutineState;

typedef struct SuspendRequest {
    SuspendFn *fn;
    void *arg;
} SuspendRequest;

typedef struct Coroutine Coroutine;
struct Coroutine {
    SuspendedStack captured; // first: a SuspendedStacks object points here
    StackSlot *slot;         // what the collector reads about this stack
    void *sp;                // saved by the switch while parked
    Coroutine *returnTo;
    Coroutine *from; // the coroutine that last switched to this one
    CoroutineState state;
    ThreadInfo *thread; // owner of a THREAD coroutine
    void *mapping;      // start of the stack mapping this struct lives in
    StackBounds bounds;
    bool guarded; // the stack overflow guard page is installed

    // Values handed over by a switch. No safepoint runs between the store
    // and the read on the other side.
    ContinuationBody *body;
    void *bodyArg;
    void *result;
    void *resumeArg;
    SuspendRequest request;

    // Set on the coroutine that suspended or broke out. With suspend it
    // stands for the whole continuation: the coroutines from it up to `label`.
    Coroutine *label;
    Coroutine *nextUnreachable;
};

typedef struct ThreadState {
    Coroutine *current;
    Coroutine *pool; // free coroutines, linked through `returnTo`
    int poolSize;
    ThreadInfo *info;
} ThreadState;

static SN_ThreadLocal ThreadState threadState;

// Not inlined: a coroutine can continue on another thread, so the result
// must not be used across a switch.
__attribute__((noinline)) static ThreadState *currentThread(void) {
    ThreadState *thread = &threadState;
    if (thread->info == NULL)
        thread->info = scalanative_currentThreadInfo();
    return thread;
}

// =============================
// Stacks

/* A coroutine lives at the high end of its own stack mapping; the lowest
 * page is an inaccessible guard. Free coroutines keep their mapping and are
 * linked through `returnTo`. */
static Coroutine *sharedPool;
static int sharedPoolSize;
static pthread_mutex_t sharedPoolLock = PTHREAD_MUTEX_INITIALIZER;

static pthread_key_t threadStateKey;
static pthread_once_t threadStateKeyOnce = PTHREAD_ONCE_INIT;

// Slots are never returned to the system; free ones are linked through
// `bottom`, which the collector ignores while `waitingTop` is unset.
static StackSlot *freeSlots;
static pthread_mutex_t slotsLock = PTHREAD_MUTEX_INITIALIZER;

static StackSlot *acquireSlot(void) {
    pthread_mutex_lock(&slotsLock);
    StackSlot *slot = freeSlots;
    if (slot != NULL) {
        freeSlots = slot->bottom;
    } else {
        StackSlotBlock *block = scalanative_GC_stackSlots;
        if (block == NULL || block->used == STACK_SLOTS_PER_BLOCK) {
            block = calloc(1, sizeof(StackSlotBlock));
            if (block == NULL)
                FATAL("out of memory");
            block->next = scalanative_GC_stackSlots;
            scalanative_GC_stackSlots = block;
        }
        slot = &block->slots[block->used++];
    }
    pthread_mutex_unlock(&slotsLock);
    return slot;
}

static void releaseSlot(StackSlot *slot) {
    pthread_mutex_lock(&slotsLock);
    slot->bottom = freeSlots;
    freeSlots = slot;
    pthread_mutex_unlock(&slotsLock);
}

static size_t pageSize(void) {
    static size_t size;
    if (size == 0)
        size = (size_t)sysconf(_SC_PAGESIZE);
    return size;
}

static Coroutine *mapCoroutine(void) {
    size_t page = pageSize();
    size_t size = STACK_SIZE + page;
    int flags = MAP_PRIVATE | MAP_ANON;
#ifdef MAP_STACK
    flags |= MAP_STACK; // required for stack memory on OpenBSD
#endif
    char *low = mmap(NULL, size, PROT_READ | PROT_WRITE, flags, -1, 0);
    if (low == MAP_FAILED || mprotect(low, page, PROT_NONE) != 0)
        FATAL("cannot map a stack of %zu bytes", size);
    Coroutine *coroutine =
        (Coroutine *)(((uintptr_t)low + size - sizeof(Coroutine)) &
                      ~(uintptr_t)15);
    size_t usable = (size_t)((char *)coroutine - (low + page));
    coroutine->mapping = low;
    coroutine->bounds = (StackBounds){.top = low + page,
                                      .bottom = coroutine,
                                      .size = usable,
                                      .maxSize = usable};
    coroutine->slot = acquireSlot();
    return coroutine;
}

static void unmapCoroutine(Coroutine *coroutine) {
    releaseSlot(coroutine->slot);
    munmap(coroutine->mapping, STACK_SIZE + pageSize());
}

static bool giveShared(Coroutine *coroutine) {
    bool pooled = false;
    pthread_mutex_lock(&sharedPoolLock);
    if (sharedPoolSize < SHARED_POOL_CAPACITY) {
        coroutine->returnTo = sharedPool;
        sharedPool = coroutine;
        sharedPoolSize++;
        pooled = true;
    }
    pthread_mutex_unlock(&sharedPoolLock);
    return pooled;
}

static Coroutine *takeShared(void) {
    pthread_mutex_lock(&sharedPoolLock);
    Coroutine *coroutine = sharedPool;
    if (coroutine != NULL) {
        sharedPool = coroutine->returnTo;
        sharedPoolSize--;
    }
    pthread_mutex_unlock(&sharedPoolLock);
    return coroutine;
}

// Runs when a thread exits.
static void releaseThreadState(void *unused) {
    (void)unused;
    ThreadState *thread = &threadState;
    while (thread->pool != NULL) {
        Coroutine *coroutine = thread->pool;
        thread->pool = coroutine->returnTo;
        if (!giveShared(coroutine))
            unmapCoroutine(coroutine);
    }
    thread->poolSize = 0;
    if (thread->current != NULL && thread->current->state == THREAD) {
        releaseSlot(thread->current->slot);
        free(thread->current);
        thread->current = NULL;
    }
}

static void createThreadStateKey(void) {
    pthread_key_create(&threadStateKey, releaseThreadState);
}

// A pooled coroutine keeps its mapping, bounds and guard; every other field
// is set again before it is read.
static Coroutine *acquire(ThreadState *thread) {
    Coroutine *coroutine = thread->pool;
    if (coroutine != NULL) {
        thread->pool = coroutine->returnTo;
        thread->poolSize--;
        return coroutine;
    }
    coroutine = takeShared();
    return coroutine != NULL ? coroutine : mapCoroutine();
}

static void destroy(ThreadState *thread, Coroutine *coroutine) {
    if (thread->poolSize < LOCAL_POOL_CAPACITY) {
        coroutine->returnTo = thread->pool;
        thread->pool = coroutine;
        thread->poolSize++;
    } else if (!giveShared(coroutine)) {
        unmapCoroutine(coroutine);
    }
}

/* Switches stacks: saves the frame pointer, a resume address and the
 * floating point control state below the stack pointer, stores the stack
 * pointer in `*saveSp`, and continues what was saved at `loadSp`, possibly on
 * another thread later.
 *
 * Every other register is declared clobbered, so the compiler keeps the
 * callee-saved ones of the callers, vector registers included, in the frame
 * of the enclosing function, where the collector finds them. Being inlined
 * gives each use its own indirect jump, which predicts well; a shared one
 * would alternate between targets.
 *
 * The control state (rounding mode, exception masks) is callee-saved by the
 * ABI and belongs to the computation, not to the thread it happens to run
 * on: without switching it, a mode set before a suspend would stay on the
 * carrier and be lost by the continuation.
 *
 * SWITCH_FRAME bytes are used below the stack pointer, past any red zone.
 */
#if defined(__aarch64__)
#define SWITCH_FRAME 144
static inline __attribute__((always_inline)) void switchStacks(void **saveSp,
                                                               void *loadSp) {
    register void **save __asm__("x0") = saveSp;
    register void *load __asm__("x1") = loadSp;
    __asm__ volatile(
        "adr x9, 1f\n"
        "mrs x10, fpcr\n"
        "stp x29, x9, [sp, #-144]!\n"
        "str x10, [sp, #16]\n"
        "mov x9, sp\n"
        "str x9, [x0]\n"
        "mov sp, x1\n"
        "ldr x10, [sp, #16]\n"
        "mrs x11, fpcr\n"
        "cmp x10, x11\n"
        "b.eq 2f\n"
        "msr fpcr, x10\n" // slow, and rarely needed
        "2: ldp x29, x9, [sp], #144\n"
        "br x9\n"
        "1: hint #36\n" // bti j
        : "+r"(save), "+r"(load)
        :
        : "x2", "x3", "x4", "x5", "x6", "x7", "x8", "x9", "x10", "x11", "x12",
          "x13", "x14", "x15", "x16", "x17", "x19", "x20", "x21", "x22", "x23",
          "x24", "x25", "x26", "x27", "x28", "x30", "v0", "v1", "v2", "v3",
          "v4", "v5", "v6", "v7", "v8", "v9", "v10", "v11", "v12", "v13", "v14",
          "v15", "v16", "v17", "v18", "v19", "v20", "v21", "v22", "v23", "v24",
          "v25", "v26", "v27", "v28", "v29", "v30", "v31", "cc", "memory");
}

static void prepareControlState(void **frame) {
    uint64_t fpcr;
    __asm__("mrs %0, fpcr" : "=r"(fpcr));
    frame[2] = (void *)fpcr;
}
#elif defined(__x86_64__)
#define SWITCH_FRAME 152
static inline __attribute__((always_inline)) void switchStacks(void **saveSp,
                                                               void *loadSp) {
    register void **save __asm__("rdi") = saveSp;
    register void *load __asm__("rsi") = loadSp;
    __asm__ volatile("leaq -128(%%rsp), %%rsp\n"
                     "leaq 1f(%%rip), %%rax\n"
                     "pushq %%rax\n"
                     "pushq %%rbp\n"
                     "subq $8, %%rsp\n"
                     "stmxcsr (%%rsp)\n"
                     "fnstcw 4(%%rsp)\n"
                     "movq %%rsp, (%%rdi)\n"
                     "movq %%rsi, %%rsp\n"
                     "ldmxcsr (%%rsp)\n"
                     "fldcw 4(%%rsp)\n"
                     "addq $8, %%rsp\n"
                     "popq %%rbp\n"
                     "popq %%rax\n"
                     "leaq 128(%%rsp), %%rsp\n"
                     "jmpq *%%rax\n"
                     "1:\n"
                     : "+r"(save), "+r"(load)
                     :
                     : "rax", "rbx", "rcx", "rdx", "r8", "r9", "r10", "r11",
                       "r12", "r13", "r14", "r15", "xmm0", "xmm1", "xmm2",
                       "xmm3", "xmm4", "xmm5", "xmm6", "xmm7", "xmm8", "xmm9",
                       "xmm10", "xmm11", "xmm12", "xmm13", "xmm14", "xmm15",
                       "cc", "memory");
}

// The control words come first, then the frame pointer and the address.
static void prepareControlState(void **frame) {
    __asm__("stmxcsr (%0)\n"
            "fnstcw 4(%0)"
            :
            : "r"(frame - 1)
            : "memory");
}
#else
#error "Unsupported architecture"
#endif

// Builds what the first switch to `coroutine` pops: a null frame pointer,
// scalanative_continuation_entry, which finds the coroutine at the stack
// pointer and calls scalanative_continuation_main, and the control state of
// the creating context.
static void prepareStack(Coroutine *coroutine) {
#if defined(__aarch64__)
    void **frame = (void **)((char *)coroutine - SWITCH_FRAME);
    coroutine->sp = frame;
#else
    void **frame = (void **)((char *)coroutine - SWITCH_FRAME) + 1;
    coroutine->sp = frame - 1;
#endif
    frame[0] = NULL;
    frame[1] = (void *)scalanative_continuation_entry;
    prepareControlState(frame);
}

// =============================
// Switching

static void captureBounds(ThreadState *thread, Coroutine *coroutine) {
    ThreadInfo *info = thread->info;
    coroutine->bounds = (StackBounds){.top = info->stackTop,
                                      .bottom = info->stackBottom,
                                      .guardPage = info->stackGuardPage,
                                      .size = info->stackSize,
                                      .maxSize = info->maxStackSize};
}

static void applyBounds(ThreadState *thread, Coroutine *coroutine) {
    ThreadInfo *info = thread->info;
    info->stackTop = coroutine->bounds.top;
    info->stackBottom = coroutine->bounds.bottom;
    info->stackGuardPage = coroutine->bounds.guardPage;
    info->stackSize = coroutine->bounds.size;
    info->maxStackSize = coroutine->bounds.maxSize;
}

// Read by the collector only while this thread is stopped at a safepoint.
static void addRoots(Coroutine *coroutine) {
    StackSlot *slot = coroutine->slot;
    slot->bottom = coroutine->bounds.bottom;
    atomic_store_explicit(&slot->waitingTop, coroutine->sp,
                          memory_order_relaxed);
}

static void removeRoots(Coroutine *coroutine) {
    atomic_store_explicit(&coroutine->slot->waitingTop, NULL,
                          memory_order_relaxed);
}

// First thing a coroutine does when it gets control. The switch stored the
// callee-saved registers of `from` on its stack, above its saved sp.
static ThreadState *arrive(Coroutine *self) {
    ThreadState *thread = currentThread();
    thread->current = self;
    applyBounds(thread, self);
    removeRoots(self);
    CoroutineState state = self->from->state;
    if (state != FINISHED && state != LEFT)
        addRoots(self->from);
    return thread;
}

// Returns when another coroutine switches back, possibly on another thread.
static inline __attribute__((always_inline)) ThreadState *
switchTo(ThreadState *thread, Coroutine *self, Coroutine *target) {
    if (target->state == THREAD && target->thread != thread->info)
        FATAL("switch to the stack of another thread");
    // The stack of a thread can grow; the others have fixed bounds.
    if (self->state == THREAD)
        captureBounds(thread, self);
    target->from = self;
    switchStacks(&self->sp, target->sp);
    return arrive(self);
}

static Coroutine *currentCoroutine(ThreadState *thread) {
    if (thread->current == NULL) {
        Coroutine *self = calloc(1, sizeof(Coroutine));
        if (self == NULL)
            FATAL("out of memory");
        self->state = THREAD;
        self->thread = thread->info;
        self->slot = acquireSlot();
        thread->current = self;
        pthread_once(&threadStateKeyOnce, createThreadStateKey);
        pthread_setspecific(threadStateKey, thread);
    }
    return thread->current;
}

// Entered through scalanative_continuation_entry.
__attribute__((visibility("hidden"))) void
scalanative_continuation_main(Coroutine *self) {
    ThreadState *thread = arrive(self);
    if (!self->guarded) {
        scalanative_StackOverflowGuards_setupSegment();
        captureBounds(thread, self);
        self->guarded = true;
    }
    self->state = RUNNING;
    // The body catches every Throwable, nothing unwinds past this frame.
    self->result = self->body((ContinuationBoundaryLabel)self, self->bodyArg);
    self->state = FINISHED;
    switchTo(currentThread(), self, self->returnTo);
    FATAL("a finished coroutine was resumed");
}

// =============================
// Continuations

#define FOREACH_CAPTURED(it, top)                                              \
    for (Coroutine *it = (top), *it##Last = NULL; it##Last != (top)->label;    \
         it##Last = it, it = it->returnTo)

// Continuations waiting to be resumed.
static atomic_size_t capturedCount;
static atomic_size_t capturedLimit = MIN_CAPTURED_LIMIT;
static atomic_uint_fast64_t reclaimedIn;

// From `first` up to and including `label`.
static void destroyChain(ThreadState *thread, Coroutine *first,
                         Coroutine *label) {
    for (Coroutine *it = first, *next;; it = next) {
        next = it->returnTo;
        removeRoots(it);
        it->captured = (SuspendedStack){0};
        destroy(thread, it);
        if (it == label)
            break;
    }
}

static void checkEnclosing(Coroutine *self, Coroutine *target) {
    Coroutine *enclosing = self;
    while (enclosing != target && enclosing->state != THREAD)
        enclosing = enclosing->returnTo;
    if (enclosing != target || target->state == THREAD)
        FATAL("the boundary does not enclose the caller");
}

// Frees the continuations that the last completed mark cycle did not reach.
static void reclaimUnreachable(ThreadState *thread) {
    uint_fast64_t cycle = atomic_load(&scalanative_GC_completedMarkCycle);
    if (atomic_load_explicit(&reclaimedIn, memory_order_relaxed) == cycle ||
        atomic_exchange(&reclaimedIn, cycle) == cycle)
        return;
    Coroutine *unreachable = NULL;
    size_t count = 0;
    pthread_mutex_lock(&slotsLock);
    FOREACH_STACK_SLOT(slot) {
        Coroutine *top = atomic_load(&slot->captured);
        if (top != NULL && slot->capturedIn < cycle &&
            atomic_load(&slot->markedIn) != cycle) {
            atomic_store(&slot->captured, NULL);
            top->nextUnreachable = unreachable;
            unreachable = top;
            count++;
        }
    }
    pthread_mutex_unlock(&slotsLock);
    size_t remaining = atomic_fetch_sub(&capturedCount, count) - count;
    atomic_store(&capturedLimit, remaining * 2 > MIN_CAPTURED_LIMIT
                                     ? remaining * 2
                                     : MIN_CAPTURED_LIMIT);
    while (unreachable != NULL) {
        Coroutine *top = unreachable;
        unreachable = top->nextUnreachable;
        destroyChain(thread, top, top->label);
    }
}

// Runs in the context that received control in `boundary` or `resume`.
static void *dispatch(ThreadState *thread, Coroutine *self) {
    Coroutine *from = self->from;
    if (from->state == FINISHED) {
        void *result = from->result;
        destroy(thread, from);
        return result;
    }
    if (from->state == LEFT) {
        void *result = from->result;
        destroyChain(thread, from, from->label);
        return result;
    }
    // Once the callback publishes the continuation, another thread can
    // resume `from` and change its fields.
    SuspendRequest request = from->request;
    return request.fn((Continuation *)from, request.arg);
}

void *scalanative_continuation_oneshot_boundary(ContinuationBody *body,
                                                void *arg) {
    ThreadState *thread = currentThread();
    reclaimUnreachable(thread);
    Coroutine *self = currentCoroutine(thread);
    Coroutine *coroutine = acquire(thread);
    coroutine->state = CREATED;
    coroutine->body = body;
    coroutine->bodyArg = arg;
    coroutine->returnTo = self;
    prepareStack(coroutine);
    thread = switchTo(thread, self, coroutine);
    return dispatch(thread, self);
}

void *scalanative_continuation_oneshot_suspend(ContinuationBoundaryLabel label,
                                               SuspendFn *f, void *arg) {
    ThreadState *thread = currentThread();
    Coroutine *self = currentCoroutine(thread);
    Coroutine *target = (Coroutine *)label;
    checkEnclosing(self, target);

    self->request = (SuspendRequest){f, arg};
    self->label = target;
    self->state = SUSPENDED;
    switchTo(thread, self, target->returnTo);

    self->state = RUNNING;
    return self->resumeArg;
}

/* Returns `result` from the boundary of `label` and drops the computation
 * inside it, without running anything it left pending. */
void scalanative_continuation_oneshot_leave(ContinuationBoundaryLabel label,
                                            void *result) {
    ThreadState *thread = currentThread();
    Coroutine *self = currentCoroutine(thread);
    Coroutine *target = (Coroutine *)label;
    checkEnclosing(self, target);
    self->result = result;
    self->label = target;
    self->state = LEFT;
    switchTo(thread, self, target->returnTo);
    FATAL("a coroutine that left its boundary was resumed");
}

/* Hands the stacks of a suspended continuation over to `owner`, the
 * SuspendedStacks object that points to `continuation`. The caller keeps it
 * reachable. Until this call the stacks are scanned as roots. */
void scalanative_continuation_oneshot_captured(Continuation *continuation,
                                               void *owner) {
    // Lets the collector recognise these objects; the same for all of them.
    scalanative_GC_suspendedStacksRtti = *(void **)owner;
    Coroutine *top = (Coroutine *)continuation;
    FOREACH_CAPTURED(it, top) {
        it->captured.top = it->sp;
        it->captured.bottom = it->bounds.bottom;
        it->captured.next = it == top->label ? NULL : &it->returnTo->captured;
        removeRoots(it);
    }
    FOREACH_CAPTURED(it, top) it->captured.slot = top->slot;
    atomic_store(&top->slot->markedIn, 0);
    top->slot->capturedIn = atomic_load(&scalanative_GC_completedMarkCycle);
    atomic_store(&top->slot->captured, top);
    if (atomic_fetch_add(&capturedCount, 1) >= atomic_load(&capturedLimit)) {
        scalanative_GC_collect();
        reclaimUnreachable(currentThread());
    }
}

/* The caller resumes a continuation at most once and has cleared the
 * SuspendedStacks object that pointed to it. */
void *scalanative_continuation_oneshot_resume(Continuation *continuation,
                                              void *arg) {
    Coroutine *top = (Coroutine *)continuation;
    ThreadState *thread = currentThread();
    Coroutine *self = currentCoroutine(thread);
    atomic_store(&top->slot->captured, NULL);
    atomic_fetch_sub(&capturedCount, 1);
    FOREACH_CAPTURED(it, top) {
        addRoots(it);
        it->captured = (SuspendedStack){0};
    }
    top->label->returnTo = self;
    top->resumeArg = arg;
    thread = switchTo(thread, self, top);
    return dispatch(thread, self);
}

#endif
