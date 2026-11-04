#ifdef SCALANATIVE_FREESTANDING

#include <errno.h>
#include <stdarg.h>
#include <stddef.h>
#include <stdint.h>
#include <string.h>
#include "sn_platform.h"

// A FILE is only a console stream number; files cannot be opened. glibc's
// <stdio.h> is not included because it defines some of these functions as
// extern inline.
typedef struct _IO_FILE FILE;
struct _IO_FILE {
    int stream;
};
static FILE console_in = {0}, console_out = {1}, console_err = {2};
FILE *stdin = &console_in;
FILE *stdout = &console_out;
FILE *stderr = &console_err;

static int stream_of(FILE *f) { return f->stream; }

static void console(int stream, const char *buf, size_t len) {
    if (stream == 1 || stream == 2)
        scalanative_platform_console_write(stream, buf, len);
}

typedef struct {
    char *buf;
    size_t cap, len;
} Out;

static void put(Out *o, char c) {
    if (o->len + 1 < o->cap)
        o->buf[o->len] = c;
    o->len++;
}

static void pad(Out *o, char c, int n) {
    while (n-- > 0)
        put(o, c);
}

static void emit(Out *o, const char *sign, const char *digits, int ndigits,
                 int width, int left, int zero) {
    int slen = (int)strlen(sign);
    int fill = width - slen - ndigits;
    if (!left && !zero)
        pad(o, ' ', fill);
    for (int i = 0; i < slen; i++)
        put(o, sign[i]);
    if (!left && zero)
        pad(o, '0', fill);
    for (int i = 0; i < ndigits; i++)
        put(o, digits[i]);
    if (left)
        pad(o, ' ', fill);
}

static int utoa(unsigned long long v, int base, int upper, int minDigits,
                char *out) {
    char tmp[32];
    int n = 0;
    const char *dig = upper ? "0123456789ABCDEF" : "0123456789abcdef";
    do {
        tmp[n++] = dig[v % base];
        v /= base;
    } while (v);
    while (n < minDigits && n < 32)
        tmp[n++] = '0';
    for (int i = 0; i < n; i++)
        out[i] = tmp[n - 1 - i];
    return n;
}

// The conversions the runtime uses: d i u o x X p c s f %, with flags - 0 +
// space, width and precision (including *), and length modifiers hh h l ll z
// j t L. %e and %g print as %f.
int vsnprintf(char *buf, size_t cap, const char *fmt, va_list ap) {
    Out o = {buf, cap, 0};
    for (; *fmt; fmt++) {
        if (*fmt != '%') {
            put(&o, *fmt);
            continue;
        }
        int left = 0, zero = 0, plus = 0, space = 0, width = 0, prec = -1;
        for (;; fmt++) {
            char f = fmt[1];
            if (f == '-')
                left = 1;
            else if (f == '0')
                zero = 1;
            else if (f == '+')
                plus = 1;
            else if (f == ' ')
                space = 1;
            else if (f != '#')
                break;
        }
        fmt++;
        if (*fmt == '*') {
            width = va_arg(ap, int);
            if (width < 0)
                left = 1, width = -width;
            fmt++;
        } else
            while (*fmt >= '0' && *fmt <= '9')
                width = width * 10 + (*fmt++ - '0');
        if (*fmt == '.') {
            fmt++;
            prec = 0;
            if (*fmt == '*') {
                prec = va_arg(ap, int);
                fmt++;
            } else
                while (*fmt >= '0' && *fmt <= '9')
                    prec = prec * 10 + (*fmt++ - '0');
        }
        int lng = 0;
        while (*fmt == 'l' || *fmt == 'h' || *fmt == 'z' || *fmt == 'j' ||
               *fmt == 't' || *fmt == 'L') {
            if (*fmt == 'l' || *fmt == 'z' || *fmt == 'j' || *fmt == 't')
                lng++;
            fmt++;
        }
        char digits[64];
        int n;
        const char *sign = "";
        switch (*fmt) {
        case 'd':
        case 'i': {
            long long v = lng ? va_arg(ap, long long) : va_arg(ap, int);
            unsigned long long u = v < 0 ? -(unsigned long long)v : v;
            sign = v < 0 ? "-" : plus ? "+" : space ? " " : "";
            n = utoa(u, 10, 0, prec < 0 ? 1 : prec, digits);
            emit(&o, sign, digits, n, width, left, zero && prec < 0);
            break;
        }
        case 'u':
        case 'o':
        case 'x':
        case 'X': {
            unsigned long long u =
                lng ? va_arg(ap, unsigned long long) : va_arg(ap, unsigned);
            int base = *fmt == 'u' ? 10 : *fmt == 'o' ? 8 : 16;
            n = utoa(u, base, *fmt == 'X', prec < 0 ? 1 : prec, digits);
            emit(&o, "", digits, n, width, left, zero && prec < 0);
            break;
        }
        case 'p':
            n = utoa((uintptr_t)va_arg(ap, void *), 16, 0, 1, digits);
            emit(&o, "0x", digits, n, width, left, 0);
            break;
        case 'c':
            digits[0] = (char)va_arg(ap, int);
            emit(&o, "", digits, 1, width, left, 0);
            break;
        case 's': {
            const char *s = va_arg(ap, const char *);
            if (!s)
                s = "(null)";
            int len = (int)strlen(s);
            if (prec >= 0 && prec < len)
                len = prec;
            emit(&o, "", s, len, width, left, 0);
            break;
        }
        case 'f':
        case 'F':
        case 'e':
        case 'g': {
            double v = va_arg(ap, double);
            if (prec < 0)
                prec = 6;
            if (prec > 18)
                prec = 18;
            sign = v < 0 ? "-" : plus ? "+" : space ? " " : "";
            if (v < 0)
                v = -v;
            unsigned long long scale = 1;
            for (int i = 0; i < prec; i++)
                scale *= 10;
            unsigned long long ip = (unsigned long long)v;
            unsigned long long fp =
                (unsigned long long)((v - (double)ip) * scale + 0.5);
            if (fp >= scale)
                ip++, fp -= scale;
            n = utoa(ip, 10, 0, 1, digits);
            if (prec > 0) {
                digits[n++] = '.';
                n += utoa(fp, 10, 0, prec, digits + n);
            }
            emit(&o, sign, digits, n, width, left, zero);
            break;
        }
        case '%':
            put(&o, '%');
            break;
        default:
            put(&o, '%');
            if (!*fmt)
                fmt--;
            else
                put(&o, *fmt);
            break;
        }
    }
    if (cap)
        buf[o.len < cap ? o.len : cap - 1] = 0;
    return (int)o.len;
}

int snprintf(char *buf, size_t cap, const char *fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    int r = vsnprintf(buf, cap, fmt, ap);
    va_end(ap);
    return r;
}

int vsprintf(char *buf, const char *fmt, va_list ap) {
    return vsnprintf(buf, SIZE_MAX, fmt, ap);
}

int sprintf(char *buf, const char *fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    int r = vsprintf(buf, fmt, ap);
    va_end(ap);
    return r;
}

int vfprintf(FILE *f, const char *fmt, va_list ap) {
    char buf[1024];
    va_list copy;
    va_copy(copy, ap);
    int n = vsnprintf(buf, sizeof buf, fmt, copy);
    va_end(copy);
    console(stream_of(f), buf,
            n < (int)sizeof buf ? (size_t)n : sizeof buf - 1);
    return n;
}

int fprintf(FILE *f, const char *fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    int r = vfprintf(f, fmt, ap);
    va_end(ap);
    return r;
}

int vprintf(const char *fmt, va_list ap) { return vfprintf(stdout, fmt, ap); }

int printf(const char *fmt, ...) {
    va_list ap;
    va_start(ap, fmt);
    int r = vfprintf(stdout, fmt, ap);
    va_end(ap);
    return r;
}

int fputs(const char *s, FILE *f) {
    console(stream_of(f), s, strlen(s));
    return 0;
}

int puts(const char *s) {
    fputs(s, stdout);
    console(1, "\n", 1);
    return 0;
}

int fputc(int c, FILE *f) {
    char ch = (char)c;
    console(stream_of(f), &ch, 1);
    return (unsigned char)c;
}

int putc(int c, FILE *f) { return fputc(c, f); }
int putchar(int c) { return fputc(c, stdout); }

size_t fwrite(const void *p, size_t sz, size_t n, FILE *f) {
    console(stream_of(f), p, sz * n);
    return n;
}

int fflush(FILE *f) {
    (void)f;
    return 0;
}

FILE *fopen(const char *path, const char *mode) {
    (void)path, (void)mode;
    errno = ENOENT;
    return NULL;
}

int fclose(FILE *f) {
    (void)f;
    return 0;
}

char *fgets(char *s, int n, FILE *f) {
    (void)s, (void)n, (void)f;
    return NULL;
}

void perror(const char *msg) {
    if (msg && *msg)
        fprintf(stderr, "%s: ", msg);
    fprintf(stderr, "%s (errno %d)\n", strerror(errno), errno);
}

// Only reached for values parsed out of environment variables, and getenv
// finds none.
static int no_scan(const char *s, const char *fmt, ...) {
    (void)s, (void)fmt;
    return 0;
}
int sscanf(const char *s, const char *fmt, ...)
    __attribute__((alias("no_scan")));
int __isoc99_sscanf(const char *s, const char *fmt, ...)
    __attribute__((alias("no_scan")));
int __isoc23_sscanf(const char *s, const char *fmt, ...)
    __attribute__((alias("no_scan")));

#endif
