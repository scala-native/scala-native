#ifndef _OS_WIN_DescriptorGuard_H_
#define _OS_WIN_DescriptorGuard_H_

#include <vector>
#include <string>

struct DescriptorGuard {
    DescriptorGuard();
    ~DescriptorGuard();
    enum Desc { EMPTY, FILE, SOCKET, FOLDER };
    struct Entry {
        Desc type = EMPTY;
        uint32_t data = -1;
        std::string name;
    };

    bool openFile(int fildes, std::string &&name, bool isFolder = false);

    int getFile(const std::string &name);

    bool openSocket(int fildes, uint32_t socket);

    Desc close(int fildes);

    const Entry &get(int fildes);

    int getEmpty();

    std::vector<Entry> db;
    static Entry empty;
};

DescriptorGuard &descriptorGuard();

#endif