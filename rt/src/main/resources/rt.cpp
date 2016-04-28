#include <stdio.h>
#include <assert.h>
#include <exception>
#include <gc.h>

namespace scalanative {
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
        return ((scalanative::ExceptionWrapper*) wrapper)->obj;
    }

    void scalanative_end_catch() {
    }

    void* scalanative_alloc(void* info, size_t size) {
        void** alloc = (void**) GC_malloc(size);
        *alloc = info;
        return (void*) alloc;
    }
}

