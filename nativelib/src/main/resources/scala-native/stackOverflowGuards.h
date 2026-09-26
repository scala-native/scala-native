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
    // On Windows that's area guaranteed to be available under StackOverflow
    // detection It's need to be big enough to perform handling - collecting
    // stack trace and throwing exception.
    static size_t computed = -1;
    if (computed == -1) {
        computed = (32 * 1024 + resolvePageSize() - 1) / resolvePageSize();
#ifndef _WIN32
        // Additionally on Unix it needs to be big enough to allow program to
        // reenter recursive function so we can handle it.
        computed = computed * 3 / 2;
#endif
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
static inline bool inStackPageBound(void *pageAddr, void *addr) {
    void *upperBound = (char *)pageAddr + resolvePageSize() * stackGuardPages();
    return isInRange(addr, pageAddr, upperBound);
}
#endif