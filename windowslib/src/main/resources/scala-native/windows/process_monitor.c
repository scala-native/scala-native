#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <malloc.h>
#include <windows.h>
#include <stdio.h>

typedef struct _ProcessMonitorQueueEntry {
    HANDLE iocp;
    HANDLE proc;
    PTP_WAIT wait;
    DWORD pid;
    volatile LONG refcount;
} ProcessMonitorQueueEntry;

__declspec(dllexport) HANDLE ProcessMonitorQueueCreate(void) {
    return CreateIoCompletionPort(INVALID_HANDLE_VALUE, NULL, 0, 0);
}

static LONG
ProcessMonitorQueueEntryDecrementRefCount(ProcessMonitorQueueEntry *entry) {
    return InterlockedDecrement(&entry->refcount);
}

static VOID CALLBACK
ProcessMonitorQueueEntryCallback(PTP_CALLBACK_INSTANCE instance, PVOID ctx,
                                 PTP_WAIT wait, TP_WAIT_RESULT waitResult) {
    (void)instance;
    (void)wait;
    (void)waitResult; // unused

    if (NULL == ctx) {
        fprintf(stderr, "XXX ProcessMonitorQueueEntryCallback received NULL\n");
        return;
    }

    ProcessMonitorQueueEntry *entry = (ProcessMonitorQueueEntry *)ctx;
    fprintf(stderr, "XXX ProcessMonitorQueueEntryCallback received: %lu\n",
            entry->pid);

    LONG refcount = ProcessMonitorQueueEntryDecrementRefCount(entry);

    BOOL ok =
        PostQueuedCompletionStatus(entry->iocp, 0, 0, (LPOVERLAPPED)entry);
    fprintf(stderr,
            "XXX ProcessMonitorQueueEntryCallback send status (refcount=%lu): "
            "%lu\n",
            refcount, entry->pid);
    (void)ok;
}

static VOID
ProcessMonitorQueueEntryCloseThreadPoolWait(ProcessMonitorQueueEntry *entry) {

    HANDLE oldWait =
        (HANDLE)InterlockedExchangePointer((PVOID *)&entry->wait, NULL);

    if (oldWait != NULL) {

        fprintf(stderr, "XXX ProcessMonitorQueuePull cancel wait: %lu\n",
                entry->pid);
        /* Cancel any future waits for this object */
        SetThreadpoolWait(oldWait, NULL, NULL);

        /* Close the threadpool wait object and wait for any running callbacks
         * to finish */
        fprintf(stderr,
                "XXX ProcessMonitorQueuePull close threadpool wait: %lu\n",
                entry->pid);
        CloseThreadpoolWait(oldWait);
    }
}

__declspec(dllexport) BOOL
    ProcessMonitorQueueRegister(HANDLE iocp, HANDLE process, DWORD pid) {
    ProcessMonitorQueueEntry *entry =
        (ProcessMonitorQueueEntry *)malloc(sizeof(ProcessMonitorQueueEntry));
    if (!entry)
        return FALSE;

    entry->iocp = iocp;
    entry->proc = NULL;
    entry->pid = pid;
    entry->refcount = 3; // 1 for this method, 1 for callback, 1 for consumer

    fprintf(stderr, "XXX ProcessMonitorQueueRegister starting: %lu\n",
            entry->pid);

    // won't depend on the original process handle
    if (!DuplicateHandle(GetCurrentProcess(), process, GetCurrentProcess(),
                         &entry->proc, 0, FALSE, DUPLICATE_SAME_ACCESS)) {
        free(entry);
        return FALSE;
    }

    fprintf(stderr,
            "XXX ProcessMonitorQueueRegister duplicated process handle\n");

    /* Create a threadpool wait object whose callback contexts to our entry */
    entry->wait =
        CreateThreadpoolWait(ProcessMonitorQueueEntryCallback, entry, NULL);
    if (entry->wait == NULL) {
        fprintf(
            stderr,
            "XXX ProcessMonitorQueueRegister failed to create threadpool\n");
        CloseHandle(entry->proc);
        free(entry);
        return FALSE;
    }

    fprintf(stderr, "XXX ProcessMonitorQueueRegister created threadpool\n");

    /* Arm the wait on the process handle. The callback will run when the handle
     * is signaled. */
    SetThreadpoolWait(entry->wait, entry->proc, NULL);

    fprintf(stderr, "XXX ProcessMonitorQueueRegister set threadpool\n");

    // check if exited before it was registered
    // return TRUE regardless, to read status from the queue
    DWORD exitCode;
    if (GetExitCodeProcess(entry->proc, &exitCode) &&
        exitCode != STILL_ACTIVE) {
        ProcessMonitorQueueEntryCloseThreadPoolWait(entry);

        LONG refcount = ProcessMonitorQueueEntryDecrementRefCount(entry);
        /* if refcount is:
         * 2: callback not fired and will not
         * 1: callback has fired, consumer will do its job
         * 0: callback has fired, consumer has too but didn't do anything
         */
        if (refcount != 1) {
            if (refcount == 2)
                ProcessMonitorQueueEntryDecrementRefCount(entry);

            PostQueuedCompletionStatus(entry->iocp, 0, 0, (LPOVERLAPPED)entry);
        }
    }

    return TRUE;
}

__declspec(dllexport) DWORD
    ProcessMonitorQueuePull(HANDLE iocp, DWORD timeoutMs) {
    DWORD bytes;
    ULONG_PTR key;
    LPOVERLAPPED overlapped;

    fprintf(stderr, "XXX ProcessMonitorQueuePull starting\n");
    GetQueuedCompletionStatus(iocp, &bytes, &key, &overlapped, timeoutMs);

    fprintf(stderr, "XXX ProcessMonitorQueuePull received isnull=%d\n",
            overlapped == NULL);

    if (overlapped == NULL)
        return (DWORD)-1;

    ProcessMonitorQueueEntry *entry = (ProcessMonitorQueueEntry *)overlapped;
    if (ProcessMonitorQueueEntryDecrementRefCount(entry) > 0)
        return (DWORD)-1;

    ProcessMonitorQueueEntryCloseThreadPoolWait(entry);

    fprintf(stderr, "XXX ProcessMonitorQueuePull close process handle: %lu\n",
            entry->pid);
    CloseHandle(entry->proc); // close the process handle copy

    DWORD pid = entry->pid;
    free(entry);

    return pid;
}

#endif
