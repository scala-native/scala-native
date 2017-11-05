#ifdef _WIN32

#include "os_win_descriptor_guard.h"

extern "C" int __imp_close(int fildes);

DescriptorGuard::Entry DescriptorGuard::empty = {DescriptorGuard::EMPTY, -1,
                                                 ""};

DescriptorGuard::DescriptorGuard() {
    db.resize(3);
    db.reserve(256);
    db[0] = {DescriptorGuard::FILE, 0};
    db[1] = {DescriptorGuard::FILE, 1};
    db[2] = {DescriptorGuard::FILE, 2};
}

DescriptorGuard::~DescriptorGuard() {
    for (int i = 3; i < db.size(); ++i) {
        auto &entry = db[i];
        if (entry.type != EMPTY) {
            std::string stype;
            switch (entry.type) {
            case FILE:
                stype = "FILE";
                break;
            case SOCKET:
                stype = "SOCKET";
                break;
            case FOLDER:
                stype = "FOLDER";
                break;
            default:
                stype = "UNKNOWN";
            }
            printf("Descriptor(%i): %s: %i, %s\n", i, stype.c_str(), entry.data,
                   entry.name.c_str());
            __imp_close(i);
        }
    }
}

bool DescriptorGuard::openSocket(int fildes, uint32_t socket) {
    db.resize(fildes + 1);
    db[fildes] = {DescriptorGuard::SOCKET, socket, ""};
    return true;
}

bool DescriptorGuard::openFile(int fildes, std::string &&name, bool isFolder) {
    db.resize(fildes + 1);
    db[fildes] = {isFolder ? DescriptorGuard::FOLDER : DescriptorGuard::FILE,
                  fildes, std::move(name)};
    return true;
}

int DescriptorGuard::getFile(const std::string &name) {
    int result = -1;
    int idx = -1;
    for (auto &entry : db) {
        ++idx;
        if (idx < 3) {
            continue;
        }

        if (entry.type == FILE && entry.name.compare(name) == 0) {
            result = idx;
            break;
        }
    }
    return result;
}

int DescriptorGuard::getEmpty() {
    int result = -1;
    int idx = -1;
    for (auto &entry : db) {
        ++idx;
        if (idx < 3) {
            continue;
        }

        if (entry.type == EMPTY) {
            result = idx;
            break;
        }
    }

    if (result < 0) {
        result = ++idx;
        db.push_back({DescriptorGuard::EMPTY, -1, ""});
    }
    return result;
}

DescriptorGuard::Desc DescriptorGuard::close(int fildes) {
    Desc result = DescriptorGuard::EMPTY;
    if (fildes < db.size()) {
        result = db[fildes].type;
        db[fildes] = {DescriptorGuard::EMPTY, -1, ""};
    }
    return result;
}

const DescriptorGuard::Entry &DescriptorGuard::get(int fildes) {
    if (fildes < db.size()) {
        return db[fildes];
    }
    return empty;
}

DescriptorGuard &descriptorGuard() {
    static DescriptorGuard dg;
    return dg;
}

#endif