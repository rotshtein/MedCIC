package lego;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import tcc.Parameters;

public class ProcMon implements Runnable
{

	final static Logger	logger	= Logger.getLogger("ProcMon");
	private Process		_proc;
	String processName = null;
	private volatile boolean	_complete	= false;
	public String				description	= "";

	/*public ProcMon(String[] vars, String description) throws Exception
	{
		this(Runtime.getRuntime().exec(vars, null), description);
	}*/

	public ProcMon(Process proc)
	{
		this(proc, "No description");
	}

	public ProcMon(Process proc, String description)
	{
		_proc = proc;
		
		this.description = description;
		Thread t = new Thread(this);
		t.start();
	}

	public boolean isComplete()
	{
		return _complete;
	}

	public Boolean Kill()
	{
		SendStop();
				
		if (!_complete)
		{
			int i = 0;
			while (_proc.isAlive())
			{
				try
				{
					Thread.sleep(200);
				}
				catch (InterruptedException e)
				{
					logger.error("Failed to sleep", e);
				}
				if (++i > 5)
				{
					break;
				}
			}
			_proc.destroy();
			return true;
		}
		KillAll("ProcessBlock");
		return false;
	}

	public void run()
	{
		_complete = false;
		try
		{
			_proc.waitFor();
		}
		catch (InterruptedException e)
		{
			logger.error("Failed to monitor process", e);

		}
		_complete = true;
		logger.info("Exiting procMon thread");
	}
	
	public static void SendStop()
	{
		try
		{
			SendStop("1");
			SendStop("2");
		}
		catch (Exception e)
		{
			logger.error("Failed to stop path", e);
		}
	}
	
	public static void SendStop(String Path) throws Exception
	{
		ConfigurationMessage cm = new ConfigurationMessage(Path,ConfigurationMessage.ISSUE_MSG_DONE);
		cm.module = "udpserver";
		byte[] message = cm.toJson().getBytes();
		try
		{
			InetAddress address = InetAddress.getByName(Parameters.Get("ManagementHost", "127.0.0.1"));
			int Port = UdpServer.getPort();
			if (Port > 0)
			{
				DatagramPacket packet = new DatagramPacket(message, message.length, address, Port );
				DatagramSocket dsocket = new DatagramSocket();
			    dsocket.send(packet);
			    logger.debug("Sending Done to path " + Path + " => " + new String(packet.getData()) );
			    //String s = new String(packet.getData());
			    dsocket.close();
			}
		}
		catch (UnknownHostException e)
		{
			logger.error("UnknownHostException", e);
		}
	}
	
	public static void KillAll(String ProcessName)
	{
		 String command = "killall " + ProcessName;
		 
		 if (System.getProperty ("os.name").indexOf("win") >= 0) 
		 {
			 command = "taskkill /IM " + ProcessName + ".exe";
         } 
		 
		 Process p = null;
		try
		{
			p = Runtime.getRuntime().exec(command);
		}
		catch (IOException e)
		{
		}
		if (p != null)
		{
			p.destroy();
		}
	}
}
