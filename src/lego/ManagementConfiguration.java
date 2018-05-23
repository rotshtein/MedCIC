/*
 * 
 */
package lego;

import tcc.Parameters;

// TODO: Auto-generated Javadoc
/**
 * The Class ManagementConfiguration.
 * 
 * Serialise and deserialse the management block in the lego config file
 */
public class ManagementConfiguration
{

	/** The server ip address. */
	String server;

	/** The server udp port port. */
	int port;

	/**
	 * Instantiates a new management configuration block.
	 *
	 * @param TextBlock
	 *            the text of the management block read from a file
	 */
	ManagementConfiguration(String TextBlock)
	{
		String[] lines = TextBlock.split("\\W+");

		for (String line : lines)
		{
			String[] val = line.split("\\s");
			if (val.length == 0) continue;

			if (line.toLowerCase().startsWith("mgmtserver"))
			{
				server = val[0];
			}

			if (line.toLowerCase().startsWith("mgmtport"))
			{
				port = Integer.parseInt(val[0]);
			}
		}
	}

	/**
	 * Instantiates a new management configuration usually for writing to a lego
	 * config file.
	 */
	ManagementConfiguration()
	{
		String host = Parameters.Get("ManagementHost");
		int port = Integer.parseInt(Parameters.Get("ManagementPort"));

		if ((port > 0) & (host != null) & (host != ""))
		{
			server = host;
			this.port = port;
		}
		else
		{
			server = "127.0.0.1";
			port = 11001;
		}
	}

	/**
	 * Instantiates a new management configuration based on ip and port.
	 *
	 * @param Server
	 *            the server
	 * @param Port
	 *            the port
	 */
	ManagementConfiguration(String Server, int Port)
	{
		server = Server;
		port = Port;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		final String NewLine = System.getProperty("line.separator");
		String msg = "mgmt 1" + NewLine;
		msg += "mgmtserver " + server + NewLine;
		msg += "mgmtport " + port + NewLine;

		return msg;
	}

}
