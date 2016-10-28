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