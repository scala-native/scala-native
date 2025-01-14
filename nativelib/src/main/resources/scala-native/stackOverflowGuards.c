#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <malloc.h>
#else // Unix
#if defined(__linux__)
#define _GNU_SOURCE 1 /* To pick up REG_RIP */
#include <ucontext.h>
#endif
#include <sys/resource.h>
#include <sys/mman.h>
#include <signal.h>
#include <unistd.h>
#endif

#include "stackOverflowGuards.h"
#include "nativeThreadTLS.h"
#include "StackTrace.h"
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <assert.h>
#include <string.h>

__attribute((noreturn)) extern void
scalanative_throwPendingStackOverflowError();

size_t scalanative_stackOverflowGuardsSize() {
    return stackGuardPages() * resolvePageSize();
}

#ifdef _WIN32
static LONG WINAPI stackOverflowHandler(PEXCEPTION_POINTERS ex) {
    switch (ex->ExceptionRecord->ExceptionCode) {
    case EXCEPTION_STACK_OVERFLOW:
        scalanative_throwPendingStackOverflowError();
        return EXCEPTION_CONTINUE_EXECUTION;
    default:
        return EXCEPTION_CONTINUE_SEARCH;
    }
    return EXCEPTION_CONTINUE_SEARCH;
}
void scalanative_setupStackOverflowGuards(bool isMainThread) {
    static bool isHandlerConfigured = false;
    if (isMainThread && !isHandlerConfigured) {
        AddVectoredExceptionHandler(0, &stackOverflowHandler);
        isHandlerConfigured = true;
    }
    ULONG stackOverflowStackSize = scalanative_stackOverflowGuardsSize();
    currentThreadInfo.stackGuardPage =
        (void *)((uintptr_t)currentThreadInfo.stackTop +
                 stackOverflowStackSize);

    if (!SetThreadStackGuarantee(&stackOverflowStackSize)) {
        fprintf(stderr,
                "Scala Native Error: Failed to set thread stack guarantee, "
                "stack overflow detection might not work correctly\n");
    }
}

void scalanative_resetStackOverflowGuards() {
    int dummy;
    void *curStackTop = &dummy;
    ThreadInfo info = currentThreadInfo;
    if (inStackPageBound(info.stackGuardPage, curStackTop))
        return; // still unwinding
    _resetstkoflw();
}

#else // Unix
static struct sigaction *resolvePreviousSignalHandler(int sig) {
    static struct sigaction previousSignalHandlers[2];
    switch (sig) {
    case SIGSEGV:
        return &previousSignalHandlers[0];
    case SIGBUS:
        return &previousSignalHandlers[1];
    default:
        fprintf(stderr,
                "ScalaNative :: StackOverflowHandler does not define handler "
                "for %d signal\n",
                sig);
        abort();
    }
}

static void protectPage(void *addr) {
    if (mprotect(addr, resolvePageSize(), PROT_NONE) == -1) {
        perror("ScalaNative Fatal Error :: StackOverflowHandler guard "
               "protection failed");
        abort();
    }
}
static void unprotectPage(void *addr) {
    if (mprotect(addr, resolvePageSize(), PROT_READ | PROT_WRITE) == -1) {
        perror("ScalaNative Fatal Error :: StackOverflowHandler guard "
               "unprotection failed");
        abort();
    }
}

static void stackOverflowHandler(int sig, siginfo_t *info, void *context) {
    void *faultAddr = info->si_addr;
    ThreadInfo threadInfo = currentThreadInfo;
    struct sigaction *previousSignalHandler;

    switch (sig) {
    case SIGSEGV:
    case SIGBUS:
        /* We cannot throw exception directly from signal handler - libunwind
         * would not be able to locate catch handler.
         * In the past we've tried to workaround it with:
         * 1. Creating synthetic stack frame by modify the ucontext_t passed to
         * signal handler as `context` and continue execution in selected
         * function but throwing exceptions would not be reliable - under
         * -fomit-frame-pointer or non zero -O optimizer settings libunwind
         * might not be able to construct correct function call chain thus
         * failing to throw. In such case we would not be able to recover.
         * Without frame pointers it was needed to use alternative information
         * like DWARF for which we cannot reliably create synthetic data.
         * 2. Using signal handler context to create a synthetic unw_context /
         * unw_cursor. This was was stored in the thread local storage and used
         * on deamand instead of resolved one at the moment of invocation. These
         * allowed to successfully and mostly correctly collect stack trace
         * outside the signal handler. Also with minimal modification it could
         * have been used in a variant of _Unwind_RaiseException. Unfortunetly,
         * even though solution allowed in most cases to correctly unwind the
         * stack/execution on return it polutted the execution state (registers)
         * with incorrect data. This lead to logic errors and undefined
         * behaviours.
         * Becouse of these we introduced soft stack overflow limit - we
         * continue execution and notify runtime to throw StackOverflowError
         * lazily. Based on closed world assumptions we identify functions that
         * are recursive, only these would check if throwing StackOverflowError
         * is needed when entering the function.
         */
        if (inStackPageBound(threadInfo.stackGuardPage, faultAddr)) {
            if (threadInfo.isMainThread &&
                threadInfo.stackSize < scalanative_mainThreadMaxStackSize()) {
                // Main thread stack size was not fully mapped
                // Try to let it grow and continue execution
                unprotectPage(threadInfo.stackGuardPage);
                if (scalanative_forceMainThreadStackGrowth()) {
                    scalanative_setupStackOverflowGuards(true);
                    return;
                }
            }
            // Let the exception be thrown after polling
            currentThreadInfo.pendingStackOverflowException = true;
            unprotectPage(currentThreadInfo.stackGuardPage);
            return;
        } else if (isInRange(faultAddr, threadInfo.stackGuardPage,
                             (void *)(char *)threadInfo.stackTop -
                                 (resolvePageSize() + 64 * 1024))) {
            // Unrecoverable if stack overflow is detected somewhere above
            // current stack top
            fprintf(stderr,
                    "ScalaNative :: Unrecoverable StackOverflow error in %s "
                    "thread, stack size = %zuKB\n",
                    threadInfo.isMainThread ? "main" : "user",
                    threadInfo.stackSize / 1024);
            StackTrace_PrintStackTrace();
            abort();
        } else if (faultAddr == NULL) {
            fprintf(stderr,
                    "ScalaNative :: Unrecoverable NullPointerException in %s "
                    "thread\n",
                    threadInfo.isMainThread ? "main" : "user");
            StackTrace_PrintStackTrace();
            abort();
        }
    default:
        previousSignalHandler = resolvePreviousSignalHandler(sig);
        if (previousSignalHandler != NULL &&
            previousSignalHandler->sa_handler != NULL) {
            void *handler = previousSignalHandler->sa_handler;
            if (handler != SIG_DFL && handler != SIG_IGN &&
                handler != SIG_ERR && handler != stackOverflowHandler) {
                if (previousSignalHandler->sa_flags & SA_SIGINFO) {
                    return previousSignalHandler->sa_sigaction(sig, info,
                                                               context);
                } else {
                    return previousSignalHandler->sa_handler(sig);
                }
            }
            return;
        } else {
            fprintf(stderr, "ScalaNative :: Unhandled signal %d, si_addr=%p\n",
                    sig, faultAddr);
            abort();
        }
    }
}

static void setupSignalHandlerAltstack() {
    stack_t handlerStack = {};
    size_t pageSize = resolvePageSize();
    handlerStack.ss_size = SIG_HANDLER_STACK_SIZE;
    handlerStack.ss_sp = &currentThreadInfo.signalHandlerStack;
    handlerStack.ss_flags = 0;
    if (sigaltstack(&handlerStack, NULL) == -1) {
        perror("Scala Native Stack Overflow Handler failed to set alt stack");
        abort();
    }
}
static void setupSignalHandler(int signal) {
    struct sigaction sa = {};
    sa.sa_sigaction = stackOverflowHandler;
    sa.sa_flags = SA_SIGINFO | SA_RESTART | SA_ONSTACK;
    sigemptyset(&sa.sa_mask);
    setupSignalHandlerAltstack();
    if (sigaction(signal, &sa, resolvePreviousSignalHandler(signal)) == -1) {
        perror(
            "scalanative :: StackOverflowHandler failed to set signal handler");
        exit(EXIT_FAILURE);
    }
}

void scalanative_setupStackOverflowGuards(bool isMainThread) {
    assert(currentThreadInfo.stackSize > 0);
    assert(currentThreadInfo.stackTop != NULL);
    assert(currentThreadInfo.stackBottom != NULL);

    currentThreadInfo.stackGuardPage =
        (void *)((uintptr_t)currentThreadInfo.stackTop +
                 scalanative_stackOverflowGuardsSize());

    assert(currentThreadInfo.stackGuardPage > currentThreadInfo.stackTop);
    assert(currentThreadInfo.stackGuardPage < currentThreadInfo.stackBottom);

    protectPage(currentThreadInfo.stackGuardPage);

    static bool isHandlerConfigured = false;
    if (isMainThread && isHandlerConfigured)
        return;

    if (isMainThread) {
        setupSignalHandler(SIGSEGV);
#if (defined(__APPLE__) && defined(__MACH__))
        setupSignalHandler(SIGBUS);
        isHandlerConfigured = true;
#endif // Apple
    } else {
        setupSignalHandlerAltstack();
    }
}

void scalanative_resetStackOverflowGuards() {
    int dummy;
    void *curStackTop = &dummy;
    ThreadInfo info = currentThreadInfo;
    currentThreadInfo.pendingStackOverflowException = false;
    if (inStackPageBound(info.stackGuardPage, curStackTop))
        return; // still unwinding
    protectPage(info.stackGuardPage);
}

#endif // Unix

void scalanative_checkStackOverflowGuards() {
#ifdef _WIN32
// unused
#else
    if (currentThreadInfo.pendingStackOverflowException) {
        currentThreadInfo.pendingStackOverflowException = false;
        scalanative_throwPendingStackOverflowError();
    }
#endif
}
