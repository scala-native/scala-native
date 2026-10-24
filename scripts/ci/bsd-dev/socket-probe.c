/* Check the raw socket ABI used by Scala Native against the host headers. */
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/wait.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <poll.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

/* Scala externs call the unversioned symbol, bypassing NetBSD's __RENAME. */
extern int raw_socket(int, int, int) __asm__("socket");

static void fail(const char *operation) {
    perror(operation);
    exit(1);
}

static void probe(int use_headers, int set_length) {
    int listener = socket(AF_INET, SOCK_STREAM, 0);
    if (listener < 0)
        fail("server socket");
    struct sockaddr_in address = {0};
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = htonl(INADDR_LOOPBACK);
    if (set_length)
        address.sin_len = sizeof(address);
    if (bind(listener, (struct sockaddr *)&address, sizeof(address)))
        fail("bind");
    if (listen(listener, 1))
        fail("listen");
    socklen_t length = sizeof(address);
    if (getsockname(listener, (struct sockaddr *)&address, &length))
        fail("getsockname");
    if (!set_length)
        address.sin_len = 0;
    pid_t child = fork();
    if (child < 0)
        fail("fork");
    if (child == 0) {
        close(listener);
        int client = use_headers ? socket(AF_INET, SOCK_STREAM, 0)
                                 : raw_socket(AF_INET, SOCK_STREAM, 0);
        if (client < 0)
            fail("client socket");
        if (connect(client, (struct sockaddr *)&address, sizeof(address)))
            fail("connect");
        if (write(client, "ping", 4) != 4)
            fail("write");
        close(client);
        _exit(0);
    }
    struct pollfd ready = {listener, POLLIN, 0};
    if (poll(&ready, 1, 5000) != 1)
        fail("accept poll");
    int accepted = accept(listener, NULL, NULL);
    if (accepted < 0)
        fail("accept");
    char buffer[4];
    ssize_t count = read(accepted, buffer, sizeof(buffer));
    if (count != 4 || memcmp(buffer, "ping", 4))
        fail("read");
    close(accepted);
    close(listener);
    int status;
    if (waitpid(child, &status, 0) < 0)
        fail("waitpid");
    if (!WIFEXITED(status) || WEXITSTATUS(status))
        exit(1);
    printf("PASS: %s socket, sin_len=%s, loopback TCP transfer\n",
           use_headers ? "header" : "raw", set_length ? "sizeof" : "zero");
}

int main(void) {
    for (int header = 0; header <= 1; header++)
        for (int length = 0; length <= 1; length++)
            probe(header, length);
    return 0;
}
