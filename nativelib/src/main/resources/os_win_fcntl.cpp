#ifdef _WIN32
#include "os_win_fcntl.h"
#include "os_win_unistd.h"
#include <exception>

extern "C" int os_win_fcntl_open(const char *pathname, int flags, mode_t mode)
{
    return __imp_open(pathname, flags, mode);
}

extern "C" int os_win_fcntl_close(int fd)
{
    return __imp_close(fd);
}

extern "C" int os_win_fcntl_fcntl(int fd, int cmd, va_list args) {
    // throw std::exception("`fcntl` is not implemented");
    return 0;
}

#endif