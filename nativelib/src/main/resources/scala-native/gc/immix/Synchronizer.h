#ifndef SYNCHRONIZER_H
#define SYNCHRONIZER_H

#include <stdlib.h>
#include <stdbool.h>
#include "shared/ThreadUtil.h"
#include <stdatomic.h>

extern atomic_bool Synchronizer_stopThreads;
void Synchronizer_init();
bool Synchronizer_acquire();
void Synchronizer_release();
void Synchronizer_wait();

#endif // SYNCHRONIZER_H