#ifdef _WIN32

#include "os_win_winsock2.h"
#include <exception>
#include <string>
#include "os_win_descriptor_guard.h"

std::string formatSystemError(DWORD error) {
    LPVOID lpMsgBuf;

    FormatMessageA(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM |
                       FORMAT_MESSAGE_IGNORE_INSERTS,
                   NULL, error, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
                   (LPSTR)&lpMsgBuf, 0, NULL);

    std::string msg((LPSTR)lpMsgBuf);
    LocalFree(lpMsgBuf);
    return msg;
}

void os_win_socket_error(const char *msg = nullptr) {
    if (msg) {
        throw std::exception(msg);
    } else {
        throw std::exception(formatSystemError(WSAGetLastError()).c_str());
    }
}

struct SocketManager {
    WSADATA wsaData = {0};
    int iResult = 0;

    SocketManager() {
        // Initialize Winsock
        iResult = WSAStartup(MAKEWORD(2, 2), &wsaData);
        if (iResult != 0) {
            os_win_socket_error();
        }
    }

    ~SocketManager() { WSACleanup(); }
};

int os_win_socket(int domain, int type, int protocol) {
    static SocketManager guard;

    if (protocol == 0) {
        switch (type) {
        case SOCK_STREAM: {
            protocol = IPPROTO_TCP; // tcp/ip protocol
            break;
        }
        default: {
            os_win_socket_error(
                "not supported protocol for that type of socket");
        }
        }
    }
    const auto result = socket(domain, type, protocol);
    if (result == INVALID_SOCKET) {
        os_win_socket_error();
    } else
        descriptorGuard().openSocket(result, result);
    return result;
}

extern "C" char *os_win_inet_ntoa(int family, struct in_addr *in) {
    const int buf_size = INET6_ADDRSTRLEN + 1;
    static char buf[buf_size];
    InetNtopA(family, in, buf, buf_size);
    return buf;
}

extern "C" in_addr_t os_win_inet_addr4(char *in) {
    in_addr_t out;
    int result = InetPtonA(AF_INET, in, &out);
    return out;
}

extern "C" in_addr6_t os_win_inet_addr6(char *in) {
    in_addr6_t out;
    int result = InetPtonA(AF_INET6, in, &out);
    return out;
}

extern "C" ssize_t readv(int fd, const struct iovec *iov, int iovcnt) {
    return 0;
}
extern "C" ssize_t writev(int fd, const struct iovec *iov, int iovcnt) {
    return 0;
}

extern "C" int os_win_closesocket(int fildes) { return closesocket(fildes); }

#endif