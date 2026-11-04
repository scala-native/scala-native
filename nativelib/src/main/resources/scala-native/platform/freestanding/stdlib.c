#ifdef SCALANATIVE_FREESTANDING

#include <errno.h>
#include <setjmp.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include "sn_platform.h"

static int errno_value;
int *__errno_location(void) { return &errno_value; }

static char *empty_environment[1] = {NULL};
char **environ = empty_environment;
char *program_invocation_short_name = "main";
char *program_invocation_name = "main";

// There is no environment, so the runtime's settings take their defaults.
char *getenv(const char *name) {
    (void)name;
    return NULL;
}

static int digit_value(char c) {
    if (c >= '0' && c <= '9')
        return c - '0';
    if (c >= 'a' && c <= 'z')
        return c - 'a' + 10;
    if (c >= 'A' && c <= 'Z')
        return c - 'A' + 10;
    return 99;
}

static unsigned long long parse_unsigned(const char *s, char **end, int base,
                                         int *negative) {
    while (*s == ' ' || *s == '\t' || *s == '\n')
        s++;
    *negative = 0;
    if (*s == '-' || *s == '+')
        *negative = *s++ == '-';
    if ((base == 0 || base == 16) && s[0] == '0' &&
        (s[1] == 'x' || s[1] == 'X'))
        base = 16, s += 2;
    else if (base == 0)
        base = s[0] == '0' ? 8 : 10;
    unsigned long long v = 0;
    for (int d; (d = digit_value(*s)) < base; s++)
        v = v * base + d;
    if (end)
        *end = (char *)s;
    return v;
}

unsigned long long strtoull(const char *s, char **end, int base) {
    int neg;
    unsigned long long v = parse_unsigned(s, end, base, &neg);
    return neg ? -v : v;
}

long long strtoll(const char *s, char **end, int base) {
    int neg;
    unsigned long long v = parse_unsigned(s, end, base, &neg);
    return neg ? -(long long)v : (long long)v;
}

unsigned long strtoul(const char *s, char **end, int base) {
    return strtoull(s, end, base);
}

long strtol(const char *s, char **end, int base) {
    return strtoll(s, end, base);
}

// glibc >= 2.38 headers rename these under _GNU_SOURCE or C2x.
unsigned long long __isoc23_strtoull(const char *s, char **end, int base)
    __attribute__((alias("strtoull")));
long long __isoc23_strtoll(const char *s, char **end, int base)
    __attribute__((alias("strtoll")));
unsigned long __isoc23_strtoul(const char *s, char **end, int base)
    __attribute__((alias("strtoul")));
long __isoc23_strtol(const char *s, char **end, int base)
    __attribute__((alias("strtol")));

#define MAX_ATEXIT 32
static void (*atexit_fns[MAX_ATEXIT])(void);
static int atexit_count;

int atexit(void (*fn)(void)) {
    if (atexit_count == MAX_ATEXIT)
        return -1;
    atexit_fns[atexit_count++] = fn;
    return 0;
}

int __cxa_atexit(void (*fn)(void *), void *arg, void *dso) {
    (void)arg, (void)dso;
    return atexit((void (*)(void))fn);
}

_Noreturn void exit(int status) {
    while (atexit_count > 0)
        atexit_fns[--atexit_count]();
    scalanative_platform_exit(status);
}

_Noreturn void _exit(int status) { scalanative_platform_exit(status); }
_Noreturn void _Exit(int status) { scalanative_platform_exit(status); }

_Noreturn void abort(void) {
    static const char msg[] = "abort()\n";
    scalanative_platform_console_write(2, msg, sizeof msg - 1);
    scalanative_platform_exit(134);
}

// Only delimited continuations longjmp, and they are not supported here.
_Noreturn void longjmp(jmp_buf env, int val) {
    (void)env, (void)val;
    static const char msg[] = "longjmp: continuations are not supported\n";
    scalanative_platform_console_write(2, msg, sizeof msg - 1);
    scalanative_platform_exit(134);
}

extern void (*__init_array_start[])(int, char **, char **);
extern void (*__init_array_end[])(int, char **, char **);
extern int main(int argc, char **argv);

_Noreturn void scalanative_freestanding_start(int argc, char **argv) {
    for (void (**f)(int, char **, char **) = __init_array_start;
         f < __init_array_end; f++)
        (*f)(argc, argv, environ);
    exit(main(argc, argv));
}

#endif
