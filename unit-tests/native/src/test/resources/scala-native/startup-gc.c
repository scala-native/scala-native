#if !defined(_WIN32) && defined(SCALANATIVE_MULTITHREADING_ENABLED)

// _GNU_SOURCE needed for MAP_ANONYMOUS and pthread_attr_setstack on glibc
// (must be before any includes)
#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif

#include <errno.h>
#include <pthread.h>
#include <sys/mman.h>

extern int scalanative_GC_pthread_create(pthread_t *, pthread_attr_t *,
                                         void *(*)(void *), void *);
extern void scalanative_GC_collect(void);

static void *collectBeforeStackBoundsAreInitialized(void *unused) {
    (void)unused;
    // The GC proxy registered this thread before Scala stack initialization.
    scalanative_GC_collect();
    return NULL;
}

int scalanative_test_collect_before_stack_bounds_are_initialized(void) {
    const size_t mappingSize = 1024 * 1024;
    const size_t stackSize = 256 * 1024;
    // Start writable: NetBSD's PaX MPROTECT refuses to raise protections.
    void *mapping = mmap(NULL, mappingSize, PROT_READ | PROT_WRITE,
                         MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    if (mapping == MAP_FAILED)
        return errno;

    // A guessed 512 KiB scan must cross the protected mapping.
    void *stack = (char *)mapping + mappingSize - stackSize;
    int result = mprotect(mapping, mappingSize - stackSize, PROT_NONE);
    if (result != 0) {
        result = errno;
        goto unmap;
    }

    pthread_attr_t attr;
    result = pthread_attr_init(&attr);
    if (result != 0)
        goto unmap;
    result = pthread_attr_setstack(&attr, stack, stackSize);
    if (result == 0) {
        pthread_t thread;
        result = scalanative_GC_pthread_create(
            &thread, &attr, collectBeforeStackBoundsAreInitialized, NULL);
        if (result == 0)
            result = pthread_join(thread, NULL);
    }
    pthread_attr_destroy(&attr);
unmap:
    munmap(mapping, mappingSize);
    return result;
}

#else
int scalanative_test_collect_before_stack_bounds_are_initialized(void) {
    return 0;
}
#endif
