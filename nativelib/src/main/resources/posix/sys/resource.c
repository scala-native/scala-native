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

int scalanative_PRIO_PROCESS() { return PRIO_PROCESS; };

int scalanative_PRIO_PGRP() { return PRIO_PGRP; };

int scalanative_PRIO_USER() { return PRIO_USER; };

rlim_t scalanative_RLIM_INFINITY() { return RLIM_INFINITY; };

rlim_t scalanative_RLIM_SAVED_CUR() { return RLIM_SAVED_CUR; };

rlim_t scalanative_RLIM_SAVED_MAX() { return RLIM_SAVED_MAX; };

int scalanative_RLIMIT_AS() { return RLIMIT_AS; };

int scalanative_RLIMIT_CORE() { return RLIMIT_CORE; };

int scalanative_RLIMIT_CPU() { return RLIMIT_CPU; };

int scalanative_RLIMIT_DATA() { return RLIMIT_DATA; };

int scalanative_RLIMIT_FSIZE() { return RLIMIT_FSIZE; };

int scalanative_RLIMIT_NOFILE() { return RLIMIT_NOFILE; };

int scalanative_RLIMIT_STACK() { return RLIMIT_STACK; };

int scalanative_RUSAGE_CHILDREN() { return RUSAGE_CHILDREN; };

int scalanative_RUSAGE_SELF() { return RUSAGE_SELF; };
