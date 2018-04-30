#include <stdio.h>
#include <stdbool.h>
#include <openssl/md5.h>

#include <string.h>
#include <time.h>
void saveinformation(char ID[],char password[]){
    FILE *fp;
	time_t ticks = time(NULL);
    char savedTime[100]={'\0'};
    snprintf(savedTime, sizeof(savedTime), "%.24s", ctime(&ticks));
	char information[1000] = {'\0'};
	sprintf(information,"%s;%s;%s\n",ID,password,savedTime);
	fp = fopen("password.txt","a+");
	fseek(fp,0,SEEK_END);
    fputs(information,fp);
    fclose(fp);
}

bool readfile(char ID[]){
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
        if(p == NULL) {
            return true;
        }
        if(strcmp(p,ID) == 0){
            fclose(fp);
            printf("The ID already exists.\n");
            return false;
        }
    }
    fclose(fp);
    return true;
}

int main(){
	char ID[100] = {'\0'};
	char password[1000] = {'\0'};
    printf("Input your ID: ");
	scanf("%s", ID);
	printf("Input your password: ");
	scanf("%s", password);

	for(int i=0;i<strlen(password);i++){
        if(password[i] == '\n') {password[i]= '\0';printf("find N\n");}
	}
    printf("password:%s\n",password);

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
        printf("realoutmd:%s\n",realoutmd);

    /*
    saveinformation(ID,realoutmd);
    */

    bool judge = readfile(ID);
    if(!judge){
        int a = 1;
    }
    else{
        saveinformation(ID,realoutmd);
    }

}
