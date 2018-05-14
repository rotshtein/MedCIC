package lego;

import java.net.DatagramPacket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.log4j.Logger;

import tcc.GuiInterface;
import tcc.GuiInterface.Channel;
import tcc.Parameters;

public class MessageParser extends Thread
{

	static final Logger				logger		= Logger.getLogger("MessageParser");
	GuiInterface					gui			= null;
	BlockingQueue<DatagramPacket>	queue		= null;
	UdpServer						server		= null;
	Boolean							stopThread	= false;
	CicChanel cic1 = null;
	CicChanel cic2 = null;
	
	class CicChanel
	{
		long prevInputBytes = 0;
		long prevOutputBytes = 0;
		long inputBytes = 0;
		long outputBytes = 0;
		
		public void Clear()
		{
			prevInputBytes = prevOutputBytes = inputBytes = outputBytes = 0;
		}
		
		public Boolean IsInSync()
		{
			if ( (inputBytes == 0) | (outputBytes == 0) )
			{
				return false;
			}
			
			if (outputBytes < prevOutputBytes + 8192)
			{
				return false;
			}
			
			if (inputBytes < prevInputBytes + 8192)
			{
				return false;
			}
			
			return true;
		}
		
		
		
		
		public void setInputByte(long Bytes)
		{
			prevInputBytes = inputBytes;
			inputBytes = Bytes;
		}
		
		public void setOutputByte(long Bytes)
		{
			prevOutputBytes = outputBytes;
			outputBytes = Bytes;
		}
	}

	public MessageParser(GuiInterface Gui) throws Exception
	{
		this(Gui, 11001);
		Parameters.Set("ManagementPort", Parameters.Get("ManagementPort", "11001"));
	}

	public MessageParser(GuiInterface Gui, int Port) throws Exception
	{
		cic1 = new CicChanel();
		cic2 = new CicChanel();
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
							cic1.Clear();
							cic2.Clear();
						}
						break;
						
					case ConfigurationMessage.ISSUE_MSG_ACTIVE:
						if (cm.module.toLowerCase().equals("udpclient"))
						{
							if (cm.path.startsWith("1"))
							{
								cic1.setInputByte(cm.input);
								if (cic1.IsInSync())
								{
									gui.OperationInSync(Channel.INPUT1);
								}
								else
								{
									gui.OperationOutOfSync(Channel.INPUT1);
								}
							}
							else if (cm.path.startsWith("2"))
							{
								cic2.setInputByte(cm.input);
								if (cic2.IsInSync())
								{
									gui.OperationInSync(Channel.INPUT2);
								}
								else
								{
									gui.OperationOutOfSync(Channel.INPUT2);
								}

							}
						}
						
						if (cm.module.toLowerCase().equals("udpserver"))
						{
							if (cm.path.startsWith("1"))
							{
								cic1.setOutputByte(cm.input);
								if (cic1.IsInSync())
								{
									gui.OperationInSync(Channel.OUTPUT1);
								}
								else
								{
									gui.OperationOutOfSync(Channel.OUTPUT1);
								}
							}
							else if (cm.path.startsWith("2"))
							{
								cic2.setOutputByte(cm.input);
								if (cic2.IsInSync())
								{
									gui.OperationInSync(Channel.OUTPUT2);
								}
								else
								{
									gui.OperationOutOfSync(Channel.OUTPUT2);
								}
							}

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
