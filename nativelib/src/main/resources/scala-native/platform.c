#ifdef _WIN32
#include <Windows.h>
#else
#include <sys/utsname.h>
#include <unistd.h>
#include <string.h>
#include <pwd.h>
#include <stdlib.h>
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

int scalanative_platform_get_all_env(void* obj, void (*add_env)(void*, const char *, const char *)) {
    char buf[4096];
    int result = 0;
#ifdef _WIN32
    LPWCH variables = GetEnvironmentStringsW();
    LPWSTR string= (LPWSTR)variables;
#else
    extern char **environ;
    char** string = environ;
#endif
    while (string && *string)
    {
#ifdef _WIN32
        int length = wcstombs(buf, string, 4096);
        if (length == -1) {
            continue;
        }
        buf[length] = 0;
#else
        strcpy(buf, *string);
#endif
        char* name = buf;
        char* value = strchr(name, '=');
        if (value) {
            if (name < value) {
                *value++ = 0;
                if (add_env) {
                    add_env(obj, name, value);
                }
                ++result;
            }
        }
#ifdef _WIN32
        string+=wcslen(string) + 1;
    }
    FreeEnvironmentStringsW(variables);
#else
        ++string;
    }
#endif
    return result;
}

// See http://stackoverflow.com/a/4181991
int scalanative_little_endian() {
    int n = 1;
    return (*(char *)&n);
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
    char buf[1024];
    if (getcwd(buf, 1024) != NULL) {
    	add_prop("user.dir", buf);
    }

    struct passwd *pBuf = getpwuid(getuid());
    if(pBuf != NULL && pBuf -> pw_dir != NULL) {
    	add_prop("user.home", pBuf -> pw_dir);
    }

#endif
}
