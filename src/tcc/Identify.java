package tcc;

import lego.ScriptFile;

public class Identify extends Operation
{
	ScriptFile scriptFile = null;
	
	public Identify (String Exe,  GuiInterface gui, String Operation)
	{
		super(Exe, gui, "Identify");
	}
	
	public ProcMon Start(String SampleFilename, String ConfigFile, String Server, int Port) throws Exception
	{
		scriptFile = new ScriptFile();
		
		return StartAction(new String [] {"IDBlock.exe",SampleFilename});
	}
}
