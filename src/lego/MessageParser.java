package lego;

import java.net.DatagramPacket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.log4j.Logger;

import tcc.GuiInterface;

public class MessageParser implements Runnable
{
	Logger logger = Logger.getLogger("MessageParser");
	GuiInterface gui = null;
	BlockingQueue<DatagramPacket> queue = null;
	UdpServer server = null;
	Boolean stopThread = false;
	
	public MessageParser(GuiInterface Gui) throws Exception
	{
		this(Gui, 11001);
	}
	
	public MessageParser(GuiInterface Gui, int Port) throws Exception
	{
		gui = Gui;
		BlockingQueue<DatagramPacket> queue = new LinkedBlockingDeque<DatagramPacket>(100);
		server = new UdpServer(Port, queue, Gui);
		
	}
	
	public void Stop()
	{
		
	}

	@Override
	public void run()
	{
		stopThread = false;
		
		while (!stopThread)
		{
			DatagramPacket pkt = null;
			try
			{
				pkt = queue.take();
			}
			catch (InterruptedException e)
			{
				logger.error("Failed to take packet for the queue", e);
			}
			
			if (pkt == null)
				continue;

			ConfigurationMessage cm = null;
			try
			{
				cm = ConfigurationMessage.fromJson(new String(pkt.getData()));
			}
			catch (Exception e)
			{
				logger.error("Failed to parse message",e);
			}
			
			/*
			if (cm.issue == ISSUE_TYPE.ISSUE_MSG_ERROR.Val() ||
				cm.issue == ISSUE_TYPE.ISSUE_MSG_WARNING.Val() ||
				cm.issue == ISSUE_TYPE.ISSUE_MSG_NOTICE.Val() ||
				cm.issue == ISSUE_TYPE.ISSUE_MSG_INFO.Val() ||
				cm.issue == ISSUE_TYPE.ISSUE_MSG_DEBUG.Val() ||
				cm.issue == ISSUE_TYPE.ISSUE_MSG_TRACE.Val() )*/
			{
				if(gui != null)
				{
					gui.UpdateStatus(new String(pkt.getAddress().toString()) + " : " + cm.issuestring + " ->" + cm.message );
				}
			}
		}
	}
}
