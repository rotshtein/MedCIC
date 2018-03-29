package lego;


public class ManagementConfiguration
{

	String server;
	int port;
	
	ManagementConfiguration(String TextBlock)
	{
		String[] lines = TextBlock.split("\\W+");
		
		for (String line : lines)
		{
			String []val = line.split("\\s");
			if (val.length == 0)
				continue;
			
			if (line.toLowerCase().startsWith("mgmtserver") )
			{
				server = val[0]; 
			}
			
			if (line.toLowerCase().startsWith("mgmtport") )
			{
				port = Integer.parseInt(val[0]); 
			}
		}
	}
	
	ManagementConfiguration()
	{
		this ("127.0.0.1", 11000);
	}
	
	ManagementConfiguration(String Server, int Port)
	{
		server = Server;
		port = Port;
	}
	
	public String toString()
	{
		final String NewLine = System.getProperty("line.separator");
		String msg = "mgmt 1" + NewLine;
		msg += "mgmtserver " + server + NewLine;
		msg += "mgmtport " + port + NewLine;
		
		return msg;
	}
	
}
