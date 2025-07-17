#if defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_JAVALIB_MEMMEM)

/* URL: https://android.googlesource.com/
 *        platform/bionic/+/ics-mr0/libc/string/memmem.c
 *
 * Modified slightly for Scala Native. At least:
 *
 *   - Use Open Group Issue 8 memmem() if available.
 *
 *   - Do not rely upon application build compiler having __builtin_expect().
 */

/*
 * Copyright (C) 2008 The Android Open Source Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
/*
 * This uses the "Not So Naive" algorithm, a very simple but
 * usually effective algorithm, see:
 * http://www-igm.univ-mlv.fr/~lecroq/string/
 */

#include <string.h>

void *scalanative_memmem(const void *haystack, size_t n, const void *needle,
                         size_t m) {

#if (_POSIX_VERSION >= 202405L) // Open Group Base Specifications Issue 8

    return memmem(haystack, n, needle, m);

#else // Windows & other "not Issue 8" operating systems.

    if (m > n || !m || !n)
        return NULL;
    if (m > 1) {
        const unsigned char *y = (const unsigned char *)haystack;
        const unsigned char *x = (const unsigned char *)needle;
        size_t j = 0;
        size_t k = 1, l = 2;
        if (x[0] == x[1]) {
            k = 2;
            l = 1;
        }
        while (j <= n - m) {
            if (x[1] != y[j + 1]) {
                j += k;
            } else {
                if (!memcmp(x + 2, y + j + 2, m - 2) && x[0] == y[j])
                    return (void *)&y[j];
                j += l;
            }
        }
    } else {
        /* degenerate case */
        return memchr(haystack, ((unsigned char *)needle)[0], n);
    }
    return NULL;

#endif // Windows & other "not Issue 8" operating systems.
}

#endif
