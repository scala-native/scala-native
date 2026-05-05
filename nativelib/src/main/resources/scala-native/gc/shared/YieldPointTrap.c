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

#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#else // Unix
#include <sys/mman.h>
#if defined(__FreeBSD__) && !defined(MAP_NORESERVE)
#define MAP_NORESERVE 0
#endif
#endif

#if defined(__APPLE__)
#include <mach/mach.h>
#endif

void YieldPointTrap_resetTaskMachBadAccessPorts(void) {
#if defined(__APPLE__)
    /* LLDB and some frameworks install task-wide Mach exception handlers for
     * EXC_BAD_ACCESS. XNU delivers Mach exceptions to those handlers before
     * translating the fault to SIGBUS, so a mutator can fault on a PROT_NONE
     * yield trap and never run our POSIX safepoint handler. Clearing the task
     * exception port for this mask restores the signal path. Call from init
     * and again before arming traps (e.g. each GC) because dylibs may
     * reinstall handlers after startup. */
    kern_return_t kr = task_set_exception_ports(
        mach_task_self(), EXC_MASK_BAD_ACCESS, MACH_PORT_NULL,
        EXCEPTION_STATE_IDENTITY, MACHINE_THREAD_STATE);
    if (kr != KERN_SUCCESS)
        GC_LOG_WARN("Failed to reset GC safepoint Mach EXC_BAD_ACCESS ports "
                    "(kern_return_t=%d)",
                    (int)kr);
#endif
}

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
        exit(errno);
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

void YieldPointTrap_free(safepoint_t ref) {
    if (ref == NULL) {
        return;
    }
#ifdef _WIN32
    if (!VirtualFree((LPVOID)ref, 0, MEM_RELEASE)) {
        GC_LOG_WARN("Failed to release GC safepoint trap memory: %lu",
                    GetLastError());
    }
#else
    if (munmap((void *)ref, sizeof(safepoint_t)) != 0) {
        GC_LOG_WARN("Failed to unmap GC safepoint trap: %s", strerror(errno));
    }
#endif
}

#endif
