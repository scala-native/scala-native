#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
#else
#include <sys/utsname.h>
#endif
#include <stdlib.h>
#include <string.h>

int scalanative_platform_is_linux() {
#ifdef __linux__
    return 1;
#else
    return 0;
#endif
}

int scalanative_platform_is_mac() {
#ifdef __APPLE__
    return 1;
#else
    return 0;
#endif
}

int scalanative_platform_is_windows() {
#ifdef _WIN32
    return 1;
#else
    return 0;
#endif
}

char *scalanative_windows_get_user_lang() {
#ifdef _WIN32
    char *dest = malloc(9);
    GetLocaleInfoA(LOCALE_USER_DEFAULT, LOCALE_SISO639LANGNAME, dest, 9);
    return dest;
#endif
    return "";
}

char *scalanative_windows_get_user_country() {
#ifdef _WIN32
    char *dest = malloc(9);
    GetLocaleInfoA(LOCALE_USER_DEFAULT, LOCALE_SISO3166CTRYNAME, dest, 9);
    return dest;
#endif
    return "";
}

// See http://stackoverflow.com/a/4181991
int scalanative_little_endian() {
    int n = 1;
    return (*(char *)&n);
}

void scalanative_set_os_props(void (*add_prop)(const char *, const char *)) {
#ifdef _WIN32
    add_prop("os.name", "Windows (Unknown version)");
#elif defined(__APPLE__)
    add_prop("os.name", "Mac OS X");
#else
    struct utsname name;
    if (uname(&name) == 0) {
        add_prop("os.name", name.sysname);
        add_prop("os.version", name.release);
    }
#endif

    char *arch = "unknown";
#ifdef _WIN32
#if defined(_M_AMD64)
    arch = "amd64";
#elif defined(_X86_)
    arch = "x86";
#elif defined(_M_ARM64)
    arch = "aarch64";
#endif // Windows

#else // on Unix
    struct utsname buffer;
    if (uname(&buffer) >= 0) {
        arch = buffer.machine;
// JVM compliance logic
// On Linux we replace x86 with i386
#ifdef __linux__
        if (strcmp("x86", arch) == 0) {
            arch = "i386";
        }
#endif
// On all platforms except MacOSX replace x86_64 with amd64
#ifndef __APPLE__
        if (strcmp("x86_64", arch) == 0) {
            arch = "amd64";
        }
#endif
    }
#endif
    add_prop("os.arch", arch);
}

size_t scalanative_wide_char_size() { return sizeof(wchar_t); }
