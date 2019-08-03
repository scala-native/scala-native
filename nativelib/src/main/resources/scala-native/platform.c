#ifdef _WIN32
#include <Windows.h>
#else
#include <sys/utsname.h>
#include <unistd.h>
#endif

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

// See http://stackoverflow.com/a/4181991
int scalanative_little_endian() {
    int n = 1;
    return (*(char *)&n);
}

int scalanative_platform_get_all_env(void (*add_env)(const char *, const char *)) {
#ifdef _WIN32
    LPWCH lpEnvStrings = GetEnvironmentStringsW();
    LPWSTR lpszVariable= (LPWSTR)lpEnvStrings;
    char buf[1024];
    int result = 0;
    while (*lpszVariable)
    {
        if (wcstombs(buf, lpszVariable, 1024) != -1) {
            char* name = buf;
            char* value = name;
            while (value && value == name)
            {
                value = strchr(value + 1, '=');
            }
            if (value) {
                *value = 0;
                int name_length = value - name;
                if (name_length > 0) {
                    if (add_env) {
                        add_env(name, value + 1);
                    }
                    ++result;
                }
            }
        }
        lpszVariable+=wcslen(lpszVariable) + 1;
    }
    FreeEnvironmentStringsW(lpEnvStrings);
    return result;
#else
    char** string = environ;
    int result = 0;
    while(string)
    {
        char* name = *string;
        char* value = name;
        while (value && value == name)
        {
            value = strchr(value + 1, '=');
        }
        if (value) {
            *value = 0;
            int name_length = value - name;
            if (name_length > 0) {
                if (add_env) {
                    add_env(name, value + 1);
                }
                ++result;
            }
        }
        ++string;
    }
    return result;
#endif
}

void scalanative_set_os_props(void (*add_prop)(const char *, const char *)) {
#ifdef _WIN32
    add_prop("os.name", "Windows (Unknown version)");
    wchar_t wcharBuf[MAX_PATH];
    char buf[MAX_PATH];
    if (GetTempPathW(MAX_PATH, wcharBuf) &&
        wcstombs(buf, wcharBuf, MAX_PATH) != -1) {
        add_prop("java.io.tmpdir", (const char *)buf);
    }
    if (GetCurrentDirectoryW(MAX_PATH, wcharBuf) &&
        wcstombs(buf, wcharBuf, MAX_PATH) != -1) {
        add_prop("user.dir", (const char *)buf);
    }
    if (GetLocaleInfoW(LOCALE_USER_DEFAULT, LOCALE_SISO639LANGNAME, wcharBuf, MAX_PATH) &&
        wcstombs(buf, wcharBuf, MAX_PATH) != -1) {
        add_prop("user.language", (const char *)buf);
    }
    if (GetLocaleInfoW(LOCALE_USER_DEFAULT, LOCALE_SISO3166CTRYNAME, wcharBuf, MAX_PATH) &&
        wcstombs(buf, wcharBuf, MAX_PATH) != -1) {
        add_prop("user.country", (const char *)buf);
    }
#else
#ifdef __APPLE__
    add_prop("os.name", "Mac OS X");
#else
    struct utsname name;
    if (uname(&name) == 0) {
        add_prop("os.name", name.sysname);
        add_prop("os.version", name.release);
    }
#endif
    add_prop("java.io.tmpdir", "/tmp");
    char buf[1024];
    add_prop("user.dir", getcwd(buf, 1024));
#endif
}