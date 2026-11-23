// Foreign host program using Scala Native static library from its own threads,
// the same way as an iOS application would do.
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int ScalaNativeInit(void);
char *sn_transform(const char *input, int seed);
void sn_release(char *result);

#define THREADS 4
#define SEEDS 16
#define ITERATIONS 2000

static char *expected[SEEDS];

static int check(const char *who, int iteration) {
    int seed = iteration % SEEDS;
    char *result = sn_transform("input", seed);
    int failed = result == NULL || strcmp(result, expected[seed]) != 0;
    if (failed)
        fprintf(stderr, "%s: iteration %d, expected '%s', got '%s'\n", who,
                iteration, expected[seed], result ? result : "(null)");
    sn_release(result);
    return failed;
}

static void *worker(void *arg) {
    char name[32];
    snprintf(name, sizeof(name), "thread-%ld", (long)arg);
    long failures = 0;
    for (int i = 0; i < ITERATIONS; i++)
        failures += check(name, i);
    return (void *)failures;
}

int main(void) {
    if (ScalaNativeInit() != 0) {
        fprintf(stderr, "ScalaNativeInit failed\n");
        return 1;
    }
    for (int seed = 0; seed < SEEDS; seed++) {
        char *result = sn_transform("input", seed);
        expected[seed] = strdup(result);
        sn_release(result);
    }
    long failures = 0;
    pthread_t threads[THREADS];
    for (long i = 0; i < THREADS; i++) {
        if (pthread_create(&threads[i], NULL, worker, (void *)i) != 0) {
            perror("pthread_create");
            return 1;
        }
    }
    for (int i = 0; i < ITERATIONS; i++)
        failures += check("main", i);
    for (int i = 0; i < THREADS; i++) {
        void *result = NULL;
        pthread_join(threads[i], &result);
        failures += (long)result;
    }
    if (failures != 0) {
        fprintf(stderr, "%ld calls failed\n", failures);
        return 1;
    }
    printf("OK: %d calls from %d threads\n", (THREADS + 1) * ITERATIONS,
           THREADS + 1);
    return 0;
}
