#if defined(__SCALANATIVE_POSIX_SYS_RESOURCE)
#if defined(__unix__) || defined(__unix) || defined(unix) ||                   \
    (defined(__APPLE__) && defined(__MACH__))
// The Open Group Base Specifications Issue 7, 2018 edition
// https://pubs.opengroup.org/onlinepubs/9699919799/basedefs\
//     /sys_resource.h.html

// XSI

#include <sys/resource.h>

struct scalanative_rlimit {
    rlim_t rlim_cur;
    rlim_t rlim_max;
};

struct scalanative_rusage {
    struct timeval ru_utime;
    struct timeval ru_stime;
};

static void convert_from_scalanative_rlimit(struct scalanative_rlimit *in,
                                            struct rlimit *out) {
    out->rlim_cur = in->rlim_cur;
    out->rlim_max = in->rlim_max;
}

static void convert_to_scalanative_rlimit(struct rlimit *in,
                                          struct scalanative_rlimit *out) {
    out->rlim_cur = in->rlim_cur;
    out->rlim_max = in->rlim_max;
}

static void convert_to_scalanative_rusage(struct rusage *in,
                                          struct scalanative_rusage *out) {
    out->ru_utime = in->ru_utime;
    out->ru_stime = in->ru_stime;
}

int scalanative_getrlimit(int resource, struct scalanative_rlimit *snRlim) {
    struct rlimit rlim;

    int status = getrlimit(resource, &rlim);

    if (status >= 0) {
        convert_to_scalanative_rlimit(&rlim, snRlim);
    } // contents at snRlim undefined on error.

    return status;
}

int scalanative_getrusage(int who, struct scalanative_rusage *snUsage) {
    struct rusage usage;

    int status = getrusage(who, &usage);

    if (status >= 0) {
        convert_to_scalanative_rusage(&usage, snUsage);
    } // contents at snUsage undefined on error.

    return status;
}

int scalanative_setrlimit(int resource, struct scalanative_rlimit *snRlim) {

    struct rlimit rlim;

    convert_from_scalanative_rlimit(snRlim, &rlim);

    int status = setrlimit(resource, &rlim);

    return status;
}

int scalanative_prio_process() { return PRIO_PROCESS; };

int scalanative_prio_pgrp() { return PRIO_PGRP; };

int scalanative_prio_user() { return PRIO_USER; };

rlim_t scalanative_rlim_infinity() { return RLIM_INFINITY; };

rlim_t scalanative_rlim_saved_cur() { return RLIM_SAVED_CUR; };

rlim_t scalanative_rlim_saved_max() { return RLIM_SAVED_MAX; };

int scalanative_rlimit_as() {
#ifdef RLIMIT_AS
    return RLIMIT_AS;
#else
    return 0;
#endif
};

int scalanative_rlimit_core() { return RLIMIT_CORE; };

int scalanative_rlimit_cpu() { return RLIMIT_CPU; };

int scalanative_rlimit_data() { return RLIMIT_DATA; };

int scalanative_rlimit_fsize() { return RLIMIT_FSIZE; };

int scalanative_rlimit_nofile() { return RLIMIT_NOFILE; };

int scalanative_rlimit_stack() { return RLIMIT_STACK; };

int scalanative_rusage_children() { return RUSAGE_CHILDREN; };

int scalanative_rusage_self() { return RUSAGE_SELF; };

#endif // Unix or Mac OS
#endif