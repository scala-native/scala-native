#ifdef _WIN32
#include "os_win_fcntl.h"
#include "os_win_unistd.h"
#include <exception>
#include "os_win_descriptor_guard.h"

extern "C" int os_win_fcntl_open(const char *pathname, int flags, mode_t mode) {
    return __imp_open(pathname, flags, mode);
}

extern "C" int os_win_fcntl_close(int fd) { return __imp_close(fd); }

extern "C" int os_win_fcntl_fcntl(int fd, int cmd, va_list args) {
    switch (cmd) {
    case F_DUPFD:
        std::exception("not implemented");
        break;
    case F_GETFD:
        if (descriptorGuard().get(fd).type == DescriptorGuard::FILE) {
            return fd;
        }
        break;
    case F_SETFD:
        std::exception("not implemented");
        break;
    case F_GETFL:
        std::exception("not implemented");
        break;
    case F_SETFL:
        std::exception("not implemented");
        break;
    case F_GETOWN:
        std::exception("not implemented");
        break;
    case F_SETOWN:
        std::exception("not implemented");
        break;
    case F_GETLK:
        std::exception("not implemented");
        break;
    case F_SETLK:
        std::exception("not implemented");
        break;
    case F_SETLKW:
        std::exception("not implemented");
        break;
    default:
        return -1;
    }
    return -1;
}

#endif