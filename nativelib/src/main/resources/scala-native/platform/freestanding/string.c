#ifdef SCALANATIVE_FREESTANDING

#define _GNU_SOURCE
#include <errno.h>
#include <stddef.h>
#include <string.h>
#include <strings.h>

// String instructions, so the compiler cannot lower these back into calls to
// themselves.
void *memcpy(void *dst, const void *src, size_t n) {
    void *ret = dst;
    __asm__ volatile("rep movsb" : "+D"(dst), "+S"(src), "+c"(n) : : "memory");
    return ret;
}

void *memmove(void *dst, const void *src, size_t n) {
    if (dst <= src || (const char *)src + n <= (char *)dst)
        return memcpy(dst, src, n);
    char *d = (char *)dst + n - 1;
    const char *s = (const char *)src + n - 1;
    __asm__ volatile("std\n\trep movsb\n\tcld"
                     : "+D"(d), "+S"(s), "+c"(n)
                     :
                     : "memory");
    return dst;
}

void *memset(void *dst, int c, size_t n) {
    void *ret = dst;
    __asm__ volatile("rep stosb" : "+D"(dst), "+c"(n) : "a"(c) : "memory");
    return ret;
}

int memcmp(const void *a, const void *b, size_t n) {
    const unsigned char *x = a, *y = b;
    for (size_t i = 0; i < n; i++)
        if (x[i] != y[i])
            return x[i] - y[i];
    return 0;
}

int bcmp(const void *a, const void *b, size_t n) { return memcmp(a, b, n); }

size_t strlen(const char *s) {
    const char *p = s;
    while (*p)
        p++;
    return p - s;
}

int strcmp(const char *a, const char *b) {
    while (*a && *a == *b)
        a++, b++;
    return (unsigned char)*a - (unsigned char)*b;
}

int strncmp(const char *a, const char *b, size_t n) {
    for (; n && *a && *a == *b; n--)
        a++, b++;
    return n ? (unsigned char)*a - (unsigned char)*b : 0;
}

static int lower(int c) { return c >= 'A' && c <= 'Z' ? c + 32 : c; }

int strcasecmp(const char *a, const char *b) {
    while (*a && lower(*a) == lower(*b))
        a++, b++;
    return lower((unsigned char)*a) - lower((unsigned char)*b);
}

char *strcpy(char *dst, const char *src) {
    char *d = dst;
    while ((*d++ = *src++))
        ;
    return dst;
}

char *strncpy(char *dst, const char *src, size_t n) {
    size_t i = 0;
    for (; i < n && src[i]; i++)
        dst[i] = src[i];
    for (; i < n; i++)
        dst[i] = 0;
    return dst;
}

// Parenthesized: glibc >= 2.43 defines strchr as a _Generic macro.
char *(strchr)(const char *s, int c) {
    for (;; s++) {
        if (*s == (char)c)
            return (char *)s;
        if (!*s)
            return NULL;
    }
}

char *strerror(int e) { return e ? "error" : "success"; }

#endif
