#ifdef SCALANATIVE_FREESTANDING

#define _GNU_SOURCE
#include <dlfcn.h>
#include <elf.h>
#include <errno.h>
#include <link.h>
#include <pthread.h>
#include <pwd.h>
#include <sched.h>
#include <signal.h>
#include <sys/syscall.h>
#include <stddef.h>
#include <stdint.h>
#include <string.h>
#include <sys/resource.h>
#include <sys/utsname.h>
#include <unistd.h>
#include "sn_platform.h"

size_t scalanative_freestanding_reported_memory(void);

ssize_t write(int fd, const void *buf, size_t n) {
    if (fd != 1 && fd != 2) {
        errno = EBADF;
        return -1;
    }
    scalanative_platform_console_write(fd, buf, n);
    return (ssize_t)n;
}

ssize_t read(int fd, void *buf, size_t n) {
    (void)fd, (void)buf, (void)n;
    errno = EBADF;
    return -1;
}

int close(int fd) {
    (void)fd;
    return 0;
}

int isatty(int fd) { return fd <= 2; }

long sysconf(int name) {
    switch (name) {
    case _SC_PAGESIZE:
        return 4096;
    case _SC_PHYS_PAGES:
    case _SC_AVPHYS_PAGES:
        return (long)(scalanative_freestanding_reported_memory() / 4096);
    case _SC_NPROCESSORS_ONLN:
    case _SC_NPROCESSORS_CONF:
        return 1;
    // With glibc >= 2.34 headers SIGSTKSZ expands to this call.
    case _SC_SIGSTKSZ:
        return 16384;
    case _SC_MINSIGSTKSZ:
        return 4096;
    default:
        errno = EINVAL;
        return -1;
    }
}

int getpagesize(void) { return 4096; }

int getrlimit(__rlimit_resource_t res, struct rlimit *rl) {
    void *base;
    size_t size;
    scalanative_platform_stack(&base, &size);
    rl->rlim_cur = rl->rlim_max = res == RLIMIT_STACK ? size : RLIM_INFINITY;
    return 0;
}

// One thread whose stack is the platform's; every lock is uncontended.
pthread_t pthread_self(void) { return 1; }
int pthread_getattr_np(pthread_t t, pthread_attr_t *a) {
    (void)t;
    memset(a, 0, sizeof *a);
    return 0;
}
int pthread_attr_init(pthread_attr_t *a) {
    memset(a, 0, sizeof *a);
    return 0;
}
int pthread_attr_destroy(pthread_attr_t *a) { return (void)a, 0; }
int pthread_attr_getstack(const pthread_attr_t *a, void **base, size_t *sz) {
    (void)a;
    scalanative_platform_stack(base, sz);
    return 0;
}
int pthread_attr_getguardsize(const pthread_attr_t *a, size_t *g) {
    (void)a;
    *g = 0;
    return 0;
}
int pthread_mutexattr_init(pthread_mutexattr_t *a) { return (void)a, 0; }
int pthread_mutexattr_settype(pthread_mutexattr_t *a, int t) {
    return (void)a, (void)t, 0;
}
int pthread_mutexattr_destroy(pthread_mutexattr_t *a) { return (void)a, 0; }
int pthread_mutex_init(pthread_mutex_t *m, const pthread_mutexattr_t *a) {
    return (void)m, (void)a, 0;
}
int pthread_mutex_destroy(pthread_mutex_t *m) { return (void)m, 0; }
int pthread_mutex_lock(pthread_mutex_t *m) { return (void)m, 0; }
int pthread_mutex_trylock(pthread_mutex_t *m) { return (void)m, 0; }
int pthread_mutex_unlock(pthread_mutex_t *m) { return (void)m, 0; }
int pthread_rwlock_init(pthread_rwlock_t *l, const pthread_rwlockattr_t *a) {
    return (void)l, (void)a, 0;
}
int pthread_rwlock_rdlock(pthread_rwlock_t *l) { return (void)l, 0; }
int pthread_rwlock_wrlock(pthread_rwlock_t *l) { return (void)l, 0; }
int pthread_rwlock_unlock(pthread_rwlock_t *l) { return (void)l, 0; }
int pthread_once(pthread_once_t *once, void (*fn)(void)) {
    if (*once == 0) {
        *once = 1;
        fn();
    }
    return 0;
}

// Thread-specific data for the one thread.
#define MAX_KEYS 16
static void *key_values[MAX_KEYS];
static int key_count;
int pthread_key_create(pthread_key_t *key, void (*dtor)(void *)) {
    (void)dtor;
    if (key_count == MAX_KEYS)
        return EAGAIN;
    *key = (pthread_key_t)key_count++;
    return 0;
}
void *pthread_getspecific(pthread_key_t key) {
    return key < MAX_KEYS ? key_values[key] : NULL;
}
int pthread_setspecific(pthread_key_t key, const void *v) {
    if (key >= MAX_KEYS)
        return EINVAL;
    key_values[key] = (void *)v;
    return 0;
}

// With one thread no condition is ever waited on by anyone else.
int pthread_cond_init(pthread_cond_t *c, const pthread_condattr_t *a) {
    return (void)c, (void)a, 0;
}
int pthread_cond_destroy(pthread_cond_t *c) { return (void)c, 0; }
int pthread_cond_signal(pthread_cond_t *c) { return (void)c, 0; }
int pthread_cond_broadcast(pthread_cond_t *c) { return (void)c, 0; }
int pthread_cond_wait(pthread_cond_t *c, pthread_mutex_t *m) {
    return (void)c, (void)m, 0;
}

int sched_yield(void) { return 0; }

// The runtime only asks for the thread id.
long syscall(long number, ...) {
    if (number == SYS_gettid)
        return 1;
    errno = ENOSYS;
    return -1;
}

// C code built with unwind tables (pthread_cleanup_push) refers to the GCC
// personality routine. No exception ever needs a cleanup in a C frame here,
// so every frame is passed through: _URC_CONTINUE_UNWIND.
int __gcc_personality_v0(int version, int actions, uint64_t exceptionClass,
                         void *exception, void *context) {
    (void)version, (void)actions, (void)exceptionClass, (void)exception,
        (void)context;
    return 8;
}

// No signals are ever delivered.
int sigaction(int sig, const struct sigaction *act, struct sigaction *old) {
    (void)sig, (void)act;
    if (old)
        memset(old, 0, sizeof *old);
    return 0;
}
int sigaltstack(const stack_t *ss, stack_t *old) {
    (void)ss;
    if (old)
        memset(old, 0, sizeof *old);
    return 0;
}
int sigemptyset(sigset_t *s) { return memset(s, 0, sizeof *s), 0; }
int sigfillset(sigset_t *s) { return memset(s, 0xff, sizeof *s), 0; }
int sigaddset(sigset_t *s, int sig) { return (void)s, (void)sig, 0; }
int sigprocmask(int how, const sigset_t *set, sigset_t *old) {
    (void)how, (void)set;
    if (old)
        memset(old, 0, sizeof *old);
    return 0;
}

// There is one image, the program itself: libunwind finds its unwind tables
// through its program headers, located via the linker's __ehdr_start.
extern const ElfW(Ehdr) __ehdr_start;

int dl_iterate_phdr(int (*cb)(struct dl_phdr_info *, size_t, void *),
                    void *data) {
    const ElfW(Phdr) *ph = (const ElfW(Phdr) *)((const char *)&__ehdr_start +
                                                __ehdr_start.e_phoff);
    struct dl_phdr_info info;
    memset(&info, 0, sizeof info);
    info.dlpi_name = "";
    info.dlpi_phdr = ph;
    info.dlpi_phnum = __ehdr_start.e_phnum;
    for (int i = 0; i < info.dlpi_phnum; i++)
        if (ph[i].p_type == PT_LOAD && ph[i].p_offset == 0)
            info.dlpi_addr = (uintptr_t)&__ehdr_start - ph[i].p_vaddr;
    return cb(&info, sizeof info, data);
}

void *dlopen(const char *f, int flags) { return (void)f, (void)flags, NULL; }
void *dlsym(void *h, const char *s) { return (void)h, (void)s, NULL; }
int dlclose(void *h) { return (void)h, 0; }
char *dlerror(void) { return "no dynamic loading"; }
int dladdr(const void *addr, Dl_info *info) {
    return (void)addr, (void)info, 0;
}

int uname(struct utsname *u) {
    memset(u, 0, sizeof *u);
    strcpy(u->sysname, "ScalaNative");
    strcpy(u->nodename, "freestanding");
    strcpy(u->release, "0");
    strcpy(u->version, "0");
#if defined(__x86_64__)
    strcpy(u->machine, "x86_64");
#elif defined(__aarch64__)
    strcpy(u->machine, "aarch64");
#endif
    return 0;
}

pid_t getpid(void) { return 1; }
uid_t getuid(void) { return 0; }
uid_t geteuid(void) { return 0; }

struct passwd *getpwuid(uid_t uid) {
    (void)uid;
    errno = ENOENT;
    return NULL;
}

struct passwd *getpwnam(const char *name) {
    (void)name;
    errno = ENOENT;
    return NULL;
}

char *getcwd(char *buf, size_t size) {
    if (size < 2) {
        errno = ERANGE;
        return NULL;
    }
    return strcpy(buf, "/");
}

#endif
