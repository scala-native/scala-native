#ifndef CountingMap_hpp
#define CountingMap_hpp

#include <map>
#include <stdio.h>
#include <string>

using namespace std;

class CountingMap {
private:
    map<string, map<int, unsigned long> > backing;

public:
    void insert_occ(string key, int value);
    bool contains(string key);
    bool contains_pair(string key, int value);
    unsigned long occurrences(string key, int value);
    void print(FILE* out);
};

#endif /* CountingMap_hpp */
