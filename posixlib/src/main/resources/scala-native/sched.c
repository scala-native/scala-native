#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_POSIX_SCHED)
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))

#include <sched.h>

int scalanative_sched_other() { return SCHED_OTHER; }

int scalanative_sched_fifo() { return SCHED_FIFO; }

int scalanative_sched_rr() { return SCHED_RR; }

int scalanative_sched_sporadic() {
#ifdef SCHED_SPORADIC
    return SCHED_SPORADIC;
#else
    return SCHED_OTHER;
#endif
}

int scalanative_sched_batch() {
#ifdef SCHED_BATCH
    return SCHED_BATCH;
#else
    return SCHED_OTHER;
#endif
}

int scalanative_sched_idle() {
#ifdef SCHED_IDLE
    return SCHED_IDLE;
#else
    return SCHED_OTHER;
#endif
}

int scalanative_sched_deadline() {
#ifdef SCHED_DEADLINE
    return SCHED_DEADLINE;
#else
    return SCHED_OTHER;
#endif
}

#endif // Unix or Mac OS
#endif