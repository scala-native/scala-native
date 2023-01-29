#ifndef SYNCHRONIZER_H
#define SYNCHRONIZER_H

#include <stdlib.h>
#include <stdbool.h>
#include "ThreadUtil.h"

void Synchronizer_init();
bool Synchronizer_acquire();
void Synchronizer_release();
void Synchronizer_wait();

#endif // SYNCHRONIZER_H