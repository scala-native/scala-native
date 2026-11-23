static void *exclusive_worker(void *unused) {
    (void)unused;
    for (int i = 0; i < iterations; ++i)
        CHECK(external_exclusive() == 1);
    return NULL;
}
static void ownership(void) {
    if (multithreaded)
        return;
    host_thread_t *threads =
        (host_thread_t *)calloc((size_t)workers, sizeof(*threads));
    CHECK(threads);
    for (int i = 0; i < workers; ++i)
        threads[i] = worker_start(exclusive_worker, NULL);
    for (int i = 0; i < workers; ++i)
        worker_join(threads[i]);
    free(threads);
}
