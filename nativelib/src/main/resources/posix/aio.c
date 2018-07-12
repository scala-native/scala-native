#include <aio.h>
#include "aio.h"

// symbolic constants

int scalanative_aio_alldone() { return AIO_ALLDONE; }
int scalanative_aio_canceled() { return AIO_CANCELED; }
int scalanative_aio_notcanceled() { return AIO_NOTCANCELED; }
int scalanative_lio_nop() { return LIO_NOP; }
int scalanative_lio_nowait() { return LIO_NOWAIT; }
int scalanative_lio_read() { return LIO_READ; }
int scalanative_lio_wait() { return LIO_WAIT; }
int scalanative_lio_write() { return LIO_WRITE; }

// scala native functions

int scalanative_aio_cancel(int fd, struct scalanative_aiocb *sn_aiocb) {
    return 0;
}

int scalanative_aio_error(struct scalanative_aiocb *sn_aiocb) { return 0; }

int scalanative_aio_fsync(int op, struct scalanative_aiocb *sn_aiocb) {
    return 0;
}

int scalanative_aio_read(struct scalanative_aiocb *sn_aiocb) { return 0; }

ssize_t scalanative_aio_return(struct scalanative_aiocb *sn_aiocb) { return 0; }

int scalanative_aio_suspend(struct aiocb **aiocblist, int nent,
                            struct timespec *timeoutp) {
    return 0;
}

int scalanative_aio_write(struct scalanative_aiocb *sn_aiocb) { return 0; }

int scalanative_lio_listio(int mode, struct aiocb **aiocblist, int nent,
                           struct sigevent *sigp) {
    return 0;
}

// scala native conversion functions - in, out

static void scalanative_aiocb_init(struct aiocb *aiocb,
                                   struct scalanative_aiocb *sn_aiocb) {
    sn_aiocb->aio_fildes = aiocb->aio_fildes;
    sn_aiocb->aio_offset = aiocb->aio_offset;
    sn_aiocb->aio_buf = aiocb->aio_buf;
    sn_aiocb->aio_nbytes = aiocb->aio_nbytes;
    sn_aiocb->aio_reqprio = aiocb->aio_reqprio;
    sn_aiocb->aio_sigevent = &(aiocb->aio_sigevent);
    sn_aiocb->aio_lio_opcode = aiocb->aio_lio_opcode;
}

static void aiocb_init(struct scalanative_aiocb *sn_aiocb,
                       struct aiocb *aiocb) {
    aiocb->aio_fildes = sn_aiocb->aio_fildes;
    aiocb->aio_offset = sn_aiocb->aio_offset;
    aiocb->aio_buf = sn_aiocb->aio_buf;
    aiocb->aio_nbytes = sn_aiocb->aio_nbytes;
    aiocb->aio_reqprio = sn_aiocb->aio_reqprio;
    aiocb->aio_sigevent = *(sn_aiocb->aio_sigevent);
    aiocb->aio_lio_opcode = sn_aiocb->aio_lio_opcode;
}
