#include <stdatomic.h>
#include <stdbool.h>
#include <stdint.h>
#ifdef _WIN32
#include <windows.h>
static void pause_thread(void) { SwitchToThread(); }
#else
#include <sched.h>
static void pause_thread(void) { sched_yield(); }
#endif

static atomic_bool armed;
static atomic_bool paused;
static atomic_bool timedOut;
static atomic_int contentions;
static atomic_int invalidGrowth;
extern long long Time_current_millis(void);
extern int scalanative_gc_test_stale_generation(void);
extern int scalanative_gc_test_recycled_capacity(void);
extern int scalanative_gc_test_reservation(void);

void arm(void) { atomic_store(&armed, true); }
void scalanative_gc_test_on_acquire(void) {
    if (!atomic_exchange(&armed, false))
        return;
    atomic_store(&paused, true);
    long long deadline = Time_current_millis() + 5000;
    while (atomic_load(&contentions) < 2) {
        if (Time_current_millis() > deadline) {
            atomic_store(&timedOut, true);
            break;
        }
        pause_thread();
    }
    atomic_store(&paused, false);
}
void scalanative_gc_test_on_contention(void) {
    if (atomic_load(&paused))
        atomic_fetch_add(&contentions, 1);
}
void scalanative_gc_test_on_grow(void) {
    if (atomic_load(&paused))
        atomic_fetch_add(&invalidGrowth, 1);
}
int checkHandoff(void) {
    return atomic_load(&contentions) >= 2 && atomic_load(&invalidGrowth) == 0 &&
           !atomic_load(&timedOut);
}
int checkStaleGeneration(void) {
    return scalanative_gc_test_stale_generation();
}
int checkRecycledCapacity(void) {
    return scalanative_gc_test_recycled_capacity();
}
int checkReservation(void) { return scalanative_gc_test_reservation(); }
