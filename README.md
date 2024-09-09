
# Own Memcached Server

This is a tool which is my own version Memcached Server! This is a part of <a href="https://codingchallenges.fyi/challenges/challenge-memcached">Coding challenges</a> . 

<h1>Get started </h1>
This tool is in Java , so please ensure that you have latest Java version installed in your device 
<br><br>

step 1: git clone https://github.com/priyapatelsp/Memcached_Server.git <br>

step 2: Follow the below steps to start the program <br>
<h3>Steps </h3>

Step 1: Run the java application <br>

Step 2:Open terminal and Connect to a Server:
```
telnet <hostname> <port>
```
Step 3: On that telnet terminal you can type any of the below command <br>

````
Instructions :: 
set <key> :  Stores a value associated with a key 
get <key> : Retrieves the value associated with a key.
add <key>: Stores a value only if the key does not already exist.
replace <key>: Stores a value only if the key already exists.
append <key>: Appends data to an existing key’s value.
prepend <key>: Prepends data to the beginning of an existing key’s value.
````

You will get the output in the telnet terminal .


<h3>Notes: </h3>
If you don't have telnet installed already then use below command for mac: 

```
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```
Install telnet using Homebrew:

```
brew install telnet
```


<h3> Useful resources : </h3>
<a href="https://codingchallenges.fyi/challenges/challenge-memcached">Coding Challenges </a><br>
<a href="https://en.wikipedia.org/wiki/Berkeley_sockets">Berkeley_sockets </a>

<br>


<h1>Author</h1><br>
Priya Patel <br>
Github : <a href="https://github.com/priyapatelsp">priyapatelsp</a>
