#include "GCRoots.h"

#include <stdio.h>

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

void GC_Roots_RemoveByRange(GC_Roots **head, AddressRange range) {
    GC_Roots *current = *head;
    GC_Roots *prev = NULL;
    while (current != NULL) {
        if (AddressRange_Contains(range, current->range)) {
            bool detached = false;
            if (prev == NULL) {
                *head = current->next;
                GC_Roots *expected = NULL;
                detached = atomic_compare_exchange_strong(
                    (_Atomic(GC_Roots *) *)head, &expected, current->next);
            } else {
                prev->next = current->next;
                GC_Roots *expected = current;
                detached = atomic_compare_exchange_strong(
                    &prev->next, &expected, current->next);
            }
            if (detached) {
                free(current);
                prev = current;
                current = current->next;
            }
        }
    }
}