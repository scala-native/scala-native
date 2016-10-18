#include <map>
#include <stdio.h>
#include <string>
#include "CountingMap.hpp"

using namespace std;

bool CountingMap::contains(string key) {
    return this->backing.find(key) != this->backing.end();
}

bool CountingMap::contains_pair(string key, int value) {
    if (this->contains(key)) {
        std::map<int, unsigned long> values = this->backing[key];
        return values.find(value) != values.end();
    } else {
        return false;
    }
}

void CountingMap::insert_occ(string key, int value) {
    if (!this->contains(key)) {
        this->backing[key] = *new map<int, unsigned long>();
    }

    if (!this->contains_pair(key, value)) {
        this->backing[key][value] = 0L;
    }

    this->backing[key][value] += 1L;
}

unsigned long CountingMap::occurrences(string key, int value) {
    if (this->contains_pair(key, value)) {
        return this->backing[key][value];
    } else {
        return 0L;
    }
}

void CountingMap::print(FILE* out) {
    typedef map<string, map<int, unsigned long> >::iterator outer_it;
    typedef map<int, unsigned long>::iterator inner_it;

    for (outer_it m = this->backing.begin(); m != this->backing.end(); ++m) {
        fprintf(out, "= %s:\n", m->first.c_str());
        for (inner_it n = m->second.begin(); n != m->second.end(); ++n) {
            fprintf(out, "\t%d (%lu)\n", n->first, n->second);
        }
    }
}
