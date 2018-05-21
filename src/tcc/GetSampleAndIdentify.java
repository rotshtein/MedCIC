package tcc;

import java.net.URI;
import lego.ProcMon;
import lego.ScriptFile;

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

	public ProcMon Start(String InputUri, String SampleFilename, String ConfigFile, String Server, int Port) throws Exception
	{
		//scriptFile = new ScriptFile();

		String IdetifyExe = Parameters.Get("AutoIdentificationExe","java");
		URI uri = new URI(InputUri);
		return StartAction(new String[]	{ IdetifyExe, "-jar", "AutoRun.jar", 
															"-i", uri.getHost() + "," + uri.getPort(), 
															"-c", ConfigFile, 
															"-s", SampleFilename, 
															"-m", Server, 
															"-p",  String.valueOf(Port)});
	}
}
