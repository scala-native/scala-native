#ifdef SCALANATIVE_FREESTANDING

#include <errno.h>
#include <stddef.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>
#include "sn_platform.h"

#define PAGE 4096
#define MIN_ARENA (4u << 20)
#define MAX_ARENA (64u << 20)

// The platform region, split once into the mmap area (GC heap and its
// metadata) followed by the malloc arena.
static struct {
    int ready;
    char *mmap_base;
    size_t mmap_size, mmap_used;
    char *arena_base;
    size_t arena_size, arena_used;
} region;

static void region_init(void) {
    if (region.ready)
        return;
    void *base;
    size_t size;
    scalanative_platform_memory(&base, &size);
    uintptr_t start = ((uintptr_t)base + PAGE - 1) & ~(uintptr_t)(PAGE - 1);
    uintptr_t end = ((uintptr_t)base + size) & ~(uintptr_t)(PAGE - 1);
    size_t usable = end > start ? end - start : 0;
    size_t arena = usable / 8;
    if (arena < MIN_ARENA)
        arena = MIN_ARENA;
    if (arena > MAX_ARENA)
        arena = MAX_ARENA;
    if (arena > usable)
        arena = usable;
    region.mmap_base = (char *)start;
    region.mmap_size = usable - arena;
    region.arena_base = (char *)start + region.mmap_size;
    region.arena_size = arena;
    region.ready = 1;
}

// What sysconf reports as physical memory: the GC reserves this much for the
// heap and needs room for its metadata next to it.
size_t scalanative_freestanding_reported_memory(void) {
    region_init();
    return region.mmap_size / 3;
}

// Hands out zeroed pages from the mmap area. Protection and flags are ignored
// and munmap returns nothing.
void *mmap(void *addr, size_t len, int prot, int flags, int fd, off_t off) {
    (void)addr, (void)prot, (void)flags, (void)fd, (void)off;
    region_init();
    size_t rounded = (len + PAGE - 1) & ~(size_t)(PAGE - 1);
    if (rounded > region.mmap_size - region.mmap_used) {
        errno = ENOMEM;
        return MAP_FAILED;
    }
    void *p = region.mmap_base + region.mmap_used;
    region.mmap_used += rounded;
    memset(p, 0, rounded);
    return p;
}

int munmap(void *addr, size_t len) {
    (void)addr, (void)len;
    return 0;
}

int mprotect(void *addr, size_t len, int prot) {
    (void)addr, (void)len, (void)prot;
    return 0;
}

int madvise(void *addr, size_t len, int advice) {
    (void)addr, (void)len, (void)advice;
    return 0;
}

// malloc: power-of-two size classes with a 16-byte header holding the class.
// Freed blocks go on a per-class list and are reused before the arena grows.
#define MIN_CLASS 4
#define MAX_CLASS 40
#define HEADER 16

static void *free_lists[MAX_CLASS + 1];

static int size_class(size_t n) {
    int c = MIN_CLASS;
    while (((size_t)1 << c) < n + HEADER)
        c++;
    return c;
}

void *malloc(size_t n) {
    region_init();
    if (n > ((size_t)1 << MAX_CLASS) - HEADER) {
        errno = ENOMEM;
        return NULL;
    }
    int c = size_class(n);
    char *block = free_lists[c];
    if (block) {
        free_lists[c] = *(void **)block;
    } else {
        size_t sz = (size_t)1 << c;
        if (sz > region.arena_size - region.arena_used) {
            errno = ENOMEM;
            return NULL;
        }
        block = region.arena_base + region.arena_used;
        region.arena_used += sz;
    }
    *(int *)block = c;
    return block + HEADER;
}

void free(void *p) {
    if (!p)
        return;
    char *block = (char *)p - HEADER;
    int c = *(int *)block;
    *(void **)block = free_lists[c];
    free_lists[c] = block;
}

void *calloc(size_t n, size_t sz) {
    if (sz && n > SIZE_MAX / sz) {
        errno = ENOMEM;
        return NULL;
    }
    void *p = malloc(n * sz);
    if (p)
        memset(p, 0, n * sz);
    return p;
}

void *realloc(void *p, size_t n) {
    if (!p)
        return malloc(n);
    int c = *(int *)((char *)p - HEADER);
    size_t cap = ((size_t)1 << c) - HEADER;
    if (n <= cap)
        return p;
    void *q = malloc(n);
    if (q) {
        memcpy(q, p, cap);
        free(p);
    }
    return q;
}

// Blocks are 16-byte aligned; larger alignments over-allocate and are never
// freed back into a size class.
int posix_memalign(void **out, size_t align, size_t n) {
    if (align <= 16) {
        *out = malloc(n);
        return *out ? 0 : ENOMEM;
    }
    char *raw = malloc(n + align);
    if (!raw)
        return ENOMEM;
    *out = (void *)(((uintptr_t)raw + align - 1) & ~(uintptr_t)(align - 1));
    return 0;
}

void *aligned_alloc(size_t align, size_t n) {
    void *p;
    return posix_memalign(&p, align, n) == 0 ? p : NULL;
}

#endif
