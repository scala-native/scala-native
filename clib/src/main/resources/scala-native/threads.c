// On windows, threads.h doesn't exist.

#ifdef __has_include
#if __has_include("threads.h")
#ifndef __STDC_NO_THREADS__
#include <threads.h>
#include <stdlib.h>

size_t scalanative_thrd_t_size = sizeof(thrd_t);
size_t scalanative_mtx_t_size = sizeof(mtx_t);
size_t scalanative_cnd_t_size = sizeof(cnd_t);
size_t scalanative_tss_t_size = sizeof(tss_t);
size_t scalanative_once_flag_size = sizeof(once_flag);

int scalanative_thrd_join(thrd_t *thr, int *res) {
    return thrd_join(*thr, res);
}

int scalanative_thrd_equal(thrd_t *lhs, thrd_t *rhs) {
    return thrd_equal(*lhs, *rhs);
}

void scalanative_thrd_current(thrd_t *current) {
    *current = thrd_current();
    return;
}

int scalanative_thrd_detach(thrd_t *thr) { return thrd_detach(*thr); }

void scalanative_once_flag_init(once_flag *flag) {
    once_flag f = ONCE_FLAG_INIT;
    *flag = f;
    return;
}

int scalanative_tss_dtor_iterations = TSS_DTOR_ITERATIONS;

void *scalanative_tss_get(tss_t *tss_key) { return tss_get(*tss_key); }
int scalanative_tss_set(tss_t *tss_id, void *val) {
    return tss_set(*tss_id, val);
}
void scalanative_tss_delete(tss_t *tss_id) { return tss_delete(*tss_id); }
#endif
#endif
#endif