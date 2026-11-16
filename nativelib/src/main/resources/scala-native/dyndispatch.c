#include "gc/immix_commix/headers/ObjectHeader.h"
#include <stddef.h>
#include <stdint.h>

/* Reflective proxies of the program, keep in sync with
 * codegen.ReflectiveProxyTable: an open-addressing table, at most half full,
 * of {class rtti, signature key, proxy} entries. A NULL rtti marks an empty
 * slot. */
typedef struct {
    Rtti *rtti;
    int32_t key;
    void *proxy;
} ReflectiveProxy;

typedef struct {
    int32_t mask;
    ReflectiveProxy *entries;
} ReflectiveProxyTable;

static const ReflectiveProxyTable *reflectiveProxies;

/* Called by the program at startup, before any Scala code runs. */
void scalanative_register_reflective_proxies(
    const ReflectiveProxyTable *table) {
    reflectiveProxies = table;
}

/* Proxy for the reflectively called signature `key` on an instance of
 * `rtti`, or NULL when the class has none. */
void *scalanative_dynmethod(Rtti *rtti, int32_t key) {
    const ReflectiveProxyTable *table = reflectiveProxies;
    if (table == NULL)
        return NULL;
    uint32_t classKey = (uint32_t)rtti->rt.flags >> RTTI_CLASS_KEY_SHIFT;
    uint32_t mask = (uint32_t)table->mask;
    uint32_t pos = (((uint32_t)key * 0x9e3779b1u) ^ classKey) & mask;
    for (uint32_t probes = 0; probes <= mask; probes++) {
        ReflectiveProxy *entry = &table->entries[pos];
        if (entry->rtti == rtti && entry->key == key)
            return entry->proxy;
        if (entry->rtti == NULL)
            return NULL;
        pos = (pos + 1) & mask;
    }
    return NULL;
}
