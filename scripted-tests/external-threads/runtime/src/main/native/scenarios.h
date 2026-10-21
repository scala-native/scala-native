#include "embedding.h"
#ifndef EXTERNAL_SHARED_LIBRARY
#include "lifecycle.h"
#include "callbacks.h"
#include "gc.h"
#include "ownership.h"
static void bootstrap_failure_before(void) { external_bootstrap_failure(0); }
static void bootstrap_failure_after(void) { external_bootstrap_failure(1); }
#endif
static const scenario scenarios[] = {
    {"LIB-01", loading_thread},
    {"LIB-02", host_stack},
#ifndef EXTERNAL_SHARED_LIBRARY
    {"INIT-02", automatic_before_init},
    {"ID-01", identities},
    {"ID-02", handoff},
    {"EXIT-02", idle_exit},
    {"EXIT-03", abandoned_scopes},
    {"EXIT-05", deferred_retirement},
    {"CALL-01", entry_wrappers},
    {"CALL-02", nested_state},
    {"CALL-03", exception_lifetime},
    {"CALL-04", virtual_identity},
    {"GC-01", live_references},
    {"GC-02", explicit_roots},
    {"GC-03", idle_collection},
    {"GC-04", attachment_race},
    {"OWN-01", ownership},
    {"BOOT-01", external_bootstrap_race},
    {"BOOT-02", bootstrap_failure_before},
    {"BOOT-03", bootstrap_failure_after},
#endif
};
