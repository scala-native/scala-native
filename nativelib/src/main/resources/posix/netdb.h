#include <netdb.h>
#include "sys/socket.h"

struct scalanative_addrinfo
{
  int ai_flags;			/* Input flags.  */
  int ai_family;		/* Protocol family for socket.  */
  int ai_socktype;		/* Socket type.  */
  int ai_protocol;		/* Protocol for socket.  */
  socklen_t ai_addrlen;		/* Length of socket address.  */
  struct sockaddr *ai_addr;	/* Socket address for socket.  */
  char *ai_canonname;		/* Canonical name for service location.  */
  void *ai_next;		/* Pointer to next in list.  */
};

void scalanative_convert_scalanative_addrinfo(struct scalanative_addrinfo *in,
					      struct addrinfo *out);

void scalanative_convert_scalanative_addrinfo_list(struct scalanative_addrinfo **in,
						   struct addrinfo **out);

void scalanative_convert_addrinfo(struct addrinfo *in,
				  struct scalanative_addrinfo *out);

void scalanative_convert_addrinfo_list(struct addrinfo **in,
				       struct scalanative_addrinfo **out);
