#include <stdio.h>

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

void print_jstring(jstring* str) {
    int length = str->count;
    char chars[length + 1];
    for (int i = 0; i < length; ++i) {
        chars[i] = (char) str->value->chars[i];
    }
    chars[length] = '\0';
    fprintf(stdout, "%s\n", chars);
}

extern "C" {
    void method_call_log(jstring* callee_t, jstring* method_name) {
        fprintf(stdout, "callee_t: ");
        print_jstring(callee_t);
        fprintf(stdout, "method_name: ");
        print_jstring(method_name);
        // int length = tid->count;
        // for (int i = 0; i < length; ++i) {
        //     fprintf(stdout, "[i = %d] c = %d\n", i, tid->value->chars[i]);
        // }
        // fprintf(stdout, "-------------------\n");
        // for (int i = 0; i < 100; ++i) {
        //     fprintf(stdout, "[i = %d] scala-native says something that has length '%d'.\n", i, (int) *(tid + i));
        // }
    }
}