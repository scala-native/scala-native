#include "GCRoots.h"

#include <stdio.h>
#include <assert.h>

void GC_Roots_Add(GC_Roots **head, AddressRange range) {
    // Prepend the node with given range to the head of linked list of GC roots
    GC_Roots *node = (GC_Roots *)malloc(sizeof(GC_Roots));
    node->range = range;
    node->next = NULL;
    GC_Roots *currentHead = *head;

    do {
        node->next = currentHead;
    } while (!atomic_compare_exchange_strong((_Atomic(GC_Roots *) *)head,
                                             &currentHead, node));
}

void GC_Roots_Add_Range_Except(GC_Roots **head, AddressRange range,
                               AddressRange except) {
    assert(AddressRange_Contains(range, except));
    if (range.address_low < except.address_low) {
        GC_Roots_Add(head,
                     (AddressRange){range.address_low, except.address_low});
    }
    if (range.address_high > except.address_high) {
        GC_Roots_Add(head,
                     (AddressRange){except.address_high, range.address_high});
    }
}

void GC_Roots_RemoveByRange(GC_Roots **head, AddressRange range) {
    GC_Roots *current = *head;
    GC_Roots *prev = NULL;
    while (current != NULL) {
        if (AddressRange_Contains(range, current->range)) {
            AddressRange current_range = current->range;
            bool detached = false;
            if (prev == NULL) {
                *head = current->next;
            } else {
                prev->next = current->next;
            }
            GC_Roots_Add_Range_Except(head, current_range, range);
            prev = current;
            GC_Roots *next = current->next;
            free(current);
            current = next;
        } else {
            prev = current;
            current = current->next;
        }
    }
}

// void GC_Roots_Print(GC_Roots *head) {
//     printf("== GC Roots start ==\n");
//     GC_Roots *current = head;
//     while (current != NULL) {
//         printf("GC_Roots[%p, %p)\n", current->range.address_low,
//                current->range.address_high);
//         current = current->next;
//     }
//     printf("== GC Roots end ==\n");
// }