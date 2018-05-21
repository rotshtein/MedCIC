package tcc;

import java.io.File;
import java.net.URI;

import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import lego.ProcMon;
import lego.ScriptFile;
import medcic_proto.MedCic.ENCAPSULATION;

public class AutomaticStart extends WebSocketClient implements Runnable
{

	Logger logger = Logger.getLogger("AutomaticStart");
	static final int SAMPLE_FILE_SIZE = 1024*1024*2;
	ScriptFile		scriptFile	= null;
	Boolean			stopThread	= false;
	GuiInterface	gui			= null;
	String			sourceUri1;
	String			idFile;
	String			destUri1;
	String			sourceUri2;
	String			destUri2;
	String			configFile;
	String			managementServer;
	int				managementPort;
	ProcMon 		procMon = null;
	WebSocket conn = null;
		
	public AutomaticStart( 
			String Source1Uri, 	String Dest1tUri, 
			String Source2Uri, 	String Dest2tUri, 
			String IdFile, 	
			String ConfigFile,
			String Server, int Port,
			WebSocket Connection) throws Exception
	{
		super(new URI(Parameters.Get("ServerUri", "ws://127.0.0.1:8887")));
		
		this.connect();
		
		if ( (Source1Uri == null | Source1Uri == "") | (Dest1tUri == null | Dest1tUri == ""))
		{
			throw new Exception("CIC 1 details are wrong");
		}

		if ( (Source2Uri == null | Source2Uri == "") | (Dest2tUri == null | Dest2tUri == ""))
		{
			throw new Exception("CIC 2 details are wrong");
		}
		
		this.idFile = IdFile;

		this.sourceUri1 = Source1Uri;
		this.destUri1 = Dest1tUri;
		this.sourceUri2 = Source2Uri;
		this.destUri2 = Dest2tUri;
		
		this.configFile = ConfigFile;
		this.managementServer = Server;
		this.managementPort = Port;
		
		this.conn = Connection;
	}

	public void Stop()
	{
		stopThread = true;
	}

	@Override
	public void run()
	{
		
		
		
	}
	
	
	public Boolean GetSampleFile(String InputUri, String Filename, int Duration)
	{
		return true;
	}
	
	public ENCAPSULATION IdentifyEncapsulation(String Filename)
	{
		return null;
		
	}
	
	public ENCAPSULATION GetEncapsulation()
	{
		try
		{
			// Get sample file
			GetSamples gs = new GetSamples(gui, "Getting Samples");
			procMon = gs.Start(sourceUri1, idFile, configFile, managementServer, managementPort);
			while (!procMon.isComplete())
			{
				if  (new File(idFile).length() > SAMPLE_FILE_SIZE) 	
				{
					stopThread = true;
				}
				
				if (stopThread)
				{
					Kill();
					gui.UpdateStatus("Terminating Auto start process - Getting samples");
					logger.debug("Terminating Auto start process - Getting samples");
					return null;
				}
				Thread.sleep(50);
			}
		}
		catch (Exception e)
		{
			gui.UpdateStatus("Failed to get samples for identification - Aborting");
			logger.error("Failed to get samples for identification", e);
			Kill();
			return null;
		}
		
		// Identify
		try
		{
			Identify idn = new Identify(gui);
			procMon = idn.Start(idFile, configFile, managementServer, managementPort);

			while (!procMon.isComplete())
			{
				Thread.sleep(50);
				if (stopThread)
				{
					Kill();
					gui.UpdateStatus("Terminating Auto start process - Getting samples");
					logger.debug("Terminating Auto start process - Getting samples");
					return null;
				}
			}
		}
		catch (Exception e)
		{
			gui.UpdateStatus("Failed to idetify CIC-1 sampelse - Aborting");
			logger.error("Failed to idetify sampelse", e);
			Kill();
			return null;
		}
		
		// get encapsulation module name
		
		ENCAPSULATION e = null;
	
		try
		{
			ScriptFile sf = new ScriptFile("c:\\bin\\lego\\config\\Script.lego");
			e = sf.getEncapsolation();
			if (e == null)
			{
				throw new Exception("Unrecognized ancapsulation");
			}
		}
		catch (Exception e1)
		{
			logger.error("Failed to filnd the encapsulation methos from file", e1);
			return null;
		}
		
		return e;
	}
	
	public void Kill()
	{
		try
		{
			if(procMon != null)
			{
				procMon.Kill();
			}
		}
		catch (Exception e)
		{
			logger.error("Failed to kill", e);
		}
	}

	@Override
	public void onOpen(ServerHandshake handshakedata)
	{
		logger.debug("Internal cient connected");
	}

	@Override
	public void onMessage(String message)
	{

		
	}

	@Override
	public void onClose(int code, String reason, boolean remote)
	{
		logger.debug("Internal cient disconnected");
		
	}

	@Override
	public void onError(Exception ex)
	{
		logger.error("Internal cient socket error",ex);
		
	}
}
