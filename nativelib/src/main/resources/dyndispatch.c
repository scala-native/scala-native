#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "methodtable.h"

int hash(char* signature, int sign_length, int d);

void* scalanative_dyndispatch(MethodTable* methodTable, char* sign, int sign_length) {
	//printf("akjsa");
	//fflush(stdout);
	int tableSize = methodTable->size;
	//printf("size: %d\n", tableSize);
	//fflush(stdout);
	int h1 = hash(sign, sign_length, 0) % tableSize;
	int d = methodTable->keys[h1];

	//printf("h1 %d", h1);
	//printf("d %d", d);
	fflush(stdout);

	if(d < 0) {
		return &(methodTable->ptrs[- d - 1]);
	} else {
		int h2 = hash(sign, sign_length, d) % tableSize;
		return &(methodTable->ptrs[h2]);
	}
}

int hash(char* signature, int sign_length, int d) {
	int h = d;

	for(int i = 0; i < sign_length; i++) {
		h = 7 * h + (int) signature[i];
	}

	return h & 0x7fffffff;
}



