#include <stdlib.h>
#include <stdio.h>
#include "jstring.c"

char* to_string_helper(jstring* str);

void* scalanative_dyndispatch(void* ty, void* sign, int sign_length) {
	//jstring* str = (jstring*) sign;
	//printf("dispatch %s\n", to_string_helper(sign));
	return ty;
}

char* to_string_helper(jstring* str) {
    size_t length = str->count;
    char* cs = (char*) scalanative_alloc(NULL, (length + 1) * sizeof(char));

    for (int i = 0; i < length; ++i) {
        cs[i] = (char) str->value->chars[i];
    }
    cs[length] = '\0';

    return cs;
}