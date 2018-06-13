package lego;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import tcc.GuiInterface;
import tcc.Parameters;
import tcc.GuiInterface.Channel;

public class SyncMessageFilter extends Thread
{
	static final Logger	logger			= Logger.getLogger("SyncMessageFilter");
	GuiInterface	gui				= null;
	Boolean			cic1PrevSync	= false;
	Boolean			cic2PrevSync	= false;
	Boolean			cic1Sync		= false;
	Boolean			cic2Sync		= false;
	Boolean			stopThread		= false;
	long			duration		= 500;
	InetAddress		legoAddress;


	public SyncMessageFilter(GuiInterface Gui)
	{
		this(Gui, 500);
	}

	public SyncMessageFilter(GuiInterface Gui, long Duration)
	{
		gui = Gui;
		duration = Duration;
		try
		{
			legoAddress = InetAddress.getByName(Parameters.Get("ManagementHost", "127.0.0.1"));
		}
		catch (UnknownHostException e)
		{
			logger.error("Can't get the server IP",e);
		}
		this.start();
	}

	public void Stop()
	{
		stopThread = true;
		try
		{
			this.join(duration + 5);
		}
		catch (InterruptedException e)
		{
			logger.error("Failed to wait n join", e);
		}
	}

	public void Restart()
	{
		cic1PrevSync = false;
		cic2PrevSync = false;
		cic1Sync = false;
		cic2Sync = false;
	}

	public void setCic1Sync(Boolean state)
	{
		cic1Sync = state;
	}

	public Boolean getCic1Sync()
	{
		return cic1Sync;
	}

	public void setCic2Sync(Boolean state)
	{
		cic2Sync = state;
	}

	public Boolean getCic2Sync()
	{
		return cic2Sync;
	}

	public void SendSyncStatusRequest(String Path, int port) throws Exception
	{
		ConfigurationMessage cm = new ConfigurationMessage(Path, ConfigurationMessage.ISSUE_MSG_IS_SYNC);
		cm.module = "";
		byte[] message = cm.toJson().getBytes();
		try
		{
			
			if (UdpServer.getPort() > 0)
			{
				DatagramPacket packet = new DatagramPacket(message, message.length, legoAddress, port);
				DatagramSocket dsocket = new DatagramSocket();
				dsocket.send(packet);
				logger.debug("Sending sync status request to path " + Path + " => " + new String(packet.getData()));
				dsocket.close();
			}
		}
		catch (UnknownHostException e)
		{
			logger.error("UnknownHostException", e);
		}
	}
	
	@Override
	public void run()
	{
		stopThread = false;
		while (!stopThread)
		{
			try
			{
				Thread.sleep(duration);
			}
			catch (InterruptedException e)
			{
				logger.error("Failed to sleep", e);
			}

			if (cic1Sync)// != cic1PrevSync)
			{
				cic1PrevSync = cic1Sync;
				MessageParser.Cic1SendSync = true;
				if (cic1Sync) gui.OperationInSync(Channel.OUTPUT1);
				else gui.OperationOutOfSync(Channel.OUTPUT1);
			}

			if (cic2Sync)//.equals(cic2PrevSync))
			{
				cic2PrevSync = cic2Sync;
				MessageParser.Cic2SendSync = true;
				if (cic2Sync) gui.OperationInSync(Channel.OUTPUT2);
				else gui.OperationOutOfSync(Channel.OUTPUT2);
			}

		}
	}

}