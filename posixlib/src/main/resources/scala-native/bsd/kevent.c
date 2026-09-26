#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_POSIX_KEVENT)

#define KQUEUE_NONE 0
#define KQUEUE_FREEBSD 1
// also OpenBSD etc.

#if defined(__FreeBSD__) || defined(__APPLE__) && defined(__MACH__)
#define HAVE_KQUEUE KQUEUE_FREEBSD
#endif

#ifdef HAVE_KQUEUE

// Beware: FreeBSD and other BSD layouts have not been tested.

/* kqueue/kevent usage reference, slightly old but useful:
 *   https://wiki.netbsd.org/tutorials/kqueue_tutorial/
 */

#include <stddef.h>
#include <sys/event.h>

int scalanative_kevent_evfilt_read() { return EVFILT_READ; }
int scalanative_kevent_evfilt_write() { return EVFILT_WRITE; }
int scalanative_kevent_evfilt_proc() { return EVFILT_PROC; }

// actions
int scalanative_kevent_ev_add() { return EV_ADD; }
int scalanative_kevent_ev_delete() { return EV_DELETE; }
int scalanative_kevent_ev_enable() { return EV_ENABLE; }
int scalanative_kevent_ev_disable() { return EV_DISABLE; }
int scalanative_kevent_ev_dispatch() { return EV_DISPATCH; }

// flags
int scalanative_kevent_ev_oneshot() { return EV_ONESHOT; }
int scalanative_kevent_ev_clear() { return EV_CLEAR; }
int scalanative_kevent_ev_receipt() { return EV_RECEIPT; }

/* ... with or without EV_ERROR */
/* ... use KEVENT_FLAG_ERROR_EVENTS */
/*     on syscalls supporting flags */

int scalanative_kevent_ev_eof() { return EV_EOF; }
int scalanative_kevent_ev_error() { return EV_ERROR; }

int scalanative_kevent_note_exit() { return NOTE_EXIT; }
int scalanative_kevent_note_exitstatus() { return NOTE_EXITSTATUS; }

size_t scalanative_kevent_size() { return sizeof(struct kevent); }

void scalanative_kevent_set(struct kevent *ev, int idx, uintptr_t ident,
                            int16_t filter, uint16_t flags, uint32_t fflags,
                            intptr_t data, void *udata) {
    struct kevent *evidx = ev + idx;
    evidx->ident = ident;
    evidx->filter = filter;
    evidx->flags = flags;
    evidx->fflags = fflags;
    evidx->data = data;
    evidx->udata = udata;
}

void scalanative_kevent_get(struct kevent *ev, int idx, uintptr_t *ident,
                            int16_t *filter, uint16_t *flags, uint32_t *fflags,
                            intptr_t *data, void **udata) {
    struct kevent *evidx = ev + idx;
    if (NULL != ident)
        *ident = evidx->ident;
    if (NULL != filter)
        *filter = evidx->filter;
    if (NULL != flags)
        *flags = evidx->flags;
    if (NULL != fflags)
        *fflags = evidx->fflags;
    if (NULL != data)
        *data = evidx->data;
    if (NULL != udata)
        *udata = evidx->udata;
}

#endif // HAVE_KQUEUE

#endif // __SCALANATIVE_POSIX_KEVENT
