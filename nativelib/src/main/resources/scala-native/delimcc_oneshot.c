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
 * GC (gc/shared/GCHooks.h): ThreadInfo describes the stack that runs. A
 * stack that waits for a coroutine it started or resumed is a dynamic root.
 * The stacks of a suspended continuation are traced through its
 * SuspendedStacks object; one that a completed mark cycle did not trace is
 * unreachable, and its stacks are reclaimed.
 *
 * A collector that stops threads anywhere needs collections paused from
 * before a switch until the receiving side has taken over, and around every
 * change of what the callbacks read. None of this code has a safepoint, so
 * the pauses are no-ops for the other collectors.
 */

#include "delimcc_oneshot.h"
#include "nativeThreadTLS.h"
#include "stackOverflowGuards.h"
#include "gc/shared/GCHooks.h"
#include <pthread.h>
#include <stdatomic.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdio.h>
#include <sys/mman.h>
#include <unistd.h>

extern void scalanative_GC_collect(void);
extern size_t scalanative_StackOverflowGuards_size(void);

// delimcc/switch.S
extern void scalanative_continuation_entry(void);

#define STACK_SIZE ((size_t)1024 * 1024)
// Free stacks kept per thread, and by all threads together.
#define LOCAL_POOL_CAPACITY 16
#define SHARED_POOL_CAPACITY 1024
// Continuations waiting to be resumed before a collection is requested to
// find the unreachable ones. Their stacks are invisible to the GC heap.
#define MIN_CAPTURED_LIMIT 1024

#define pauseCollections scalanative_GC_pause_collections
#define resumeCollections scalanative_GC_resume_collections

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
    void *guardLow;
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

/* What the collector callbacks read about a stack. Slots live in a table of
 * blocks that changes only while no collection runs.
 *
 * - Waiting for a coroutine: `waitingTop` is set and [waitingTop, bottom) is
 *   a root.
 * - Captured by a suspended continuation: tracing its owner scans the list
 *   of stacks and records the mark cycle in the slot of the first one.
 */
typedef struct StackSlot {
    _Atomic(void *) waitingTop;
    void *bottom;
    void *guardPage; // inaccessible, may lie inside [waitingTop, bottom)
    atomic_uint_fast64_t markedIn;
    atomic_uint_fast64_t capturedIn;
    atomic_uint_fast64_t capture; // 0 while no continuation holds the stack
    void *captured;
} StackSlot;

#define STACK_SLOTS_PER_BLOCK 80

typedef struct StackSlotBlock {
    struct StackSlotBlock *next;
    size_t used;
    StackSlot slots[STACK_SLOTS_PER_BLOCK];
} StackSlotBlock;

static StackSlotBlock *stackSlots;

#define FOREACH_STACK_SLOT(slot)                                               \
    for (StackSlotBlock *slot##Block = stackSlots; slot##Block != NULL;        \
         slot##Block = slot##Block->next)                                      \
        for (StackSlot *slot = slot##Block->slots,                             \
                       *slot##End = slot + slot##Block->used;                  \
             slot < slot##End; slot++)

typedef struct SuspendedStack {
    void *top;       // lowest address in use, NULL while not handed over
    void *bottom;    // highest address
    void *guardPage; // inaccessible, may lie inside [top, bottom)
    struct SuspendedStack *next;
    StackSlot *slot;
} SuspendedStack;

typedef struct Coroutine Coroutine;
struct Coroutine {
    SuspendedStack captured; // first: a SuspendedStacks object points here
    StackSlot *slot;         // what the collector reads about this stack
    void *sp;                // saved by the switch while parked
    Coroutine *returnTo;
    Coroutine *from; // the coroutine that last switched to this one
    CoroutineState state;
    ThreadInfo *thread; // owner of a THREAD coroutine
    void *threadBottom; // of a THREAD coroutine, as the collector knows it
    void *mapping;      // start of the stack mapping this struct lives in
    StackBounds bounds;
    // ThreadInfo.pendingStackOverflowException while this stack does not run.
    bool pendingOverflow;
    // ThreadInfo.stackGuardOpen while this stack does not run.
    bool guardOpen;

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

// On the heap: it has to outlive the thread-local block for the cleanup
// that runs when the thread exits.
static SN_ThreadLocal ThreadState *threadState;
static pthread_key_t threadStateKey;
static pthread_once_t threadStateKeyOnce = PTHREAD_ONCE_INIT;

static void releaseThreadState(void *state);

static void createThreadStateKey(void) {
    pthread_key_create(&threadStateKey, releaseThreadState);
}

// Not inlined: a coroutine can continue on another thread, so the result
// must not be used across a switch.
__attribute__((noinline)) static ThreadState *currentThread(void) {
    ThreadState *thread = threadState;
    if (thread == NULL) {
        thread = calloc(1, sizeof(ThreadState));
        if (thread == NULL)
            FATAL("out of memory");
        thread->info = scalanative_currentThreadInfo();
        threadState = thread;
        pthread_once(&threadStateKeyOnce, createThreadStateKey);
        pthread_setspecific(threadStateKey, thread);
    }
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

// Slots are never returned to the system; free ones are linked through
// `bottom`, which is not read while `waitingTop` is unset.
static StackSlot *freeSlots;
static pthread_mutex_t slotsLock = PTHREAD_MUTEX_INITIALIZER;

// Skips the guard page: a frame larger than the room below the guard can
// leave the stack pointer under it.
static void scanStack(GC_ScanRangeFn *scan, void *ctx, void *top, void *bottom,
                      void *guardPage) {
    ThreadInfo guard = {.stackGuardPage = guardPage};
    StackScanRange ranges[2];
    int count = threadStackScanableRanges(top, bottom, &guard, ranges);
    for (int i = 0; i < count; i++) {
        scan(ctx, ranges[i].from, ranges[i].to);
    }
}

// GC_RootProviderFn: the stacks that wait for a coroutine.
static void scanWaitingStacks(void *data, GC_ScanRangeFn *scan, void *ctx) {
    (void)data;
    FOREACH_STACK_SLOT(slot) {
        void *top =
            atomic_load_explicit(&slot->waitingTop, memory_order_relaxed);
        if (top != NULL)
            scanStack(scan, ctx, top, slot->bottom, slot->guardPage);
    }
}

// GC_ObjectTracerFn: the stacks of the continuation that a SuspendedStacks
// object points to with its first field.
static void scanCapturedStacks(void **fields, GC_ScanRangeFn *scan, void *ctx) {
    SuspendedStack *stacks = fields[0];
    // Empty until the continuation is handed over to its object.
    if (stacks == NULL || stacks->top == NULL)
        return;
    atomic_store(&stacks->slot->markedIn, scalanative_GC_mark_cycle());
    for (SuspendedStack *it = stacks; it != NULL; it = it->next) {
        scanStack(scan, ctx, it->top, it->bottom, it->guardPage);
    }
}

// GC_ObjectTracerFn of an anchor: {slot, capture number}. An anchor that died
// earlier can still be marked, so the stacks are read through the slot,
// which is never freed, and only while it holds that capture.
static void scanAnchoredStacks(void **fields, GC_ScanRangeFn *scan, void *ctx) {
    StackSlot *slot = fields[0];
    uint_fast64_t capture = (uintptr_t)fields[1];
    if (atomic_load(&slot->capture) != capture)
        return;
    atomic_store(&slot->markedIn, scalanative_GC_mark_cycle());
    // The first stack; a SuspendedStack is the start of its coroutine.
    for (SuspendedStack *it = slot->captured; it != NULL; it = it->next) {
        scanStack(scan, ctx, it->top, it->bottom, it->guardPage);
    }
}

static StackSlot *acquireSlot(void) {
    pthread_mutex_lock(&slotsLock);
    StackSlot *slot = freeSlots;
    if (slot != NULL) {
        freeSlots = slot->bottom;
    } else {
        StackSlotBlock *block = stackSlots;
        if (block == NULL &&
            !scalanative_GC_add_root_provider(scanWaitingStacks, NULL))
            FATAL("cannot register the stacks with the GC");
        if (block == NULL || block->used == STACK_SLOTS_PER_BLOCK) {
            block = calloc(1, sizeof(StackSlotBlock));
            if (block == NULL)
                FATAL("out of memory");
            block->next = stackSlots;
            stackSlots = block;
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

// Low to high: an inaccessible page, room to handle a stack overflow, the
// guard page that detects one (stackOverflowGuards.c), then the stack and
// the coroutine itself. The room stays inaccessible until an overflow, so a
// stack is two mappings to the system: Linux limits their number. Returns
// NULL when the system refuses the mapping.
static Coroutine *mapCoroutine(void) {
    size_t page = pageSize();
    size_t size = STACK_SIZE + page;
    int flags = MAP_PRIVATE | MAP_ANON;
#ifdef MAP_STACK
    flags |= MAP_STACK; // required for stack memory on OpenBSD
#endif
    char *low = mmap(NULL, size, PROT_READ | PROT_WRITE, flags, -1, 0);
    if (low == MAP_FAILED)
        return NULL;
    char *top = low + page;
    char *guardPage = top + scalanative_StackOverflowGuards_size();
    if (mprotect(low, (size_t)(guardPage + page - low), PROT_NONE) != 0) {
        munmap(low, size);
        return NULL;
    }
    Coroutine *coroutine =
        (Coroutine *)(((uintptr_t)low + size - sizeof(Coroutine)) &
                      ~(uintptr_t)15);
    size_t usable = (size_t)((char *)coroutine - top);
    coroutine->mapping = low;
    coroutine->bounds = (StackBounds){.top = top,
                                      .bottom = coroutine,
                                      .guardPage = guardPage,
                                      .guardLow = top,
                                      .size = usable,
                                      .maxSize = usable};
    coroutine->slot = acquireSlot();
    return coroutine;
}

static void unmapCoroutine(Coroutine *coroutine) {
    releaseSlot(coroutine->slot);
    munmap(coroutine->mapping, STACK_SIZE + pageSize());
}

// Advises the system about the memory of a free stack, except the page of
// the coroutine itself.
static void adviseStack(Coroutine *coroutine, int advice) {
    size_t page = pageSize();
    char *low = (char *)coroutine->bounds.guardPage + page;
    char *high = (char *)((uintptr_t)coroutine & ~(uintptr_t)(page - 1));
    madvise(low, (size_t)(high - low), advice);
}

#if defined(__APPLE__)
#define RELEASE_PAGES MADV_FREE_REUSABLE
#elif defined(__linux__)
#define RELEASE_PAGES MADV_DONTNEED
#else
#define RELEASE_PAGES MADV_FREE
#endif

// Stacks in the shared pool hold no memory besides the coroutine.
static bool giveShared(Coroutine *coroutine) {
    pthread_mutex_lock(&sharedPoolLock);
    bool pooled = sharedPoolSize < SHARED_POOL_CAPACITY;
    if (pooled)
        sharedPoolSize++;
    pthread_mutex_unlock(&sharedPoolLock);
    if (!pooled)
        return false;
    adviseStack(coroutine, RELEASE_PAGES);
    pthread_mutex_lock(&sharedPoolLock);
    coroutine->returnTo = sharedPool;
    sharedPool = coroutine;
    pthread_mutex_unlock(&sharedPoolLock);
    return true;
}

static Coroutine *takeShared(void) {
    pthread_mutex_lock(&sharedPoolLock);
    Coroutine *coroutine = sharedPool;
    if (coroutine != NULL) {
        sharedPool = coroutine->returnTo;
        sharedPoolSize--;
    }
    pthread_mutex_unlock(&sharedPoolLock);
#if defined(__APPLE__)
    // Without it the pages used again are not accounted to the process.
    if (coroutine != NULL)
        adviseStack(coroutine, MADV_FREE_REUSE);
#endif
    return coroutine;
}

// Runs when a thread exits.
static void releaseThreadState(void *state) {
    ThreadState *thread = state;
    while (thread->pool != NULL) {
        Coroutine *coroutine = thread->pool;
        thread->pool = coroutine->returnTo;
        if (!giveShared(coroutine))
            unmapCoroutine(coroutine);
    }
    if (thread->current != NULL && thread->current->state == THREAD) {
        releaseSlot(thread->current->slot);
        free(thread->current);
    }
    free(thread);
}

// A pooled coroutine keeps its mapping, bounds and guards; every other
// field is set again before it is read. Returns NULL when none is free and
// the system refuses a new mapping.
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
    // An overflow opened the guard and nothing closed it again.
    if (coroutine->guardOpen) {
        char *guardLow = coroutine->bounds.guardLow;
        char *guardEnd = (char *)coroutine->bounds.guardPage + pageSize();
        mprotect(guardLow, (size_t)(guardEnd - guardLow), PROT_NONE);
        coroutine->guardOpen = false;
    }
    coroutine->pendingOverflow = false;
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
// x18 is reserved by the platform on Apple; elsewhere it is an ordinary
// register that the switch does not preserve.
#if defined(__APPLE__)
#define PLATFORM_REGISTER
#else
#define PLATFORM_REGISTER "x18",
#endif
static inline __attribute__((always_inline)) void switchStacks(void **saveSp,
                                                               void *loadSp) {
    register void **save __asm__("x0") = saveSp;
    register void *load __asm__("x1") = loadSp;
    __asm__ volatile("adr x9, 1f\n"
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
                     : PLATFORM_REGISTER "x2", "x3", "x4", "x5", "x6", "x7",
                       "x8", "x9", "x10", "x11", "x12", "x13", "x14", "x15",
                       "x16", "x17", "x19", "x20", "x21", "x22", "x23", "x24",
                       "x25", "x26", "x27", "x28", "x30", "v0", "v1", "v2",
                       "v3", "v4", "v5", "v6", "v7", "v8", "v9", "v10", "v11",
                       "v12", "v13", "v14", "v15", "v16", "v17", "v18", "v19",
                       "v20", "v21", "v22", "v23", "v24", "v25", "v26", "v27",
                       "v28", "v29", "v30", "v31", "cc", "memory");
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
                                      .guardLow = info->stackGuardLow,
                                      .size = info->stackSize,
                                      .maxSize = info->maxStackSize};
}

static void applyBounds(ThreadState *thread, Coroutine *coroutine) {
    ThreadInfo *info = thread->info;
    info->stackTop = coroutine->bounds.top;
    info->stackBottom = coroutine->bounds.bottom;
    info->stackGuardPage = coroutine->bounds.guardPage;
    info->stackGuardLow = coroutine->bounds.guardLow;
    info->stackSize = coroutine->bounds.size;
    info->maxStackSize = coroutine->bounds.maxSize;
}

// The page a scan of the parked stack has to skip. The guard region of a
// continuation stack opens as a whole, so a stack that reaches into it is
// readable there; on the stack of a thread a large frame can step over the
// guard page while it is still closed.
static void *closedGuardPage(Coroutine *coroutine) {
    return coroutine->bounds.guardLow != NULL ? NULL
                                              : coroutine->bounds.guardPage;
}

// Where the scan of a parked stack starts. A stack can be left while it runs
// a multi-shot continuation from its heap fragment: its stack pointer is
// then outside of it and the stack is scanned as a whole; the fragment is
// scanned with the continuation that owns it.
static void *scanTop(Coroutine *coroutine) {
    void *sp = coroutine->sp;
    if (coroutine->state == THREAD) {
        ThreadInfo stack = {.stackSize = coroutine->bounds.size,
                            .maxStackSize = coroutine->bounds.maxSize,
                            .stackTop = coroutine->bounds.top,
                            .stackBottom = coroutine->bounds.bottom,
                            .stackGuardPage = coroutine->bounds.guardPage};
        if (isOnThreadStack(&stack, sp))
            return sp;
        void *limit = threadStackScanableLimit(&stack);
        return limit != NULL ? limit : stack.stackTop;
    }
    // The guard region, [guardLow, guardPage], is readable and can hold
    // frames only once an overflow opened it.
    void *low = coroutine->bounds.guardLow;
    if (isInRange(sp, low, coroutine->bounds.bottom))
        return sp;
    return coroutine->guardOpen
               ? low
               : (void *)((char *)coroutine->bounds.guardPage + pageSize());
}

// Read by the collector only while this thread is stopped at a safepoint.
static void addRoots(Coroutine *coroutine) {
    StackSlot *slot = coroutine->slot;
    slot->bottom = coroutine->bounds.bottom;
    slot->guardPage = closedGuardPage(coroutine);
    atomic_store_explicit(&slot->waitingTop, scanTop(coroutine),
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
    thread->info->pendingStackOverflowException = self->pendingOverflow;
    thread->info->stackGuardOpen = self->guardOpen;
    if (self->state == THREAD)
        scalanative_GC_set_running_stack(NULL, self->threadBottom);
    else
        scalanative_GC_set_running_stack(self->mapping, self->bounds.bottom);
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
    self->pendingOverflow = thread->info->pendingStackOverflowException;
    self->guardOpen = thread->info->stackGuardOpen;
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
        self->threadBottom = scalanative_GC_current_stack_bottom();
        self->slot = acquireSlot();
        thread->current = self;
    }
    return thread->current;
}

// Entered through scalanative_continuation_entry.
__attribute__((visibility("hidden"))) void
scalanative_continuation_main(Coroutine *self) {
    arrive(self);
    self->state = RUNNING;
    ContinuationBody *body = self->body;
    void *bodyArg = self->bodyArg;
    resumeCollections();
    // The body catches every Throwable, nothing unwinds past this frame.
    void *result = body((ContinuationBoundaryLabel)self, bodyArg);
    pauseCollections();
    self->result = result;
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
static atomic_uint_fast64_t captures; // numbers them, starting at 1

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
    if (atomic_load_explicit(&reclaimedIn, memory_order_relaxed) ==
        scalanative_GC_completed_mark_cycle())
        return;
    // The marks must not change while they are compared with `cycle`.
    pauseCollections();
    uint_fast64_t cycle = scalanative_GC_completed_mark_cycle();
    if (atomic_exchange(&reclaimedIn, cycle) == cycle) {
        resumeCollections();
        return;
    }
    Coroutine *unreachable = NULL;
    size_t count = 0;
    pthread_mutex_lock(&slotsLock);
    FOREACH_STACK_SLOT(slot) {
        // Another thread may resume this continuation and capture the stack
        // again meanwhile. The exchange fails then, as the number changed,
        // and what was read in between is ignored.
        uint_fast64_t capture = atomic_load(&slot->capture);
        if (capture != 0 && atomic_load(&slot->capturedIn) < cycle &&
            atomic_load(&slot->markedIn) != cycle &&
            atomic_compare_exchange_strong(&slot->capture, &capture, 0)) {
            Coroutine *top = slot->captured;
            top->nextUnreachable = unreachable;
            unreachable = top;
            count++;
        }
    }
    pthread_mutex_unlock(&slotsLock);
    resumeCollections();
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

// Runs in the context that received control in `boundary` or `resume`, with
// collections still paused by the side that switched here.
static void *dispatch(ThreadState *thread, Coroutine *self) {
    Coroutine *from = self->from;
    if (from->state == FINISHED) {
        void *result = from->result;
        resumeCollections();
        destroy(thread, from);
        return result;
    }
    if (from->state == LEFT) {
        void *result = from->result;
        resumeCollections();
        destroyChain(thread, from, from->label);
        return result;
    }
    // Once the callback publishes the continuation, another thread can
    // resume `from` and change its fields.
    SuspendRequest request = from->request;
    resumeCollections();
    return request.fn((Continuation *)from, request.arg);
}

void *scalanative_continuation_oneshot_boundary(ContinuationBody *body,
                                                void *arg) {
    ThreadState *thread = currentThread();
    reclaimUnreachable(thread);
    Coroutine *self = currentCoroutine(thread);
    Coroutine *coroutine = acquire(thread);
    if (coroutine == NULL) {
        // Stacks of unreachable continuations may be all that is missing.
        scalanative_GC_collect();
        thread = currentThread();
        reclaimUnreachable(thread);
        coroutine = acquire(thread);
        if (coroutine == NULL)
            return NULL;
    }
    coroutine->pendingOverflow = false;
    pauseCollections();
    coroutine->state = CREATED;
    coroutine->body = body;
    coroutine->bodyArg = arg;
    coroutine->returnTo = self;
    prepareStack(coroutine);
    thread = switchTo(thread, self, coroutine);
    return dispatch(thread, self);
}

// From delimcc_multishot.c once multi-shot continuations are in use: tells
// whether a multi-shot boundary or resume of this thread is in progress on
// the stack [low, high].
static bool (*multiShotBoundaryIn)(void *low, void *high);

void scalanative_continuation_oneshot_watch_multishot(void *probe) {
    multiShotBoundaryIn = (bool (*)(void *, void *))probe;
}

// Whether suspending or leaving to `label` would take a multi-shot boundary
// off this thread while its handler stays registered here.
bool scalanative_continuation_oneshot_crosses_multishot(
    ContinuationBoundaryLabel label) {
    if (multiShotBoundaryIn == NULL)
        return false;
    Coroutine *target = (Coroutine *)label;
    Coroutine *it = currentCoroutine(currentThread());
    // A label that does not enclose the caller is reported by suspend.
    while (it != NULL && it->state != THREAD) {
        if (multiShotBoundaryIn(it->bounds.top, it->bounds.bottom))
            return true;
        if (it == target)
            return false;
        it = it->returnTo;
    }
    return false;
}

void *scalanative_continuation_oneshot_suspend(ContinuationBoundaryLabel label,
                                               SuspendFn *f, void *arg) {
    ThreadState *thread = currentThread();
    Coroutine *self = currentCoroutine(thread);
    Coroutine *target = (Coroutine *)label;
    checkEnclosing(self, target);

    pauseCollections();
    self->request = (SuspendRequest){f, arg};
    self->label = target;
    self->state = SUSPENDED;
    switchTo(thread, self, target->returnTo);

    self->state = RUNNING;
    void *resumeArg = self->resumeArg;
    resumeCollections();
    return resumeArg;
}

/* Returns `result` from the boundary of `label` and drops the computation
 * inside it, without running anything it left pending. */
void scalanative_continuation_oneshot_leave(ContinuationBoundaryLabel label,
                                            void *result) {
    ThreadState *thread = currentThread();
    Coroutine *self = currentCoroutine(thread);
    Coroutine *target = (Coroutine *)label;
    checkEnclosing(self, target);
    pauseCollections();
    self->result = result;
    self->label = target;
    self->state = LEFT;
    switchTo(thread, self, target->returnTo);
    FATAL("a coroutine that left its boundary was resumed");
}

/* Hands the stacks of a suspended continuation, roots until now, over to
 * `owner`, the SuspendedStacks object that points to it. Returns the anchor
 * to keep reachable from `owner`, or NULL when `owner` itself is traced. */
void *scalanative_continuation_oneshot_captured(Continuation *continuation,
                                                void *owner) {
    Coroutine *top = (Coroutine *)continuation;
    void **anchor = scalanative_GC_trace_owned(owner, scanCapturedStacks,
                                               scanAnchoredStacks);
    pauseCollections();
    FOREACH_CAPTURED(it, top) {
        it->captured.top = scanTop(it);
        it->captured.bottom = it->bounds.bottom;
        it->captured.guardPage = closedGuardPage(it);
        it->captured.next = it == top->label ? NULL : &it->returnTo->captured;
        it->captured.slot = top->slot;
        removeRoots(it);
    }
    atomic_store(&top->slot->markedIn, 0);
    top->slot->capturedIn = scalanative_GC_completed_mark_cycle();
    top->slot->captured = top;
    uint_fast64_t capture = atomic_fetch_add(&captures, 1) + 1;
    atomic_store(&top->slot->capture, capture);
    if (anchor != NULL) {
        anchor[0] = top->slot;
        anchor[1] = (void *)(uintptr_t)capture;
    }
    resumeCollections();
    if (atomic_fetch_add(&capturedCount, 1) >= atomic_load(&capturedLimit)) {
        scalanative_GC_collect();
        reclaimUnreachable(currentThread());
    }
    return anchor;
}

/* `stacks` is the field of a SuspendedStacks object that points to the
 * continuation; resumed at most once. Its stacks are roots again before the
 * field is cleared. */
void *scalanative_continuation_oneshot_resume(void **stacks, void *arg) {
    ThreadState *thread = currentThread();
    Coroutine *self = currentCoroutine(thread);
    pauseCollections();
    Coroutine *top = (Coroutine *)*stacks;
    atomic_store(&top->slot->capture, 0);
    atomic_fetch_sub(&capturedCount, 1);
    FOREACH_CAPTURED(it, top) {
        addRoots(it);
        it->captured = (SuspendedStack){0};
    }
    *stacks = NULL;
    top->label->returnTo = self;
    top->resumeArg = arg;
    thread = switchTo(thread, self, top);
    return dispatch(thread, self);
}

/* Releases a suspended continuation that cannot be handed over to an owner,
 * dropping what it left pending. */
void scalanative_continuation_oneshot_discard(Continuation *continuation) {
    Coroutine *top = (Coroutine *)continuation;
    destroyChain(currentThread(), top, top->label);
}

// Entry points that `boundary` and `suspend` were inlined to in code
// compiled against 0.5.12 and older.
void *scalanative_continuation_boundary(ContinuationBody *body, void *arg) {
    return scalanative_continuation_oneshot_boundary(body, arg);
}

void *scalanative_continuation_suspend(ContinuationBoundaryLabel label,
                                       SuspendFn *f, void *arg,
                                       void *alloc_arg) {
    (void)alloc_arg;
    if (scalanative_continuation_oneshot_crosses_multishot(label))
        FATAL("suspension across a multi-shot boundary");
    return scalanative_continuation_oneshot_suspend(label, f, arg);
}

#elif defined(_WIN32) && defined(__SCALANATIVE_DELIMCC_ONESHOT)

// Not supported on Windows yet. Programs that only mention continuations
// still have to link; the Scala side reports the unsupported feature.
#include "delimcc_oneshot.h"
#include <stdio.h>
#include <stdlib.h>

#define UNSUPPORTED(signature)                                                 \
    signature {                                                                \
        fprintf(stderr, "[ScalaNative Continuations | Error] one-shot "        \
                        "continuations are not supported on Windows\n");       \
        abort();                                                               \
    }

UNSUPPORTED(void *scalanative_continuation_oneshot_boundary(
    ContinuationBody *body, void *arg))
UNSUPPORTED(void *scalanative_continuation_oneshot_suspend(
    ContinuationBoundaryLabel label, SuspendFn *f, void *arg))
UNSUPPORTED(void scalanative_continuation_oneshot_leave(
    ContinuationBoundaryLabel label, void *result))
UNSUPPORTED(void *scalanative_continuation_oneshot_captured(
    Continuation *continuation, void *owner))
UNSUPPORTED(
    void scalanative_continuation_oneshot_discard(Continuation *continuation))
UNSUPPORTED(void *scalanative_continuation_oneshot_resume(void **stacks,
                                                          void *arg))
UNSUPPORTED(void scalanative_continuation_oneshot_watch_multishot(void *probe))
UNSUPPORTED(bool scalanative_continuation_oneshot_crosses_multishot(
    ContinuationBoundaryLabel label))
UNSUPPORTED(void *scalanative_continuation_boundary(ContinuationBody *body,
                                                    void *arg))
UNSUPPORTED(void *scalanative_continuation_suspend(
    ContinuationBoundaryLabel label, SuspendFn *f, void *arg, void *alloc_arg))

#endif
