package lego;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ScriptFile
{
	List<ModuleConfiguration> moduleList = new ArrayList<ModuleConfiguration>();
	ManagementConfiguration management = null;
	
	public ScriptFile(String Filename)
	{
		
	}
	
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
		
		management.toString();
		
		out.close();
		
		return true;
	}
	
}
