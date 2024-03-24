#if (defined(SCALANATIVE_COMPILE_ALWAYS) || defined(__SCALANATIVE_VMOFFSET) && \
                                                defined(__APPLE__) &&          \
                                                defined(__MACH__))
#include <mach-o/dyld.h>
#include <stdio.h>
#include <stdint.h>
#include <string.h>

// see:
// https://stackoverflow.com/questions/10301542/getting-process-base-address-in-mac-osx
// https://developer.apple.com/library/archive/documentation/System/Conceptual/ManPages_iPhoneOS/man3/dyld.3.html
intptr_t scalanative_get_vmoffset() {
    char path[1024];
    uint32_t size = sizeof(path);
    if (_NSGetExecutablePath(path, &size) != 0)
        return -1;
    for (uint32_t i = 0; i < _dyld_image_count(); i++) {
        if (strcmp(_dyld_get_image_name(i), path) == 0)
            return _dyld_get_image_vmaddr_slide(i);
    }
    return 0;
}

#else

// should be unused, we can get vmoffset from /proc/pid/maps at least in Linux.
// Not sure about windows.
#include <stdint.h>
intptr_t scalanative_get_vmoffset() { return 0; }

#endif