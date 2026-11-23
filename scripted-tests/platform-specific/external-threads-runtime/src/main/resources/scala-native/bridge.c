#include <stdint.h>
int64_t external_bridge_blocking(int64_t (*fn)(void)) { return fn(); }
int64_t external_bridge_plain(int64_t (*fn)(void)) { return fn(); }
void external_bridge_blocking_void(void (*fn)(void)) { fn(); }
