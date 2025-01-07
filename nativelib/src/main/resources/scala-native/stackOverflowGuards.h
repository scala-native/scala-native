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

extern size_t scalanative_page_size();
static inline size_t resolvePageSize() { return scalanative_page_size(); }

#define StackGuardPages 2
static inline bool inStackPageBound(void *pageAddr, void *addr) {
    void *upperBound = (char *)pageAddr + resolvePageSize() * StackGuardPages;
    return addr >= pageAddr && addr < upperBound;
}
static inline bool belowStackPageBounds(void *pageAddr, void *addr) {
    void *upperBound = (char *)pageAddr + resolvePageSize() * StackGuardPages;
    return addr < upperBound;
}

size_t scalanative_stackOverflowGuardsSize();
void scalanative_resetStackOverflowGuards();
void scalanative_handlePendingStackOverflowError();

#endif