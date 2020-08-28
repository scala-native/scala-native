#include <stdio.h>

namespace __scalanative {
class Hook {
    void (*hook)(void) = nullptr;
    Hook() {}
    ~Hook() {
        if (hook)
            hook();
    }

  public:
    static Hook &global() {
        static Hook h;
        return h;
    }
    void setHook(void (*h)(void)) {
        if (!hook) {
            hook = h;
        } else {
            fprintf(stderr, "Tried to set global hook multiple times\n");
        }
    }
};
} // namespace __scalanative

extern "C" {
void scalanative_native_shutdown_init(void (*h)(void)) {
    __scalanative::Hook::global().setHook(h);
}
}
