#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <malloc.h>
#include <windows.h>
#include <stdio.h>

typedef struct _ProcessMonitorQueueEntry {
    HANDLE iocp;
    HANDLE proc;
    HANDLE wait;
    DWORD pid;
} ProcessMonitorQueueEntry;

__declspec(dllexport) HANDLE ProcessMonitorQueueCreate(void) {
    return CreateIoCompletionPort(INVALID_HANDLE_VALUE, NULL, 0, 0);
}

static VOID CALLBACK
ProcessMonitorQueueEntryCallback(PVOID lpParameter, BOOLEAN TimerOrWaitFired) {
    ProcessMonitorQueueEntry *entry = (ProcessMonitorQueueEntry *)lpParameter;
    fprintf(stderr, "XXX ProcessMonitorQueueEntryCallback received: %lu\n",
            entry->pid);
    PostQueuedCompletionStatus(entry->iocp, 0, 0, (LPOVERLAPPED)entry);
    fprintf(stderr, "XXX ProcessMonitorQueueEntryCallback send status\n",
            entry->pid);
}

__declspec(dllexport) BOOL
    ProcessMonitorQueueRegister(HANDLE iocp, HANDLE process, DWORD pid) {
    ProcessMonitorQueueEntry *entry =
        (ProcessMonitorQueueEntry *)malloc(sizeof(ProcessMonitorQueueEntry));
    if (!entry)
        return FALSE;

    entry->iocp = iocp;
    entry->proc = NULL;
    entry->wait = NULL;
    entry->pid = pid;

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

    BOOL ok = RegisterWaitForSingleObject(&entry->wait, entry->proc,
                                          ProcessMonitorQueueEntryCallback,
                                          entry, INFINITE, WT_EXECUTEONLYONCE | WT_EXECUTELONGFUNCTION);

    fprintf(stderr, "XXX ProcessMonitorQueueRegister registered: %d\n", ok);

    if (!ok) {
        CloseHandle(entry->proc);
        free(entry);
    }

    return ok;
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

    fprintf(stderr, "XXX ProcessMonitorQueuePull unregister wait: %lu\n",
            entry->pid);
    UnregisterWaitEx(entry->wait, INVALID_HANDLE_VALUE);

    fprintf(stderr, "XXX ProcessMonitorQueuePull close process handle: %lu\n",
            entry->pid);
    CloseHandle(entry->proc); // close the process handle copy

    DWORD pid = entry->pid;
    free(entry);

    return pid;
}

#endif
