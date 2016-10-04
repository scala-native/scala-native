#include <stdlib.h>
#include <stdio.h>
#include <string.h>

typedef struct node {
    char* value;
    node* next;
} node;

typedef struct linkedmap {
    char* key;
    node* head;
    linkedmap* next;
} linkedmap;

node* node_init(char* value) {
    node* n = (node*) malloc(sizeof(node));

    if (n == NULL)
        return NULL;

    char* value_field = (char*) calloc(strlen(value), sizeof(char));
    strcpy(value_field, value);

    n->value = value_field;
    n->next = NULL;

    return n;
}

linkedmap* linkedmap_init(char* key, char* value) {
    node* n = node_init(value);

    linkedmap* map = (linkedmap*) malloc(sizeof(linkedmap));

    if (map == NULL)
        return NULL;

    char* key_field = (char*) calloc(strlen(key), sizeof(char));
    strcpy(key_field, key);

    map->key = key_field;
    map->head = n;
    map->next = NULL;

    return map;
}

void node_insert(node* n, char* value) {
    node* prev = n;
    while (prev->next != NULL)
        prev = prev->next;

    node* new_node = node_init(value);
    prev->next = new_node;
}

bool node_contains(node* node, char* value) {
    if (node == NULL)
        return false;
    else if (strcmp(node->value, value) == 0)
        return true;
    else
        return node_contains(node->next, value);
}

bool linkedmap_contains(linkedmap* map, char* key) {
    if (map == NULL)
        return false;
    else if (strcmp(map->key, key) == 0)
        return true;
    else
        return linkedmap_contains(map->next, key);
}

node* linkedmap_get(linkedmap* map, char* key) {
    if (map == NULL)
        return NULL;
    else if (strcmp(map->key, key) == 0)
        return map->head;
    else
        return linkedmap_get(map->next, key);
}

bool linkedmap_contains_pair(linkedmap* map, char* key, char* value) {
    node* n = linkedmap_get(map, key);

    if (n == NULL)
        return false;
    else
        return node_contains(n, value);
}

void linkedmap_insert(linkedmap* map, char* key, char* value) {
    if (linkedmap_contains(map, key)) {
        node* n = linkedmap_get(map, key);

        if (!node_contains(n, value)) {
            node_insert(n, value);
        }
    } else {
        linkedmap* new_node = linkedmap_init(key, value);

        while (map->next != NULL)
            map = map->next;

        map->next = new_node;
    }
}

void node_print(node* n, FILE* out) {
    while (n != NULL) {
        fprintf(out, "    %s\n", n->value);
        n = n->next;
    }
}

void linkedmap_print(linkedmap* map, FILE* out) {
    while (map != NULL) {
        fprintf(out, "Key = %s:\n", map->key);
        node_print(map->head, out);
        fprintf(out, "\n");
        map = map->next;
    }
}

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
    char* cs = (char*) calloc(length + 1, sizeof(char));
    for (int i = 0; i < length; ++i) {
        cs[i] = (char) str->value->chars[i];
    }
    cs[length] = '\0';

    return cs;
}

linkedmap* method_calls = NULL;

void method_call_dump(FILE* out) {
    linkedmap_print(method_calls, out);
}

extern "C" {
    void method_call_log(jstring* callee_t, jstring* method_name) {
        char* c = to_string(callee_t);
        char* m = to_string(method_name);
        if (method_calls == NULL) {
            method_calls = linkedmap_init(m, c);
        } else {
            if (!linkedmap_contains_pair(method_calls, m, c)) {
                linkedmap_insert(method_calls, m, c);
            }
        }
    }

    void method_call_dump_file(jstring* file_name) {
        FILE* file = fopen(to_string(file_name), "w");
        if (file == NULL) {
            fprintf(stderr, "Couldn't open file %s for writing.\n", to_string(file_name));
            exit(1);
        }
        method_call_dump(file);
    }

    void method_call_dump_console() {
        method_call_dump(stdout);
    }


}