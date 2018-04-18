package tcc;

import lego.ScriptFile;

public class Identify extends Operation
{

	ScriptFile		scriptFile	= null;
	static String	Exe			= Parameters.Get("IdentificationExe", "C:\\programs\\lego\\bin\\IDBlock.exe");

	public Identify(GuiInterface gui)
	{
		this(gui, "Identify");
	}
	
	public Identify(GuiInterface gui, String Description)
	{
		super(Exe, gui, Description);
	}

	public ProcMon Start(String SampleFilename, String ConfigFile, String Server, int Port) throws Exception
	{
		scriptFile = new ScriptFile();

		String IdetifyExe = Parameters.Get("IdentificationExe","C\\:programs\\lego\\bin\\IdBlock.exe");
		return StartAction(new String[]	{ IdetifyExe, SampleFilename, ConfigFile });
	}
}
