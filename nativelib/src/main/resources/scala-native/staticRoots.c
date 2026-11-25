#include "staticRoots.h"
#include "gc/shared/ScalaNativeGC.h"
#include "gc/shared/GCHooks.h"
#include <stdatomic.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <stdlib.h>

#ifdef SCALANATIVE_MULTITHREADING_ENABLED
#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#else
#include <pthread.h>
#endif
#endif

static void registerSection(const SNStaticSection *section) {
    if (section->start != section->end)
        scalanative_GC_add_roots(section->start, section->end);
}

#ifdef SCALANATIVE_MULTITHREADING_ENABLED

/* Lock words of static objects (classes, string literals and their backing
 * arrays) whose monitor
 * was inflated: the only references these objects hold to the heap. The list
 * only grows, monitors are never deflated. Writers serialize with a lock;
 * collectors read the published count without locking, so they never wait
 * for a mutator stopped while holding it.
 */
#define LOCK_WORDS_PER_CHUNK 64

typedef struct LockWordChunk {
    struct LockWordChunk *next;
    void **lockWords[LOCK_WORDS_PER_CHUNK];
} LockWordChunk;

static LockWordChunk firstChunk;
static LockWordChunk *lastChunk = &firstChunk;
static atomic_size_t publishedLockWords;

#ifdef _WIN32
static SRWLOCK lockWordsLock = SRWLOCK_INIT;
static void lockLockWords(void) { AcquireSRWLockExclusive(&lockWordsLock); }
static void unlockLockWords(void) { ReleaseSRWLockExclusive(&lockWordsLock); }
#else
static pthread_mutex_t lockWordsLock = PTHREAD_MUTEX_INITIALIZER;
static void lockLockWords(void) {
    if (pthread_mutex_lock(&lockWordsLock) != 0)
        abort();
}
static void unlockLockWords(void) { pthread_mutex_unlock(&lockWordsLock); }
#endif

// The i-th registered lock word, `chunk` holds the chunk of entry i - 1
static void **lockWordAt(LockWordChunk **chunk, size_t i) {
    if (i > 0 && i % LOCK_WORDS_PER_CHUNK == 0)
        *chunk = (*chunk)->next;
    return (*chunk)->lockWords[i % LOCK_WORDS_PER_CHUNK];
}

static bool isRegistered(void **lockWord, size_t count) {
    LockWordChunk *chunk = &firstChunk;
    for (size_t i = 0; i < count; i++) {
        if (lockWordAt(&chunk, i) == lockWord)
            return true;
    }
    return false;
}

/* Registration completes before the caller publishes the monitor in the lock
 * word, so a collection either sees the lock word or finds it not inflated.
 */
static void registerLockWord(void **lockWord) {
    lockLockWords();
    size_t count =
        atomic_load_explicit(&publishedLockWords, memory_order_relaxed);
    // Threads racing to inflate the same monitor register it once
    if (!isRegistered(lockWord, count)) {
        size_t index = count % LOCK_WORDS_PER_CHUNK;
        if (count > 0 && index == 0) {
            LockWordChunk *chunk = calloc(1, sizeof(LockWordChunk));
            if (chunk == NULL)
                abort();
            lastChunk->next = chunk;
            lastChunk = chunk;
        }
        lastChunk->lockWords[index] = lockWord;
        atomic_store_explicit(&publishedLockWords, count + 1,
                              memory_order_release);
    }
    unlockLockWords();
}

/* Root provider, see GCHooks.h: scans the inflated lock words, the only ones
 * pointing to a monitor, thin locks hold none. */
static void scanLockWords(void *data, GC_ScanRangeFn *scan, void *ctx) {
    (void)data;
    size_t count =
        atomic_load_explicit(&publishedLockWords, memory_order_acquire);
    LockWordChunk *chunk = &firstChunk;
    for (size_t i = 0; i < count; i++) {
        void **lockWord = lockWordAt(&chunk, i);
        if (Field_isInflatedLock(*lockWord))
            scan(ctx, lockWord, lockWord + 1);
    }
}

static bool containsObject(const SNStaticSection *section, void *object) {
    uintptr_t address = (uintptr_t)object;
    return address >= (uintptr_t)section->start &&
           address < (uintptr_t)section->end;
}

/* Called when a monitor is inflated on any object. Heap objects and other
 * static storage are ignored: only objects in the class and string sections
 * need their lock word registered, global objects are always scanned. This
 * works independently of the collector's heap layout or allocation API.
 */
SN_SECTION_LOCAL void scalanative_register_object_roots(void *object) {
    if (containsObject(scalanative_static_classinfo_section(), object) ||
        containsObject(scalanative_static_strings_section(), object)) {
        registerLockWord((void **)&((Object *)object)->lockWord);
    }
}

static void registerStaticObjectRoots(void) {
    registerSection(scalanative_static_globals_section());
    if (!scalanative_GC_add_root_provider(scanLockWords, NULL))
        abort();
}

#else

// Without multithreading objects have no lock word to register

static void registerStaticObjectRoots(void) {
    registerSection(scalanative_static_globals_section());
}

SN_SECTION_LOCAL void scalanative_register_object_roots(void *object) {
    (void)object;
}

static void lockLockWords(void) {}
static void unlockLockWords(void) {}

#endif

/* Array classes created at runtime, see runtime.ArrayClasses. Never freed;
 * their name and lock word are roots. Append-only, read without locking.
 */
#define RUNTIME_CLASSES_PER_CHUNK 64

typedef struct RuntimeClassChunk {
    struct RuntimeClassChunk *next;
    Rtti *classes[RUNTIME_CLASSES_PER_CHUNK];
} RuntimeClassChunk;

static RuntimeClassChunk firstClassChunk;
static RuntimeClassChunk *lastClassChunk = &firstClassChunk;
static atomic_size_t publishedRuntimeClasses;

SN_SECTION_LOCAL void *scalanative_alloc_runtime_class(size_t size) {
    Rtti *cls = calloc(1, size);
    if (cls == NULL)
        return NULL;
    lockLockWords();
    size_t count =
        atomic_load_explicit(&publishedRuntimeClasses, memory_order_relaxed);
    size_t index = count % RUNTIME_CLASSES_PER_CHUNK;
    if (count > 0 && index == 0) {
        RuntimeClassChunk *chunk = calloc(1, sizeof(RuntimeClassChunk));
        if (chunk == NULL)
            abort();
        lastClassChunk->next = chunk;
        lastClassChunk = chunk;
    }
    lastClassChunk->classes[index] = cls;
    atomic_store_explicit(&publishedRuntimeClasses, count + 1,
                          memory_order_release);
    unlockLockWords();
    return cls;
}

static void scanRuntimeClasses(void *data, GC_ScanRangeFn *scan, void *ctx) {
    (void)data;
    size_t count =
        atomic_load_explicit(&publishedRuntimeClasses, memory_order_acquire);
    RuntimeClassChunk *chunk = &firstClassChunk;
    for (size_t i = 0; i < count; i++) {
        if (i > 0 && i % RUNTIME_CLASSES_PER_CHUNK == 0)
            chunk = chunk->next;
        Rtti *cls = chunk->classes[i % RUNTIME_CLASSES_PER_CHUNK];
        scan(ctx, &cls->rt.name, &cls->rt.name + 1);
#ifdef USES_LOCKWORD
        if (Field_isInflatedLock(cls->rt.lockWord))
            scan(ctx, &cls->rt.lockWord, &cls->rt.lockWord + 1);
#endif
    }
}

extern const int32_t __scalanative_object_array_class_size;

SN_SECTION_LOCAL size_t scalanative_object_array_class_size(void) {
    return (size_t)__scalanative_object_array_class_size;
}

SN_SECTION_LOCAL void scalanative_register_static_roots(void) {
    registerStaticObjectRoots();
    if (!scalanative_GC_add_root_provider(scanRuntimeClasses, NULL))
        abort();
}
