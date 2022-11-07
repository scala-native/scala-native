#if defined SCALANATIVE_DYLIB && !defined SCALANATIVE_NO_DYLIB_CTOR

#include <stdlib.h>
#include <stdio.h>

#define NO_DYLIB_CTOR_ENV "SCALANATIVE_NO_DYLIB_CTOR"
extern int ScalaNativeInit(void);

#ifdef _WIN32
#include <windows.h>
BOOL WINAPI DllMain(HINSTANCE hinstDLL, // handle to DLL module
                    DWORD fdwReason,    // reason for calling function
                    LPVOID lpReserved) {
    switch (fdwReason) {
    case DLL_PROCESS_ATTACH:
        // Initialize once for each new process.
        if (!getenv(NO_DYLIB_CTOR_ENV)) {
            if (0 != ScalaNativeInit()) {
                printf("Failed to initialize Scala Native");
                return FALSE;
            }
        }

    case DLL_THREAD_ATTACH:
        break;

    case DLL_THREAD_DETACH:
        break;

    case DLL_PROCESS_DETACH:
        break;
    }
    return TRUE; // Successful DLL_PROCESS_ATTACH.
}
#else
static void __attribute__((constructor)) __scala_native_init(void) {
    if (!getenv(NO_DYLIB_CTOR_ENV)) {
        if (0 != ScalaNativeInit()) {
            printf("Failed to initialize Scala Native");
            exit(1);
        }
    }
}
#endif
#endif // SCALANATIVE_DYLIB