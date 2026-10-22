static void *library_worker(void *unused) {
    (void)unused;
    callback_pressure();

    return NULL;
}
static event altstack_ready = EVENT_INIT;
static event altstack_done = EVENT_INIT;
static void altstack_signal_ready(void) { event_set(&altstack_ready); }
static int altstack_is_done(void) { return event_is_set(&altstack_done); }
static void *altstack_collector(void *unused) {
    (void)unused;
    event_wait(&altstack_ready);
    for (int i = 0; i < 8; ++i)
        callback_collect();
    event_set(&altstack_done);

    return NULL;
}
static void *alternate_stack_worker(void *unused) {
    (void)unused;
#ifdef _WIN32
    /* Windows has a stack-overflow guarantee instead of sigaltstack. */
    ULONG guarantee = 64 * 1024;
    CHECK(SetThreadStackGuarantee(&guarantee));
    ULONG before = 0;
    CHECK(SetThreadStackGuarantee(&before));
#else
    stack_t before, after, original;
    memset(&before, 0, sizeof(before));
    before.ss_size = (size_t)SIGSTKSZ * 4;
    before.ss_sp = malloc(before.ss_size);
    CHECK(before.ss_sp);
    CHECK(sigaltstack(&before, &original) == 0);
#endif

    callback_pressure();
    if (multithreaded) {
        host_thread_t collector = worker_start(altstack_collector, NULL);

        external_altstack_race(altstack_signal_ready, altstack_is_done);

        worker_join(collector);
        event_close(&altstack_ready);
        event_close(&altstack_done);
        milestone("altstack-collection-race");
    }
#ifdef _WIN32
    ULONG after = 0;
    CHECK(SetThreadStackGuarantee(&after));
    CHECK(after == before);

    /* The guarantee cannot be reduced; worker exit releases its stack. */
#else
    CHECK(sigaltstack(NULL, &after) == 0);
    CHECK(after.ss_sp == before.ss_sp && after.ss_size == before.ss_size);
#ifdef __APPLE__
    /* Darwin validates size even when restoring a disabled signal stack. */
    if ((original.ss_flags & SS_DISABLE) && original.ss_size < MINSIGSTKSZ)
        original.ss_size = MINSIGSTKSZ;
#endif
    CHECK(sigaltstack(&original, NULL) == 0);
    free(before.ss_sp);
#endif
    return NULL;
}
static void host_stack(void) {
    worker_join(worker_start(alternate_stack_worker, NULL));
}
static void loading_thread(void) {
    worker_join(worker_start_with_stack(library_worker, NULL, 512 * 1024));
}
