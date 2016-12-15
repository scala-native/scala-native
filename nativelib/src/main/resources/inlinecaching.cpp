#include <stdlib.h>
#include <stdio.h>
#include <string>
#include "CountingMap.hpp"
#include "gc.h"

typedef struct tpe
{
    int id;
    void* name;
} tpe;

typedef struct chararray
{
    tpe* u;
    int length;
    int unused;
    short chars[];
} chararray;

typedef struct jstring
{
    tpe* u;
    int cachedHashCode;
    int count;
    int offset;
    chararray* value;
} jstring;

char* to_string(jstring* str) {
    size_t length = str->count;
    char* cs = (char*) scalanative_alloc(NULL, (length + 1) * sizeof(char));

    for (int i = 0; i < length; ++i) {
        cs[i] = (char) str->value->chars[i];
    }
    cs[length] = '\0';

    return cs;
}

CountingMap method_calls;

void method_call_dump(FILE* out) {
    method_calls.print(out);
}

extern "C" {

    void method_call_log(int callee_t, jstring* method_name) {
        string m(to_string(method_name));
        method_calls.insert_occ(m, callee_t);
    }

    void method_call_dump_file(jstring* file_name) {
        FILE* file = fopen(to_string(file_name), "w");
        if (file == NULL) {
            fprintf(stderr, "Couldn't open file %s for writing.\n", to_string(file_name));
            exit(1);
        }
        method_call_dump(file);

        fclose(file);
    }

    void method_call_dump_console() {
        method_call_dump(stdout);
    }

}