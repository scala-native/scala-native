#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "methodtable.h"

int mod(long a, int b);
long hash(char* signature, int sign_length, long d);

void* scalanative_dyndispatch(MethodTable* methodTable, char* sign, int sign_length) {
	long tableSize = (long) methodTable->size;
	printf("size: %ld\n", tableSize);
	fflush(stdout);
	long lh1 = mod(hash(sign, sign_length, 0), tableSize);

	printf("lh1 %ld\n", lh1);
	fflush(stdout);

	int h1 = mod(lh1, tableSize);
	printf("h1 %d\n", h1);
	fflush(stdout);
	long d = (long) methodTable->keys[h1];

	printf("d %ld\n", d);
	fflush(stdout);

	if(d < 0) {
		return &(methodTable->ptrs[- d - 1]);
	} else {
		int h2 = mod(hash(sign, sign_length, d), tableSize);
		return &(methodTable->ptrs[h2]);
	}
}

/*int hash(char* signature, int sign_length, int d) {
	int h = d;

	for(int i = 0; i < sign_length; i++) {
		h = 7 * h + (int) signature[i];
	}

	return h & 0x7fffffff;
}*/

long hash(char* buf, int len, long seed) {
    for (int i = 0; i < len; i++) {
      seed ^= buf[i];
      seed += (seed << 1) + (seed << 4) + (seed << 7) + (seed << 8) + (seed << 24);
    }
    return seed; //& 0x7fffffffffffffff;
}

int mod(long a, int b) {
	int m = a % b;
	if(m < 0) {
		return m + b;
	} else {
		return m;
	}
}



