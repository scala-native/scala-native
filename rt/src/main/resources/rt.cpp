#include <stdio.h>
#include <assert.h>
#include <exception>
#include <gc.h>

namespace scalanative {
    struct Exc {
        void*   wrapper;
        int32_t id;
    }

    class ExceptionWrapper: public std::exception {
    public:
        ExceptionWrapper(void* _obj): obj(_obj) { }
        void* obj;
    };
}

extern "C" {
    void scalanative_throw(void* obj) {
        throw new scalanative::ExceptionWrapper(obj);
    }

    void* scalanative_begin_catch(void* wrapper) {
        __cxa_begin_catch(wrapper);
        return ((scanative::ExceptionWrapper*) wrapper)->obj;
    }

    void scalanative_end_catch() {
        __cxa_end_catch();
    }

    void* scalanative_alloc(void* info, size_t size) {
        void** alloc = (void**) GC_malloc(size);
        *alloc = info;
        return (void*) alloc;
    }

    int main() {
        try {
            scalanative_throw((void*) 42);
        } catch (scalanative::ExceptionWrapper* e) {
            long value = (long) scalanative_begin_catch(e);
            printf("%ld", value);
            scalanative_end_catch();
        }
        return 0;
    }
}

