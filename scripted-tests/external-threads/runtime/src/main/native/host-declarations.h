#ifndef EXTERNAL_HOST_DECLARATIONS_H
#define EXTERNAL_HOST_DECLARATIONS_H
#ifdef __cplusplus
extern "C" {
#endif
int ScalaNativeInit(void);
void external_test_track_identity(void);
void external_test_check_reclaimed(int);
void external_altstack_race(void (*ready)(void), int (*done)(void));
void external_bootstrap_race(void);
void external_bootstrap_failure(int);
int external_collect_on_unguarded_stack(void);
int64_t external_identity(void);
void external_local_set(int64_t);
int64_t external_local_get(void);
void external_collect(void);
void external_throw(void);
void external_exception_check(void);
void external_exception_reclaimed(void);
void external_nested_throw(void);
void external_scala_worker_identity(void);
void external_virtual_worker_identity(int, int);
int64_t external_pressure(int, int);
void external_object_new(void **);
int external_object_check(void *);
void *external_function_pointer(void);
void *external_boxed_function_pointer(void);
int external_cold_get(void);
void external_cold_set(int);
int64_t external_nested(int64_t (*)(void));
int64_t external_nested_plain(int64_t (*)(void));
int external_exclusive(void);
int64_t external_get_value(void);
void external_set_value(int64_t);
void scalanative_GC_add_roots(void *, void *);
void scalanative_GC_remove_roots(void *, void *);
#ifdef __cplusplus
}
#endif
#endif
