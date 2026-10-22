static void explicit_roots(void) {
    void **root = (void **)calloc(4, sizeof(void *));
    CHECK(root);

    scalanative_GC_add_roots(root, root + 1);
    scalanative_GC_add_roots(root + 1, root + 2);
    scalanative_GC_add_roots(root + 3, root + 4);
    scalanative_GC_add_roots(root + 1, root + 2); /* duplicate registration */
    external_object_new(root);
    external_object_new(root + 1);
    external_object_new(root + 3);

    callback_pressure();

    CHECK(external_object_check(root[0]) == 42);
    CHECK(external_object_check(root[1]) == 42);
    root[0] = root[1] = NULL;
    /* Remove consecutive/duplicate list entries while retaining another range.
     */
    scalanative_GC_remove_roots(root, root + 2);
    external_collect();
    CHECK(external_object_check(root[3]) == 42);
    root[3] = NULL;
    scalanative_GC_remove_roots(root + 3, root + 4);

    free(root);
    callback_collect();
}
static event idle_ready = EVENT_INIT;
static event idle_done = EVENT_INIT;
static void *idle_worker(void *unused) {
    (void)unused;
    (void)callback_identity();
    milestone("worker-idle");
    event_set(&idle_ready);
    event_wait(&idle_done);
    return NULL;
}
static void idle_collection(void) {
    host_thread_t worker = worker_start(idle_worker, NULL);
    event_wait(&idle_ready);
    callback_collect();
    milestone("collection-complete");
    event_set(&idle_done);
    worker_join(worker);
}
static void *racing_worker(void *unused) {
    (void)unused;
    for (int i = 0; i < iterations; ++i) {
        callback_pressure();
    }
    return NULL;
}
static void attachment_race(void) {
    host_thread_t *threads =
        (host_thread_t *)calloc((size_t)workers, sizeof(*threads));
    CHECK(threads);
    for (int i = 0; i < workers; ++i)
        threads[i] = worker_start(racing_worker, NULL);
    for (int i = 0; i < iterations; ++i)
        callback_collect();
    for (int i = 0; i < workers; ++i)
        worker_join(threads[i]);
    free(threads);
}

static void live_references(void) {
    for (int i = 0; i < iterations; ++i) {
        callback_pressure();
        // The next owner must scan its own stack, then restore this owner's
        // stack.
        worker_join(worker_start(racing_worker, NULL));
        callback_pressure();
    }
}
