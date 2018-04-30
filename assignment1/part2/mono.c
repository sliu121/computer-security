#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<time.h>

#define MAXNUM 25
#define MAXLETTER 10000

char input_str;


struct ID {
	char input_letter;
	char myth_letter;
};
struct ID id[MAXNUM];


char encryptID(struct ID id[], char str)
{
	char buffer;
	for (int i = 0; i <= MAXNUM; i++)
	{
		if (id[i].input_letter == str)
		{
			buffer = id[i].myth_letter;
			return buffer;
		}
		else if (str == ' '||str == '\n')
		{
			buffer = str;
			return buffer;
		}
	}
}

char decryptID(struct ID id[],char str)
{
	char buffer;
	for (int i = 0; i <= MAXNUM; i++)
	{
		if (id[i].myth_letter == str)
		{
			buffer = id[i].input_letter;
			return buffer;
		}
		else if (str == ' '|| str == '\n')
		{
			buffer = str;
			return buffer;
		}
	}
	
}

struct ID initializeID(struct ID id, int count,char text)
{
	char alphabet[] = { 'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z' };
	id.input_letter = alphabet[count];
	id.myth_letter = text;
	return id;
}

void print_initialize_string(struct ID str[])
{
	for (int i = 0; i <= MAXNUM; i++)
	{
		printf("%c - %c\n",str[i].input_letter, str[i].myth_letter);
	}
	printf("\n");
}

int main(int argc,char *argv[])
{
	int seed = *argv[3];
	char buffer[MAXLETTER];
	char str[MAXLETTER];
	char out_read[MAXLETTER];


	/*seed and create the cipher*/

	srand((unsigned)seed);
	struct ID var[MAXNUM];
	char text[] = { 'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z' };
	for (int i = 0; i <= MAXNUM; i++)
	{
		char temp;
		int Randnum = MAXNUM - i;
		if (Randnum == 0)
		{
			var[i] = initializeID(var[i], i, text[Randnum]);
			break;
		}

		int randnum = rand() % Randnum;
		var[i] = initializeID(var[i], i, text[randnum]);

		temp = text[randnum];
		text[randnum] = text[Randnum];
		text[Randnum] = temp;
	}


	print_initialize_string(var);

	/*read the file*/
	FILE *fp_r;
	fp_r = fopen(argv[1], "r");
	if ((fp_r = fopen(argv[1], "r")) == NULL)
	{
		printf("cant open and read the inputfile.\n");
		return 0;
	}
	else {
		fgets(str,MAXLETTER, fp_r);
		printf("Before the operation: %s\n", str);
	}
	fclose(fp_r);

	/*write the file*/
	FILE *fp_w;
	fp_w = fopen(argv[2], "w+");
	
	if ((fp_w = fopen(argv[2], "a+")) == NULL)
	{
		printf("cant open and write the file\n");
		return 0;
	}
	else if (*argv[4]=='1')
	{
		for (int i = 0; i < strlen(str); i++)
		{
			buffer[i] = encryptID(var, str[i]);
		//	printf("%c\n", buffer[i]);
		}
		fputs(buffer, fp_w);
	} 
	else if (*argv[4]=='0')
	{
		for (int i = 0; i < strlen(str); i++)
		{
			buffer[i] = decryptID(var, str[i]);
		}
		fputs(buffer, fp_w);
	}
	fclose(fp_w);
	/*output the file*/

	FILE *fp_out;
	fp_out = fopen(argv[2], "r");
	if ((fp_out = fopen(argv[2], "r")) == NULL)
	{
		printf("cant open and output the file\n");
		return 0;
	}
	else
	{
		fgets(out_read,MAXLETTER,fp_out);
		printf("After the operation: %s\n", out_read);
	}

	fclose(fp_out);

	return 0;

}