# Enguage(TM) - (c) Martin Wheatman
A pragmatic utterance mediator: appropriating a context-dependent interpretation.

Usage: java -jar enguage.jar [-d <configDir>] [-p <port> | -s | [--server [<port>]] -t ]
where: -d <configDir>
          config directory, default="./src/assets"

       -p <port>, --port <port>
          listens on local TCP/IP port number

       -c, --client
          runs Enguage as a shell

       -s <port>, --server <port>
          switch to send test commands to a server.
          This is only a test, and is on localhost.
          (Needs to be initialised with -p nnnn)

       -t, --test
          runs a sanity check


<hr/>
<h2>Notes</h2>
<p>Enguage is a natural language engine: a virtual machine for utterances. It uses human social interaction as a model for human computer interaction by defining vocal norms as mappings between utterances  and their replies. This is different to the simple command model, developed from internet search, used by popular voice driven devices. This is novel because mappings—including any intermediate interaction with machine state—are determined by further utterances: there’s no pesky JavaScript or corporate websites to get in the way! Furthermore, mappings are also created by utterance: there’s no central website on which to create your skills.</p>
<p>This notes are are a little out of date now, originally created for the JAR only repo. 
          But they contain all the interesting stuff like hooking it all up to a backend database.
          Created from my installation on Ubuntu Desktop 18.04 LTS.</p>
<h3>Install</h3>
<p>To install the Enguage jarfile if it is 7zipped:</p>
<pre>
martin@vBox:~$ mkdir enguage/
martin@vBox:~$ mv enguage.7z enguage/
martin@vBox:~$ cd enguage/
martin@vBox:~/enguage$ sudo apt install p7zip-full
martin@vBox:~/enguage$ 7z x enguage.7z
</pre>
<p>You can then run the test suite with the -t option:</p>
<pre>
martin@vBox:~/enguage$ java -jar enguage.jar -d assets/ -t
</pre>

<p>This should give you many lines of example utterances. Great! You can also run the jarfile as a shell. It loads the config.xml file in the assets directory, which contains lots of options to configure Enguage. Then you can type in utterances like a command line interface.
<pre>
martin@vBox:~/enguage$ java -jar enguage.jar -d assets/ -s
Enguage (c) Martin Wheatman, 2001-4, 2011-17
Enguage main(): overlay is: [ /home/martin/yagadi.com, tmp(2) ]
Saving name='VALUESEP', value='+'
Saving name='SERVER', value='localhost'
Saving name='PORT', value='5678'
Saving name='XMLPOST', value='</SOAP-ENV:Body></SOAP-ENV:Envelope>'
Saving name='XMLPRE', value='<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"xmlns:example="http://ws.cdyne.com/"><SOAP-ENV:Header/><SOAP-ENV:Body>'
Found: 9 concept(s)
id=colloquia, op=load
id=engine, op=load
id=plural, op=load
id=temporal, op=load
id=spatial, op=load
id=settings, op=load
id=arithmetic, op=load
id=day, op=load
id=meeting, op=prime
Prime repertoire is 'meeting'
Initialisation in: 886ms
0 clashes in a total of 117
Enguage> i need a coffee.
Shell.interpret() =>Ok , you need a coffee.
</pre>
<p>First thing to note, the Shell.interpret() => is just output by my shell code, nothing to worry about—it doesn’t get output when running as a server!
Now you’re ready to programme Enguage…
<h3>Programming</h3>
Create a plain text file in the assets directory—a repertoire—called show_me_all_files.txt containing:
<pre>
On "show me all files":
	run "ls";
	then, reply "ok, ...".
</pre>
<p>This is a sign—a mapping between an utterance and a reply. Each repertoire consists of one or more signs. A repertoire supports [an aspect of] a concept. This representation of a sign does look a little like a function, with a ‘name’ show me all files; and, a ‘body’ consisting of a semi-colon separated list. This is intentional as Semioticians often talk of signs functioning. The ‘name’ is actually a pattern, but this example is contain string literals, it contains no VARIABLES (i.e. represented as literals in uppercase). The body is a list of further utterances. This is all meant to read, and respond, like natural language.
<p>In this case the filename is the full utterance. However, a repertoire file is made up of the constant literals in the utterance—this is the framework which defines the aspect of the concept. In more developed repertoires, this can be paired down to one or two words, such as in meeting.txt.
<p>Now this utterance can be typed in, although this would normally be collected from some speech-to-text software!
<pre>   
Enguage> show me all files.
Shell.interpret() =>Ok , assets enguage.7z enguage.jar variable.
Enguage> 
</pre>
<P>This matches the utterance with the repertoire names and loads them temporarily, so language support is both scalable and dynamic. It then loads all the signs in the repertoire, some of which may not match the filename but are ancillary to the repertoire. It then matches the utterance with the ‘name’ of the actual sign: it must match the whole utterance, this is not a keyword search like a chatbot(!)
<p>When it has found a matching sign, it interprets the sign’s body—interpretant—to see if it can produce a reply. This is a simple example, with a run command in it. This runs the content of the string which is the Linux ls command which, if you’re not familiar with Linux, produces a list of files in the current directory.
<p>The next command is the reply which says ok , … the ellipsis is replaced with whatever the output from the previous command was (i.e the file list), and it outputs this, as seen above.
If any of this hadn’t matched/run correctly, it would have go on to find a sign to match elsewhere. If no match had been made it would have replied with, “I don’t understand”.
<p>We’ll now go on to customise the above example, but for the moment it will be best if you put the commands (e.g. to interrogate your database) into a shell command. See below!
<h3>Running with MySql</h3>
Ok, here’s a similar example with MySql. I have created a new repertoire in the assets directory called show_me_all.txt, containing:
<pre>
On "show me all ENTITY":
	run "squelch ENTITY";
	then, reply "ok, ENTITY include ...".
</pre>
I have also created the executable shell script squelch which is on the path (I’ve got mine in ~/bin). In future this will be encapsulated within Enguage. It contains:
<pre>
#!/bin/sh
# squelch: a simple but insecure enguage -> sql script
#

if [ $# -lt 6 -o $# -eq 7 ]; then
	echo "Usage: DB USER PASSWD CMD TABLE COLUMN [ID VAL]\n  got: " $* >&2
	exit 255
fi

export DB=$1;	 shift
export USR=$1; shift
export PWD=$1; shift
export CMD=$1; shift
export TBL=$1; shift
export COL=$1; shift
if [ $# -ge 2 ]; then
	export ID=$1; shift
	export VAL="$*"
fi

	case $CMD in
	"select")
		export COMMAND="use $DB; $CMD $COL from $TBL"
		if [ ! -z "$ID" -a ! -z "$VAL" ]; then
			export COMMAND="$COMMAND  where $ID = \"$VAL\""
		fi
		export COMMAND="$COMMAND ;"
		;;
	esac
	
sqlQuery () {
	mysql -u$USR -p$PWD <<EOF
	$COMMAND
EOF
}
</pre>

<p>In the following code, it calls the function above, and then
the sed turns field delimiting tabs into “, “
the tail removes line one (the column titles) from the output
<pre>
sqlQuery | \
	tail --lines=+2  | \
	tr '\n' ',' | sed 's/.$//' | sed 's/,/, /g'
echo # add a newline
</pre>
I have also created a simple test database using mysql. I assume you’re more familiar with mysql than I am:
<pre>
mysql> create database test;
Query OK, 1 row affected (0.00 sec)

mysql> use test;
Database changed

mysql> create table entity (names varchar(20), descriptions varchar(255));
Query OK, 0 rows affected (0.01 sec)

mysql> insert into entity (names, descriptions) values ("coffee", "americano");
Query OK, 1 row affected (0.01 sec)

mysql> insert into entity (names, descriptions) values ("milk", "dairy free");
Query OK, 1 row affected (0.01 sec)

mysql> select * from entity;
+--------+--------------+
| names  | descriptions |
+--------+--------------+
| coffee | americano    |
| milk   | dairy free   |
+--------+--------------+
2 rows in set (0.00 sec)

I can test squelch on its own from the command line:
martin@vBox:~/enguage$ cp squelch ~/bin/

martin@vBox:~/enguage$ squelch test martin secret select entity descriptions
americano, dairy free
</pre>
So when I run Enguage now:
<pre>
martin@vBox:~/enguage$ java -jar enguage.jar -d assets/ -s
Enguage (c) Martin Wheatman, 2001-4, 2011-17
Enguage main(): overlay is: [ /home/martin/yagadi.com, tmp(2) ]
.
.
.
Initialisation in: 765ms
2 clashes in a total of 117
Enguage> show me all names.
Shell.interpret() =>Ok , names include coffee , milk.
Enguage> show me all descriptions.
Shell.interpret() =>Ok , descriptions include americano , dairy free.
Enguage> 
</pre>
So, this isn’t brilliant. You can probably use MySql to separate fields with commas.  (I really have thrown this together!) And, you can still run the original example, Enguage will take longest match first:
<pre>
Enguage> show me all files.
Shell.interpret() =>Ok , assets enguage.7z enguage.jar variable.
Enguage>
</pre>
<h3>Running under Httpd</h3>
<p>Let’s put this behind a web server. I use Ubuntu desktop which doesn’t seem to come with a webserver, so I’m setting up apache2 from scratch. I’ve also created a simple cgi-bin program to call the Enguage jarfile running on the localhost with the arguments. This can be installed:
<pre>
martin@vBox:~/enguage$ su
root@vBox:/home/martin/enguage$ apt-get install apache2
root@vBox:/home/martin/enguage$ chown www-data.www-data *
root@vBox:/home/martin/enguage$ cp cgi-enguage.c /usr/lib/cgi-bin
root@vBox:/home/martin/enguage$ cp index.html /var/www/html
root@vBox:/home/martin/enguage$ cd /var/www/html
root@vBox:/var/www/html$ more index.html
<html>
<body>
<script>
function loadDoc() {
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (this.readyState == 4 && this.status == 200) {
      document.getElementById("reply").innerHTML = this.responseText;
    }
  };
  var utterance = document.getElementById("utterance").value;
  xhttp.open("GET", "cgi-bin/cgi-enguage?"+utterance, true);
  xhttp.send();
}
</script>
&lt;h1>hello there</h1>
<input type="text" id="utterance">
<button type="button" onclick="loadDoc()">Send...</button>
<div id="reply">Reply goes here...</div>
</body>
</html>
</pre>
Then we can go and look at, and build, the cgi-bin program. Apache.conf specifies the location of cgi-bin in /usr/lib. Below, I have created a simple CGI program which takes ajax enguage queries and sends them to an adjacent server on this machine. 
<pre>
martin@vBox:/var/www/html$ su
root@vBox:/var/www/html# cd /usr/lib/cgi-bin
root@vBox:/usr/lib/cgi-bin# cc -o enguage.cgi cgi-enguage.c
root@vBox:/usr/lib/cgi-bin# more cgi-enguage.c
#include &lt;stdio.h>
#include &lt;sys/socket.h>
#include &lt;stdlib.h>
#include &lt;unistd.h>
#include &lt;netinet/in.h>
#include &lt;arpa/inet.h>
#include &lt;string.h>

int main( int argc, char* argv[] ){
        int n;
        int ptr = 0;
        int clientSocket;
        char buffer[1024];
        struct sockaddr_in serverAddr;
        socklen_t addr_size;

        memset(buffer, '\0', 1024);
	
        // create the socket
        if (-1 == (clientSocket = socket(PF_INET, SOCK_STREAM, 0))) {
                strcpy( buffer, "socket error" );
        } else {
                // configure the server address
                serverAddr.sin_family = AF_INET;
                serverAddr.sin_port = htons(8080);
                serverAddr.sin_addr.s_addr = inet_addr("127.0.0.1");
                memset(serverAddr.sin_zero, '\0', sizeof serverAddr.sin_zero);

                // onnect to the server
                addr_size = sizeof serverAddr;
		
                if (-1 == connect(clientSocket, (struct sockaddr *) &serverAddr, addr_size)) {
                        strcpy( buffer, "connect error: enguage server down?" );
                } else {
                        // read the args into a string
                        if (argc>1) { // GET
                                while (--argc && ++argv) {
                                        // strip '.' from last arg
                                        if (argc == 1) {
                                                int len = strlen( argv[ 0 ]);
                                                if (len && argv[ 0 ][ len - 1 ] == '.')
                                                        argv[ 0 ][ len - 1 ] = '\0';
                                        }
                                        strcat( buffer, argv[ 0 ]);
                                        if (argc>1) strcat( buffer, " " ); // add sep
                                }
                                strcat( buffer, "\n" );
			} else { // POST
                                ptr = 0;
                                while (0 < (n = read( 0, buffer+ptr, 1024 - ptr )))
					ptr += n;
			}
			
			// send teh string to the server
			if (-1 == send(clientSocket, buffer, 1024, 0)) {
                                strcpy( buffer, "send() error: enguage server down?" );
			} else {
                                // read the reply
                                memset(buffer, '\0', 1024);
                                ptr = 0;
                                while (0 < (n = recv(clientSocket, buffer+ptr, 1024 - ptr, 0)))
                                        ptr += n;
				
                                if (-1 == n) strcpy( buffer, "recv() error: enguage server down?" );
        }	}	}
	
        // print it:- http-style
        printf("Content-type: text/plain\n\n%s",buffer);	 
        return 0;
}
</pre>
You will also have to enable support for CGI programs:
<pre>
root@vBox:/var/www/html# cd /etc/apache2/mods-enabled
root@vBox:/var/www/html# ln -s ../mods-available/cgi.load .
</pre>
<p>The apache webserver can now be started with:
<pre>
root@vBox:/var/www/html# systemctl start apache2
</pre>
<p>We start with typing in the utterance/sentence into the input box in the web browser and press the Send button. If the input box is updated on the Web text to speech software returning a string, the send could be performed on input box change event. The AJAX code sends the utterance to the CGI-script which places the utterance next-door onto the Enguage jar-file, running as a server.  Enguage interprets the utterance, and in the case of “show me all names” it runs the squelch script to interrogate the database. This is returned back to the web browser as plain text in the CGI program. Hopefully this example is enough to get you going. Here’s what is should look like at the end:

<h3>Enguage Web Speech-to-Text</h3>
<p>This needs Chrome(!) It works on my Ubuntu installation.  This index.html integrates the Enguage installation, as described above, with the speech-to-text web api:
<p>index.html:
<pre>
&lt;!DOCTYPE html>
&lt;html lang="en">
&lt;head>
  &lt;title>Speech Recognition</title>
&lt;/head>
&lt;body>
	&lt;script>
window.SpeechRecognition = window.webkitSpeechRecognition || window.SpeechRecognition;

function buttonPress() {
	recognition = new SpeechRecognition();
	recognition.start();
	recognition.onresult = function(event) {	
		if (event.results[0].isFinal) {
			speechToText = event.results[0][0].transcript;
			document.getElementById( "heard" ).innerHTML = speechToText;
			interp( speechToText );
}	}	}

function interp( utterance ) {
	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function() {
		if (this.readyState == 4 && this.status == 200) {
			synth = window.speechSynthesis;
			synth.speak( new SpeechSynthesisUtterance( this.responseText ));
			document.getElementById( "reply" ).innerHTML = this.responseText;
	}	}
	xhttp.open("GET", "cgi-bin/enguage.cgi?"+ utterance, true);
	xhttp.send();
}
	&lt;/script>
	&lt;button onClick="buttonPress();">Click and Speak</button>
	&lt;div id="heard">I heard...</div>
	&lt;div id="reply">my reply...</div>
&lt;/body>
&lt;/html>
</pre>

<p>Using the above HTML/JavaScript, along with the running jarfile and cgi scripts, above, we can get the below. Remember, if it says sorry, I don’t know the value of database, this can be solved by saying set the value of database to test, and set the value of table to entity.

<h3>End-to-end</h3>
This is an end-to-end test from the user to the database to retrieve values from a relational database by voice. Because this involves a Java implementation on the client, it means that all traffic comes through this one piece of code. If we were to use this in real life it would mean context would leak from one user to another: not good! We need a port to JavaScript, or some context swapping server Java.
<h3>Play with Enguage:</h3>
<P>For further examples of repertoires, see the assets directory. The most complete is need+needs.txt, but other examples include meeting.txt which is both a temporal and a spatial concept. The output from the test you’ve already run is produced from the repertoires in the assets directory—search through this for examples of how you can say/code things. If a variable has PHRASE- in front of it it will match more than one word. If it has NUMERIC- it will match an apparent number (this, I’m afraid is English based for the moment, e.g. ‘a’ = 1, sorry!)
<p>If you don’t like it, tell me; if you, do tell others!
<p>Happy programming!<br/>
martin@wheatman.net

<p>P.S. Again, if there are any questions, please please please skype/text/email.
