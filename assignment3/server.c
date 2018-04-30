#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <sys/wait.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <openssl/ssl.h>
#include <openssl/err.h>
#include <stdbool.h>
#include <openssl/md5.h>
#include <time.h>
#include <stdbool.h>

#define MAXBUF 1024


bool compareIDpassword(char ID[],char password[]){
    MD5_CTX ctx;
    unsigned char outmd[16];
    char temp[100] = {'\0'};
    char realoutmd[1000];
    memset(realoutmd,0,sizeof(realoutmd));
    memset(outmd,0,sizeof(outmd));
    MD5_Init(&ctx);
    MD5_Update(&ctx,password,strlen(password));
    MD5_Final(outmd,&ctx);
    for(int i=0;i<16;i++)
    {
        sprintf(temp,"%02X",outmd[i]);
        strcat(realoutmd,temp);
    }
    //printf("password:%s\n",password);
    //printf("realomd:%s\n",realoutmd);
    FILE *fp;
    fp = fopen("password.txt", "r");
    fseek(fp,0,SEEK_SET);
    bool comparision = false;
    while(!feof(fp)) {
        char new_result[100] = {'\0'};
        fgets(new_result, sizeof(new_result), fp);
        char *oldID = strtok(new_result, ";");
        if (oldID != NULL) {
            if (strcmp(ID,oldID) == 0){
                char *oldpassword = strtok(NULL, ";");
                    //printf("oldpassword:%s\n",oldpassword);
                if (strcmp(realoutmd,oldpassword)==0) {
                    comparision = true;
                }
                break;
            }
        }
    }
    fclose(fp);
    return comparision;
}

bool readfile(char ID[]){
    //printf("open the file to find %s.\n",ID);

    FILE *fp;
    fp = fopen("password.txt","r");
    fseek(fp,0,SEEK_SET);

    if(!fp){
        printf("Cannot open the file.\n");
    }


    while(!feof(fp)){
        char buffer[100] = {'\0'};
        fgets(buffer,sizeof(buffer),fp);
        char *p = strtok(buffer,";");
                    //printf("p: %s\n",p);

        if(p == NULL) {
                //printf("NULL\n");
            return true;
        }
        if(strcmp(p,ID) == 0){
            fclose(fp);

            printf("The ID already exists.\n");

            return false;
        }
    }
    printf("The ID not already exists.\n");

    fclose(fp);
    return true;
}

int main(int argc, char **argv)
{
int sockfd, new_fd;
socklen_t len;
struct sockaddr_in my_addr, their_addr;
unsigned int myport, lisnum;
char buf[MAXBUF + 1];
SSL_CTX *ctx;

if (argv[1]){
    myport = atoi(argv[1]);
}
else{
    myport = 7838;
}


lisnum = 1;

SSL_library_init();
OpenSSL_add_all_algorithms();
SSL_load_error_strings();
ctx = SSL_CTX_new(SSLv23_server_method());
if (ctx == NULL) {
ERR_print_errors_fp(stdout);
exit(1);
}
if (SSL_CTX_use_certificate_file(ctx, "./cacert.pem", SSL_FILETYPE_PEM) <= 0) {
ERR_print_errors_fp(stdout);
exit(1);
}
if (SSL_CTX_use_PrivateKey_file(ctx, "./privkey.pem", SSL_FILETYPE_PEM) <= 0) {
ERR_print_errors_fp(stdout);
exit(1);
}
if (!SSL_CTX_check_private_key(ctx)) {
ERR_print_errors_fp(stdout);
exit(1);
}
if ((sockfd = socket(PF_INET, SOCK_STREAM, 0)) == -1) {
perror("socket");
exit(1);
} else
printf("socket created\n");

bzero(&my_addr, sizeof(my_addr));
my_addr.sin_family = PF_INET;
my_addr.sin_port = htons(myport);
if (argv[2])
my_addr.sin_addr.s_addr = inet_addr(argv[2]);
else
my_addr.sin_addr.s_addr = INADDR_ANY;


if (bind(sockfd, (struct sockaddr *) &my_addr, sizeof(struct sockaddr))== -1) {
perror("bind");
exit(1);
} else
printf("binded\n");


if (listen(sockfd, lisnum) == -1) {
perror("listen");
exit(1);
} else
printf("begin listen\n");

//while (1) {

    SSL *ssl;
    len = sizeof(struct sockaddr);
    //new_fd = accept(sockfd, (struct sockaddr *) &their_addr, &len);

    if ((new_fd = accept(sockfd, (struct sockaddr *) &their_addr, &len)) == -1) {
        //printf("abc\n");
        perror("accept");
        exit(errno);
    } else
    printf("server: got connection from %s, port %d, socket %d\n",inet_ntoa(their_addr.sin_addr),ntohs(their_addr.sin_port), new_fd);

    ssl = SSL_new(ctx);
    SSL_set_fd(ssl, new_fd);


    for( ; ; ){

        ssl = SSL_new(ctx);
        SSL_set_fd(ssl, new_fd);

        if (SSL_accept(ssl) == -1) {
            perror("accept");
            close(new_fd);
            break;
        }

        else{
            char *inputstring = "Please input your ID and password: (eg:hjiang38 123)\n";

            len = SSL_write(ssl, inputstring, strlen(inputstring));
            if (len <= 0) {
            printf("messages'%s'fail to send!wrong code is %d,wrong message is '%s'\n",buf, errno, strerror(errno));
            SSL_shutdown(ssl);
            SSL_free(ssl);
            close(new_fd);
            }
            else{
                char buffer[1025]={'\0'};
                while(SSL_read(ssl, buffer, 1024) >= 0){
                    if(len == 0){
                        SSL_shutdown(ssl);
                        SSL_free(ssl);
                        close(sockfd);
                        exit(1);
                    }

                    else{
                        char ID[100] = {'\0'};
                        char password[1000] = {'\0'};
                        //printf("buffer: %s\n",buffer);
                        int isID = 1;
                        int index = 0;
                        for (int i=0; i<strlen(buffer); i++) {
                            if (buffer[i]==' ') {
                                isID = 0;
                                index = 0;
                                continue;
                            }else if (buffer[i]=='\n'){
                                    //printf("break\n");
                                    break;
                            }
                            if(isID){
                                ID[index++] = buffer[i];
                            }else{
                                password[index++] = buffer[i];
                            }

                        }
                        //printf("Print IDLength:%d\n",strlen(ID));
                        /*
                        for(int i=0;i<strlen(ID);i++){
                            printf("%c",ID[i]);
                        }
                        */

                        char result[100];
                        if(!readfile(ID)){
                            if(compareIDpassword(ID,password)){
                                strcpy(result, "OK.\n");
                                //printf("correct");
                            }
                            else{
                                strcpy(result, "password is incorrect.\n");
                                //printf("incorrect");
                            }
                        }
                        else{
                            //printf("not find\n");

                            strcpy(result, "password is incorrect.\n");
                        }
                        result[strlen(result)] = '\0';

                        SSL_write(ssl, result, (int)strlen(result));
                    }
                    memset(buffer,0,1024);
                }
                /*
                if (SSL_read(ssl, buffer, 1024) < 0) { perror("read"); close(new_fd);exit(0); }
                */
            }
        }

    //}
}


    return 0;
}
