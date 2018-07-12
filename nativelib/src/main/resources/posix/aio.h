#ifndef __AIO_H
#define __AIO_H

struct scalanative_aiocb {
    int aio_fildes;
    off_t aio_offset;
    volatile void *aio_buf;
    size_t aio_nbytes;
    int aio_reqprio;
    struct sigevent *aio_sigevent;
    int aio_lio_opcode;
};

#endif //  __AIO_H
