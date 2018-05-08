package lego;

import java.net.DatagramPacket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.log4j.Logger;

import tcc.GuiInterface;
import tcc.Parameters;

public class MessageParser extends Thread
{

	static final Logger				logger		= Logger.getLogger("MessageParser");
	GuiInterface					gui			= null;
	BlockingQueue<DatagramPacket>	queue		= null;
	UdpServer						server		= null;
	Boolean							stopThread	= false;

	public MessageParser(GuiInterface Gui) throws Exception
	{
		this(Gui, 11001);
		Parameters.Set("ManagementPort", Parameters.Get("ManagementPort", "11001"));
	}

	public MessageParser(GuiInterface Gui, int Port) throws Exception
	{
		gui = Gui;
		queue = new LinkedBlockingDeque<DatagramPacket>(100);
		server = new UdpServer(Port, queue, Gui);
		server.start();
	}

	public void Stop()
	{
		stopThread = true;
		server.Stop();
		server = null;
	}

	@Override
	public void run()
	{
		stopThread = false;

		while (!stopThread)
		{
			try
			{
				if (Thread.interrupted())
				{
					logger.debug("Message Parser thread interrupted");
					break;
				}

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
				{
					continue;
				}

				ConfigurationMessage cm = null;
				try
				{
					cm = ConfigurationMessage.fromJson(new String(pkt.getData()));
				}
				catch (Exception e)
				{
					logger.error("Failed to parse message", e);
				}

				logger.debug("Lego message" + new String(pkt.getData()));

				
				gui.UpdateStatus(cm.path + "-" + cm.module + ": " + cm.StatusMessage());
				if (gui != null)
				{
					switch (cm.issue)
					{
					case ConfigurationMessage.ISSUE_MSG_START:

						if ((cm.path.equals("0")) && (cm.StatusMessage() != null))
						{
							gui.UpdateStatus(cm.StatusMessage());
						}
						break;
						
					case ConfigurationMessage.ISSUE_MSG_ACTIVE:
						//if ((cm.module.equals("udpclient")) && (cm.StatusMessage() != null))
						if (cm.isinput == 1)
						{
							gui.UpdateStatus(cm.module + ": " + cm.input);
						}
						
						if (cm.isoutput == 1)
						{
							gui.UpdateStatus(cm.module + ": " + cm.output);
						}
						break;

					default:
						String status = cm.StatusMessage();
						if (status != null)
						{
							gui.UpdateStatus(status);
						}
						break;
					}
				}
			}
			catch (Exception e)
			{
				logger.error("Thread exception", e);
			}
		}
		logger.debug("Message Parser thread exit");
	}
}
