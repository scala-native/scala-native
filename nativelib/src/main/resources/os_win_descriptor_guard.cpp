#ifdef _WIN32

#include "os_win_descriptor_guard.h"

DescriptorGuard::DescriptorGuard() {
    db.resize(3);
    db.reserve(256);
    db[0] = {DescriptorGuard::FILE, 0};
    db[1] = {DescriptorGuard::FILE, 1};
    db[2] = {DescriptorGuard::FILE, 2};
}

bool DescriptorGuard::openSocket(int fildes) {
    db.resize(fildes + 1);
    db[fildes] = {DescriptorGuard::SOCKET, fildes};
    return true;
}

bool DescriptorGuard::openFile(int fildes) {
    db.resize(fildes + 1);
    db[fildes] = {DescriptorGuard::FILE, fildes};
    return true;
}

DescriptorGuard::Desc DescriptorGuard::close(int fildes) {
    Desc result = DescriptorGuard::EMPTY;
    if (fildes < db.size()) {
        result = db[fildes].type;
        db[fildes] = {DescriptorGuard::EMPTY, -1};
    }
    return result;
}

DescriptorGuard::Desc DescriptorGuard::get(int fildes) {
    Desc result = DescriptorGuard::EMPTY;
    if (fildes < db.size()) {
        result = db[fildes].type;
    }
    return result;
}

DescriptorGuard &descriptorGuard() {
    static DescriptorGuard dg;
    return dg;
}

#endif