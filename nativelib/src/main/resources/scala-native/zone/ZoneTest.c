// /**
//  * Run:
//  * ```
//  * cc MemoryPool.c LargeMemoryPool.c Util.c Zone.c ../gc/shared/MemoryMap.c
//  * ZoneTest.c -o out && ./out
//  * ```
//  */
// #include <stdio.h>
// #include <stdlib.h>
// #include <memory.h>
// #include <assert.h>
// #include "Zone.h"
// #include "Util.h"

// void scalanative_GC_add_roots(void *start, void *end) {}
// void scalanative_GC_remove_roots(void *start, void *end) {}
// void calculate_memory_pool_info(size_t n, size_t *sizes, size_t *pagesSize,
//                                 size_t *pageOffsets);
// size_t Util_debug_pages_length(MemoryPage *head);
// void Util_debug_print_page(MemoryPage *page, size_t idx);
// void Util_debug_print_pages(MemoryPage *head);

// void test() {
//     size_t sizes[] = {0x10,  0x2000, 0x800,  0x700, 0x900,
//                       0x500, 0x1000, 0x2000, 0x600};
//     size_t n = sizeof(sizes) / sizeof(size_t);
//     void *infos[n];
//     size_t pagesSize = 0;
//     size_t pageOffsets[n];
//     size_t largePagesSize = 0;
//     size_t largePageOffsets[n];
//     size_t sum = 0;
//     size_t sumLarge = 0;
//     for (int i = 0; i < n; i++) {
//         infos[i] = malloc(sizes[i]);
//     }
//     size_t startsSize = 0;
//     void *starts[n];
//     MemoryPage *page = NULL;

//     // z0
//     size_t n0 = 5;
//     Zone *z0 = scalanative_zone_open();
//     for (int i = 0; i < n0; i++) {
//         scalanative_zone_alloc(z0, infos[i], sizes[i]);
//     }
//     calculate_memory_pool_info(n0, sizes, &pagesSize, pageOffsets);
//     assert(Util_debug_pages_length(z0->page) == pagesSize);
//     page = z0->page;
//     for (int i = 0; i < pagesSize; i++) {
//         assert(page->offset == pageOffsets[pagesSize - 1 - i]);
//         starts[i] = page->start;
//         page = page->next;
//     }
//     startsSize = pagesSize;
//     // Allocate large objects.
//     size_t largeSizes0[] = {0x3000, 0x100000, 0x4000, 0x8000};
//     for (int i = 0; i < 4; i++) {
//         void *info = malloc(largeSizes0[i]);
//         scalanative_zone_alloc(z0, info, largeSizes0[i]);
//         free(info);
//     }
//     scalanative_zone_close(z0);

//     // z1
//     size_t n1 = 9;
//     Zone *z1 = scalanative_zone_open();
//     for (int i = 0; i < n1; i++) {
//         scalanative_zone_alloc(z1, infos[i], sizes[i]);
//     }
//     calculate_memory_pool_info(n1, sizes, &pagesSize, pageOffsets);
//     assert(Util_debug_pages_length(z0->page) == pagesSize);
//     page = z0->page;
//     for (int i = 0; i < pagesSize; i++) {
//         assert(page->offset == pageOffsets[pagesSize - 1 - i]);
//         size_t idx = pagesSize - 1 - i;
//         if (idx < startsSize) {
//             assert(page->start == starts[idx]);
//         }
//         page = page->next;
//     }
//     size_t largeSizes1[] = {0x100000, 0x4000, 0x3000, 0x8000};
//     for (int i = 0; i < 4; i++) {
//         void *info = malloc(largeSizes1[i]);
//         scalanative_zone_alloc(z1, info, largeSizes1[i]);
//         free(info);
//     }
//     // Allocate large objects.
//     assert(Util_debug_pages_length(z1->largePage) == 3);
//     assert(z1->largePage->size == 0x8000);
//     assert(z1->largePage->next->size == 0x8000);
//     assert(z1->largePage->next->next->size == 0x100000);
//     scalanative_zone_close(z1);

//     for (int i = 0; i < n; i++) {
//         free(infos[i]);
//     }
// }

// int main() {
//     test();
//     return 0;
// }

// void calculate_memory_pool_info(size_t n, size_t *sizes, size_t *pagesSize,
//                                 size_t *pageOffsets) {
//     size_t sum = 0;
//     *pagesSize = 0;
//     for (int i = 0; i < n; i++) {
//         if (sizes[i] <= MEMORYPOOL_PAGE_SIZE) {
//             if (sum + sizes[i] > MEMORYPOOL_PAGE_SIZE) {
//                 pageOffsets[*pagesSize] = sum;
//                 *pagesSize += 1;
//                 sum = 0;
//             }
//             sum += sizes[i];
//         }
//     }
//     pageOffsets[*pagesSize] = sum;
//     *pagesSize += 1;
// }

// size_t Util_debug_pages_length(MemoryPage *head) {
//     MemoryPage *page = head;
//     size_t length = 0;
//     while (page != NULL) {
//         page = page->next;
//         length += 1;
//     }
//     return length;
// }

// void Util_debug_print_page(MemoryPage *page, size_t idx) {
//     printf("%02zu page (start: %p, size: %zx, offset: %zx)\n", idx,
//     page->start,
//            page->size, page->offset);
// }

// void Util_debug_print_pages(MemoryPage *head) {
//     printf("== pages start ==\n");
//     MemoryPage *page = head;
//     int idx = Util_debug_pages_length(head) - 1;
//     while (page != NULL) {
//         Util_debug_print_page(page, idx);
//         page = page->next;
//         idx -= 1;
//     }
//     printf("== pages end ==\n\n");
// }