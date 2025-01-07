#include "stackOverflowGuards.h"
#include <stdio.h>
#include <stdlib.h>

#include "nativeThreadTLS.h"
#include "gc/shared/ThreadUtil.h"
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <assert.h>
#include <string.h>
#ifndef _WIN32
#include <sys/resource.h>
#include <sys/mman.h>
#include <signal.h>
#include <unistd.h>
#endif

#define StackGuardPages 2

static void protectPage(void *addr) {
#ifdef _WIN32
// TODO: Windows support
#else
    if (mprotect(addr, resolvePageSize(), PROT_NONE) == -1) {
        perror("ScalaNative Fatal Error :: StackOverflowHandler guard "
               "protection failed");
        abort();
    }
#endif
}
static void unprotectPage(void *addr) {
#ifdef _WIN32
// TODO: Windows support
#else
    if (mprotect(addr, resolvePageSize(), PROT_READ | PROT_WRITE) == -1) {
        perror("ScalaNative Fatal Error :: StackOverflowHandler guard "
               "unprotection failed");
        abort();
    }
#endif
}

extern void scalanative_throwPendingStackOverflowError();

void scalanative_handlePendingStackOverflowError() {
    currentThreadInfo.checkPendingExceptions = false;
    scalanative_throwPendingStackOverflowError();
    currentThreadInfo.checkPendingExceptions = true;
}

#ifndef _WIN32
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

static void scalanative_stackOverflowHandler(int sig, siginfo_t *info,
                                             void *context) {
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
#if defined(__APPLE__) && defined(__MACH__)
#if defined(__x86_64__)
            ucontext_t *ctx = (ucontext_t *)context;
            ctx->uc_mcontext->__ss.__rip =
                (uintptr_t)scalanative_throwPendingStackOverflowError;
#elif defined(__arm64__) || defined(__aarch64__)
            ucontext_t *ctx = (ucontext_t *)context;
            ctx->uc_mcontext->__ss.__lr = ctx->uc_mcontext->__ss.__pc;
            ctx->uc_mcontext->__ss.__pc =
                (uintptr_t)scalanative_throwPendingStackOverflowError;
#endif // arm64
#elif defined(__linux__)
#if defined(__aarch64__)
            ucontext_t *ctx = (ucontext_t *)context;
            ctx->uc_mcontext.pc =
                (uintptr_t)scalanative_throwPendingStackOverflowError;
#elif defined(__x86_64__) || defined(__i386__)
            ucontext_t *ctx = (ucontext_t *)context;
            ctx->uc_mcontext.gregs[REG_EIP] =
                (uintptr_t)scalanative_throwPendingStackOverflowError;
#endif
#endif
            return;
        } else if (faultAddr <= threadInfo.firstStackGuardPage &&
                   faultAddr > (void *)(char *)threadInfo.stackTop -
                                   2 * resolvePageSize()) {
            fprintf(stderr,
                    "ScalaNative :: Unrecoverable StackOverflow error in %s "
                    "thread, stack size = %zuKB\n",
                    threadInfo.isMainThread ? "main" : "user",
                    threadInfo.stackSize / 1024);
            abort();
        }
    default:
        previousSignalHandler = resolvePreviousSignalHandler(sig);
        if (previousSignalHandler != NULL &&
            previousSignalHandler->sa_handler != NULL) {
            void *handler = previousSignalHandler->sa_handler;
            if (handler != SIG_DFL && handler != SIG_IGN &&
                handler != SIG_ERR &&
                handler != scalanative_stackOverflowHandler) {
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
            fprintf(stderr, "ScalaNative :: Unhandled signal %d\n", sig);
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
    sa.sa_sigaction = scalanative_stackOverflowHandler;
    sa.sa_flags = SA_SIGINFO | SA_RESTART | SA_ONSTACK;
    sigemptyset(&sa.sa_mask);
    setupSignalHandlerAltstack();
    if (sigaction(signal, &sa, resolvePreviousSignalHandler(signal)) == -1) {
        perror(
            "scalanative :: StackOverflowHandler failed to set signal handler");
        exit(EXIT_FAILURE);
    }
}
#endif // not _WIN32
// TODO: Windows upport

size_t scalanative_stackOverflowGuardsSize() {
    return 2 * StackGuardPages * resolvePageSize();
}

void scalanative_setupStackOverflowGuards(bool isMainThread) {
    size_t pageSize = resolvePageSize();
    assert(currentThreadInfo.stackSize > 0);
    assert(currentThreadInfo.stackTop != NULL);
    assert(currentThreadInfo.stackBottom != NULL);

    currentThreadInfo.secondStackGuardPage =
        (void *)((uintptr_t)currentThreadInfo.stackTop +
                 pageSize * StackGuardPages);
    currentThreadInfo.firstStackGuardPage =
        (void *)((uintptr_t)currentThreadInfo.secondStackGuardPage +
                 pageSize * StackGuardPages);

    assert(currentThreadInfo.secondStackGuardPage <
           currentThreadInfo.firstStackGuardPage);
    assert(currentThreadInfo.firstStackGuardPage > currentThreadInfo.stackTop);
    assert(currentThreadInfo.firstStackGuardPage <
           currentThreadInfo.stackBottom);

// Windows is not supported
#if !defined(_WIN32)
    protectPage(currentThreadInfo.firstStackGuardPage);
    protectPage(currentThreadInfo.secondStackGuardPage);
    if (isMainThread) {
        setupSignalHandler(SIGSEGV);
#if defined(SCALANATIVE_MULTITHREADING_ENABLED) &&                             \
    (defined(__APPLE__) && defined(__MACH__))
        setupSignalHandler(SIGBUS);
#endif
#endif
    } else {
        setupSignalHandlerAltstack();
    }
}

void scalanative_resetStackOverflowGuards() {
    int dummy;
    void *stackTop = &dummy;
    ThreadInfo info = currentThreadInfo;
    if (belowStackPageBounds(info.firstStackGuardPage, stackTop))
        return;
    protectPage(currentThreadInfo.firstStackGuardPage);

    if (belowStackPageBounds(info.secondStackGuardPage, stackTop))
        return;
    protectPage(currentThreadInfo.secondStackGuardPage);
}
