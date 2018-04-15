package tcc;


import java.io.File;
import org.apache.log4j.Logger;

import lego.ModuleConfiguration;
import lego.ScriptFile;
import medcic_proto.MedCic.ENCAPSULATION;
import sun.font.Script;

public class Mediate extends Operation
{

	final static Logger logger = Logger.getLogger("Record");

	public Mediate(String MediateExe, GuiInterface gui)
	{
		super(MediateExe, gui, "Production");
	}

	public ProcMon Start(ENCAPSULATION encap, String InputUrl, String OutputUrl1, String ConfigFilename) throws Exception
	{
		ProcMon p = null;
		if (new File(super.exe_file).exists())
		{
				ScriptFile.BuildProductionScript(encap, InputUrl, OutputUrl1, ConfigFilename, "127.0.0.1", 11001);

			try
			{
				String [] vars =	{ super.exe_file, ConfigFilename};
				p = super.StartAction(vars);
			}
			catch (Exception ex)
			{
				throw ex;
			}
		}
		return p;
	}
}
