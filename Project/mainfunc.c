#include<stdio.h>
#include<string.h>
#include<time.h>


#define MAXNUM 1024
#define TOTAL_LINE 2

struct voters
{
	char voter_name[MAXNUM];
	int voter_regno[MAXNUM];
	int isVoterd;
	char voterres[MAXNUM];
};

struct candidate
{
	char cand_name[MAXNUM];
	int voted_num
};

/*struct voteinfo
{
	int voteing_regno[MAXNUM];
	int vote_num;
	char votetimes[MAXNUM];
};*/

/*check whether the voter has voted*/
bool is_voter_voted(voters voter) {
	FILE *fp_r;					// read the file 
	fp_r = ("./history", "r");
	struct voters *read_inf;
	
	for (int i = 0; i <= TOTAL_LINE ; i++)
	{
		read_inf = malloc(sizeof(struct voters));		
		fscanf(fp_r, "%d %s", (*read_inf).voter_regno, (*read_inf).voterres);	//read from "history"file, 
		if (voter.voter_regno == read_inf->voter_regno)	//				to check if there is a same reg name
		{
			voter.isVoted = 1;	
		}
		free(read_inf);
	}
	fclose(fp_r);
	if (voter.isVoted == 0)
	{
		return true;
	}
	else
	{
		return false;
	}
}
/*save vote info*/
void vote_to_people(int vote_num, voters voter) {
	if (!is_voter_voted(voter))
	{
		printf("you have already voted\n");
		return;
	}
	else {
		FILE *fp_w;
		fp_w = fopen("./history", "a+");
		char vote_date[MAXNUM] = { '\0' };
		time_t t;
		t = time(NULL);
		vote_date = ctime(&t);	// add time
		fwrite(voter.regnum, sizeof(int), MAXNUM, fp_w);	// write reg number into the file
		fputs(vote_date, fp_w);	//write time into the file, because ctime() has added \n so seems we dont need add \n.
		fclose(fp_w);

		if (vote_num == 1)	// when the num = 1; it means voter votes to Tim, so we need 
		{					// change the number in the file 
			FILE *fp_w;
			fp_w = fopen("./result", "a+");
			struct candidate *read_info;
			for (int i = 0; i < TOTAL_LINE; i++) 
			{
				read_info = (int)malloc(sizeof(struct candidate));
				fscanf("%s %d", read_info->cand_name, read_info->voted_num);
				if (read_info->cand_name == "Tim")	// when we had read->cand_name equals to 
				{									// Tim, we need change the read->num
					read_info->voted_num += 1;
				}
				fputs(read_info->cand_name, fp_w);
				fputs(" ", fp_w);
				fwrite(read_info->voted_num, fp_w);	//fwrite seems to write the arr in the file?
				free(read_info);
			}
			fclose(fp_w);
		}
		else if (vote_num == 2)
		{
			FILE *fp_w;
			fp_w = fopen("./result", "a+");
			struct candidate *read_info;
			for (int i = 0; i < TOTAL_LINE; i++)
			{
				read_info = (int)malloc(sizeof(struct candidate));
				fscanf("%s %d", read_info->cand_name, read_info->voted_num);
				if (read_info->cand_name == "Linda")	//when we had read->name equals to 
				{										//Linda, we need inc read->num by 1
					read_info->voted_num += 1;
				}
				fputs(read_info->cand_name, fp_w);
				fputs(" ", fp_w);
				fwrite(read_info->voted_num, fp_w);
				free(read_info);
			}
			fclose(fp_w);

		}
		else
		{
			printf("Invalid vote number!\n");
		}
		
		return;
	}
	
}