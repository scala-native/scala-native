#ifndef SN_STACKOVERFLOW_GUARDS_H
#define SN_STACKOVERFLOW_GUARDS_H

#include "nativeThreadTLS.h"
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

#ifdef _WIN32
#include <windows.h>
#else
#include <signal.h>
#include <unistd.h>
#endif

size_t scalanative_StackOverflowGuards_size();
void scalanative_StackOverflowGuards_setup(bool isMainThread);
void scalanative_StackOverflowGuards_reset();
void scalanative_StackOverflowGuards_close();
void scalanative_StackOverflowGuards_check();

extern size_t scalanative_page_size();
static inline size_t resolvePageSize() { return scalanative_page_size(); }

static inline void *alignToNextPage(void *addr) {
    size_t pageSize = resolvePageSize();
    return (void *)(((uintptr_t)addr + pageSize - 1) & ~(pageSize - 1));
}
static inline void *alignToPageStart(void *addr) {
    size_t pageSize = resolvePageSize();
    return (void *)(((uintptr_t)addr) & ~(pageSize - 1));
}

static inline size_t stackGuardPages() {
    // Reserved SO region must be large enough to finish the faulting frame and
    // re-enter a recursive method so StackOverflowGuards_check can throw
    // outside the signal/VEH context (monitor/finally cleanup requires that).
    static size_t computed = -1;
    if (computed == -1) {
        computed = (32 * 1024 + resolvePageSize() - 1) / resolvePageSize();
        computed = computed * 3 / 2;
    }
    return computed;
}
static inline void *threadStackScanableLimit(ThreadInfo *threadInfo) {
    if (threadInfo == NULL)
        return NULL;
    if (threadInfo->stackGuardPage == NULL || threadInfo->stackTop == NULL)
        return NULL;
    void *ofStackGuard = (char *)threadInfo->stackGuardPage +
                         stackGuardPages() * resolvePageSize();
    void *ofStackTop = threadInfo->stackTop;
    return (ofStackGuard > ofStackTop) ? ofStackGuard : ofStackTop;
}

typedef struct StackScanRange {
    void *from;
    void *to;
} StackScanRange;

// [stackTop, stackBottom) split to exclude the PROT_NONE guard page so the
// conservative GC scan cannot fault on it; writes the 1-2 sub-ranges to `out`
// and returns their count.
static inline int threadStackScanableRanges(void *stackTop, void *stackBottom,
                                            ThreadInfo *threadInfo,
                                            StackScanRange out[2]) {
    void *guard = threadInfo != NULL ? threadInfo->stackGuardPage : NULL;
    // That guard region opens as a whole: a stack reaching into it is
    // readable there.
    if (threadInfo != NULL && threadInfo->stackGuardLow != NULL)
        guard = NULL;
    if (guard != NULL && guard >= stackTop && guard < stackBottom) {
        void *afterGuard = (char *)guard + resolvePageSize();
        out[0].from = stackTop;
        out[0].to = guard;
        if (afterGuard < stackBottom) {
            out[1].from = afterGuard;
            out[1].to = stackBottom;
            return 2;
        }
        return 1;
    }
    out[0].from = stackTop;
    out[0].to = stackBottom;
    return 1;
}

static inline bool isInRange(void *addr, void *start, void *end) {
    if (start > end) {
        void *temp = start;
        start = end;
        end = temp;
    }
    return start <= addr && addr < end;
}
// Frames larger than the guard can jump past the soft limit (stackTop) into
// the rest of the thread's stack.
static inline bool isOnThreadStack(ThreadInfo *threadInfo, void *addr) {
    size_t size = threadInfo->maxStackSize > threadInfo->stackSize
                      ? threadInfo->maxStackSize
                      : threadInfo->stackSize;
    uintptr_t bottom = (uintptr_t)threadInfo->stackBottom;
    // An unlimited RLIMIT_STACK would wrap below address 0.
    void *low = size < bottom ? (void *)(bottom - size) : NULL;
    return isInRange(addr, low, threadInfo->stackBottom);
}
#ifndef _WIN32
static inline bool isOnSignalHandlerStack(ThreadInfo *threadInfo, void *addr) {
    return threadInfo->signalHandlerStack != NULL &&
           isInRange(addr, threadInfo->signalHandlerStack,
                     (char *)threadInfo->signalHandlerStack +
                         threadInfo->signalHandlerStackSize);
}
#endif
static inline bool inStackPageBound(void *pageAddr, void *addr) {
    void *upperBound = (char *)pageAddr + resolvePageSize() * stackGuardPages();
    return isInRange(addr, pageAddr, upperBound);
}
#ifndef _WIN32
// SIGSEGV/SIGBUS raised by this process (e.g. re-raised by LLVM's crash
// handler, #4805) has no fault address; returning without chaining to
// previous handlers re-executes the fault.
static inline bool isSelfSentSignal(const siginfo_t *info) {
    switch (info->si_code) {
    case SI_USER:
    case SI_QUEUE:
#ifdef SI_TKILL
    case SI_TKILL:
#endif
#ifdef SI_LWP
    case SI_LWP:
#endif
        return info->si_pid == getpid();
    default:
        return false;
    }
}
#endif
#endif