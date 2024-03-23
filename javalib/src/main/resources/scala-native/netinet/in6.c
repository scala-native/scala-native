#if defined(__SCALANATIVE_JAVALIB_NETINET_IN6)
#ifndef _WIN32
#include <netinet/in.h>
#endif

/* Internet Engineering Task Force (IETF) RFC2553 describes in6.h
 * being accessed via netinet/in.h, which includes it, and not directly.
 */

// This file implements only the sole declaration need by java.net.

int scalanative_ipv6_tclass() {
#ifndef IPV6_TCLASS
    /* Force a runtime error, probably errno 92: "Protocol not available"
     * Do no force link errors for something which is used in the wild
     * only by experts, and then rarely.
     */
    return 0; // 0 is an invalid socket option.
#else
    /* Operating system specific.
     *   Known values: Linus 67, macOS 36, FreeBSD 61.
     *   Windows seems to not have it at all, although WSL might.
     */
    return IPV6_TCLASS;
#endif
}
#endif