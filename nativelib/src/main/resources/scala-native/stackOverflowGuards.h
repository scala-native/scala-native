#ifndef SN_STACKOVERFLOW_GUARDS_H
#define SN_STACKOVERFLOW_GUARDS_H

#include <stdbool.h>
#include <stddef.h>
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

static inline size_t stackGuardPages() {
    static size_t computed = -1;
    if (computed == -1) {
        computed = (32 * 1024 + resolvePageSize() - 1) / resolvePageSize();
    }
    return computed;
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