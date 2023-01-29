#include <atomic>

// Due to some bug (in GCC?) it might not be possible to access
// atomic_thread_fence using C (would fail with undefined reference to function)
// For some strange reason it works in C++
extern "C" {
void scalanative_atomic_thread_fence(std::memory_order order) {
    return std::atomic_thread_fence(order);
}
void scalanative_atomic_signal_fence(std::memory_order order) {
    return std::atomic_signal_fence(order);
}
}
