package tcc;

import lego.ProcMon;
import lego.ScriptFile;

public class GetSamples extends Operation
{
	static String	Exe	= Parameters.Get("GetSamplesExe", "C:\\programs\\lego\\bin\\ProcesssBlock.exe");

	public GetSamples(GuiInterface gui)
	{
		this(gui, "Geting Sample for Identification");
	}

	public GetSamples(GuiInterface gui, String Description)
	{
		super(Exe, gui, Description);
	}

	public ProcMon Start(	String SourceUri, String IdFile, String ConfigFile, String Server, int Port) throws Exception
	{
		ScriptFile scriptFile = new ScriptFile(ConfigFile, Server, Port);
		
		scriptFile.BuildRecordToFileScript(SourceUri, SourceUri);
		scriptFile.Write();
		
		return StartAction(new String[]	{ Exe, ConfigFile });
	}
}
