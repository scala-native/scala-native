#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <assert.h>
#include <string.h>
#include "libtest.h"

int main() {
    fprintf(stderr, "xxx1\n");
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

    fprintf(stderr, "%p\n", (void *)p);

    updateStruct(p);


    assert(p != NULL);
    assert(p->arg1 == 42);
    assert(p->arg2 == 2021);
    assert(p->arg3 == 27);
    assert(p->arg4 == 14.4556);
    assert(strcmp(p->arg5, "ScalaNativeRocks!") == 0);

    free(p);

    sn_runGC();

    return 0;
}