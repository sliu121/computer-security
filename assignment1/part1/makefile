all:ser cli
ser:ser.c
ifeq ($(shell uname), SunOS)
	gcc ser.c -o telnetser -lsocket -lnsl -std=c99
else
	gcc ser.c -o telnetser -std=c99
endif
cli:cli.c
ifeq ($(shell uname), SunOS)
	gcc cli.c -o telnetcli -lsocket -lnsl -std=c99
else
	gcc cli.c -o telnetcli -std=c99
endif