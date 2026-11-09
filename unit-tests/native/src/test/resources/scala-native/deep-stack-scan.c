#include <alloca.h>
#include <stddef.h>

// Runs callback with a slot about `gap` bytes below the caller, without
// touching the stack in between, as a frame larger than a page would.
void scalanative_test_call_below(size_t gap, void (*callback)(void **)) {
    void **slot = (void **)alloca(gap);
    *slot = NULL;
    callback(slot);
    __asm__ volatile("" : : "r"(slot) : "memory");
}
