package legoID;

import lego.ProcMon;
import tcc.GuiInterface;
import tcc.Operation;
import tcc.Parameters;

public class Identify extends Operation
{
	static String	Exe	= Parameters.Get("GetSamplesExe", "C:\\programs\\lego\\bin\\ProcesssBlock.exe");

	public Identify(GuiInterface gui)
	{
		this(gui, "Geting Sample for Identification");
	}

	public Identify(GuiInterface gui, String Description)
	{
		super(Exe, gui, Description);
	}

	public ProcMon Start(	String SourceUri, String SampleFile, String ConfigFile, String Server, int Port) throws Exception
	{
		String IdentificationExe = Parameters.Get("IdentificationExe", "C\\:\\bin\\lego\\bin\\IdBlock.exe");
		return StartAction(new String[]	{ IdentificationExe, SampleFile, "-o", ConfigFile });
	}
}


