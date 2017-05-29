#include <atomic>
#include <stdint.h>

using namespace std;

// short
int compare_and_swap_weak_short(int16_t* value, int16_t expected, int16_t desired) {
    atomic<int16_t> atm = reinterpret_cast<std::atomic<int16_t>*>(value);
    *atm = ATOMIC_VAR_INIT(value);
    return atomic_compare_exchange_weak(atm, expected, desired);
}

int compare_and_swap_strong_short(int16_t* value, int16_t expected, int16_t desired) {
    atomic<int16_t> atm = reinterpret_cast<std::atomic<int16_t>*>(value);
    *atm = ATOMIC_VAR_INIT(value);
    return atomic_compare_exchange_strong(atm, expected, desired);
}

// integer
int compare_and_swap_weak_int(int32_t* value, int32_t expected, int32_t desired) {
    atomic<int32_t> atm = reinterpret_cast<std::atomic<int32_t>*>(value);
    *atm = ATOMIC_VAR_INIT(value);
    return atomic_compare_exchange_weak(atm, expected, desired);
}

int compare_and_swap_strong_int(int32_t* value, int32_t expected, int32_t desired) {
    atomic<int32_t> atm = reinterpret_cast<std::atomic<int32_t>*>(value);
    *atm = ATOMIC_VAR_INIT(value);
    return atomic_compare_exchange_strong(atm, expected, desired);
}