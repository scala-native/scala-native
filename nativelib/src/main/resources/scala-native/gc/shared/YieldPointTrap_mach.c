#if defined(SCALANATIVE_GC_IMMIX) || defined(SCALANATIVE_GC_COMMIX) ||         \
    defined(SCALANATIVE_GC_NONE) || defined(SCALANATIVE_GC_EXPERIMENTAL)

#if defined(__APPLE__)

/*
 * Apple-only translation unit for the Mach exception-port reset used by the
 * yieldpoint trap.
 *
 * It MUST NOT include "shared/ThreadUtil.h": that header typedef'd `thread_t`
 * and `semaphore_t` to POSIX wrappers (`pthread_t`, `sem_t *`), which collides
 * with `<mach/mach_types.h>` (mach_port_t-based) and breaks the build on the
 * macOS SDK.
 *
 * Therefore this file deliberately depends only on the Mach headers and on
 * the public YieldPointTrap.h (which has no transitive POSIX typedefs).
 */

#include <mach/mach.h>
#include "YieldPointTrap.h"
#include "shared/Log.h"

void YieldPointTrap_resetTaskMachBadAccessPorts(void) {
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
}

#endif // __APPLE__

#endif
