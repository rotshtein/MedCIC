package tcc;

import lego.ScriptFile;

public class Identify extends Operation
{

	ScriptFile		scriptFile	= null;
	static String	Exe			= Parameters.Get("IdentificationExe", "C:\\programs\\lego\\bin\\IDBlock.exe");

	public Identify(GuiInterface gui)
	{
		super(Exe, gui, "Identify");
	}

	public ProcMon Start(String SampleFilename, String ConfigFile, String Server, int Port) throws Exception
	{
		scriptFile = new ScriptFile();

		return StartAction(new String[]
		{ "IDBlock.exe", SampleFilename });
	}
}
