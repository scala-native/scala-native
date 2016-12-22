#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "perfecthashmap.h"

int mod(int a, int b);
int hash(char* signature, int sign_length, int d);

void* scalanative_dyndispatch(PerfectHashMap* perfectHashMap, char* sign, int sign_length) {
	int size = perfectHashMap->size;
	int lh1 = mod(hash(sign, sign_length, 0), size);


	int h1 = mod(lh1, size);
	int d = perfectHashMap->keys[h1];


	if(d < 0) {
		return &(perfectHashMap->values[- d - 1]);
	} else {
		int h2 = mod(hash(sign, sign_length, d), size);
		return &(perfectHashMap->values[h2]);
	}
}

int hash(char* buf, int len, int seed) {
    for (int i = 0; i < len; i++) {
      seed ^= buf[i];
      seed += (seed << 1) + (seed << 4) + (seed << 5) + (seed << 7) + (seed << 8) + (seed << 25);
    }
    return seed;
}

int mod(int a, int b) {
	int m = a % b;
	if(m < 0) {
		return m + b;
	} else {
		return m;
	}
}



