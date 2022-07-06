#if defined(_WIN32)
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
#include "win_freq.h"

static int winFreqQuadPartValue = 0;

int winFreqQuadPart() {
    if (winFreqQuadPartValue == 0) {
        LARGE_INTEGER freq;
        QueryPerformanceFrequency(&freq);
        winFreqQuadPartValue = freq.QuadPart;
    }
    return winFreqQuadPartValue;
}

#endif
