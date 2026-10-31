/* Run in a fresh process: the production once gates must initially be unused.
 */
#include "staticSections.h"
#include <assert.h>
#include <stdatomic.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
#include <pthread.h>
#include <sched.h>
#endif

void scalanative_register_static_roots(void);
void scalanative_register_object_roots(void *);

#ifdef POPULATED_SECTIONS
#ifdef __APPLE__
#define CLASS_SECTION "__DATA,__sn_classinfo"
#define STRING_SECTION "__DATA,__sn_strings"
#define GLOBAL_SECTION "__DATA,__sn_globals"
#else
#define CLASS_SECTION "sn_classinfo"
#define STRING_SECTION "sn_strings"
#define GLOBAL_SECTION "sn_global_objects"
#endif
#define STATIC_OBJECT(name)                                                    \
    __attribute__((section(name), used, aligned(sizeof(uintptr_t))))
static uintptr_t class_object STATIC_OBJECT(CLASS_SECTION);
static uintptr_t string_object STATIC_OBJECT(STRING_SECTION);
static uintptr_t global_object STATIC_OBJECT(GLOBAL_SECTION);
#endif

static const SNStaticSection *sections[3];
static void *objects[3];
static atomic_int calls[3], completed[3];
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
static atomic_int started, returned;
static atomic_bool release_registration;
#endif
static int first;

void scalanative_GC_add_roots(void *low, void *high) {
    int index = -1;
    for (int i = 0; i < 3; i++)
        if (low == sections[i]->start && high == sections[i]->end)
            index = i;
    assert(index >= 0);
    assert((uintptr_t)low % sizeof(uintptr_t) == 0);
    assert((uintptr_t)high % sizeof(uintptr_t) == 0);
    assert((uintptr_t)low < (uintptr_t)high);
    assert(atomic_fetch_add(&calls[index], 1) == 0);
#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    if (index == first)
        while (!atomic_load(&release_registration))
            sched_yield();
#endif
    atomic_store(&completed[index], 1);
}

#ifdef SCALANATIVE_MULTITHREADING_ENABLED
static void *register_first(void *unused) {
    (void)unused;
    atomic_fetch_add(&started, 1);
    scalanative_register_object_roots(objects[first]);
    assert(atomic_load(&completed[first]) == 1);
    atomic_fetch_add(&returned, 1);
    return NULL;
}
#endif

int main(int argc, char **argv) {
    assert(argc == 2);
    first = strcmp(argv[1], "strings") == 0 ? 1 : 0;
    sections[0] = scalanative_static_classinfo_section();
    sections[1] = scalanative_static_strings_section();
    sections[2] = scalanative_static_globals_section();
    for (int i = 0; i < 3; i++)
        objects[i] = sections[i]->start;
#ifdef POPULATED_SECTIONS
    objects[0] = &class_object;
    objects[1] = &string_object;
    objects[2] = &global_object;
    for (int i = 0; i < 3; i++) {
        assert((uintptr_t)objects[i] >= (uintptr_t)sections[i]->start);
        assert((uintptr_t)objects[i] < (uintptr_t)sections[i]->end);
    }
#endif
    scalanative_register_static_roots();
    assert(calls[2] == 1 && calls[0] == 0 && calls[1] == 0);

    void *heap_object = malloc(32);
    assert(heap_object != NULL);
    uintptr_t stack_object = 0;
    scalanative_register_object_roots(heap_object);
    scalanative_register_object_roots(&stack_object);
    scalanative_register_object_roots(NULL);
    free(heap_object);
    assert(calls[0] == 0 && calls[1] == 0);

#ifdef SCALANATIVE_MULTITHREADING_ENABLED
    pthread_t workers[16];
    for (int i = 0; i < 16; i++)
        assert(pthread_create(&workers[i], NULL, register_first, NULL) == 0);
    while (atomic_load(&started) != 16 || atomic_load(&calls[first]) != 1)
        sched_yield();
    assert(calls[1 - first] == 0);
    /* The other section must complete while the first registration is held. */
    scalanative_register_object_roots(objects[1 - first]);
    assert(completed[1 - first] == 1);
    assert(completed[first] == 0 && returned == 0);
    atomic_store(&release_registration, true);
    for (int i = 0; i < 16; i++)
        assert(pthread_join(workers[i], NULL) == 0);
    assert(returned == 16);
#else
    scalanative_register_object_roots(objects[first]);
    assert(completed[first] == 1 && calls[1 - first] == 0);
    scalanative_register_object_roots(objects[1 - first]);
#endif
    scalanative_register_object_roots(objects[0]);
    scalanative_register_object_roots(objects[1]);
    for (int i = 0; i < 3; i++)
        assert(calls[i] == 1 && completed[i] == 1);
    puts("PASS: lazy independent sections, outside exclusion, once and "
         "completion");
}
