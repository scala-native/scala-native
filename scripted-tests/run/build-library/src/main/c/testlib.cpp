#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <assert.h>
#include <string.h>
#include "libtest.hpp"

int main() {
    assert(ScalaNativeInit() == 0);

    sayHello();

    assert(addLongs(123456789L, 876543210L) == 999999999L);

    struct Foo *p = retStructPtr();
    assert(p != NULL);
    assert(p->arg1 == 42);
    assert(p->arg2 == 2020);
    assert(p->arg3 == 27);
    assert(p->arg4 == 14.4556);
    assert(strcmp(p->arg5, "ScalaNativeRocks!") == 0);

    updateStruct(p);
    assert(p != NULL);
    assert(p->arg1 == 42);
    assert(p->arg2 == 2021);
    assert(p->arg3 == 27);
    assert(p->arg4 == 14.4556);
    assert(strcmp(p->arg5, "ScalaNativeRocks!") == 0);
    free(p);

    sn_runGC();

    bool exceptionCaught = false;
    try {
        fail();
        // TODO this fails to catch right now. There must be some mismatch between the specification of
        //   scalanative::ExceptionWrapper in eh.cpp and the one in libtest.hpp
//    } catch (const scalanative::ExceptionWrapper &e) {
    } catch (const std::exception &e) {
        exceptionCaught = true;
    }
    assert(exceptionCaught);

    return 0;
}
