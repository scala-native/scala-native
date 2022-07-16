#if defined(_WIN32)
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
#include "win_freq.h"

static int winFreqQuadPartValue = 0;

int winFreqQuadPart(int *quad) {
    int ret = 0;
    // check if cache is set
    if (winFreqQuadPartValue == 0) {
        LARGE_INTEGER freq;
        ret = QueryPerformanceFrequency(&freq);
        if (ret) {
            winFreqQuadPartValue = freq.QuadPart;
        }
    }
    *quad = winFreqQuadPartValue;

    return ret;
}

#endif
