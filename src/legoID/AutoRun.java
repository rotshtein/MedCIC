package legoID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

public class AutoRun
{

	static final Logger logger = Logger.getLogger("AutoRun");

	public static void main(String[] args)
	{
		CommandLine cmd = null;
		if ((cmd = GetParameters(args)) == null)
		{
			logger.warn("Iligal parameters");
			System.exit(-1);
		}
		try
		{
			AutoIdentify ai = new AutoIdentify(cmd.getOptionValue("input"), cmd.getOptionValue("config"),
					cmd.getOptionValue("sample"), cmd.getOptionValue("host"),
					Integer.parseInt(cmd.getOptionValue("port")));

			ai.Start();
		}
		catch (Exception e)
		{
			logger.error("Error during Autoidentification process", e);
		}
	}

	private static CommandLine GetParameters(String[] args)
	{
		Options options = new Options();

		Option input = new Option("i", "input", true, "input URI");
		Option config = new Option("c", "config", true, "configuration file location");
		Option sample = new Option("s", "sample", true, "sample file path");

		Option host = new Option("m", "host", true, "Management host ip address");
		Option port = new Option("p", "port", true, "Management port");

		input.setRequired(true);
		options.addOption(input);

		config.setRequired(true);
		options.addOption(config);

		sample.setRequired(true);
		options.addOption(sample);

		host.setRequired(true);
		options.addOption(host);

		port.setRequired(true);
		options.addOption(port);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try
		{
			cmd = parser.parse(options, args);
		}
		catch (ParseException e)
		{
			System.out.println(e.getMessage());
			formatter.printHelp("AutoRun", options);

			cmd = null;
		}
		return cmd;
	}
}
