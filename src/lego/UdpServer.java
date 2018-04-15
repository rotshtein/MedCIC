package lego;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import tcc.GuiInterface;

public class UdpServer extends Thread
{

	Logger							logger		= Logger.getLogger("UdpServer");
	DatagramSocket					socket		= null;
	private final static int		PACKETSIZE	= 255;
	Boolean							stopThread	= false;
	BlockingQueue<DatagramPacket>	queue		= null;
	GuiInterface					gui			= null;

	public UdpServer(int Port, BlockingQueue<DatagramPacket> Queue, GuiInterface Gui) throws Exception
	{
		socket = new DatagramSocket(Port);
		socket.setSoTimeout(500);
		queue = Queue;
		gui = Gui;
		logger.info("UDP Server started listtning on port " + Port);
	}

	public void Stop()
	{
		stopThread = true;
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
					logger.debug("UdpServer thread interrupted");
				    break;
				}
				
				DatagramPacket packet = new DatagramPacket(new byte[PACKETSIZE], PACKETSIZE);
	
				try
				{
					// Receive a packet (blocking) with 500 mSec timeout
					socket.receive(packet);
				}
				catch (SocketTimeoutException te)
				{
					continue;
				}
				catch (IOException e)
				{
					logger.error("Error while receiving packet", e);
				}
	
				try
				{
					queue.add(packet);
				}
				catch (IllegalStateException e)
				{
					logger.error("Udp mssage queue full", e);
				}
			}
			catch (Exception e)
			{
				logger.error("Thread exception" , e);
			}
		}
		logger.debug("UdpServer thread exit");
	}

}
