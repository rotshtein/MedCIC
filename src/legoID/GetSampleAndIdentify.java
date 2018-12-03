package legoID;

import java.net.URI;
import lego.ProcMon;
import lego.ScriptFile;
import tcc.GuiInterface;
import tcc.Operation;
import tcc.Parameters;

public class GetSampleAndIdentify extends Operation
{

	ScriptFile		scriptFile	= null;
	static String	Exe			= Parameters.Get("IdentificationExe", "C:\\programs\\lego\\bin\\IDBlock.exe");

	public GetSampleAndIdentify(GuiInterface gui)
	{
		this(gui, "Identify");
	}

	public GetSampleAndIdentify(GuiInterface gui, String Description)
	{
		super(Exe, gui, Description);
	}

	public ProcMon Start(String InputUri, String SampleFilename, String ConfigFile, String Server, int Port, String LegoPath)
			throws Exception
	{
		// scriptFile = new ScriptFile();

		String IdetifyExe = Parameters.Get("AutoIdentificationExe", "java");
		String autoIdentificationJar = Parameters.Get("AutoIdentificationJar", "./AutoRun.jar");
		URI uri = new URI(InputUri);
		return StartAction(new String[]
		{ IdetifyExe, "-jar", autoIdentificationJar, "-i", uri.getHost() + "," + uri.getPort(), "-c", ConfigFile, "-s",
				SampleFilename, "-m", Server, "-p", String.valueOf(Port), "-l", LegoPath });
	}
}
