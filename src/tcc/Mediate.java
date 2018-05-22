package tcc;

import java.io.File;
import org.apache.log4j.Logger;

import lego.ProcMon;
import lego.ScriptFile;
import medcic_proto.MedCic.ENCAPSULATION;

public class Mediate extends Operation
{

	final static Logger logger = Logger.getLogger("Record");

	public Mediate(String MediateExe, GuiInterface gui)
	{
		this(MediateExe, gui, "Production");
	}

	public Mediate(String MediateExe, GuiInterface gui, String description)
	{
		super(MediateExe, gui, description);
	}

	public ProcMon Start(ENCAPSULATION encap, String InputUrl1, String OutputUrl1, String InputUrl2, String OutputUrl2,
			String ConfigFilename) throws Exception
	{
		ProcMon p = null;
		if (new File(super.exe_file).exists())
		{
			ScriptFile sf = new ScriptFile(ConfigFilename, Parameters.Get("ManagementHost", "127.0.0.1"),
					Integer.parseInt(Parameters.Get("ManagementPort", "11001")));
			if (sf.BuildProductionScript(encap, InputUrl1, OutputUrl1, InputUrl2, OutputUrl2) == false)
			{
				throw new Exception("Unknown encapsulation");
			}
			sf.Write();
			sf.Write("c:\\bin\\lego\\config\\script.lego"); // $$$$
			try
			{
				String[] vars =
				{ super.exe_file, ConfigFilename };
				p = super.StartAction(vars);
			}
			catch (Exception ex)
			{
				logger.error("Failed to run mediation. Throwigg exception", ex);
				throw ex;
			}
		}
		return p;
	}
}
