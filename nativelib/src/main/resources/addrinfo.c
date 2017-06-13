#include <sys/socket.h>
#include <sys/types.h>
#include <netdb.h>
#include <string.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <netinet/ip_icmp.h>
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

// based on: https://stackoverflow.com/a/20105379/3645391
bool scalanative_is_reachable_by_icmp(char* ip, int time, bool v6) {
	struct sockaddr addr;
	int family = AF_INET;
	if(v6) {
		struct sockaddr_in6 addr6;
		inet_pton(AF_INET6, ip, &(addr6.sin6_addr));
		addr6.sin6_family = AF_INET6;
		addr = *((struct sockaddr *)&addr6);
		family = AF_INET6;
	}
	else {
		struct sockaddr_in addr4;
		inet_pton(AF_INET, ip, &(addr4.sin_addr)); 
		addr4.sin_family = AF_INET;
		addr = *((struct sockaddr *)&addr4);
	}

	struct icmphdr icmp_hdr;
	int sequence = 0;
	int sock = socket(family, SOCK_DGRAM, IPPROTO_ICMP);
	if(sock < 0) {
		return false;
	}

	memset(&icmp_hdr, 0, sizeof icmp_hdr);
	icmp_hdr.type = ICMP_ECHO;
	icmp_hdr.un.echo.id = 1337; // arbitrary id

	unsigned char data[2048];
	int rc;
	struct timeval timeout;
	timeout.tv_sec = time / 1000;
	timeout.tv_usec = (time % 1000) * 1000;
	fd_set read_set;
	socklen_t slen;
	struct icmphdr rcv_hdr;

	icmp_hdr.un.echo.sequence = sequence++;
	memcpy(data, &icmp_hdr, sizeof icmp_hdr);
        memcpy(data + sizeof icmp_hdr, "echo", 4); //icmp payload
        rc = sendto(sock, data, sizeof icmp_hdr + 5,
                0, &addr, sizeof addr);
        if (rc <= 0) {
		return false;
        }

        memset(&read_set, 0, sizeof read_set);
        FD_SET(sock, &read_set);

        //wait for a reply with a timeout
        rc = select(sock + 1, &read_set, NULL, NULL, &timeout);
        if (rc <= 0) {
		return false;
        }

        //we don't care about the sender address in this example..
        slen = 0;
        rc = recvfrom(sock, data, sizeof data, 0, NULL, &slen);
        if (rc <= 0) {
		return false;
        } else if (rc < sizeof rcv_hdr) {
		return false;
        }
        memcpy(&rcv_hdr, data, sizeof rcv_hdr);
        if (rcv_hdr.type == ICMP_ECHOREPLY) {
		return true;
        } 
	return false;
}

bool scalanative_is_reachable_by_echo(char* ip, int time, bool v6) {
	struct sockaddr addr;
	int family;
	if(v6) {
		struct sockaddr_in6 addr6;
		inet_pton(AF_INET6, ip, &(addr6.sin6_addr));
		addr6.sin6_family = AF_INET6;
		addr6.sin6_port = htons(7);
		addr = *((struct sockaddr *)&addr6);
		family = AF_INET6;
	}
	else {
		struct sockaddr_in addr4;
		inet_pton(AF_INET, ip, &(addr4.sin_addr)); 
		addr4.sin_family = AF_INET;
		addr4.sin_port = htons(7);
		addr = *((struct sockaddr *)&addr4);
		family = AF_INET;
	}

	int sock = socket(family, SOCK_STREAM, 0);
	if(sock < 0) {
		return false;
	}
	if(connect(sock, &addr, sizeof(addr)) < 0){
		return false;
	}

	int rc;
	struct timeval timeout;
	timeout.tv_sec = time / 1000;
	timeout.tv_usec = (time % 1000) * 1000;
	fd_set read_set;

	rc = send(sock, "echo", strlen("echo"), 0); 
        if (rc < strlen("echo")) {
		return false;
        }

        memset(&read_set, 0, sizeof read_set);
        FD_SET(sock, &read_set);

        //wait for a reply with a timeout
        rc = select(sock + 1, &read_set, NULL, NULL, &timeout);
        if (rc <= 0) {
		return false;
        }

	char buf[5];
	rc = recv(sock, buf, 5, 0);
	if(rc < strlen("echo")){
		return false;
	}
	return true;
}
