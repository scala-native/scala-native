#ifndef _OS_WIN_DescriptorGuard_H_
#define _OS_WIN_DescriptorGuard_H_

#include <vector>
#include <string>

struct DescriptorGuard {
    DescriptorGuard();
    ~DescriptorGuard();
    enum Desc { EMPTY, FILE, SOCKET };
    struct Entry {
        Desc type = EMPTY;
        uint32_t data = -1;
        std::string name;
    };

    bool openFile(int fildes, const char *name);

    bool openSocket(int fildes, uint32_t socket);

    Desc close(int fildes);

    const Entry &get(int fildes);

    std::vector<Entry> db;
    static Entry empty;
};

DescriptorGuard &descriptorGuard();

#endif