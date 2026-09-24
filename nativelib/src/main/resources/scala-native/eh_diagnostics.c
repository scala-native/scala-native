#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif

#include "eh_diagnostics.h"

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>

#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <process.h>
#else
#include <pthread.h>
#include <unistd.h>
#endif

#include "nativeThreadTLS.h"
#include "platform/unwind.h"
#include "string_constants.h"

extern int scalanative_unwind_get_proc_name_by_ip(size_t ip, char *buffer,
                                                  size_t length,
                                                  size_t *offset);

/* Windows unwind context is a few KB; keep this on the stack so dump never
 * goes through malloc/GC while already aborting. */
enum { EH_DIAG_UNWIND_BUF = 8192 };

static _Thread_local int eh_diag_dumping;

int scalanative_eh_enter_abort_dump(void) {
    if (eh_diag_dumping) {
        fprintf(stderr, "%s abort dump reentered; aborting immediately\n",
                snFatalErrorPrefix);
        fflush(stderr);
        fflush(stdout);
        abort();
    }
    eh_diag_dumping = 1;
    return 1;
}

int scalanative_eh_java_thread_available(void) {
    return scalanative_currentThread() != NULL;
}

static void print_native_thread_name(void) {
#ifdef _WIN32
    fprintf(stderr, "os thread: tid=%lu\n",
            (unsigned long)GetCurrentThreadId());
#else
    char name[64];
    memset(name, 0, sizeof(name));
#if defined(__APPLE__) || defined(__linux__)
    pthread_getname_np(pthread_self(), name, sizeof(name));
#endif
    fprintf(stderr, "os thread: pthread=%p name='%s'\n", (void *)pthread_self(),
            name);
#endif
}

static void print_stack_bounds(void) {
    ThreadInfo *info = scalanative_currentThreadInfo();
    int dummy = 0;
    fprintf(stderr,
            "thread info: javaThread=%p nativeThread=%p isMain=%d "
            "stackTop=%p stackBottom=%p stackSize=%zu maxStackSize=%zu "
            "approxSp=%p\n",
            scalanative_currentThread(), scalanative_currentNativeThread(),
            info ? (int)info->isMainThread : -1, info ? info->stackTop : NULL,
            info ? info->stackBottom : NULL, info ? info->stackSize : 0,
            info ? info->maxStackSize : 0, (void *)&dummy);
}

static void print_native_backtrace(void) {
    size_t cursorSize = scalanative_unwind_sizeof_cursor();
    size_t contextSize = scalanative_unwind_sizeof_context();
    unsigned char cursorBuf[EH_DIAG_UNWIND_BUF];
    unsigned char contextBuf[EH_DIAG_UNWIND_BUF];
    if (cursorSize > sizeof(cursorBuf) || contextSize > sizeof(contextBuf)) {
        fprintf(stderr,
                "native backtrace: unwind buffers too small "
                "(cursor=%zu context=%zu cap=%d)\n",
                cursorSize, contextSize, EH_DIAG_UNWIND_BUF);
        return;
    }
    void *cursor = cursorBuf;
    void *context = contextBuf;
    memset(cursor, 0, cursorSize);
    memset(context, 0, contextSize);
    if (scalanative_unwind_get_context(context) != 0 ||
        scalanative_unwind_init_local(cursor, context) != 0) {
        fprintf(stderr, "native backtrace: unwind init failed\n");
        return;
    }

    fprintf(stderr, "native backtrace:\n");
    int frames = 0;
    const int maxFrames = 64;
    while (scalanative_unwind_step(cursor) > 0 && frames < maxFrames) {
        size_t pc = 0;
        scalanative_unwind_get_reg(cursor, scalanative_unw_reg_ip(), &pc);
        if (pc == 0) {
            break;
        }
        char sym[256];
        memset(sym, 0, sizeof(sym));
        size_t offset = 0;
        int named = scalanative_unwind_get_proc_name(cursor, sym, sizeof(sym),
                                                     &offset) == 0;
        if (!named || sym[0] == '\0') {
            scalanative_unwind_get_proc_name_by_ip(pc, sym, sizeof(sym),
                                                   &offset);
        }
        fprintf(stderr, "  #%d ip=%p %s+0x%zx\n", frames, (void *)pc,
                sym[0] ? sym : "??", offset);
        frames++;
    }
    if (frames >= maxFrames) {
        fprintf(stderr, "  ... truncated after %d frames\n", maxFrames);
    }
}

void scalanative_eh_dump_abort_context(const char *detail, int unwind_code) {
    fprintf(stderr, "%s exception diagnostics: %s (unwind_code=%d)\n",
            snFatalErrorPrefix, detail ? detail : "(no detail)", unwind_code);
#ifdef _WIN32
    fprintf(stderr, "pid=%d\n", _getpid());
#else
    fprintf(stderr, "pid=%d\n", (int)getpid());
#endif
    print_native_thread_name();
    print_stack_bounds();
    if (scalanative_eh_java_thread_available()) {
        scalanative_dumpThreadDiagnostics();
    } else {
        fprintf(stderr, "Scala thread: skipped (TLS already cleared)\n");
    }
    print_native_backtrace();
    fflush(stderr);
    fflush(stdout);
}
