package tcc;

import org.apache.log4j.Logger;

import lego.ScriptFile;

public class AutomaticStart implements Runnable
{

	Logger logger = Logger.getLogger("AutomaticStart");

	ScriptFile		scriptFile	= null;
	Boolean			stopThread	= false;
	GuiInterface	gui			= null;
	String			sourceUri;
	String			idFile;
	String			destUri;
	String			configFile;
	String			managementServer;
	int				managementPort;

	public AutomaticStart(GuiInterface gui, String SourceUri, String IdFile, String DestUri, String ConfigFile,
			String Server, int Port)
	{
		this.gui = gui;
		this.sourceUri = SourceUri;
		this.idFile = IdFile;
		this.destUri = DestUri;
		this.configFile = ConfigFile;
		this.managementServer = Server;
		this.managementPort = Port;
	}

	/*
	 * public ProcMon Start(String SampleFilename, String ConfigFile, String Server,
	 * int Port) throws Exception { scriptFile = new ScriptFile();
	 * 
	 * return StartAction(new String [] {"IDBlock.exe",SampleFilename}); }
	 */

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
			GetSamples gs = new GetSamples(gui);
			ProcMon getSamplesProcess = gs.Start(sourceUri, configFile, managementServer, managementPort);
			while (!getSamplesProcess.isComplete())
			{
				Thread.sleep(50);
				if (stopThread)
				{
					getSamplesProcess.kill();
					gui.UpdateStatus("Terminating Auto start process - Getting samples");
					logger.debug("Terminating Auto start process - Getting samples");
					return;
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Failed to get samples for identification", e);
		}
		// Identify
		try
		{
			Identify idn = new Identify(gui);
			ProcMon idetifyProcess = idn.Start("C:\\programs\\lego\\config\\IDScript.lego", configFile,
					managementServer, managementPort);

			while (!idetifyProcess.isComplete())
			{
				Thread.sleep(50);
				if (stopThread)
				{
					idetifyProcess.kill();
					gui.UpdateStatus("Terminating Auto start process - Idetify");
					logger.debug("Terminating Auto start process - Idetify");
					return;
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Failed to identify", e);
		}

		// Start Production

	}

}
