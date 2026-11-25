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

/* Append-only lists of pointers. Writers serialize with a lock; collectors
 * read the published count without locking, so they never wait for a mutator
 * stopped while holding it.
 */
#define POINTERS_PER_CHUNK 64

typedef struct PointerChunk {
    struct PointerChunk *next;
    void *items[POINTERS_PER_CHUNK];
} PointerChunk;

typedef struct {
    PointerChunk first;
    PointerChunk *last;
    atomic_size_t published;
} PointerList;

#if defined(SCALANATIVE_MULTITHREADING_ENABLED) && defined(_WIN32)
static SRWLOCK listsLock = SRWLOCK_INIT;
static void lockLists(void) { AcquireSRWLockExclusive(&listsLock); }
static void unlockLists(void) { ReleaseSRWLockExclusive(&listsLock); }
#elif defined(SCALANATIVE_MULTITHREADING_ENABLED)
static pthread_mutex_t listsLock = PTHREAD_MUTEX_INITIALIZER;
static void lockLists(void) {
    if (pthread_mutex_lock(&listsLock) != 0)
        abort();
}
static void unlockLists(void) { pthread_mutex_unlock(&listsLock); }
#else
static void lockLists(void) {}
static void unlockLists(void) {}
#endif

// Caller holds the lock
static size_t PointerList_size(PointerList *list) {
    return atomic_load_explicit(&list->published, memory_order_relaxed);
}

// Caller holds the lock
static void PointerList_append(PointerList *list, void *item) {
    size_t count = PointerList_size(list);
    size_t index = count % POINTERS_PER_CHUNK;
    if (count > 0 && index == 0) {
        PointerChunk *chunk = calloc(1, sizeof(PointerChunk));
        if (chunk == NULL)
            abort();
        list->last->next = chunk;
        list->last = chunk;
    }
    list->last->items[index] = item;
    atomic_store_explicit(&list->published, count + 1, memory_order_release);
}

static size_t PointerList_published(PointerList *list) {
    return atomic_load_explicit(&list->published, memory_order_acquire);
}

// The i-th item, `chunk` holds the chunk of item i - 1, initially `first`
static void *PointerList_at(PointerChunk **chunk, size_t i) {
    if (i > 0 && i % POINTERS_PER_CHUNK == 0)
        *chunk = (*chunk)->next;
    return (*chunk)->items[i % POINTERS_PER_CHUNK];
}

#ifdef SCALANATIVE_MULTITHREADING_ENABLED

/* Lock words of static objects (classes, string literals and their backing
 * arrays) whose monitor was inflated: the only references these objects hold
 * to the heap. Monitors are never deflated.
 */
static PointerList lockWords = {.last = &lockWords.first};

static bool isRegistered(void **lockWord) {
    PointerChunk *chunk = &lockWords.first;
    size_t count = PointerList_size(&lockWords);
    for (size_t i = 0; i < count; i++) {
        if (PointerList_at(&chunk, i) == lockWord)
            return true;
    }
    return false;
}

/* Registration completes before the caller publishes the monitor in the lock
 * word, so a collection either sees the lock word or finds it not inflated.
 */
static void registerLockWord(void **lockWord) {
    lockLists();
    // Threads racing to inflate the same monitor register it once
    if (!isRegistered(lockWord))
        PointerList_append(&lockWords, lockWord);
    unlockLists();
}

/* Root provider, see GCHooks.h: scans the inflated lock words, the only ones
 * pointing to a monitor, thin locks hold none. */
static void scanLockWords(void *data, GC_ScanRangeFn *scan, void *ctx) {
    (void)data;
    size_t count = PointerList_published(&lockWords);
    PointerChunk *chunk = &lockWords.first;
    for (size_t i = 0; i < count; i++) {
        void **lockWord = PointerList_at(&chunk, i);
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

#endif

/* Array classes created at runtime, see runtime.ArrayClasses. Never freed;
 * their name and lock word are roots.
 */
static PointerList runtimeClasses = {.last = &runtimeClasses.first};

SN_SECTION_LOCAL void *scalanative_alloc_runtime_class(size_t size) {
    Rtti *cls = calloc(1, size);
    if (cls == NULL)
        return NULL;
    lockLists();
    PointerList_append(&runtimeClasses, cls);
    unlockLists();
    return cls;
}

static void scanRuntimeClasses(void *data, GC_ScanRangeFn *scan, void *ctx) {
    (void)data;
    size_t count = PointerList_published(&runtimeClasses);
    PointerChunk *chunk = &runtimeClasses.first;
    for (size_t i = 0; i < count; i++) {
        Rtti *cls = PointerList_at(&chunk, i);
        scan(ctx, &cls->rt.name, &cls->rt.name + 1);
#ifdef USES_LOCKWORD
        if (Field_isInflatedLock(cls->rt.lockWord))
            scan(ctx, &cls->rt.lockWord, &cls->rt.lockWord + 1);
#endif
    }
}

extern const int32_t __scalanative_object_array_class_size;
extern void *const __scalanative_reference_array_classes[];
extern const int32_t __scalanative_reference_array_class_count;

SN_SECTION_LOCAL size_t scalanative_object_array_class_size(void) {
    return (size_t)__scalanative_object_array_class_size;
}

SN_SECTION_LOCAL void *const *scalanative_reference_array_classes(void) {
    return __scalanative_reference_array_classes;
}

SN_SECTION_LOCAL size_t scalanative_reference_array_class_count(void) {
    return (size_t)__scalanative_reference_array_class_count;
}

SN_SECTION_LOCAL void scalanative_register_static_roots(void) {
    registerStaticObjectRoots();
    if (!scalanative_GC_add_root_provider(scanRuntimeClasses, NULL))
        abort();
}
