/* Internal compiler/runtime entry protocol. Hosts call guarded Scala exports.
 */
#ifndef SCALANATIVE_EXTERNAL_THREADS_H
#define SCALANATIVE_EXTERNAL_THREADS_H
#include <stdint.h>
#ifdef __cplusplus
extern "C" {
#endif
typedef uintptr_t scalanative_thread_scope_t;
enum {
    SCALANATIVE_THREAD_SUCCESS = 0,
    SCALANATIVE_THREAD_NOT_INITIALIZED,
    SCALANATIVE_THREAD_INVALID_OPERATION,
    SCALANATIVE_THREAD_UNSUPPORTED_STACK,
    SCALANATIVE_THREAD_RESOURCE_FAILURE
};
scalanative_thread_scope_t scalanative_enter_managed_automatic(void);
void scalanative_leave_managed(scalanative_thread_scope_t scope);
#ifdef __cplusplus
}
#endif
#endif
