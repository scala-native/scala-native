#ifndef GC_MEMORY_H
#define GC_MEMORY_H

#include <stddef.h>
#include <stdint.h>

/* Implementations live in shared/MemoryInfo.c. This header only declares the
 * public API and is safe to include from multiple translation units. */

/** Returns the size of physical memory (RAM) in bytes. */
size_t getMemorySize(void);

/** Returns the size of available free memory (RAM) in bytes. */
size_t getFreeMemorySize(void);

/** OS page size in bytes; result is cached after the first call.
 *
 * Uses `sysconf(_SC_PAGESIZE)` on POSIX and `GetSystemInfo` on Windows — not
 * `getpagesize()`, which is not reliably declared on modern macOS SDKs.
 */
uintptr_t getPageSize(void);

#endif // GC_MEMORY_H
