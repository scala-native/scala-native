#include <stdio.h>
#include <stdlib.h>

struct Foo {
    short arg1;
    int arg2;
    long arg3;
    double arg4;
    char *arg5;
};

short native_number();
void native_set_number(short);
const char *native_constant_string();
void sayHello(void);
long add_longs(long l, long r);
struct Foo *retStructPtr(void);
void updateStruct(struct Foo *p);
void sn_runGC(void);
