#include <stdio.h>
#include <stdlib.h>
#include "Util.h"

size_t Util_pad(size_t addr, size_t alignment) {
    size_t alignment_mask = alignment - 1;
    size_t padding = ((addr & alignment_mask) == 0)
                         ? 0
                         : (alignment - (addr & alignment_mask));
    return addr + padding;
}

size_t Util_debug_pages_length(MemoryPage *head) {
    MemoryPage *page = head;
    size_t length = 0;
    while (page != NULL) {
        page = page->next;
        length += 1;
    }
    return length;
}

void Util_debug_print_page(MemoryPage *page, size_t idx) {
    printf("%02zu page (start: %p, size: %zx, offset: %zx)\n", idx, page->start,
           page->size, page->offset);
}

void Util_debug_print_pages(MemoryPage *head) {
    printf("== pages start ==\n");
    MemoryPage *page = head;
    int idx = Util_debug_pages_length(head) - 1;
    while (page != NULL) {
        Util_debug_print_page(page, idx);
        page = page->next;
        idx -= 1;
    }
    printf("== pages end ==\n\n");
}