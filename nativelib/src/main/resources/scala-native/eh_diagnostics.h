#ifndef EH_DIAGNOSTICS_H
#define EH_DIAGNOSTICS_H

#ifdef __cplusplus
extern "C" {
#endif

/* Print thread identity, stack bounds, and a native backtrace to stderr.
 * Intended for fatal abort paths only (uncaught exception / unwind failure).
 * Must not throw. Skip Scala thread dump when TLS is already cleared. */
void scalanative_eh_dump_abort_context(const char *detail, int unwind_code);

/* Returns 1 on the first abort dump on this thread. Nested calls print one
 * line and abort immediately so dump helpers cannot recurse. */
int scalanative_eh_enter_abort_dump(void);

int scalanative_eh_java_thread_available(void);

void scalanative_dumpThreadDiagnostics(void);

#ifdef __cplusplus
}
#endif

#endif
