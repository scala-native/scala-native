#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "perfecthashmap.h"

int mod(int a, int b);
int hash(int key, int d);

void* scalanative_dyndispatch(PerfectHashMap* perfectHashMap, int key) {
	int size = perfectHashMap->size;
	int lh1 = mod(hash(key, 0), size);


	int h1 = mod(lh1, size);
	int salt = perfectHashMap->salts[h1];


	if(salt < 0) {
	    int index = - salt - 1;
	    if(perfectHashMap->keys[index] == key) {
		    return &(perfectHashMap->values[index]);
	    } else {
	        return NULL;
	    }
	} else {
		int index = mod(hash(key, salt), size);
		if(perfectHashMap->keys[index] == key) {
            return &(perfectHashMap->values[index]);
        } else {
            return NULL;
        }
	}
}

inline int hash(int key, int salt) {
    return (key + (salt * 31)) ^ salt;
}

inline int mod(int a, int b) {
	int m = a % b;
	if(m < 0) {
		return m + b;
	} else {
		return m;
	}
}



