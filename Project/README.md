# Secure Virtual Election Booth

*The project implements a secure virtual election booth. The implementation provides a secure way
for people to vote online.*
---

## Introduction
This project implements a secure virtual election booth. The implementation provides a secure way
for people to vote online. The secure virtual election booth meets the following requirements:
* No one can vote more than once.
* No one can determine the candidate for whom anyone else voted.

Assume that there are 3 voters (Alice, Bob, John) and 2 candidates (Tim and Linda). Each voter has a voter
registration number. The voter registration number is given below, which is stored in a file `voterinfo`:
* **Alice 112550000**
* **Bob 113880000**
* **John 114660000**

The voters connect to the Voting Facility (VF) to vote. All voters have the public keys of VF
and VF has the public keys of all voters. *Manually generate the public keys for VF and all voters,
store them in files, and then used them in the program*

The VF server must be a concurrent server that enables **multiple voters to connect to the
server simultaneously**

----
## System Should be...
*|| represent concatenation, pub(X) represent the public-key of X, priv(X) represent the private-key of X, DS(X) represent the digital signature of X, and E(K,M) represent encrypting message M using key K.*

1. The voter invokes `voter-cli` to connect to the VF server
2. Upon connection, the voter is prompted to enter his/her name and voter registration number vnumber
3. After the voter enters vnumber, `voter-cli` sends `E(pub(VF), name||vnumber)||DS(name)` to the VF
server (*||* represents concatenation).
4. The VF server checks whether the name and vnumber received match the information in file
voterinfo. If not, the VF server sends 0 to `voter-cli`. `voter-cli` then prints ***" Invalid name or
registration number"*** and terminates the connection.
Otherwise, VF sends 1 to `voter-cli`. `Voter-cli` prints the user’s name and prompts the user to select
an action to perform:
	```
	Welcome, <user’s name>
			Main Menu
		Please enter a number (1-4)
		1. Vote
		2. My vote history
		3. Election result
		4. Quit
	```
5. If the voter enters ***"1"***, then `voter-cli` sends 1 to the VF server. The VF server checks whether the
voter has voted (based on file history describe in Step 6). If so, VF sends 0 to `voter-cli`, and `voter-cli`
prints ***" you have already voted"*** and displays the Main menu. Otherwise, VF sends 1 to `voter-cli` and
`voter-cli` displays the following:
	```
	Please enter a number (1-2)
	1. Tim
	2. Linda
	```
    
6. After the user enters the number, the client sends the number to VF encrypted using the public-key
of VF. The VF server then updates the result in file ***"result"*** which has the following Format
(initially the total number of votes is 0):
	```
	Tim <the total number of votes>
	Linda <the total number of votes>
	```
	VF also adds the date and time when the voter votes to a file ***"history"*** that has the following
format (if history does not exist, then create the file):
	`<registration number> <date and time when the voter votes>`
	If the user is the last user who voted (i.e. all users have voted), then the VF server prints the results
using the following format:
	```
	<Candidate’s name> Win
	Tim <the total number of votes>
	Linda <the total number of votes>
	```
	Vote-cli then displays Main Menu in step 4. Otherwise, go to Main Menu in step 4.
7. If the user enters ***"2"*** (i.e. My vote history), the VF server retrieves the corresponding entry in file
***"history"*** and sends the entry to `voter-cli`. `voter-cli` then displays the entry to the user. Go to
Main Menu in step 4
8. If the user enters ***"3"*** (i.e. election result), then the VF server checks whether all users have voted. If
not, then VF sends 0 to `voter-cli` and `voter-cli` displays ***"the result is not available"***
Otherwise, VF sends `voter-cli` the candidate who wins the election and the number of votes each
candidate got. `Voter-cli` then displays the results with the following format:
	```
	<Candidate’s name> Win
	Tim <the total number of votes>
	Linda <the total number of votes>
	```
9. If the user enters ***"4"***, then `voter-cli` terminates.

---
## How to Start

   * make : it will generate all class file in each file
   * java Vf 8000: run server
   * java voter_cli `<IP address>` 8000: run client
   * clean: remove all *.class file in each file 
 ---
 
## Encryption/Decryption

***Encryption***:
    	
        SealedObject encrypt(Key key,String plain_text) {
        	this.cipher.init(Cipher.ENCRYPT_MODE,key);
            SealedObject zipbag = new SealedObject(plain_text,this.cipher);
            return  zipbag;
    	}
  
***Decryption***:
		
        
		Object decrypt(Key key, SealedObject encrypted){
	        Cipher c = Cipher.getInstance("RSA");
	        c.init(Cipher.DECRYPT_MODE, key);
	        return encrypted.getObject(c);
        }