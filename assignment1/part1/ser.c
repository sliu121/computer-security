#include <stdio.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <string.h>
#include <strings.h>
#include <unistd.h>
#include <time.h>
#include <sys/wait.h>

#define MAXLENTH 1000

void minish(char cmd[],char *result)
{
    if (cmd[strlen(cmd) - 1] == '\n') 
	{
        cmd[strlen(cmd) - 1] = '\0';
    }
    if (strcmp(cmd, "pwd") == 0) 
	{
        char buffer[MAXLENTH];
		getcwd(buffer, MAXLENTH);/*获取当前路径*/
        printf("%s\n", buffer);
        strcpy(result, buffer);/*把地址传送到result*/
        result[strlen(result)] = '\n';

    } else if (strncmp(cmd, "cd ", 3) == 0) 
	{
        if (chdir(&cmd[3])<0)/*从cd 后面的东西开始看路径是否存在，然后返回值为0表示成功*/ 
		{
            int n = 0;
            char buf[100];/*以下代码将cmd[3+n]的内容传送到buf中*/
            while (cmd[n+3]!='\0') 
			{
                buf[n] = cmd[n+3];
                n++;
            }
            sprintf(result,"%s: does not exist\n",buf);/*把buf中的内容返回到result中*/
        }
		else
		{
			result[0]='\1';/*打开文件成功*/
		}
    }
	else if((strcmp(cmd, "ls") == 0)||(strncmp(cmd, "mkdir ", 6) == 0)||(strncmp(cmd, "rmdir ", 6) == 0)||(strcmp(cmd, "exit") == 0))
	{
        int fd[2];
        pipe(fd);/*fd[0] is set up for reading, fd[1] is set up for writing*/

        int pid = fork();/*fork生成child process，负数表示child process生成失败，0表示返回至最新创建的child process, 正数返回至parent process or caller The value contains process ID of newly created child process.*/
        if (pid > 0) 
		{
            while(waitpid(pid/*小于-1：等待进程组识别码为pid绝对值的任何子进程，-1：等待任何子进程相当于wait();0:等待进程组识别码与目前进程相同的任何子进程；大于0：等待任何子进程识别码为pid的子进程*/, NULL/*不在意结束状态值，则参数status可以设成NULL*/, 0)!=pid);/*返回子进程识别码*/
            close(fd[1]);
            read(fd[0], result, MAXLENTH);/*把参数fd所指的文件传送MAXLENTH个字节到result指针所指的内存中。*/
            dup2(fd[0], 0);/*相当于close(fd[0]),fcntl(fd[0],F_DUPFD,0),0 is STDIN,把标准输入内容传送到fd[0]里*/
        }
        else if (pid == 0) 
		{
            char *parts[128];
            int np = 0, i;
            char *start = cmd;

            for (i = 0; i < strlen(cmd); i++) 
			{
                if (cmd[i] == ' ') 
				{
                    cmd[i] = '\0';
                    parts[np] = start;
                    np++;
                    start = &cmd[i + 1];
                }
                if (cmd[i] == '-') 
				{
                    strcpy(result,"invalid command!\n");
                    exit(1);
                }
            }
            parts[np] = start;
            np++;
           
            parts[np] = NULL;
     
            dup2(fd[1], 2);/*2 is error standard output*/
            dup2(fd[1], 1);/*1 is STDOUT*/ 
            int err = execvp(parts[0], parts);
            exit(err);
        } 
		else 
		{
            exit(EXIT_FAILURE);
        }
    }
	else
	{
        strcpy(result,"invalid command!\n");

    }
}




int main(int argc, char **argv) 
{
    int listenfd, connfd;
    socklen_t clilen;
    struct sockaddr_in servaddr, cliaddr;
    char buff[100];
    time_t ticks;
    
    /* Create a TCP socket */
    listenfd = socket(AF_INET, SOCK_STREAM, 0);
    
    /* Initialize server's address and well-known port */
    bzero(&servaddr, sizeof(servaddr));
    servaddr.sin_family      = AF_INET;
    servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    servaddr.sin_port        = htons(10010);   /* daytime server */
    
    /* Bind server’s address and port to the socket */
    bind(listenfd, (struct sockaddr *) &servaddr, sizeof(servaddr));
    /* Convert socket to a listening socket – max 100 pending clients*/
    listen(listenfd, 100);
    
    
    for ( ; ; ) {
        /* Wait for client connections and accept them */
        clilen = sizeof(cliaddr);
        connfd = accept(listenfd, (struct sockaddr *)&cliaddr, &clilen);
        
        /* Retrieve system time */
        ticks = time(NULL);
        snprintf(buff, sizeof(buff), "%.24s\r\n", ctime(&ticks));
        printf("%s\r\n", ctime(&ticks));
        
        /* Write to socket */
        write(connfd, buff, strlen(buff));
        
        //reading  command
        ssize_t read_n;
        char cmd[256];
        while ( (read_n = read(connfd, cmd, 256)) >= 0) {
            
            if (read_n == 0 ) {// exit
                perror("connected failed");
                close(connfd);
                exit(1);
            }else{
                cmd[read_n] = '\0';
                printf("%s", cmd);
                char result[MAXLENTH]={'\0'};
                minish(cmd, result);
                /*write result to client*/
                if (result[0] == '\0') {/*return \0 will terminate the connection*/
                    result[0] = '\1';/*at the beginning position of title*/
                }
                write(connfd, result, strlen(result));
            }
        }
        if (read_n < 0) { perror("read"); close(connfd);exit(5); }
        
    }
}

