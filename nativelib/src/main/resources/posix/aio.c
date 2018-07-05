#include <aio.h>

// symbolic constants

int scalanative_aio_alldone() { return AIO_ALLDONE; }
int scalanative_aio_canceled() { return AIO_CANCELED; }
int scalanative_aio_notcanceled() { return AIO_NOTCANCELED; }
int scalanative_lio_nop() { return LIO_NOP; }
int scalanative_lio_nowait() { return LIO_NOWAIT; }
int scalanative_lio_read() { return LIO_READ; }
int scalanative_lio_wait() { return LIO_WAIT; }
int scalanative_lio_write() { return LIO_WRITE; }
