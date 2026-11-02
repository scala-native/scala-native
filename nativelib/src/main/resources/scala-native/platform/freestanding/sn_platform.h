#ifndef SN_PLATFORM_H
#define SN_PLATFORM_H

// Hooks a freestanding program (NativeConfig.withFreestanding) must define.
// The runtime calls them before the GC is initialized and while it collects,
// so they must not allocate on the GC heap.

#include <stddef.h>
#include <stdint.h>

// stream is 1 for standard output, 2 for standard error.
void scalanative_platform_console_write(int stream, const char *buf,
                                        size_t len);
int64_t scalanative_platform_monotonic_nanos(void);
int64_t scalanative_platform_realtime_nanos(void);
// The RAM the runtime may use for its heap and malloc arena, in one region.
void scalanative_platform_memory(void **base, size_t *size);
// The main thread's stack: lowest address and size.
void scalanative_platform_stack(void **base, size_t *size);
_Noreturn void scalanative_platform_exit(int status);

// Entry for the program's own startup code: runs constructors, then main,
// then exit.
_Noreturn void scalanative_freestanding_start(int argc, char **argv);

#endif
