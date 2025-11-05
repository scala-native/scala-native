#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_POSIX_EPOLL)

#ifdef __linux__

#include <sys/epoll.h>

int scalanative_epoll_cloexec() { return EPOLL_CLOEXEC; }

int scalanative_epoll_ctl_add() { return EPOLL_CTL_ADD; }
int scalanative_epoll_ctl_mod() { return EPOLL_CTL_MOD; }
int scalanative_epoll_ctl_del() { return EPOLL_CTL_DEL; }

int scalanative_epollin() { return EPOLLIN; }
int scalanative_epollout() { return EPOLLOUT; }
int scalanative_epollrdhup() { return EPOLLRDHUP; }
int scalanative_epollpri() { return EPOLLPRI; }
int scalanative_epollerr() { return EPOLLERR; }
int scalanative_epollhup() { return EPOLLHUP; }

int scalanative_epollet() { return EPOLLET; }
int scalanative_epolloneshot() { return EPOLLONESHOT; }

// linux 3.5
// int scalanative_epollwakeup() { return EPOLLWAKEUP; }
// linux 4.5
// int scalanative_epollexclusive() { return EPOLLEXCLUSIVE; }

#endif // __linux__

#endif // __SCALANATIVE_POSIX_EPOLL
