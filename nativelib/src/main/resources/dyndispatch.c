#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "methodtable.h"

int mod(long a, int b);
long hash(char* signature, int sign_length, long d);

void* scalanative_dyndispatch(MethodTable* methodTable, char* sign, int sign_length) {
	long tableSize = (long) methodTable->size;
	long lh1 = mod(hash(sign, sign_length, 0), tableSize);

	int h1 = mod(lh1, tableSize);
	long d = (long) methodTable->keys[h1];


	if(d < 0) {
		return &(methodTable->ptrs[- d - 1]);
	} else {
		int h2 = mod(hash(sign, sign_length, d), tableSize);
		return &(methodTable->ptrs[h2]);
	}
}

long hash(char* buf, int len, long seed) {
    for (int i = 0; i < len; i++) {
      seed ^= buf[i];
      seed += (seed << 1) + (seed << 4) + (seed << 5) + (seed << 7) + (seed << 8)+ (seed << 40);
    }
    return seed;
}

int mod(long a, int b) {
	int m = a % b;
	if(m < 0) {
		return m + b;
	} else {
		return m;
	}
}



