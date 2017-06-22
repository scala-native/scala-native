#include "netdb.h"
#include <stddef.h>
#include <stdlib.h>

void scalanative_convert_scalanative_addrinfo(struct scalanative_addrinfo *in,
				  struct addrinfo *out) {
	out->ai_flags = in->ai_flags;
	out->ai_family = in->ai_family;
	out->ai_socktype = in->ai_socktype;
	out->ai_protocol = in->ai_protocol;
	out->ai_addrlen = in->ai_addrlen;
	out->ai_addr = in->ai_addr;
	out->ai_canonname = in->ai_canonname;
	if(in->ai_next != NULL) {
		struct addrinfo *converted = malloc(sizeof(struct addrinfo)); 
		scalanative_convert_scalanative_addrinfo(
				(struct scalanative_addrinfo *)in->ai_next, converted);
		out->ai_next = converted;
		return;
	}
	else {
		out->ai_next = NULL;
		return;
	}
}

void scalanative_convert_addrinfo(struct addrinfo *in, struct scalanative_addrinfo *out) {
	out->ai_flags = in->ai_flags;
	out->ai_family = in->ai_family;
	out->ai_socktype = in->ai_socktype;
	out->ai_protocol = in->ai_protocol;
	out->ai_addrlen = in->ai_addrlen;
	out->ai_addr = in->ai_addr;
	out->ai_canonname = in->ai_canonname;
	if(in->ai_next != NULL) {
		struct scalanative_addrinfo *converted = malloc(
				sizeof(struct scalanative_addrinfo));
		scalanative_convert_addrinfo(in->ai_next, converted);
		out->ai_next = (void *)converted;
	}
	else {
		out->ai_next = NULL;
	}
}

void scalanative_convert_addrinfo_list(struct addrinfo **in,
				       struct scalanative_addrinfo **out) {
	scalanative_convert_addrinfo(*in, *out);
}


int scalanative_getaddrinfo(char *name, char *service,
			    struct scalanative_addrinfo *hints,
			    struct scalanative_addrinfo **res) {
	struct addrinfo hints_converted;
	struct addrinfo *res_c;
	scalanative_convert_scalanative_addrinfo(hints, &hints_converted);
	int status = getaddrinfo(name, service, &hints_converted, &res_c);
	scalanative_convert_addrinfo_list(&res_c, res); 
	free(res_c);
	return status;
}


