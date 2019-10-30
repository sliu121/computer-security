all:client server gen_pass

client:
	gcc client.c -o sslcli -lcrypto -lssl
server:
	gcc server.c -o sslserv -lcrypto -lssl
gen_pass:
	gcc gen_pass.c -o gen-pass -lcrypto -lssl
clean:
	rm sslcli sslserv gen-pass
