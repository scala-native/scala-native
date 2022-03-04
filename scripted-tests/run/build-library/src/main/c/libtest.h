#include <stdio.h>
#include <stdlib.h>

struct Foo {
    short arg1;
    int arg2;
    long arg3;
    double arg4;
    char *arg5;
};

int ScalaNativeInit(); // needs to be called before first SN heap allocation
                       // (GC)
void sayHello(void);
long addLongs(long l, long r);
struct Foo *retStructPtr(void);
struct Foo *allocFoo(void);
void updateStruct(struct Foo *p);
void updateAgainStruct(struct Foo *p);
void sn_runGC(void);
