package tcc;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Parameters
{

	File		configFile	= null;
	Properties	props;

	public Parameters(String Filename) throws Exception
	{
		configFile = new File("config.properties");
		if (!configFile.exists())
		{
			if (!configFile.createNewFile())
			{
				throw (new Exception("No configuration file"));
			}

		}
		FileReader config = new FileReader(configFile);
		props = new Properties();
		props.load(config);
		config.close();
	}

	public String Get(String name)
	{
		return Get(name, "");
	}

	public String Get(String name, String defaultValue)
	{
		String value = "";
		try
		{
			value = props.getProperty(name);
		}
		catch (Exception e)
		{
		}

		if (value == null)
		{
			try
			{
				Set(name, defaultValue);
				value = defaultValue;
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return value;
	}

	public boolean Set(String name, String Value) throws IOException
	{
		props.setProperty(name, Value);
		FileWriter writer = new FileWriter(configFile);
		props.store(writer, "mediation settings");
		writer.close();
		return true;
	}

}
