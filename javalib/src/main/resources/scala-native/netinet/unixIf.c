#if defined(__SCALANATIVE_JAVALIB_NETINET_UNIXIF)
#if defined(_WIN32)
// No Windows support. These are dummies for linking.
int scalanative_iff_loopback() { return 0; }
int scalanative_iff_multicast() { return 0; }
int scalanative_iff_pointopoint() { return 0; }
int scalanative_iff_up() { return 0; }
void *if_nameindex(void) { return (void *)0; }
void if_freenameindex(void *dummy){};
#else
#include <sys/ioctl.h>
#include <net/if.h>

// Possibility for macOS, which lacks SIOCGIFHWADDR
// https://stackoverflow.com/questions/10593736/
//    mac-address-from-interface-on-os-x-c

// Ref: "man 7 netdevice"

// Symbolic constants

int scalanative_ifnamesiz() { return IFNAMSIZ; }

/* Broadcast address valid.  */
int scalanative_iff_broadcast() { return IFF_BROADCAST; }

/* Is a loopback net.  */
int scalanative_iff_loopback() { return IFF_LOOPBACK; }

/* Supports multicast.  */
int scalanative_iff_multicast() { return IFF_MULTICAST; }

/* Interface is point-to-point link.  */
int scalanative_iff_pointopoint() { return IFF_POINTOPOINT; }

/* Resources allocated.  */
int scalanative_iff_running() { return IFF_RUNNING; }

/* get flags */
int scalanative_siocgifflags() { return SIOCGIFFLAGS; }

// FIXME macOS appears to not have this ioctl. Hard to find replacement.

#ifndef SIOCGIFHWADDR
#define SIOCGIFHWADDR 0 // cause failure
#endif
/* Get hardware address */
int scalanative_siocgifhwaddr() { return SIOCGIFHWADDR; }

/* get MTU size */
int scalanative_siocgifmtu() { return SIOCGIFMTU; }

/* Interface is up.  */
int scalanative_iff_up() { return IFF_UP; }

#endif // !_WIN32
#endif // defined(__SCALANATIVE_JAVALIB_NETINET_UNIXIF)
