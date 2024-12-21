#include <stdio.h>
#include <stdlib.h>
#include <exception>

namespace scalanative {
class ExceptionWrapper : std::exception {};
} // namespace scalanative

struct Foo {
    short arg1;
    int arg2;
    long arg3;
    double arg4;
    char *arg5;
};

extern "C" {
short native_number();
void native_set_number(short);
const char *native_constant_string();
void sayHello(void);
long add_longs(long l, long r);
struct Foo *retStructPtr(void);
void updateStruct(struct Foo *p);
void handledException();
void sn_runGC(void);
}
