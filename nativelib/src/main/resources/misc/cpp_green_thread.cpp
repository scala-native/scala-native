#include <assert.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

enum {
    MaxGThreads = 16,
    MaxGTQueue = 2000000,
    StackSize = 0x400000,
};

typedef int scalanative_misc_greenthreads_threadId;

typedef void (*scalanative_misc_greenthreads_func)(
    scalanative_misc_greenthreads_threadId);

struct scalanative_misc_greenthreads_gt {
    scalanative_misc_greenthreads_threadId id;
    enum {
        Unused,
        Running,
        Ready,
    } st;
    char *stack;
    scalanative_misc_greenthreads_func f;
    struct gtctx {
        uint64_t rsp;
        uint64_t r15;
        uint64_t r14;
        uint64_t r13;
        uint64_t r12;
        uint64_t rbx;
        uint64_t rbp;
    } ctx;
    int stackSize;
};

scalanative_misc_greenthreads_gt gttbl[MaxGThreads];
scalanative_misc_greenthreads_gt *gtcur = 0;
scalanative_misc_greenthreads_gt *gtqueue = 0;
scalanative_misc_greenthreads_gt *gtqueue_tail = 0;

extern "C" void scalanative_misc_greenthreads_gtinit(void);
extern "C" void scalanative_misc_greenthreads_gtret(int ret);
extern "C" void scalanative_misc_greenthreads_gtswtch(
    scalanative_misc_greenthreads_gt::gtctx *oldt,
    scalanative_misc_greenthreads_gt::gtctx *newt);
extern "C" bool scalanative_misc_greenthreads_gtyield(void);
extern "C" void scalanative_misc_greenthreads_gtstop(void);
extern "C" int
scalanative_misc_greenthreads_gtgo(scalanative_misc_greenthreads_threadId id,
                                   scalanative_misc_greenthreads_func f);

extern "C" void __attribute__((noinline))
scalanative_misc_greenthreads_gtinit(void) {
    scalanative_misc_greenthreads_gt *p;
    gtcur = &gttbl[0];
    gtcur->st = scalanative_misc_greenthreads_gt::Running;
    gtcur->stack = 0; // (char*)malloc(StackSize);
    gtcur->id = 0;
    gtcur->f = 0;
    gtcur->stackSize = 0;
    gtqueue = (scalanative_misc_greenthreads_gt *)malloc(
        sizeof(scalanative_misc_greenthreads_gt) * MaxGTQueue);
    gtqueue_tail = &gtqueue[0];

    for (p = &gttbl[1]; p != &gttbl[MaxGThreads]; p++) {
        p->st = scalanative_misc_greenthreads_gt::Unused;
        p->stack = (char *)malloc(StackSize);
        p->stackSize = StackSize;
    }

    for (p = &gtqueue[0]; p != &gtqueue[MaxGTQueue]; p++) {
        p->st = scalanative_misc_greenthreads_gt::Unused;
        p->stack = 0;
        p->stackSize = 0;
    }
}

extern "C" void __attribute__((noreturn)) __attribute__((noinline))
scalanative_misc_greenthreads_gtret(int ret) {
    if (gtcur != &gttbl[0]) {
        gtcur->st = scalanative_misc_greenthreads_gt::Unused;
        scalanative_misc_greenthreads_gtyield();
        assert(!"reachable");
    }
    while (scalanative_misc_greenthreads_gtyield())
        ;
    exit(ret);
}

extern "C" void
scalanative_misc_greenthreads_pop(scalanative_misc_greenthreads_gt *p,
                                  scalanative_misc_greenthreads_gt *q) {
    memset(p->stack, StackSize, 0);

    if (q->stackSize == 0) {
        *(uint64_t *)&p->stack[StackSize - 8] =
            (uint64_t)scalanative_misc_greenthreads_gtstop;
        *(uint64_t *)&p->stack[StackSize - 16] = (uint64_t)q->f;
        p->ctx.rsp = (uint64_t)&p->stack[StackSize - 16];
    } else {
        memcpy(p->stack + (StackSize - q->stackSize), q->stack, q->stackSize);
        p->ctx.rsp = (uint64_t)&p->stack[StackSize - q->stackSize];
    }

    p->st = scalanative_misc_greenthreads_gt::Ready;
    p->id = q->id;
    q->st = scalanative_misc_greenthreads_gt::Unused;
    p->f = q->f;
}

extern "C" bool __attribute__((noinline))
scalanative_misc_greenthreads_gtyield(void) {
    if (!gtcur) {
        return false;
    }

    scalanative_misc_greenthreads_gt *p, *q;
    scalanative_misc_greenthreads_gt temp;
    scalanative_misc_greenthreads_gt::gtctx *oldt, *newt;

    for (p = &gttbl[1];
         p != &gttbl[MaxGThreads] &&
         (gtqueue && gtqueue[0].st == scalanative_misc_greenthreads_gt::Ready);
         p++) {
        if (p != gtcur && p->st == scalanative_misc_greenthreads_gt::Unused) {
            for (q = &gtqueue[0]; q != gtqueue_tail; q++) {
                if (q->st == scalanative_misc_greenthreads_gt::Ready) {
                    scalanative_misc_greenthreads_pop(p, q);
                    if (&gtqueue[0] != gtqueue_tail) {
                        --gtqueue_tail;
                    }
                    if (q != gtqueue_tail) {
                        temp = *q;
                        *q = *gtqueue_tail;
                        *p = temp;
                    }
                    break;
                }
            }
        }
    }

    p = gtcur;
    while (p->st != scalanative_misc_greenthreads_gt::Ready) {
        if (++p == &gttbl[MaxGThreads])
            p = &gttbl[0];
        if (p == gtcur)
            return false;
    }

    if (gtcur->st != scalanative_misc_greenthreads_gt::Unused)
        gtcur->st = scalanative_misc_greenthreads_gt::Ready;
    p->st = scalanative_misc_greenthreads_gt::Running;
    oldt = &gtcur->ctx;
    newt = &p->ctx;
    // printf("Switch %lld->%lld\n", gtcur->id, p->id);
    gtcur = p;
    scalanative_misc_greenthreads_gtswtch(oldt, newt);
    return true;
}

extern "C" void __attribute__((noinline))
scalanative_misc_greenthreads_gtstop(void) {
    scalanative_misc_greenthreads_gtret(0);
}

extern "C" int __attribute__((noinline))
scalanative_misc_greenthreads_gtgo(scalanative_misc_greenthreads_threadId id,
                                   scalanative_misc_greenthreads_func f) {
    scalanative_misc_greenthreads_gt *p = gtqueue_tail++;
    if (p == &gtqueue[MaxGTQueue]) {
        --gtqueue_tail;
        printf("Queue overflow on %d\n", id);
        return -1;
    }

    p->st = scalanative_misc_greenthreads_gt::Ready;
    p->f = f;
    p->id = id;
    // printf("Queued #%lld\n", id);

    return 0;
}

scalanative_misc_greenthreads_gt *getUnusedSlot(bool wait = false) {
    scalanative_misc_greenthreads_gt *p = 0;
    do {
        for (p = &gttbl[1]; p != &gttbl[MaxGThreads]; p++) {
            if (p->st == scalanative_misc_greenthreads_gt::Unused) {
                return p;
            }
        }
        scalanative_misc_greenthreads_gtyield();
    } while (wait);
    return p;
}

extern "C" int __attribute__((noinline))
scalanative_misc_greenthreads_fork(scalanative_misc_greenthreads_threadId id) {
    if (!gtcur) {
        scalanative_misc_greenthreads_gtinit();
    }

    scalanative_misc_greenthreads_gt *p = gtqueue_tail++;
    if (p == &gtqueue[MaxGTQueue]) {
        --gtqueue_tail;
        printf("Queue overflow on %d\n", id);
        return -1;
    } else {
        p->st = scalanative_misc_greenthreads_gt::Ready;
        p->id = id;
        p->f = (scalanative_misc_greenthreads_func)__builtin_return_address(0);
        long long **frameAddress =
            (long long **)((char *)__builtin_frame_address(0)
#ifdef _WIN32
                           + 0x50
#endif
                           );
        long long *returnAddress = (long long *)__builtin_extract_return_addr(
            __builtin_return_address(0));
        while (frameAddress[0] != returnAddress) {
            frameAddress++;
        }
        p->stackSize = 0x100;
        if (p->stack == 0) {
            p->stack = (char *)malloc(p->stackSize);
        }
        memcpy(p->stack, frameAddress, p->stackSize);
    }

    scalanative_misc_greenthreads_gtyield();
    return 0;
}

extern "C" void __attribute__((noinline)) scalanative_misc_greenthreads_gtswtch(
    scalanative_misc_greenthreads_gt::gtctx *oldt,
    scalanative_misc_greenthreads_gt::gtctx *newt) {
#ifdef _WIN32
    asm("addq $16, %rsp");
    asm("movq %rsp, 0x00(%rcx)");
    asm("movq %r15, 0x08(%rcx)");
    asm("movq %r14, 0x10(%rcx)");
    asm("movq %r13, 0x18(%rcx)");
    asm("movq %r12, 0x20(%rcx)");
    asm("movq %rbx, 0x28(%rcx)");
    asm("movq %rbp, 0x30(%rcx)");

    asm("movq 0x00(%rdx), %rsp");
    asm("movq 0x08(%rdx), %r15");
    asm("movq 0x10(%rdx), %r14");
    asm("movq 0x18(%rdx), %r13");
    asm("movq 0x20(%rdx), %r12");
    asm("movq 0x28(%rdx), %rbx");
    asm("movq 0x30(%rdx), %rbp");
    asm("subq $16, %rsp");
#else
    asm("pop %rbp");
    asm("movq %rsp, 0x00(%rdi)");
    asm("movq %r15, 0x08(%rdi)");
    asm("movq %r14, 0x10(%rdi)");
    asm("movq %r13, 0x18(%rdi)");
    asm("movq %r12, 0x20(%rdi)");
    asm("movq %rbx, 0x28(%rdi)");
    asm("movq %rbp, 0x30(%rdi)");

    asm("movq 0x00(%rsi), %rsp");
    asm("movq 0x08(%rsi), %r15");
    asm("movq 0x10(%rsi), %r14");
    asm("movq 0x18(%rsi), %r13");
    asm("movq 0x20(%rsi), %r12");
    asm("movq 0x28(%rsi), %rbx");
    asm("movq 0x30(%rsi), %rbp");
    asm("push %rbp");
#endif
}

extern "C" scalanative_misc_greenthreads_threadId
scalanative_misc_greenthreads_currentthreadid() {
    return gtcur ? gtcur->id : 0;
}

extern "C" void
scalanative_misc_greenthreads_join(scalanative_misc_greenthreads_threadId id) {
    scalanative_misc_greenthreads_gt *p = 0;
    bool wait = false;
    do {
        wait = false;
        for (p = &gttbl[1]; p != &gttbl[MaxGThreads]; p++) {
            if (p->id == id &&
                p->st != scalanative_misc_greenthreads_gt::Unused) {
                wait = true;
                break;
            }
        }
        scalanative_misc_greenthreads_gtyield();
    } while (wait);
}
