#ifndef _OS_WIN_DescriptorGuard_H_
#define _OS_WIN_DescriptorGuard_H_

#include <vector>

struct DescriptorGuard {
    DescriptorGuard();
    enum Desc { EMPTY, FILE, SOCKET };
    struct Entry {
        Desc type = EMPTY;
        int fildes = -1;
    };

    bool openSocket(int fildes);

    bool closeIfSocket(int fildes);

    std::vector<Entry> db;
};

DescriptorGuard &descriptorGuard();

#endif