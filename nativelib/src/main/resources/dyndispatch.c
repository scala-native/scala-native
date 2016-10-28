#include <stdlib.h>
#include <stdio.h>


void* scalanative_dyndispatch(void** ty, char* sign, int sign_length) {
	printf("dispatch %s\n", sign);
	return *ty;
}