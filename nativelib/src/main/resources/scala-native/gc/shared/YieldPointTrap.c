#if defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX) ||         \
    defined(SCALANATIVE_GC_NONE) || defined(SCALANATIVE_GC_EXPERIMENTAL)

// Disable MSVC deprecation warnings for standard C functions
#ifdef _WIN32
#define _CRT_SECURE_NO_WARNINGS
#endif

#include "YieldPointTrap.h"
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include "shared/MemoryMap.h"
#include "shared/Log.h"
#include "shared/ThreadUtil.h"

#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#else // Unix
#include <sys/mman.h>
#if defined(__FreeBSD__) && !defined(MAP_NORESERVE)
#define MAP_NORESERVE 0
#endif
#endif

#if !defined(__APPLE__)
void YieldPointTrap_resetTaskMachBadAccessPorts(void) {
    /* No-op on non-Apple. The Apple implementation lives in
     * YieldPointTrap_mach.c (kept separate to avoid a typedef collision
     * between <mach/mach_types.h> and shared/ThreadUtil.h). */
}
#endif

safepoint_t YieldPointTrap_init() {
    bool allocated;
    void *addr =
#ifdef _WIN32
        VirtualAlloc(NULL, sizeof(safepoint_t), MEM_RESERVE | MEM_COMMIT,
                     PAGE_READWRITE);
    allocated = addr != NULL;
#else
        mmap(NULL, sizeof(safepoint_t), PROT_WRITE | PROT_READ,
             MAP_NORESERVE | MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    allocated = addr != MAP_FAILED;
#endif
    if (!allocated) {
        GC_LOG_ERROR("Failed to create GC safepoint trap: %s", strerror(errno));
        return NULL;
    }

#if defined(__APPLE__)
    YieldPointTrap_resetTaskMachBadAccessPorts();
#endif
    return addr;
}

void YieldPointTrap_arm(safepoint_t ref) {
    bool success;
#ifdef _WIN32
    DWORD oldAccess;
    success = VirtualProtect((LPVOID)ref, sizeof(safepoint_t), PAGE_NOACCESS,
                             &oldAccess);
#else
    success = mprotect((void *)ref, sizeof(safepoint_t), PROT_NONE) == 0;
#endif
    if (!success) {
        GC_LOG_ERROR("Failed to enable GC collect trap: %s", strerror(errno));
        exit(errno);
    }
}

void YieldPointTrap_disarm(safepoint_t ref) {
    bool success;
#ifdef _WIN32
    DWORD oldAccess;
    success = VirtualProtect((LPVOID)ref, sizeof(safepoint_t), PAGE_READWRITE,
                             &oldAccess);
#else
    success =
        mprotect((void *)ref, sizeof(safepoint_t), PROT_WRITE | PROT_READ) == 0;
#endif
    if (!success) {
        GC_LOG_ERROR("Failed to disable GC collect trap: %s", strerror(errno));
        exit(errno);
    }
}

#endif
