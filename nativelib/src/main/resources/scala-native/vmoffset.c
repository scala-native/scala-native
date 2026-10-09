#if (defined(SCALANATIVE_COMPILE_ALWAYS) ||                                    \
     defined(__SCALANATIVE_VMOFFSET)) &&                                       \
    defined(__APPLE__) && defined(__MACH__)
#include <mach-o/dyld.h>
#include <stdio.h>
#include <stdint.h>
#include <string.h>

// see:
// https://stackoverflow.com/questions/10301542/getting-process-base-address-in-mac-osx
// https://developer.apple.com/library/archive/documentation/System/Conceptual/ManPages_iPhoneOS/man3/dyld.3.html
int64_t scalanative_get_vmoffset() {
    char path[1024];
    uint32_t size = sizeof(path);
    if (_NSGetExecutablePath(path, &size) != 0)
        return -1;
    for (uint32_t i = 0; i < _dyld_image_count(); i++) {
        if (strcmp(_dyld_get_image_name(i), path) == 0)
            return (int64_t)_dyld_get_image_vmaddr_slide(i);
    }
    return 0;
}

#elif defined(_WIN32)

#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <stdint.h>

int64_t scalanative_get_vmoffset() {
    // Runtime load base; Backtrace combines this with the PE preferred ImageBase
    // parsed from the on-disk executable to translate runtime PCs to DWARF VAs.
    return (int64_t)(uintptr_t)GetModuleHandleW(NULL);
}

#else

// Linux and other ELF targets resolve the slide from /proc/pid/maps in vmoffset.
#include <stdint.h>
int64_t scalanative_get_vmoffset() { return 0; }

#endif