package tcc;

import lego.ScriptFile;

public class GetSamples extends Operation
{
	ScriptFile scriptFile = null;
	static String Exe = Parameters.Get("GetSamplesExe", "C:\\programs\\lego\\bin\\BuildingBlock.exe");
	
	public GetSamples(GuiInterface gui)
	{
		super(Exe, gui, "Geting Sample for Identification");
	}
	
	
	public ProcMon Start(String SourceUri, String ConfigFile, String Server, int Port) throws Exception
	{
		scriptFile = new ScriptFile();
		
		if (scriptFile.BuildRecordToFileScript(SourceUri, ConfigFile, Server, Port))
		{
			return StartAction(new String [] {"ProcessBlock",ConfigFile});
		}
		else
		{
			return null;
		}
	}

}
