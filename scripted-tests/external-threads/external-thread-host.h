#ifndef EXTERNAL_HOST_H
#define EXTERNAL_HOST_H
#include <assert.h>
#include <errno.h>
#include <inttypes.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "host-declarations.h"

#define CHECK(test)                                                            \
    do {                                                                       \
        if (!(test)) {                                                         \
            fprintf(stderr, "CHECK failed: %s (%s:%d)\n", #test, __FILE__,     \
                    __LINE__);                                                 \
            exit(90);                                                          \
        }                                                                      \
    } while (0)
static void milestone(const char *name) {
    printf("MILESTONE %s\n", name);
    fflush(stdout);
}
#include "external-thread-platform.h"
static int iterations = 4;
static int workers = 4;
static int pressure_bytes = 65536;
static int multithreaded = 0;

static int64_t callback_identity(void) {
    int64_t id = external_identity();
    return id;
}
static void callback_collect(void) { external_collect(); }
static void callback_pressure(void) {
    CHECK(external_pressure(iterations, pressure_bytes) == 18);
}
typedef void (*scenario_fn)(void);
typedef struct scenario {
    const char *id;
    scenario_fn run;
} scenario;
static int parse_number(const char *arg) {
    char *end = NULL;
    errno = 0;
    long value = strtol(arg, &end, 10);
    CHECK(!errno && *arg && !*end && value > 0 && value <= 10000000);
    return (int)value;
}
static int run_host(int argc, char **argv, const scenario *table,
                    size_t count) {
    host_initialize();
    CHECK(argc >= 2);
    const scenario *selected = NULL;
    for (size_t i = 0; i < count; ++i)
        if (!strcmp(argv[1], table[i].id))
            selected = &table[i];
    if (!selected) {
        fprintf(stderr, "unknown scenario: %s\n", argv[1]);
        return 64;
    }
    int skip_init = 0;
    for (int i = 2; i < argc; ++i) {
        if (!strcmp(argv[i], "--skip-init"))
            skip_init = 1;
        else if (!strcmp(argv[i], "--multithreaded"))
            multithreaded = 1;
        else if (!strcmp(argv[i], "--iterations") && i + 1 < argc)
            iterations = parse_number(argv[++i]);
        else if (!strcmp(argv[i], "--workers") && i + 1 < argc)
            workers = parse_number(argv[++i]);
        else if (!strcmp(argv[i], "--pressure") && i + 1 < argc)
            pressure_bytes = parse_number(argv[++i]);
        else {
            fprintf(stderr, "unsupported option: %s\n", argv[i]);
            return 64;
        }
    }
    if (!skip_init) {
#ifdef EXTERNAL_SHARED_LIBRARY
        /* Shared-library constructors initialize before main unless the host
         * explicitly suppresses them. Never initialize the runtime twice. */
        if (getenv("SCALANATIVE_NO_DYLIB_CTOR"))
            CHECK(ScalaNativeInit() == 0);
#else
        CHECK(ScalaNativeInit() == 0);
#endif
        milestone("initialized");
    }
    selected->run();
    printf("COMPLETE %s\n", selected->id);
    return 0;
}
#endif
