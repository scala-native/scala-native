#include "Safepoint.h"
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include "MemoryMap.h"

#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#else // Unix
#include <sys/mman.h>
#endif

#if defined(__APPLE__)
#include <mach/mach.h>
#endif

void Safepoint_init(safepoint_t *ref) {
    void *addr =
#ifdef _WIN32
        VirtualAlloc(NULL, sizeof(safepoint_t), MEM_RESERVE | MEM_COMMIT,
                     PAGE_READONLY);
#else
        mmap(NULL, sizeof(safepoint_t), PROT_READ,
             MAP_NORESERVE | MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
#endif
    if (addr > (void *)0) {
        *ref = addr;
    } else {
        perror("Failed to create GC safepoint trap");
        exit(errno);
    }

#if defined(__APPLE__)
    /* LLDB installs task-wide Mach exception handlers. XNU dispatches Mach
     * exceptions first to any registered "activation" handler and then to
     * any registered task handler before dispatching the exception to a
     * host-wide Mach exception handler that does translation to POSIX
     * signals. This makes it impossible to use LLDB with safepoints;
     * continuing execution after LLDB
     * traps an EXC_BAD_ACCESS will result in LLDB's EXC_BAD_ACCESS handler
     * being invoked again. Work around this here by
     * installing a no-op task-wide Mach exception handler for
     * EXC_BAD_ACCESS.
     */
    kern_return_t kr = task_set_exception_ports(
        mach_task_self(), EXC_MASK_BAD_ACCESS, MACH_PORT_NULL,
        EXCEPTION_STATE_IDENTITY, MACHINE_THREAD_STATE);
    if (kr != KERN_SUCCESS)
        perror("Failed to create GC safepoint bad access handler");
#endif
}

void Safepoint_arm(safepoint_t ref) {
    bool success;
#ifdef _WIN32
    DWORD oldAccess;
    success = VirtualProtect((LPVOID)ref, sizeof(safepoint_t), PAGE_NOACCESS,
                             &oldAccess);
#else
    success = mprotect((void *)ref, sizeof(safepoint_t), PROT_NONE) == 0;
#endif
    if (!success) {
        perror("Failed to enable GC collect trap");
        exit(errno);
    }
}

void Safepoint_disarm(safepoint_t ref) {
    bool success;
#ifdef _WIN32
    DWORD oldAccess;
    success = VirtualProtect((LPVOID)ref, sizeof(safepoint_t), PAGE_READONLY,
                             &oldAccess);
#else
    success = mprotect((void *)ref, sizeof(safepoint_t), PROT_READ) == 0;
#endif
    if (!success) {
        perror("Failed to disable GC collect trap");
        exit(errno);
    }
}
