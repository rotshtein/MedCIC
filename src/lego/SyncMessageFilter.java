package lego;

import org.apache.log4j.Logger;
import tcc.GuiInterface;
import tcc.GuiInterface.Channel;

public class SyncMessageFilter extends Thread
{
	final Logger	logger = Logger.getLogger("SyncMessageFilter");
	GuiInterface					gui			= null;
	Boolean cic1PrevSync = false;
	Boolean cic2PrevSync = false;
	Boolean cic1Sync = false;
	Boolean cic2Sync = false;
	Boolean stopThread = false;
	long duration = 500;
	
	public SyncMessageFilter(GuiInterface Gui) 
	{
		this(Gui, 500);
	}
	
	public SyncMessageFilter(GuiInterface Gui, long Duration) 
	{
		gui = Gui;
		duration = Duration;
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
			logger.error("Failed to wait n join",e);
		}
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
				logger.error("Failed to sleep",e);
			}

			if (cic1Sync != cic1PrevSync)
			{
				cic1PrevSync = cic1Sync;
				MessageParser.Cic1SendSync = true;
				if (cic1Sync)
					gui.OperationInSync(Channel.OUTPUT1);
				else
					gui.OperationOutOfSync(Channel.OUTPUT1);
			}

			if (!cic2Sync.equals(cic2PrevSync))
			{
				cic2PrevSync = cic2Sync;
				MessageParser.Cic2SendSync = true; 
				if (cic2Sync)
					gui.OperationInSync(Channel.OUTPUT2);
				else
					gui.OperationOutOfSync(Channel.OUTPUT2);
			}
			
		}
	}
	
}