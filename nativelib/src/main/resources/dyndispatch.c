#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "methodtable.h"

void* scalanative_dyndispatch(MethodTable* methodTable, char* sign, int sign_length) {
	int nbMethods = methodTable->nbMethods;

	Method* methods = methodTable->methods;

	for(int i = 0; i < nbMethods; i++) {
		char* methSign = methods[i].signature;
		if(strcmp(methSign, sign) == 0) {
			return &methods[i].funcPtr;
		}
	}

	return NULL;
}



