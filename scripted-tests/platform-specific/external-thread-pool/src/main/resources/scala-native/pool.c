// A fixed pool of threads which the Scala Native runtime did not create.
#ifndef _WIN32
#include <pthread.h>
#include <stdlib.h>

typedef void (*TaskFn)(void *);

typedef struct Task {
    TaskFn fn;
    void *context;
    struct Task *next;
} Task;

typedef struct Pool {
    pthread_mutex_t lock;
    pthread_cond_t wake;
    Task *head;
    Task *tail;
    int stopping;
    int size;
    pthread_t *threads;
} Pool;

static void *worker(void *raw) {
    Pool *pool = raw;
    for (;;) {
        pthread_mutex_lock(&pool->lock);
        while (pool->head == NULL && !pool->stopping)
            pthread_cond_wait(&pool->wake, &pool->lock);
        Task *task = pool->head;
        if (task == NULL) {
            pthread_mutex_unlock(&pool->lock);
            return NULL;
        }
        pool->head = task->next;
        if (pool->head == NULL)
            pool->tail = NULL;
        pthread_mutex_unlock(&pool->lock);
        task->fn(task->context);
        free(task);
    }
}

Pool *test_pool_create(int size) {
    Pool *pool = calloc(1, sizeof(Pool));
    if (pool == NULL)
        abort();
    pthread_mutex_init(&pool->lock, NULL);
    pthread_cond_init(&pool->wake, NULL);
    pool->size = size;
    pool->threads = calloc((size_t)size, sizeof(pthread_t));
    if (pool->threads == NULL)
        abort();
    for (int i = 0; i < size; i++)
        if (pthread_create(&pool->threads[i], NULL, worker, pool) != 0)
            abort();
    return pool;
}

void test_pool_submit(Pool *pool, TaskFn fn, void *context) {
    Task *task = malloc(sizeof(Task));
    if (task == NULL)
        abort();
    task->fn = fn;
    task->context = context;
    task->next = NULL;
    pthread_mutex_lock(&pool->lock);
    if (pool->tail == NULL)
        pool->head = task;
    else
        pool->tail->next = task;
    pool->tail = task;
    pthread_cond_signal(&pool->wake);
    pthread_mutex_unlock(&pool->lock);
}

// Runs the queued tasks, then waits for every worker to exit.
void test_pool_destroy(Pool *pool) {
    pthread_mutex_lock(&pool->lock);
    pool->stopping = 1;
    pthread_cond_broadcast(&pool->wake);
    pthread_mutex_unlock(&pool->lock);
    for (int i = 0; i < pool->size; i++)
        pthread_join(pool->threads[i], NULL);
    pthread_mutex_destroy(&pool->lock);
    pthread_cond_destroy(&pool->wake);
    free(pool->threads);
    free(pool);
}
#endif
