/*
 * TCPPortTest.java
 * Matthew Miller
 * Created 19NOV2015
 * 
 * This program connects to http://portquiz.net on user-specified ports and
 * reports whether it was successful in accessing the internet (got to the
 * server) or blocked/redirected somewhere in the route.
 */

import java.io.*;
import java.net.*;

class TCPPortTest implements Runnable
{
	int portNum=1;
	static int numRunning=0;

	TCPPortTest(int x)
	{
		this.portNum=x;
	}
	
	static int TIMEOUT=30000;
	static String host="portquiz.net";
	static boolean showFail=false;
	static boolean showPass=true;
	static boolean showHelp=false;
	static boolean quiet=false;
	static boolean debug=false;
	static boolean verbose=false;
	
	public static void main(String argv[]) throws Exception
	{
		int start=-1;
		int end=-1;
		
		//Parse command line flags
		for(int x=0; x < argv.length; x++)
		{

			if(argv[x].equals("-h"))
			{
				showHelp=true;
			}
			if(argv[x].equals("-f"))
			{
				showFail=true;
				showPass=false;
			}
			if(argv[x].equals("-a"))
			{
				showFail=true;
				showPass=true;
			}
			if(argv[x].equals("-q"))
			{
				quiet=true;
			}
			if(argv[x].equals("-v"))
			{
				verbose=true;
			}
			if(argv[x].equals("-vv"))
			{
				debug=true;
			}
			if(argv[x].contains("-host="))
			{
				host=argv[x].split("=")[1];
			}
			
			int val=-1;
			try
			{
				val=Integer.parseInt(argv[x]);
			}
			catch(NumberFormatException e) {}
			
			//If it's the 1st num, set start
			if(val != -1 && start == -1)
				start=val;
			//If it's any num, and we got a start num, set end
			//it's OK if this is the start # or only num, in
			//case we only want to check 1 port.
			if(val != -1 && start != -1)
				end=val;
		}
		
		//Show help
		if(showHelp || start == -1)
		{
			System.out.println("Usage: java TCPPortTest <start> [end] [-a] [-f] [-h] [-host=] [-q] [-v] ");
			System.out.println();
			System.out.println("<start>  Starting port number (required)");
			System.out.println("[end]    Ending port number (optional, to check range)");
			System.out.println("-a       Print both pass and fail results.");
			System.out.println("-f       Print only fail results.");
			System.out.println("-h       Show usage help");
			System.out.println("-host=   Specify host address (e.g. if DNS fails set portquiz.net IP)");			
			System.out.println("-q       Quiet - does not print dots, useful for scripting");
			System.out.println("-v       Verbose - prints extra metrics");
			System.out.println("-vv      Very Verbose - prints full server replies (I suggest only do 1 port!)");
			System.out.println();
			System.out.println("This program scans 1 or more TCP ports asynchroniously and prints");
			System.out.println("the results as PASS (connected and received expected reply) or");
			System.out.println("FAILED (timed out, rejected, or not expected reply).  This program");
			System.out.println("attempts to establish a connection to portquiz.net in order to");
			System.out.println("perform the tests.  Check out their website for some nifty info.");
			System.exit(0);
		}
		
		//RUN!
		for(int x=start; x <= end ; x++)
		{
			new Thread(new TCPPortTest(x)).start();
			Thread.sleep(100);
			if(x % 100 == 0 && !quiet)
				System.out.print(".");
			if(x % 100 == 0 && verbose)
				System.out.print("["+numRunning+"x]");
			if(x % 200 == 0)
			{
				if(numRunning > 10)
					Thread.sleep(2*TIMEOUT);
				else
					Thread.sleep(TIMEOUT);
			}
		}
	}

	public void run()
	{
		numRunning++;
		boolean connect=false;
		boolean pass=false;
		boolean reply=true;
		try
		{
			String modifiedSentence;
			//Socket clientSocket = new Socket("portquiz.net", portNum);
			Socket clientSocket = new Socket();
			clientSocket.connect(new InetSocketAddress(host, portNum), TIMEOUT);
			connect = clientSocket.isConnected();
			DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
			//outToServer.writeBytes("\n");
			outToServer.writeBytes( "GET / HTTP/1.1\n"+
									"Host: portquiz.net\n"+
									"Connection: close\n"+ //speeds up HTTP close, no need for long timeout wait
									"\n\n");
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			modifiedSentence = inFromServer.readLine();
			
			//sending QUIT speeds up exit of SMTP and FTP, no need for timeout
			//we do this here so that, by this time, we have already got any HTTP response.
			if(!modifiedSentence.contains("HTTP/1.1 200 OK"))
			{
				outToServer.writeBytes("QUIT\n");
			}
			
			if(debug)
				System.out.println("---------- DEBUG: BEGIN SERVER REPLY ----------");
				
			while(modifiedSentence != null)
			{
				reply=true;
				if(debug)
					System.out.println(modifiedSentence);

				if(
						modifiedSentence.contains("Outgoing Port Tester") || //Webserver
						(modifiedSentence.contains("220") && modifiedSentence.contains("FTP")) || //FTP server
						modifiedSentence.contains("SSH") || //SSH server
						(modifiedSentence.contains("220") && modifiedSentence.contains("SMTP")) || //SMTP server
						modifiedSentence.contains("HTTP to an SSL-enabled server") //HTTPS
					)
				{
					pass=true;
				}
				modifiedSentence = inFromServer.readLine();
			}

			if(debug)
				System.out.println("---------- DEBUG: END SERVER REPLY ----------");
			
			clientSocket.close();
			//System.out.println("Finished port "+portNum);
		}
		catch(Exception e) {};
		
		if(pass)
		{
			if(showPass || debug)
				System.out.println("PASS: "+portNum);
		}
		else
		{
			if(showFail || debug)
			{
				if(connect)
				{
					if(reply)
						System.out.println("FAIL_UNKNOWN_RESPONSE: "+portNum);
					else
						System.out.println("FAIL_NO_RESPONSE: "+portNum);
				}
				else
				{
					System.out.println("FAIL: "+portNum);
				}
			}
		}
		numRunning--;
	}
}
