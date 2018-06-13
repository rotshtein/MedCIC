package lego;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import tcc.GuiInterface;
import tcc.GuiInterface.Channel;
import tcc.Parameters;

public class MessageParser extends Thread
{

	static final Logger				logger				= Logger.getLogger("MessageParser");
	GuiInterface					gui					= null;
	BlockingQueue<DatagramPacket>	queue				= null;
	UdpServer						server				= null;
	Boolean							stopThread			= false;
	CicChanel						cic1				= null;
	CicChanel						cic2				= null;
	SyncMessageFilter				syncMessageFilter	= null;
	public static Boolean			Cic1SendSync		= true;
	public static Boolean			Cic2SendSync		= true;
	Statistics						stat				= null;

	class CicChanel
	{

		class LimitedSizeQueue<K> extends ArrayList<K>
		{

			/**
			 * 
			 */
			private static final long	serialVersionUID	= 1L;
			private int					maxSize;
			private long				sum;

			public LimitedSizeQueue(int size)
			{
				this.maxSize = size;

			}

			public boolean add(K k)
			{
				boolean r = super.add(k);
				if (r)
				{
					sum += (long) k;
				}

				if (size() > maxSize)
				{
					sum -= (long) (this.get(size() - maxSize - 1));
					removeRange(0, size() - maxSize - 1);
				}
				return r;
			}

			public long getSum()
			{
				return sum;
			}

			public void Clear()
			{
				this.clear();
			}
		}

		final int				lowpass			= 3;
		long					prevInputBytes	= 0;
		Boolean					alive			= false;
		LimitedSizeQueue<Long>	inputQueue		= new LimitedSizeQueue<Long>(lowpass);

		public void Clear()
		{
			prevInputBytes = 0;
			inputQueue.Clear();
			alive = false;
		}

		public Boolean IsAlive()
		{
			return alive;
		}

		public void setInputByte(long Bytes)
		{
			if (Bytes == 0)
			{
				Clear();
			}
			else
			{
				prevInputBytes = inputQueue.getSum();
				inputQueue.add(Bytes);

				if (prevInputBytes + (64000) > inputQueue.getSum())
				{
					Clear();
				}
				else
				{
					alive = true;
				}
			}
		}
	}

	public MessageParser(GuiInterface Gui) throws Exception
	{
		this(Gui, 11001);
		Parameters.Set("ManagementPort", Parameters.Get("ManagementPort", "11001"));
	}

	public MessageParser(GuiInterface Gui, int Port) throws Exception
	{
		stat = new Statistics();
		cic1 = new CicChanel();
		cic2 = new CicChanel();
		gui = Gui;

		syncMessageFilter = new SyncMessageFilter(gui);

		queue = new LinkedBlockingDeque<DatagramPacket>(1000);
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
					pkt = queue.poll(1500, TimeUnit.MILLISECONDS);
				}
				catch (InterruptedException e)
				{
					logger.error("Failed to take packet for the queue", e);
				}

				if (pkt == null)
				{
					//send sync status request
					try
					{
						syncMessageFilter.SendSyncStatusRequest();
					}
					catch (Exception e)
					{
						logger.error("Failed to send sync request",e);
					}
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

				if (cm == null)
				{
					logger.warn("Failed to parse message");
					continue;
				}
				
				if (cm.clientserver != 0)
				{
					//message sent from the server
					continue;
				}
				
				logger.debug("Lego message" + new String(pkt.getData()));

				if (gui != null)
				{
					switch (cm.issue)
					{
					case ConfigurationMessage.ISSUE_MSG_START:

						if ((cm.path.equals("0")) && (cm.StatusMessage() != null))
						{
							syncMessageFilter.Restart();
							gui.UpdateStatus(cm.StatusMessage());
							cic1.Clear();
							cic2.Clear();
						}
						gui.UpdateStatus(cm.path + " > " + cm.StatusMessage());
						break;

					case ConfigurationMessage.ISSUE_MSG_ACTIVE:
						if (cm.module.toLowerCase().equals("udpserver"))
						{
							if (cm.path.startsWith("1"))
							{
								cic1.setInputByte(cm.output);
								if (cic1.IsAlive())
								{
									gui.OperationInSync(Channel.INPUT1);
								}
								else
								{
									gui.OperationOutOfSync(Channel.INPUT1);
								}
								stat.setCic1In(cm.output);
							}
							else if (cm.path.startsWith("2"))
							{
								cic2.setInputByte(cm.output);
								if (cic2.IsAlive())
								{
									// if (cic1.)
									gui.OperationInSync(Channel.INPUT2);
								}
								else
								{
									gui.OperationOutOfSync(Channel.INPUT2);
								}
								stat.setCic2In(cm.output);
							}
							gui.UpdateCounters(stat);
						}
						else
						{
							break;
						}

						String st = cm.StatusMessage();
						if (st != null)
						{
							// gui.UpdateStatus(st);
						}
						break;

					case ConfigurationMessage.ISSUE_MSG_SYNC:
						if (cm.path.startsWith("1.1.1"))
						{
							syncMessageFilter.setCic1Sync(true);
						}

						else if (cm.path.startsWith("2.1.1"))
						{
							syncMessageFilter.setCic2Sync(true);
						}
						stat.setCic1Out(cm.output);
						break;

					case ConfigurationMessage.ISSUE_MSG_LOST_SYNC:
						if (cm.path.startsWith("1.1.1"))
						{
							syncMessageFilter.setCic1Sync(false);
						}

						else if (cm.path.startsWith("2.1.1"))
						{
							syncMessageFilter.setCic2Sync(false);
						}
						stat.setCic2Out(cm.output);
						break;

					default:
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
