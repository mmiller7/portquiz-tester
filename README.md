# portquiz-tester
This is a simple program to run outgoing port tests using the portquiz.net service.

To build, you need the Java Development Kit (JDK) installed on your computer.  Then simply run "javac TCPPortTest.java" and it will build your executable.



To run, type "java TCPPortTest" with the appropriate options added.

Usage: java TCPPortTest <start> [end] [-a] [-f] [-h] [-host=] [-q] [-v]

<start>  Starting port number (required)
[end]    Ending port number (optional, to check range)
-a       Print both pass and fail results.
-f       Print only fail results.
-h       Show usage help
-host=   Specify host address (e.g. if DNS fails set portquiz.net IP)
-q       Quiet - does not print dots, useful for scripting
-v       Verbose - prints full server replies (I suggest only do 1 port!)

This program scans 1 or more TCP ports asynchroniously and prints
the results as PASS (connected and received expected reply) or
FAILED (timed out, rejected, or not expected reply).  This program
attempts to establish a connection to portquiz.net in order to
perform the tests.  Check out their website for some nifty info.

Example:
 ~/temp]$ java TCPPortTest 80
PASS: 80
 ~/temp]$ java TCPPortTest 80 82 -a
PASS: 80
FAIL: 81
FAIL: 82
