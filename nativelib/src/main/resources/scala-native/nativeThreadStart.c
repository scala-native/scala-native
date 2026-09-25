#ifdef SCALANATIVE_MULTITHREADING_ENABLED

typedef void *(*scalanative_thread_start_t)(void *);

extern void *scalanative_NativeThread_start(void *);

/* Address of the Scala thread entry.
Returned as data so Scala can pass it to pthread_create / CreateThread without
building a CFuncPtr closure.
Basically &scalanative_NativeThread_start
*/
scalanative_thread_start_t scalanative_NativeThread_start_fn(void) {
    return scalanative_NativeThread_start;
}

#endif // SCALANATIVE_MULTITHREADING_ENABLED
