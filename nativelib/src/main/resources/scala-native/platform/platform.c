#ifdef _WIN32
#define WIN32_LEAN_AND_MEAN
#include <Windows.h>
#else
#include <sys/utsname.h>
#endif

#include <stdlib.h>
#include <string.h>

#ifdef __APPLE__
#include <sys/sysctl.h>
#endif

int scalanative_platform_is_freebsd() {
#if defined(__FreeBSD__)
    return 1;
#else
    return 0;
#endif
}

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

/* 'scalanative_platform_probe_mac_x8664_is_arm64' is original work
 * for Scala Native.  It has been informed by information at these URLs:
 *   https://developer.apple.com/documentation/kernel/1387446-sysctlbyname
 *   https://developer.apple.com/documentation/apple-silicon/
 *     about-the-rosetta-translation-environment#
 *     Determine-Whether-Your-App-Is-Running-as-a-Translated-Binary
 */

// Three expected conditions, error: -1, not emulated: 0, emulated: > 0.

int scalanative_platform_probe_mac_x8664_is_arm64() {
    int translated = 0;

#ifdef __APPLE__
    size_t translatedLen = sizeof(translated);

    int ret = sysctlbyname("sysctl.proc_translated", &translated,
                           &translatedLen, NULL, 0);

    if (ret < 0) {
        /* 'errno' has been set.
         * Do not raise C signal here because such are not Scala Native
         * exceptions and do not play nicely.
         * Return failure to caller and let it ignore error or
         * raise proper Scala Native exception.
         */
        translated = -1;
    }
    // else 'translated' has been set by sysctlbyname

    /* Getting the real hardware architecture is __hard__.
     * This test assumes 'translated == 1' means "Rosetta 2 on arm64".
     * This should be a safe assumption on any macOS more recent than
     * 2011 or so. PowerPC translation was removed about then.
     */

#endif

    return translated;
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
