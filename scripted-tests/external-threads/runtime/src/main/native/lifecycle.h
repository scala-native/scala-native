typedef struct identity_arg {
    int64_t id;
    int64_t value;
} identity_arg;
static void *identity_worker(void *raw) {
    identity_arg *arg = (identity_arg *)raw;

    CHECK(external_local_get() == -1);
    arg->id = external_identity();
    external_local_set(arg->value);

    callback_pressure();

    CHECK(external_identity() == arg->id);
    CHECK(external_local_get() == arg->value);

    return NULL;
}
static int64_t abandon_callback(void) {
    milestone("abandoning-live-scope");
    worker_exit();
    return 0;
}
static void *exit_abandoned(void *unused) {
    (void)unused;
    external_nested(abandon_callback);
    return NULL;
}
static void abandoned_scopes(void) {
    worker_join(worker_start(exit_abandoned, NULL));
    CHECK(0); /* Must terminate, never silently transfer live frames. */
}
static int64_t reclaimed_identity;
static void *reclaim_worker(void *raw) {
    (void)raw;

    reclaimed_identity = external_identity();
#ifdef EXTERNAL_RECLAIMING_GC
    external_test_track_identity();
#endif

    return NULL;
}
static void reclaim_cycles(void) {
    int64_t previous = -1;
    for (int i = 0; i < iterations; ++i) {
        worker_join(worker_start(reclaim_worker, NULL));
        CHECK(reclaimed_identity != previous);
        previous = reclaimed_identity;
#ifdef EXTERNAL_RECLAIMING_GC
        external_test_check_reclaimed(1);
#else
        callback_collect();
#endif
    }
}
static event retired_ready = EVENT_INIT;
static event retired_exit = EVENT_INIT;
static void *retired_worker(void *unused) {
    (void)unused;
    external_test_track_identity();
    event_set(&retired_ready);
    event_wait(&retired_exit);
    return NULL;
}
static host_thread_t retiring_worker;
static int64_t join_retiring_worker(void) {
    event_set(&retired_exit);
    worker_join(retiring_worker);
    milestone("identity-retired");
    return 0;
}
static void deferred_retirement(void) {
    CHECK(!multithreaded);
    for (int i = 0; i < iterations; ++i) {
        event_reset(&retired_ready);
        event_reset(&retired_exit);
        retiring_worker = worker_start(retired_worker, NULL);
        event_wait(&retired_ready);
        CHECK(external_nested(join_retiring_worker) == 0);
        external_test_check_reclaimed(1);
    }
    event_close(&retired_ready);
    event_close(&retired_exit);

    milestone("deferred-resources-reclaimed");
}

static void automatic_before_init(void) {
    milestone("entry-before-init");
    external_identity();
    CHECK(0);
}
static void identities(void) {
    int64_t coordinator = external_identity();
    external_local_set(71);
    identity_arg *args = (identity_arg *)calloc((size_t)workers, sizeof(*args));
    host_thread_t *threads =
        (host_thread_t *)calloc((size_t)workers, sizeof(*threads));
    CHECK(args && threads);
    for (int i = 0; i < workers; ++i) {
        args[i].value = i + 100;
        threads[i] = worker_start(identity_worker, &args[i]);
    }
    for (int i = 0; i < workers; ++i)
        worker_join(threads[i]);
    for (int i = 0; i < workers; ++i) {
        CHECK(args[i].id != coordinator);
        for (int j = 0; j < i; ++j)
            CHECK(args[i].id != args[j].id);
    }
    callback_collect();
    CHECK(external_identity() == coordinator && external_local_get() == 71);
    free(args);
    free(threads);
}
static void *handoff_worker(void *raw) {
    CHECK(external_get_value() == 117);
    identity_worker(raw);
    external_set_value(119);
    return NULL;
}
static void handoff(void) {
    int64_t a = external_identity();
    external_local_set(73);
    external_set_value(117);
    identity_arg b = {0, 74};
    worker_join(worker_start(handoff_worker, &b));
    CHECK(external_identity() == a && a != b.id);
    CHECK(external_local_get() == 73 && external_get_value() == 119);
}
static void idle_exit(void) { reclaim_cycles(); }
