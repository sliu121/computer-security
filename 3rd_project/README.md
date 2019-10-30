# Build a connection using SSL

*implement a client and a server using Secure Socket Layer (SSL). Upon connection, the client prompts the user to enter his/her ID and password. After the user enters the ID and the password, the client sends the ID and password to the server through SSL connection.*

## Introduction
* The server maintains a file password which has the following format:

	```
		<user ID> <hashed password> <date and time when the password is stored>
    ```

* The password can be hashed using `SHA1` or `MD5`. Use the existing implementation of
`SHA1` and `MD5`.

* When _gen-pass/Gen-pass_ is invoked, it prompts the person who invokes gen-pass to enter each
user’s ID and password. Your program then computes the hash of the password, and saves ID,
the hashed password, and the date and time when the password is saved to file password. Your
program should also check whether the ID is already in the file. If so, your program displays ` “the
ID already exists”`.

## How to use
* **The SSL server is :** 

	`sslserv <server_port>` 

	*`<server_port>` specifies the port number on which the server listens for the connection.*

* **The SSL client is :** 

	`sslcli <server_domain> <server_port>` 

	*`<server_domain>` specifies the domain name of the server*