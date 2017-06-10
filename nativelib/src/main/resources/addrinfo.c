#include <sys/socket.h>
#include <sys/types.h>
#include <netdb.h>
#include <string.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdbool.h>
#include <stdlib.h>

char* scalanative_host_to_ip(char* host) {
	int status;
	struct addrinfo hints, *ret;

	char ipstr[INET6_ADDRSTRLEN];

	memset(&hints, 0, sizeof hints);
	hints.ai_family = AF_UNSPEC;
	hints.ai_socktype = 0;

	if((status = getaddrinfo(host, NULL, &hints, &ret)) != 0) {
		return NULL;
	}

	void *addr;

	if(ret->ai_family == AF_INET) {
		struct sockaddr_in *ipv4 = (struct sockaddr_in *)ret->ai_addr;
		addr = &(ipv4->sin_addr);
	}
	else {
		struct sockaddr_in6 *ipv6 = (struct sockaddr_in6 *)ret->ai_addr;
		addr = &(ipv6->sin6_addr);
	}

	inet_ntop(ret->ai_family, addr, ipstr, sizeof ipstr);
	freeaddrinfo(ret);
	char *retip = malloc(INET6_ADDRSTRLEN * sizeof(char));
	strcpy(retip, ipstr);
	return retip;
}

char* scalanative_ip_to_host(char* ip, bool isv4) {
	char host[1024];
	char service[20];
	int status = -1;
	if(isv4) {
		struct sockaddr_in addr4;
		addr4.sin_family = AF_INET;
		inet_pton(AF_INET, ip, &(addr4.sin_addr));
		status = getnameinfo((struct sockaddr *)&addr4, sizeof addr4, host, sizeof host, service, sizeof service, 0);
	}
	else {
		struct sockaddr_in6 addr6;
		addr6.sin6_family = AF_INET6;
		inet_pton(AF_INET6, ip, &(addr6.sin6_addr));
		status = getnameinfo((struct sockaddr *)&addr6, sizeof addr6, host, sizeof host, service, sizeof service, 0);
	}
	if(status == 0) {
		char *rethost = malloc(1024 * sizeof(char));
		strcpy(rethost, host);
		return rethost;
	}
	return NULL;
}

char** scalanative_host_to_ip_array(char *host) {
	struct addrinfo hints, *res, *p;
	int status;
	char ipstr[INET6_ADDRSTRLEN];

	memset(&hints, 0, sizeof hints);
	hints.ai_family = AF_UNSPEC;
	hints.ai_socktype = SOCK_STREAM;

	char** retarr = malloc(sizeof(char*));
	
	if((status = getaddrinfo(host, NULL, &hints, &res)) != 0) {
		return NULL;
	}

	int count = 0;
	for(p = res; p != NULL; p = p->ai_next) {
		void* addr;
		char *ipver;
		char** newretarr = realloc(retarr, (count + 1) * sizeof(char*));
		if(newretarr == NULL)
			return NULL;
		retarr = newretarr;

		if(p->ai_family == AF_INET) {
			struct sockaddr_in *ipv4 = (struct sockaddr_in *)p->ai_addr;
			addr = &(ipv4->sin_addr);
		}
		else {
			struct sockaddr_in6 *ipv6 = (struct sockaddr_in6 *)p->ai_addr;
			addr = &(ipv6->sin6_addr);
		}
		inet_ntop(p->ai_family, addr, ipstr, sizeof ipstr);
		retarr[count] = malloc(INET6_ADDRSTRLEN * sizeof(char));
		strcpy(retarr[count], ipstr);
		count++;
	}
	retarr[count] = malloc(4 * sizeof(char));
	strcpy(retarr[count], "END");
	freeaddrinfo(res);
	return retarr;
}
