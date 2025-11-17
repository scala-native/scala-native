#if defined(_WIN32) || defined(WIN32)
#define WIN32_LEAN_AND_MEAN
#include <malloc.h>
#include <windows.h>

typedef struct _ProcessMonitorQueueEntry {
    HANDLE iocp;
    DWORD pid;
} ProcessMonitorQueueEntry;

__declspec(dllexport) HANDLE ProcessMonitorQueueCreate(void) {
    return CreateIoCompletionPort(INVALID_HANDLE_VALUE, NULL, 0, 0);
}

static VOID CALLBACK
ProcessMonitorQueueEntryCallback(PVOID lpParameter, BOOLEAN TimerOrWaitFired) {
    ProcessMonitorQueueEntry *pw = (ProcessMonitorQueueEntry *)lpParameter;
    PostQueuedCompletionStatus(pw->iocp, 0, (ULONG_PTR)pw->pid, NULL);
    free(pw);
}

__declspec(dllexport) BOOL
    ProcessMonitorQueueRegister(HANDLE iocp, HANDLE process, DWORD pid) {
    ProcessMonitorQueueEntry *pw =
        (ProcessMonitorQueueEntry *)malloc(sizeof(ProcessMonitorQueueEntry));
    if (!pw)
        return FALSE;

    pw->iocp = iocp;
    pw->pid = pid;

    HANDLE hWait = NULL; // won't need it, WT_EXECUTEONLYONCE will clean up

    BOOL ok = RegisterWaitForSingleObject(&hWait, process,
                                          ProcessMonitorQueueEntryCallback, pw,
                                          INFINITE, WT_EXECUTEONLYONCE);
    if (!ok)
        free(pw);

    return ok;
}

__declspec(dllexport) DWORD
    ProcessMonitorQueuePull(HANDLE iocp, DWORD timeoutMs) {
    DWORD bytes;
    ULONG_PTR key;
    LPOVERLAPPED overlapped;

    BOOL ok =
        GetQueuedCompletionStatus(iocp, &bytes, &key, &overlapped, timeoutMs);

    if (!ok && overlapped == NULL)
        return (DWORD)-1;

    return (DWORD)key; // returns the PID that completed
}

#endif
