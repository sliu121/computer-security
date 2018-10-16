#include<stdio.h>
void function() 
{
	char buffer1[4];
	int *ret;
	ret = buffer1 + 12;
	(*ret) += 7;
}

int main() {
	int x = 0;
	function();
	x = 1;
	printf(" x = %d\n",x);
	return 0;
}
