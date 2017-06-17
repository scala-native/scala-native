#ifdef _WIN32

#include "os_win_descriptor_guard.h"

DescriptorGuard::DescriptorGuard()
{
    db.reserve(256);
}

bool DescriptorGuard::openSocket(int fildes)
{
    db.resize(fildes+1);
    db[fildes] = {DescriptorGuard::SOCKET, fildes};
    return true;
}

bool DescriptorGuard::closeIfSocket(int fildes)
{
    bool result = false;
    if (fildes < db.size())
    {
        if (db[fildes].type == DescriptorGuard::SOCKET)
            result = true;
        db[fildes] = {DescriptorGuard::EMPTY, -1};
    }
    return result;
}

DescriptorGuard& descriptorGuard()
{
    static DescriptorGuard dg;
    return dg;
}

#endif