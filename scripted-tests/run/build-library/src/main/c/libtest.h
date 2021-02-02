#include <stdio.h>
#include <stdlib.h>

int ScalaNativeInit(); // needs to be called before first SN heap allocation (GC)

struct Foo {
	short arg1;
	int arg2;
	long arg3;
	double arg4;
	char* arg5;
};

int counter;

void sayHello(void);
long addLongs(long l, long r);
struct Foo* retStructPtr(void);
void updateStruct(struct Foo* p);
