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
#include "gc/shared/ThreadUtil.h"
#include "StackTrace.h"
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <assert.h>
#include <string.h>

extern void scalanative_throwPendingStackOverflowError();

size_t scalanative_stackOverflowGuardsSize() {
    return 2 * stackGuardPages() * resolvePageSize();
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
    currentThreadInfo.firstStackGuardPage =
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
    void *stackTop = &dummy;
    ThreadInfo info = currentThreadInfo;
    if (belowStackPageBounds(info.firstStackGuardPage, stackTop))
        return;
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

void scalanative_handlePendingStackOverflowError() {
    currentThreadInfo.checkPendingExceptions = false;
    scalanative_throwPendingStackOverflowError();
    currentThreadInfo.checkPendingExceptions = true;
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
         * We can modify the context to create synthetic stack frame and
         * continue execution in selected function but throwing exceptions would
         * not be reliable - under -fomit-frame-pointer or non zero -O optimizer
         * settings libunwind might not be able to construct correct function
         * call chain thus failing to throw. In such case we would not be able
         * to recover. Without frame pointers it needs to use alternative
         * information like DWARF for which we cannot reliably create synthetic
         * data. Becouse of that we introduce soft stack overflow limit - we
         * continue execution and notify runtime to throw StackOverflowError.
         * We're introducing checks in some well known locations that would
         * trigger throwing function.
         */
        if (inStackPageBound(threadInfo.firstStackGuardPage, faultAddr)) {
            if (threadInfo.isMainThread &&
                threadInfo.stackSize < scalanative_mainThreadMaxStackSize()) {
                // Main thread stack size was not fully mapped
                // Try to let it grow and continue execution
                unprotectPage(threadInfo.firstStackGuardPage);
                unprotectPage(threadInfo.secondStackGuardPage);
                if (scalanative_forceMainThreadStackGrowth()) {
                    scalanative_setupStackOverflowGuards(true);
                    return;
                }
            }
            // Let the exception be thrown after polling
            currentThreadInfo.checkPendingExceptions = true;
            currentThreadInfo.pendingStackOverflowException = true;
            unprotectPage(currentThreadInfo.firstStackGuardPage);
            return;
        } else if (inStackPageBound(threadInfo.secondStackGuardPage,
                                    faultAddr)) {
            // Try unsafely modify context to throw exception if given platform
            // supports it or let it spin with hope of handling pending stack
            // overflow exception
            unprotectPage(threadInfo.secondStackGuardPage);
            currentThreadInfo.checkPendingExceptions = true;
            currentThreadInfo.pendingStackOverflowException = true;
            // All further logic is based on context
            // Skip if it's not available
            if (context == NULL)
                return;
#if defined(__APPLE__) && defined(__MACH__)
#if defined(__x86_64__)
            ucontext_t *ctx = (ucontext_t *)context;
            ctx->uc_mcontext->__ss.__rip =
                (uintptr_t)scalanative_handlePendingStackOverflowError;
#elif defined(__arm64__) || defined(__aarch64__)
            ucontext_t *ctx = (ucontext_t *)context;
            ctx->uc_mcontext->__ss.__lr = ctx->uc_mcontext->__ss.__pc;
            ctx->uc_mcontext->__ss.__pc =
                (uintptr_t)scalanative_handlePendingStackOverflowError;
#endif // arm64
#elif defined(__linux__)
#if defined(__aarch64__)
            ucontext_t *ctx = (ucontext_t *)context;
            ctx->uc_mcontext.pc =
                (uintptr_t)scalanative_handlePendingStackOverflowError;
#elif defined(__x86_64__)
            ucontext_t *ctx = (ucontext_t *)context;
            ctx->uc_mcontext.gregs[REG_RIP] =
                (greg_t)scalanative_handlePendingStackOverflowError;
#elif defined(__i386__)
            ucontext_t *ctx = (ucontext_t *)context;
            ctx->uc_mcontext.gregs[REG_EIP] =
                (greg_t)scalanative_handlePendingStackOverflowError;
#endif
#endif
            return;
            // Check if address is close to the end stack memory region
        } else if (isInRange(faultAddr, threadInfo.firstStackGuardPage,
                             (void *)(char *)threadInfo.stackTop -
                                 (resolvePageSize() + 64 * 1024))) {
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
                    void (*sigInfoHandler)(int, siginfo_t *, void *) =
                        (void (*)(int, siginfo_t *,
                                  void *))previousSignalHandler->sa_handler;
                    return sigInfoHandler(sig, info, context);
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

#define SIG_HANDLER_STACK_SIZE (32 * 1024)
static SN_ThreadLocal char *signalHandlerStack[SIG_HANDLER_STACK_SIZE] = {0};
static void setupSignalHandlerAltstack() {
    stack_t handler_stack;
    size_t pageSize = resolvePageSize();
    handler_stack.ss_size = SIG_HANDLER_STACK_SIZE;
    handler_stack.ss_sp = &signalHandlerStack;
    handler_stack.ss_flags = 0;
    sigaltstack(&handler_stack, NULL);
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

    size_t pageSize = resolvePageSize();
    currentThreadInfo.secondStackGuardPage =
        (void *)((uintptr_t)currentThreadInfo.stackTop +
                 pageSize * stackGuardPages());
    currentThreadInfo.firstStackGuardPage =
        (void *)((uintptr_t)currentThreadInfo.secondStackGuardPage +
                 pageSize * stackGuardPages());

    assert(currentThreadInfo.secondStackGuardPage <
           currentThreadInfo.firstStackGuardPage);
    assert(currentThreadInfo.firstStackGuardPage > currentThreadInfo.stackTop);
    assert(currentThreadInfo.firstStackGuardPage <
           currentThreadInfo.stackBottom);

    protectPage(currentThreadInfo.firstStackGuardPage);
    protectPage(currentThreadInfo.secondStackGuardPage);

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
    void *stackTop = &dummy;
    ThreadInfo info = currentThreadInfo;
    currentThreadInfo.pendingStackOverflowException = false;
    currentThreadInfo.checkPendingExceptions = false;

    if (belowStackPageBounds(info.secondStackGuardPage, stackTop))
        return;
    protectPage(info.secondStackGuardPage);

    if (belowStackPageBounds(info.firstStackGuardPage, stackTop))
        return;
}
#endif // Unix
