package lego;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ScriptFile
{
	List<ModuleConfiguration> moduleList = new ArrayList<ModuleConfiguration>();
	ManagementConfiguration management = null;
	
	public ScriptFile() {}
	
	public void AddModule(ModuleConfiguration module)
	{
		moduleList.add(module);
	}
	
	public void setManagement(ManagementConfiguration mngmnt)
	{
		management = mngmnt;
	}
	
	public Boolean Write(String Filename)
	{
		PrintWriter out = null;
		try
		{
			out = new PrintWriter(Filename);
		}
		catch (FileNotFoundException e)
		{
			return false;
		}
		
		for (ModuleConfiguration m : moduleList)
		{
			out.print(m.toString());
			out.println();
		}
		
		out.print(management.toString());
		
		out.close();
		
		return true;
	}
	
	public static Boolean BuildAnalysisScript(String InputFilename, String ConfigFile, String Server, int Port)
	{
		ScriptFile sf = new ScriptFile();
		ModuleConfiguration m1 = new ModuleConfiguration("1", "bitinput", InputFilename, "1");
		sf.AddModule(m1);
		
		sf.AddModule(new ModuleConfiguration("1.1", "onebitfrombyte", null, "1.1"));
		
		sf.AddModule(new ModuleConfiguration("1.1.1", "escplusplus", "synclength=12,syncword=0xe8c0,width=551,mode=cut,0-12,21-29,36-45,311-320", "1.1.1"));
		
		sf.AddModule(new ModuleConfiguration("1.1.1.1", "bitviewer", null, "1.1.1.1"));
		
		sf.setManagement(new ManagementConfiguration(Server, Port));
		
		sf.Write(ConfigFile);
		
		return true;
	}
	
}
