#include <stdio.h>
#include <stdlib.h>
#include <exception>

extern "C" int ScalaNativeInit(); // needs to be called before first SN heap allocation (GC)

namespace scalanative{
	class ExceptionWrapper : std::exception {};
}

struct Foo {
	short arg1;
	int arg2;
	long arg3;
	double arg4;
	char* arg5;
};

int counter;

extern "C" void sayHello(void);
extern "C" long addLongs(long l, long r);
extern "C" struct Foo* retStructPtr(void);
extern "C" void updateStruct(struct Foo* p);
extern "C" void fail(void);
