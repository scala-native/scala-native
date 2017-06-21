#ifndef _OS_WIN_DescriptorGuard_H_
#define _OS_WIN_DescriptorGuard_H_

#include <vector>

struct DescriptorGuard {
    DescriptorGuard();
    enum Desc { EMPTY, FILE, SOCKET };
    struct Entry {
        Desc type = EMPTY;
        uint32_t data = -1;
    };

    bool openFile(int fildes);

    bool openSocket(int fildes);

    Desc close(int fildes);

    Desc get(int fildes);

    std::vector<Entry> db;
};

DescriptorGuard &descriptorGuard();

#endif