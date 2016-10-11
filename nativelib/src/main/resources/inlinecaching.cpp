#include <stdlib.h>
#include <stdio.h>
#include <string.h>

typedef struct node {
    int value;
    node* next;
} node;

typedef struct linkedmap {
    char* key;
    node* head;
    linkedmap* next;
} linkedmap;

node* node_init(int value) {
    node* n = (node*) malloc(sizeof(node));

    if (n == NULL) {
        fprintf(stdout, "Couldn't init node.\n");
        exit(1);
    }

    n->value = value;
    n->next = NULL;

    return n;
}

linkedmap* linkedmap_init(char* key, int value) {
    node* n = node_init(value);

    linkedmap* map = (linkedmap*) malloc(sizeof(linkedmap));

    if (map == NULL) {
        fprintf(stdout, "Coudln't init linkedmap.\n");
        exit(1);
    }

    char* key_field = (char*) calloc(strlen(key), sizeof(char));

    if (key_field == NULL) {
        fprintf(stdout, "Couldn't init key field.\n");
        exit(1);
    }

    strcpy(key_field, key);

    map->key = key_field;
    map->head = n;
    map->next = NULL;

    return map;
}

void node_insert(node* n, int value) {
    node* prev = n;
    while (prev->next != NULL)
        prev = prev->next;

    node* new_node = node_init(value);
    prev->next = new_node;
}

bool node_contains(node* node, int value) {
    if (node == NULL)
        return false;
    else if (node->value == value)
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

bool linkedmap_contains_pair(linkedmap* map, char* key, int value) {
    node* n = linkedmap_get(map, key);

    if (n == NULL)
        return false;
    else
        return node_contains(n, value);
}

void linkedmap_insert(linkedmap* map, char* key, int value) {
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
        fprintf(out, "    %d\n", n->value);
        n = n->next;
    }
}

void linkedmap_print(linkedmap* map, FILE* out) {
    while (map != NULL) {
        fprintf(out, "= %s:\n", map->key);
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

    if (cs == NULL) {
        fprintf(stdout, "Couldn't alloc memory to convert to string.\n");
        exit(1);
    }

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

    int jstring_compare(jstring* str0, jstring* str1, jstring* comment) {
        char* c0 = to_string(comment);
        char* s0 = to_string(str0);
        char* s1 = to_string(str1);
        int res = strcmp(s0, s1) == 0;
        free(c0);
        free(s0);
        free(s1);
        return res;
    }

    void method_call_log(int callee_t, jstring* method_name) {
        char* m = to_string(method_name);
        if (method_calls == NULL) {
            method_calls = linkedmap_init(m, callee_t);
        } else {
            if (!linkedmap_contains_pair(method_calls, m, callee_t)) {
                linkedmap_insert(method_calls, m, callee_t);
            }
        }

        free(m);
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