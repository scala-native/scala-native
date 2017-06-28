#include <sys/select.h>
#include <stdbool.h>
#include <errno.h>
#include <stddef.h>
#include <string.h>

struct scalanative_timeval {
    time_t tv_sec;
    suseconds_t tv_usec;
};

int scalanative_FD_SETSIZE() { return FD_SETSIZE; }

void convert_scalanative_timeval(struct scalanative_timeval *in,
                                 struct timeval *out) {
    out->tv_sec = in->tv_sec;
    out->tv_usec = in->tv_usec;
}

void scalanative_FD_ZERO(int *set) { memset(set, '\0', sizeof(*set)); }

void scalanative_FD_CLR(int fd, int *set) {
    set[fd / sizeof(int)] &= ~((int)(1 << (fd % sizeof(int))));
}

void scalanative_FD_SET(int fd, int *set) {
    set[fd / sizeof(int)] |= ((int)(1 << (fd % sizeof(int))));
}

int scalanative_FD_ISSET(int fd, int *set) {
    return (set[fd / sizeof(int)] & ((int)(1 << (fd & sizeof(int)))));
}

int scalanative_select(int nfds, int *readfds, int *writefds, int *exceptfds,
                       struct scalanative_timeval *scalanative_timeout) {
    fd_set rfds;
    fd_set wfds;
    fd_set efds;
    bool r, w, e;
    r = (readfds != NULL);
    w = (writefds != NULL);
    e = (exceptfds != NULL);

    FD_ZERO(&rfds);
    FD_ZERO(&wfds);
    FD_ZERO(&efds);
    for (int i = 0; i < FD_SETSIZE; ++i) {
        if (!r && !w && !e)
            break;
        if (r) {
            if (scalanative_FD_ISSET(i, readfds)) {
                FD_SET(i, &rfds);
            }
        }
        if (w) {
            if (scalanative_FD_ISSET(i, writefds)) {
                FD_SET(writefds[i], &wfds);
            }
        }
        if (e) {
            if (scalanative_FD_ISSET(i, exceptfds)) {
                FD_SET(exceptfds[i], &efds);
            }
        }
    }

    int status;
    if (scalanative_timeout != NULL) {
        struct timeval timeout;
        convert_scalanative_timeval(scalanative_timeout, &timeout);
        status = select(nfds, &rfds, &wfds, &efds, &timeout);
    } else {
        status = select(nfds, &rfds, &wfds, &efds, NULL);
    }

    if (status == -1) {
        errno = -1;
        return -1;
    }

    for (int i = 0; i < FD_SETSIZE; ++i) {
        if (!r && !w && !e)
            break;
        if (r && FD_ISSET(i, &rfds)) {
            scalanative_FD_SET(i, readfds);
        }
        if (w && FD_ISSET(i, &wfds)) {
            scalanative_FD_SET(i, writefds);
        }
        if (e && FD_ISSET(i, &efds)) {
            scalanative_FD_SET(i, exceptfds);
        }
    }
    return status;
}
