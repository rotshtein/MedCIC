package lego;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ScriptFile
{
	List<ModuleConfiguration> moduleList = new ArrayList<ModuleConfiguration>();
	ManagementConfiguration management = null;
	
	public ScriptFile() {}
	
	public ScriptFile(String Filename) throws FileNotFoundException, IOException 
	{
		try (BufferedReader br = new BufferedReader(new FileReader(Filename))) 
		{
		    String line;
		    String TextBlock = "";
		    while ((line = br.readLine()) != null) 
		    {
		    	if (line.trim().startsWith("#"))
		    		continue;
		    	
		    	TextBlock += line;
		    	if (line.trim().isEmpty())
		    	{
		    		if (TextBlock.toLowerCase().startsWith("path"))
		    		{
		    			ModuleConfiguration b = new ModuleConfiguration(TextBlock);
		    			AddModule(b);
		    		}
		    		else
		    		{
		    			setManagement(new ManagementConfiguration(TextBlock));
		    		}
		    		TextBlock = "";
		    	}
		    }
		}
	}
	
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
	
	public Boolean BuildRecordToFileScript(String SourceUri, String ConfigFile, String Server, int Port)
	{
		AddModule(new ModuleConfiguration("1", "udpserver", SourceUri, "1.1"));
		AddModule(new ModuleConfiguration("1.1", "cesrawinput", null, "1.1.1"));
		AddModule(new ModuleConfiguration("1.1.1", "bitoutput", ConfigFile + ",bin", ""));
	
		setManagement(new ManagementConfiguration(Server, Port));
		
		Write(ConfigFile);
		
		return true;
	}
	
	public ScriptFile BuildProductionScript(String DestUri, String ConfigFile, String Server, int Port)
	{
		ScriptFile NewScript = new ScriptFile();
		NewScript.AddModule(new ModuleConfiguration("1", "udpserver", this.moduleList.get(0).params, "1.1"));	
		NewScript.AddModule(new ModuleConfiguration("1.1", "cesrawinput", null, "1.1.1"));
		
		String encap = null;
		String parameters = null;
		for (ModuleConfiguration m : moduleList)
		{
			if (m.module == "D_E")
			{
				encap = m.module;
				parameters = m.params;
			}
		}
		if (encap == null)
		{
			return null;
		}
		NewScript.AddModule(new ModuleConfiguration("1.1.1", encap, parameters , "1.1.1.1"));
		
		NewScript.AddModule(new ModuleConfiguration("1.1.1.1", "cesrawoutput", null, "1.1.1.1.1"));
		NewScript.AddModule(new ModuleConfiguration("1.1.1.1.1", "udpclient", DestUri, ""));
		
		NewScript.setManagement(new ManagementConfiguration(Server, Port));
		return NewScript;
	}
	
	
}
