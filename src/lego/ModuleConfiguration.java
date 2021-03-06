package lego;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class ModuleConfiguration
{

	static Logger logger = Logger.getLogger("ModuleConfiguration");

	final String	PATH	= "path";
	final String	MODULE	= "module";
	final String	PARAMS	= "param";
	final String	OUT		= "out";
	final String	POS		= "pos";

	String		path;
	String		module;
	String		params;
	String[]	out;
	String		pos;

	public ModuleConfiguration(String TextBlock)
	{
		List<String> outs = new ArrayList<String>();
		String[] lines = TextBlock.split("\\r?\\n");

		for (String line : lines)
		{
			String[] val = line.split("\\s");
			if (val.length == 0) continue;

			if (line.toLowerCase().startsWith(PATH.toLowerCase()))
			{
				path = val[1];
			}

			else if (line.toLowerCase().startsWith(MODULE.toLowerCase()))
			{
				module = val[1];
			}

			else if (line.toLowerCase().startsWith(PARAMS.toLowerCase()))
			{
				params = val[1];
			}

			else if (line.toLowerCase().startsWith(OUT.toLowerCase()))
			{
				outs.add(val[1]);
			}

			else if (line.toLowerCase().startsWith(POS.toLowerCase()))
			{
				pos = val[1];
			}

		}
		out = new String[outs.size()];
		outs.toArray(out);
	}

	public ModuleConfiguration()
	{
		this("", "", "", new String[0]);
	}

	public ModuleConfiguration(String Path, String Module, String Params, String Out)
	{
		this(Path, Module, Params, new String[]
		{ Out });
	}

	public ModuleConfiguration(String Path, String Module, String Params, String[] Out)
	{

		path = Path;
		module = Module;
		params = Params;
		out = Out;
	}

	public static String UriToParam(String Uri)
	{
		try
		{
			URI u = new URI(Uri);
			return UriToParam(u);
		}
		catch (Exception e)
		{
			logger.error("Illigal Uri", e);
		}

		return null;
	}

	public static String UriToParam(URI Uri)
	{
		try
		{
			return Uri.getHost() + "," + Uri.getPort();
		}
		catch (Exception e)
		{
			logger.error("Illigal Uri", e);
		}

		return null;
	}

	public String toString()
	{
		final String NewLine = System.getProperty("line.separator");
		String msg = PATH + " " + path + NewLine;
		msg += MODULE + " " + module + NewLine;
		if (params != null & params != "")
		{
			msg += PARAMS + " " + params + NewLine;
		}
		int i = 1;
		if (out != null & out[0] != "")
		{
			for (String o : out)
			{
				if (o != "")
				{
					msg += OUT + i + " " + o + NewLine;
				}
				i++;
			}
		}

		return msg;
	}
}
