#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <strings.h>
#include <string.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <netdb.h>

#define MAXLENTH 1000



int main(int argc, char **argv) {
	int  sockfd;
	ssize_t n;
	char recvline[MAXLENTH];
	struct sockaddr_in servaddr;
	struct hostent *he;

	if (argc != 2) {
		printf("Usage : gettime <IP address>\n");
		exit(1);
	}
	if ((he = gethostbyname(argv[1])) == NULL) {  // get the host info
		perror("gethostbyname");
		exit(2);
	}
	/* Create a TCP socket */
	if ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
		perror("socket"); exit(2);
	}

	/* Specify server’s IP address and port */
	bzero(&servaddr, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_port = htons(10010); /* daytime server port */

	if (inet_pton(AF_INET, inet_ntoa(*(struct in_addr *)he->h_addr_list[0]), &servaddr.sin_addr) <= 0) {
		perror("inet_pton"); exit(3);
	}

	/* Connect to the server */
	if (connect(sockfd, (struct sockaddr *) &servaddr, sizeof(servaddr))
		< 0) {
		perror("connect"); exit(4);
	}


	/* Read the date/time from socket */
	while (1) {
		memset(recvline, 0, MAXLENTH);
		while ((n = read(sockfd, recvline, MAXLENTH)) >= 0) {
			recvline[n] = '\0';        /* null terminate */
			if (n == 0) {// exit
				close(sockfd);
				exit(1);
			}
			printf("%s", recvline);
			/*write command */
			char str[256];
			printf("telnet>");
			fgets(str, 256, stdin);
			if (strcmp(str, "exit\n") == 0) {// exit
				close(sockfd);
				exit(0);
			}
			write(sockfd, str, strlen(str));
		}

		if (n < 0) { perror("read"); exit(5); }
	}


}

