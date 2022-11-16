// /**
//  * Before running: 
//  *  Comment out this whole file and Line 83 of MemoryPoolZone.c.
//  * Run:
//  * ```
//  * cc MemoryPool.c MemoryPoolZone.c ../gc/shared/MemoryMap.c
//  * MemoryPoolZoneTest.c -o out && ./out
//  * ```
//  */
// #include <stdio.h>
// #include <stdlib.h>
// #include <memory.h>
// #include <assert.h>
// #include "MemoryPool.h"
// #include "MemoryPoolZone.h"

// void debug_print_pages(MemoryPage *head_page, MemoryPage *tail_page);

// size_t get_chunks_length(MemoryChunk *head_chunk) {
//     MemoryChunk *chunk = head_chunk;
//     size_t length = 1;
//     while (chunk != NULL) {
//         chunk = chunk->next;
//         if (chunk != NULL) {
//             length += 1;
//         }
//     }
//     return length;
// }

// size_t get_pages_length(MemoryPage *head_page, MemoryPage *tail_page) {
//     MemoryPage *page = head_page;
//     size_t length = 1;
//     while (page != NULL) {
//         if (page == tail_page)
//             break;
//         page = page->next;
//         length += 1;
//     }
//     return length;
// }

// void test_zone() {
//     MemoryPool *pool = memorypool_open();
//     assert(pool != NULL);

//     int nums_size = 5;
//     int nums[] = {0x10, 0x1000, 0x20, 0x700, 0x900};
//     int nums_more_size = 7;
//     int nums_more[] = {0x10, 0x1000, 0x20, 0x700, 0x900, 0x500, 0x300};
//     long start_pos[7];

//     // z0
//     MemoryPoolZone *z0 = memorypoolzone_open(pool);
//     assert(memorypoolzone_isopen(z0));
//     assert(!memorypoolzone_isclosed(z0));
//     for (int i = 0; i < nums_size; i++) {
//         size_t size = sizeof(char) * nums[i];
//         memorypoolzone_alloc(z0, info, size);
//     }
//     debug_print_pages(z0->head_page, z0->tail_page);
//     assert(get_pages_length(z0->head_page, z0->tail_page) == 4);
//     assert(z0->head_page->offset == 0x900);
//     assert(z0->head_page->next->offset == 0x720);
//     assert(z0->head_page->next->next->offset == 0x1000);
//     assert(z0->head_page->next->next->next->offset == 0x10);
//     MemoryPage *cur = z0->head_page;
//     int i = 0;
//     while (cur != NULL) {
//         start_pos[i] = (long)cur->start;
//         i += 1;
//         cur = cur->next;
//     }
//     memorypoolzone_close(z0);

//     // z1
//     MemoryPoolZone *z1 = memorypoolzone_open(pool);
//     for (int i = 0; i < nums_more_size; i++) {
//         size_t size = sizeof(char) * nums_more[i];
//         memorypoolzone_alloc(z1, NULL, size);
//     }
//     debug_print_pages(z1->head_page, z1->tail_page);
//     assert(get_pages_length(z1->head_page, z1->tail_page) == 5);
//     assert(z1->head_page->offset == 0x300);
//     assert(z1->head_page->next->offset == 0xe00);
//     assert(z1->head_page->next->next->offset == 0x720);
//     assert(z1->head_page->next->next->next->offset == 0x1000);
//     assert(z1->head_page->next->next->next->next->offset == 0x10);
//     assert((long)z1->head_page->next->next->next->next->start == start_pos[0]);
//     assert((long)z1->head_page->next->next->next->start == start_pos[1]);
//     assert((long)z1->head_page->next->next->start == start_pos[2]);
//     assert((long)z1->head_page->next->start == start_pos[3]);
//     memorypoolzone_close(z1);

//     memorypool_free(pool);
// }

// int main() {
//     test_zone();
//     return 0;
// }

// /** Pretty Printing for debugging */

// void debug_print_chunk(MemoryChunk *chunk, size_t idx) {
//     printf("%02zu chunk (start: %p, size: %zx, offset: %zx)\n", idx,
//            chunk->start, chunk->size, chunk->offset);
// }

// void debug_print_page(MemoryPage *page, size_t idx) {
//     printf("%02zu page (start: %p, size: %x, offset: %zx)\n", idx, page->start,
//            MEMORYPOOL_PAGE_SIZE, page->offset);
// }

// size_t debug_get_pages_length(MemoryPage *head_page, MemoryPage *tail_page) {
//     MemoryPage *page = head_page;
//     size_t length = 1;
//     while (page != NULL) {
//         if (page == tail_page)
//             break;
//         page = page->next;
//         length += 1;
//     }
//     return length;
// }

// void debug_print_pages(MemoryPage *head_page, MemoryPage *tail_page) {
//     printf("== pages start ==\n");
//     MemoryPage *page = head_page;
//     int idx = debug_get_pages_length(head_page, tail_page) - 1;
//     while (page != NULL) {
//         debug_print_page(page, idx);
//         if (page == tail_page)
//             break;
//         page = page->next;
//         idx -= 1;
//     }
//     printf("== pages end  ==\n");
// }

// size_t debug_get_chunks_length(MemoryChunk *head_chunk) {
//     MemoryChunk *chunk = head_chunk;
//     size_t length = 1;
//     while (chunk != NULL) {
//         chunk = chunk->next;
//         if (chunk != NULL) {
//             length += 1;
//         }
//     }
//     return length;
// }

// void debug_print_chunks(MemoryChunk *head_chunk) {
//     printf("== chunks start ==\n");
//     MemoryChunk *chunk = head_chunk;
//     size_t idx = debug_get_chunks_length(head_chunk) - 1;
//     while (chunk != NULL) {
//         debug_print_chunk(chunk, idx);
//         chunk = chunk->next;
//         idx -= 1;
//     }
//     printf("== chunks end  ==\n");
// }

// void debug_print_pool(void *_pool) {
//     MemoryPool *pool = (MemoryPool *)_pool;
//     printf("== pool start ==\n");
//     debug_print_chunks(pool->chunk);
//     printf("== pool end  ==\n");
// }
