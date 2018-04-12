package tcc;


import java.io.File;
import org.apache.log4j.Logger;

import medcic_proto.MedCic.ENCAPSULATION;

public class Mediate extends Operation
{

	final static Logger logger = Logger.getLogger("Record");

	public Mediate(String MediateExe, GuiInterface gui)
	{
		super(MediateExe, gui, "Production");
	}

	public ProcMon Start(ENCAPSULATION encap, String InputUrl1, String InputUrl2, String OutputUrl1, String OutputUrl2) throws Exception
	{
		ProcMon p = null;
		if (new File(super.exe_file).exists())
		{

			try
			{
				String [] vars =	{ super.exe_file, encap.getValueDescriptor().toString(),
						InputUrl1, InputUrl2,OutputUrl1, OutputUrl2};
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
