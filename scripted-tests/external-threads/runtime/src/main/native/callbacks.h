static void *raw_callback(void *unused) {
    (void)unused;
    CHECK(external_pressure(iterations, pressure_bytes) == 18);
    return NULL;
}
static int64_t nested_identity(void) { return external_identity(); }
static void virtual_identity(void) {
#ifdef EXTERNAL_VIRTUAL_THREAD_SUPPORTED
    CHECK(multithreaded);
    int64_t id = external_identity();
    external_local_set(117);
    external_virtual_worker_identity(iterations, workers);
    CHECK(external_identity() == id && external_local_get() == 117);
    milestone("virtual-callbacks-complete");

#else
    fprintf(stderr, "CALL-07 requires Scala 3 and JDK 21+\n");
    exit(64);
#endif
}
#ifdef __cplusplus
static void *check_released_exception(void *unused) {
    (void)unused;
    external_exception_reclaimed();
    return NULL;
}
#endif
static void exception_lifetime(void) {
#ifdef __cplusplus

    bool caught = false;
    try {
        external_throw();
    } catch (...) {
        caught = true;
        milestone("callback-unwound");
        /* The native catch owns the exception while another OS thread collects.
         */
        worker_join(worker_start(raw_callback, NULL));
        external_exception_check();
        callback_pressure();
        external_exception_check();
        milestone("exception-retained");
    }
    CHECK(caught);
#ifdef EXTERNAL_COLLECTING_GC
    /* Exclude stale pointers on the collecting host's suspended native stack.
     */

    worker_join(worker_start(check_released_exception, NULL));
#endif
    external_nested_throw();
    milestone("scala-caught-callback");
#ifdef EXTERNAL_CPP_EXCEPTIONS
    std::exception_ptr retained;
    try {
        external_throw();
    } catch (...) {
        retained = std::current_exception();
    }
    CHECK(retained != nullptr);
    worker_join(worker_start(raw_callback, NULL));
    external_exception_check();
    /* Destruction on an unattached OS thread must execute only native cleanup.
     */
    std::thread release([&retained] { retained = nullptr; });
    release.join();
#ifdef EXTERNAL_COLLECTING_GC
    /* Exclude stale pointers on the collecting host's suspended native stack.
     */

    worker_join(worker_start(check_released_exception, NULL));
#endif
    milestone("retained-exception-released");
#endif
    /* An escaped Scala exception must restore the automatic scope as well. */
    callback_pressure();

#else
    fprintf(stderr, "CALL-05 requires the C++ host\n");
    exit(64);
#endif
}
static void *entry_wrapper_worker(void *raw) {
    int (*fn)(int) = (int (*)(int))raw;
    CHECK(external_cold_get() == 41);
    external_cold_set(43);
    CHECK(external_cold_get() == 43);
    for (int i = 0; i < iterations; ++i)
        CHECK(fn(i) == i + 1);
#ifdef __cplusplus
    bool caught = false;
    try {
        (void)fn(-1);
    } catch (...) {
        caught = true;
    }
    CHECK(caught);
    CHECK(fn(47) == 48);
    milestone("boxed-callback-unwound");
#endif

    milestone("entry-wrappers-complete");
    return NULL;
}
static int64_t nested_pressure(void) {
    callback_pressure();
    return callback_identity();
}

static void entry_wrappers(void) {
#ifdef __cplusplus
    std::thread cpp_worker(
        [] { CHECK(external_pressure(iterations, pressure_bytes) == 18); });
    cpp_worker.join();
#endif
    worker_join(worker_start(raw_callback, NULL));
    int64_t (*fn)(void) = (int64_t (*)(void))external_function_pointer();
    CHECK(fn && fn() == external_identity());
    external_set_value(51);
    CHECK(external_get_value() == 51);
    void *boxed = external_boxed_function_pointer();
    CHECK(boxed);

    worker_join(worker_start(entry_wrapper_worker, boxed));
    callback_pressure();
}
static void nested_state(void) {
    int64_t id = external_identity();
    external_local_set(53);

    callback_pressure();

    CHECK(external_nested(nested_identity) == id);
    CHECK(external_nested(nested_pressure) == id);
    CHECK(external_nested_plain(nested_pressure) == id);
    CHECK(external_identity() == id && external_local_get() == 53);

    if (multithreaded) {
        worker_join(worker_start(raw_callback, NULL));
        external_scala_worker_identity();
    }
}
