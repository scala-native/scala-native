#if defined(_WIN32)
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include "win_freq.h"

static int winFreqQuadPartValue = 0;

int winFreqQuadPart(int *quad) {
    int retval = 1; // assume ok for caching
    // check if cache is set
    if (winFreqQuadPartValue == 0) {
        LARGE_INTEGER freq;
        retval = QueryPerformanceFrequency(&freq);
        if (retval != 0) {
            // set cache value
            winFreqQuadPartValue = freq.QuadPart;
        }
    }
    // assign cache value or default 0 on failure
    *quad = winFreqQuadPartValue;

    return retval;
}

#endif
