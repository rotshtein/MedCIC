package tcc;

import java.io.File;

import org.apache.log4j.Logger;

import lego.ScriptFile;

public class AutomaticStart implements Runnable
{

	Logger logger = Logger.getLogger("AutomaticStart");
	static final int SAMPLE_FILE_SIZE = 1024*1024*2;
	ScriptFile		scriptFile	= null;
	Boolean			stopThread	= false;
	GuiInterface	gui			= null;
	String			sourceUri1;
	String			idFile1;
	String			destUri1;
	String			sourceUri2;
	String			idFile2;
	String			destUri2;
	String			configFile;
	String			managementServer;
	int				managementPort;
	ProcMon procMon = null;
	

	public AutomaticStart(GuiInterface gui, 
			String Source1Uri, 	String Dest1tUri, 
			String Source2Uri, 	String Dest2tUri, 
			String IdFile1, 	String IdFile2, 
			String ConfigFile,
			String Server, int Port)
	{
		this.gui = gui;
		
		this.sourceUri1 = Source1Uri;
		this.idFile1 = IdFile1;
		this.destUri1 = Dest1tUri;
		
		this.sourceUri2 = Source2Uri;
		this.idFile2 = IdFile2;
		this.destUri2 = Dest2tUri;
		
		this.configFile = ConfigFile;
		this.managementServer = Server;
		this.managementPort = Port;
	}

	public void Stop()
	{
		stopThread = true;
	}

	@Override
	public void run()
	{
		try
		{
			// Get sample file
			GetSamples gs = new GetSamples(gui, "Getting Samples");
			procMon = gs.Start(sourceUri1, idFile1, sourceUri2, idFile2, configFile, managementServer, managementPort);
			while (!procMon.isComplete())
			{
				if ( (new File(idFile1).length() > SAMPLE_FILE_SIZE) &
					 (new File(idFile2).length() > SAMPLE_FILE_SIZE))	
					
				stopThread = true;
				
				if (stopThread)
				{
					Kill();
					gui.UpdateStatus("Terminating Auto start process - Getting samples");
					logger.debug("Terminating Auto start process - Getting samples");
					return;
				}
				
				Thread.sleep(50);
			}
		}
		catch (Exception e)
		{
			gui.UpdateStatus("Failed to get samples for identification - Aborting");
			logger.error("Failed to get samples for identification", e);
			Kill();
		}
		// Identify
		try
		{
			Identify idn = new Identify(gui);
			procMon = idn.Start(idFile1, configFile+"1", managementServer, managementPort);

			while (!procMon.isComplete())
			{
				Thread.sleep(50);
				if (stopThread)
				{
					Kill();
					gui.UpdateStatus("Terminating Auto start process - Getting samples");
					logger.debug("Terminating Auto start process - Getting samples");
					return;
				}
			}
			
		}
		catch (Exception e)
		{
			gui.UpdateStatus("Failed to idetify CIC-1 sampelse - Aborting");
			logger.error("Failed to idetify sampelse", e);
			Kill();
			return;
		}
		
		try
		{
			
			Identify idn = new Identify(gui);
			procMon = idn.Start(idFile2, configFile + "2", managementServer, managementPort);

			while (!procMon.isComplete())
			{
				Thread.sleep(50);
				if (stopThread)
				{
					Kill();
					gui.UpdateStatus("Terminating Auto start process - Getting samples");
					logger.debug("Terminating Auto start process - Getting samples");
					return;
				}
			}
			
		}
		catch (Exception e)
		{
			gui.UpdateStatus("Failed to idetify CIC-1 sampelse - Aborting");
			logger.error("Failed to idetify sampelse", e);
			Kill();
			return;
		}


		// Start Production
		
		// build 

	}
	
	public void Kill()
	{
		try
		{
			if(procMon != null)
			{
				procMon.kill();
			}
		}
		catch (Exception e)
		{
			logger.error("Failed to kill", e);
		}
	}
}
