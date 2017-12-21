#####ASSIGNMENT 1: QUIT######
Link to the repository:
https://git.cs.bham.ac.uk/pxm673/messaging

	Approach
I send a message trough all the "circuit" client sender->server receiver -> server sender-> client receiver. In each thread i exit the while loop after sending the message further until i reach client receiver. This approach works now, but when we will have to deal with users having the same name, the code will need changes. I will have to identify the client by his uique id and not by name when sending the message.
Another way of doing it would be to have an object wich has acces to all 4 threads , so that i can call notify ( after i would call wait in the thread) , making an domino effect . Calling wait on the threads when i receive the message quit (recipient in client sender) and after that calling notify after i close the client receiver. This way the other 3 threads will terminate only when the client receiver terminates. I think that my approach is as good as the one described above , just that it makes more sense to close the other thread after the client receiver terminates.

What i've changed / added to the original classes

	Client
I modified the order of closing the streams and socket. toServer was closed between sender.join and receiver.join and because of this i was getting "socket died" error. I tried to fix it with if(server.ready()) in client receiver but that created another error( messages were no longer sent).

	ClientSender
added boolean for the while loop
sending a message wich will trigger quitting in all other (3)  threads

	ServerReceiver
added boolean for the while loop
reporting that the user closed the chat
adding the messaje to "my queue" with the text "quit~?" so that server sender and client receiver
know to quit (so continuing the message circuit)
removing the client from the client table
 
	ServerSender
added boolean for the while loop
reading the message from my queue
sending the message to the clientreceiver
if the message is quit~!(my convention), i terminate the server sender(same as with others)

	ClientReceiver
added the boolean for the while loop
separating the message to get the actual text sent
if the text is the same with my convention "quit~?"
printing that the user has closed the chat and terminating the cclientreveiver

	ClientTable
added remove method , to remove the client from the client table

#####ASSIGNMENT 2: LOGIN LOGOUT REGISTER QUIT AND MORE############

Link to the repository:

https://git.cs.bham.ac.uk/pxm673/messaging

commit to download:  6528ef280b7618bcf61c56f4c611390546cc1cd8

<<<<<<<PLEASE RUN IN THE TERMINAL AND NOT AN IDE.>>>>>>>>>> I use the console.readpassword to read and
hide from the user the text he enters, and this doesn't work if runned in Eclipse or other IDE, but works in the terminal.
<<<<<<<Please see the comments in the code also, for more details than in SOLUTION.MD>>>>>>>>>
	Approach:
	I changed the server and the client so that everytime the server accepts a new socket, it starts a thread that deals with requests from the client (for logic or data).The client also is changed, looping for actions from the user (login and  register for now) and opens the two client side threads for the chat, while the server part(the request handler) opens the 2 threads for the server side of the chat. This way i can exchange data between the server and the client, while the server still is accepting new sockets. The client sends a queue of requests to the server to solve, and then the server sends a queue of answers back to the client.

	Functionalities:
register
login
message
logout
quit
	USAGE:
register ENTER name ENTER password ENTER
login ENTER name ENTER password ENTER

these 2 commands work if no user is logged in in the client

when logged in :

logout
message ENTER recipient ENTER text ENTER

when both logged in or not:

quit (if logged in, also loggs out and closes the client
 	else, just closes the client)

EXTRA FEATURES:

I crypted the passwords. The way i did it i found on stack overflow(i digest the string with message digest, converting it into bytes and back) , and the approach is that
i store the crypted password and when registed or login i crypt the password intered and i compare 
it to the one stored . The algorithm always generates the same string if the input is the same.


I store the information in a file which is read by the server on start . I store the usernames with the associated passwords(crypted) , and with the messages they haven't received during the time the server was running.The messages sent for a user which is registed but not logged in are saved, and the user will receive the messages when loggs in (even if the messages were sent during another session of the server).

I added shutdown hook for the server, so all the clients are closed if the server is closed unexpectedly, and the information is saved .If the client is logged in it closes right away, if not logged in in closes when the first request(after the server is closed) is entered.When i close the server, i handle the closing on the client side by exceptions thrown (mainly when the exeption message is null)

I handle unexpected closing (like ctrl+c) in the client also.


I created other clases to help me.Server logic (the reuqest handling thread) , Message queue,Message,User,Job,FileHandler.All classes are comented inside the code

#####ASSIGNMENT 3: LOGIN LOGOUT REGISTER QUIT AND MORE############

Link to the repository:

https://git.cs.bham.ac.uk/pxm673/messaging

commit to download:  537213de27d428af587433716b03941f6936081b

Added to the part 2:

Group class - cointains 2 arraylists, admins and users 
GroupTable - table having group names as keys and the groups as values

These are the only 2 classes i added from the last part.

I handle the commands for the groups in server receiver, the main messages and requests logic class for when a user is logged in.

I store the groups information in a file, the same as in part 2.So the information does not get lost between the sessions.

	Functionalities
help - shows commands you can use
logout - loggs you out
quit - loggs out and closes the client
message - sends a message to a user
registerGroup - registers a group
addMembers - add one or more members to a group
messageGroup - message a group
removeMembers - remove one or more mebers from a group
removeMe - remove yourself from a group
changeToAdmins - change normal members of a group to admins
join - send a request to join a public group
requests - shows requests for a group
showGroups - shows public groups 

Usage:

help

logout

quit

message
to:
text:

registerGroup
name:
type:

removeMembers
groupName:
usersNames(separated by '|')

removeMe
groupName:

changeToAdmins
groupName:
userNames:

join
groupName:

requests
grupName:

showGroups(only shows public groups)

addMembers
groupName:
userType:
userNames(separated by '|'):

messageGroup
groupName:
text:


If a group has no admin( if the last admin of the group is delted from the group) the group is deteled, and all current members receive a message(even if the users are logged out).


Please go to the code for more details, as i commented the code 

