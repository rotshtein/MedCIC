package legoID;

import java.io.File;
import java.net.URI;

import org.apache.log4j.Logger;

import lego.ProcMon;
import lego.ScriptFile;
import lego.Statistics;
import medcic_proto.MedCic.ENCAPSULATION;
import medcic_proto.MedCic.Header;
import medcic_proto.MedCic.IdentifiedEncapsulation;
import medcic_proto.MedCic.OPCODE;
import tcc.GuiInterface;
import tcc.ManagementClient;
import tcc.Parameters;

public class AutoIdentify implements GuiInterface
{

	final static Logger	logger	= Logger.getLogger("AutoIdentify");
	String				inputUri, configFilename, sampleFilename, managemntHost, legoDirectiry;
	int					managementPort;
	ProcMon				procMon	= null;

	public AutoIdentify(String InputUri, String ConfigFilename, String SampleFilename, String ManagemntHost,
			int ManagementPort, String LegoDirectory)
	{
		inputUri = InputUri;
		configFilename = ConfigFilename;
		sampleFilename = SampleFilename;
		managemntHost = ManagemntHost;
		managementPort = ManagementPort;
		legoDirectiry = LegoDirectory;
	}

	public void Start() throws Exception
	{
		String ServerUri = Parameters.Get("WebSocketServerUri", "ws://127.0.0.1");
		String ServerPort = Parameters.Get("WebSocketListenPort", "8887");

		ManagementClient client = new ManagementClient(new URI(ServerUri + ":" + ServerPort), this);
		try
		{
			client.connect();
		}
		catch (Exception e)
		{
			logger.error("Failed to connect to server", e);
		}

		GetSamples gs = new GetSamples(this);

		procMon = gs.Start(inputUri, sampleFilename, configFilename+"_capture", managemntHost, managementPort, legoDirectiry);

		long StartTimr = System.currentTimeMillis();
		while (!procMon.isComplete())
		{
			if ((System.currentTimeMillis() - StartTimr) > (10 * 1000))
			{
				break;
			}
			Thread.sleep(50);
		}
		;

		if (!procMon.isComplete())
		{
			procMon.Kill();
		}

		if (new File(sampleFilename).length() == 0)
		{
			if (client.isOpen())
			{
				client.SendStatus("Sample file is 0 in size");
			}
			logger.error("Sample file is 0 in size");
			throw new Exception("Can not capture sample file");
		}

		client.SendStatus("Finish to get Sample file. Starting identefication phase");

		Identify id = new Identify(this);

		new File(configFilename).delete();

		procMon = id.Start(inputUri, new File(sampleFilename).getAbsolutePath(),configFilename, managemntHost, managementPort); 

		while (!procMon.isComplete())
		{
			Thread.sleep(50);
		}

		if (!(new File(configFilename).exists()))
		{
			client.SendStatus("Failed to Identify the encasulation");
			return;
		}

		ScriptFile sf = new ScriptFile(configFilename);

		ENCAPSULATION encap = sf.getEncapsolation();
		IdentifiedEncapsulation ie = IdentifiedEncapsulation.newBuilder().setEncapsulation(encap).build();
		Header h = Header.newBuilder().setOpcode(OPCODE.IDENTYPIED_ENCAPSULATION).setMessageData(ie.toByteString())
				.build();
		if (client.isOpen())
		{
			client.send(h.toByteArray());
		}
	}

	@Override
	public void onConnectionChange(Boolean status)
	{
		logger.debug("onConnectionChange: " + status);
	}

	@Override
	public void UpdateStatus(String status)
	{
		if (status != null)
		{
			logger.debug("UpdateStatus: " + status);
			System.out.println(status);
		}

	}

	@Override
	public void OperationCompleted()
	{
		logger.debug("OperationCompleted");
		System.out.println("Operation completed");
	}

	@Override
	public void OperationStarted()
	{
		logger.debug("OperationStarted");
		System.out.println("Operation started");

	}

	@Override
	public void OperationInSync(Channel ch)
	{
		if (ch != null)
		{
			logger.debug("OperationInSync ch=" + ch);
		}
	}

	@Override
	public void OperationOutOfSync(Channel ch)
	{
		if (ch != null)
		{
			logger.debug("OperationOutOfSync ch=" + ch);
		}
	}

	@Override
	public void SetEncapsulation(ENCAPSULATION encap)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void UpdateCounters(Statistics statistics)
	{
		// TODO Auto-generated method stub

	}
}
