#include "staticRoots.h"
#include "gc/shared/ScalaNativeGC.h"
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

/* Class and string objects are not scanned: only lock words of their
 * inflated monitors are, see scalanative_register_object_roots.
 */
SN_SECTION_LOCAL void scalanative_register_static_roots(void) {
    registerSection(scalanative_static_globals_section());
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

static bool isRegistered(void **lockWord, size_t count) {
    LockWordChunk *chunk = &firstChunk;
    for (size_t i = 0; i < count; i++) {
        if (i > 0 && i % LOCK_WORDS_PER_CHUNK == 0)
            chunk = chunk->next;
        if (chunk->lockWords[i % LOCK_WORDS_PER_CHUNK] == lockWord)
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

SN_SECTION_LOCAL void
scalanative_foreach_static_lock_word(SNLockWordVisitor visit, void *context) {
    size_t count =
        atomic_load_explicit(&publishedLockWords, memory_order_acquire);
    LockWordChunk *chunk = &firstChunk;
    for (size_t i = 0; i < count; i++) {
        if (i > 0 && i % LOCK_WORDS_PER_CHUNK == 0)
            chunk = chunk->next;
        void **lockWord = chunk->lockWords[i % LOCK_WORDS_PER_CHUNK];
        // Only inflated lock words point to a monitor, thin locks hold none
        if (((word_t)*lockWord & MONITOR_INFLATION_MARK_MASK) != 0)
            visit(lockWord, context);
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
        // Object header: the RTTI, then the lock word
        registerLockWord(&((void **)object)[1]);
    }
}

#else

// Without multithreading objects have no lock word to register

SN_SECTION_LOCAL void
scalanative_foreach_static_lock_word(SNLockWordVisitor visit, void *context) {
    (void)visit;
    (void)context;
}

SN_SECTION_LOCAL void scalanative_register_object_roots(void *object) {
    (void)object;
}

#endif
