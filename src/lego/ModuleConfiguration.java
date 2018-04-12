package lego;

import java.util.ArrayList;
import java.util.List;

public class ModuleConfiguration
{
	final String PATH = "path";
	final String MODULE = "module";
	final String PARAMS = "param";
	final String OUT = "out";
	final String POS = "pos";
	
	String path;
	String module;
	String params;
	String [] out;
	String pos;
	
	public ModuleConfiguration(String TextBlock)
	{
		List<String> outs = new ArrayList<String>();
		String[] lines = TextBlock.split("\\W+");
		
		for (String line : lines)
		{
			String []val = line.split("\\s");
			if (val.length == 0)
				continue;
			
			if (line.toLowerCase().startsWith(PATH.toLowerCase()))
			{
				path = val[0];
			}
			
			if (line.toLowerCase().startsWith(MODULE.toLowerCase()))
			{
				module = val[0];
			}

			if (line.toLowerCase().startsWith(PARAMS.toLowerCase()))
			{
				params = val[0];
			}

			if (line.toLowerCase().startsWith(OUT.toLowerCase()))
			{
				outs.add(val[0]);
			}

			if (line.toLowerCase().startsWith(POS.toLowerCase()))
			{
				pos = val[0];
			}

		}
		outs.toArray(out);
	}
	
	public ModuleConfiguration()
	{
		this("","","",new String[0],"");
	}
	
	public ModuleConfiguration(String Path, String Module, String Params, String Out)
	{
		this(Path, Module, Params, new String []{Out}, "1,1");
	}
	
	public ModuleConfiguration(String Path, String Module, String Params, String []Out)
	{
		this(Path, Module, Params, Out, "1,1");
	}
	
	public ModuleConfiguration(String Path, String Module, String Params, String []Out, String Pos)
	{
		
		path = Path;
		module = Module;
		params = Params;
		out = Out;
		pos = Pos;
	}
	
	
	public String toString()
	{
		final String NewLine = System.getProperty("line.separator");
		String msg = PATH + " " + path + NewLine;
		msg += MODULE + " " + module + NewLine;
		if (params != null)
		{
			msg += PARAMS + " " + params + NewLine;
		}
		int i = 1;
		if (out != null & out[0] != "")
		{
			for (String o : out)
			{
				msg += OUT + i +" " + o + NewLine;
				i++;
			}
		}
		if (pos != null ^ pos != "")
		{
			msg += POS + " " + pos + NewLine;
		}
		
		return msg;
	}
}