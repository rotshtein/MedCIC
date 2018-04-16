package lego;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class ConfigurationMessage
{
	/*
	 * public final String ISSUE_MSG_ACTIVE_STRING = "Active"; public final String
	 * ISSUE_MSG_START_STRING = "Start"; public final String ISSUE_MSG_PAUSE_STRING
	 * = "Pause"; public final String ISSUE_MSG_DONE_STRING = "Done"; public final
	 * String ISSUE_MSG_RESTART_STRING = "Restart"; public final String
	 * ISSUE_MSG_PING_STRING = "Ping"; public final String ISSUE_MSG_UNPAUSE_STRING
	 * = "Unpause"; public final String ISSUE_MSG_SYNC_STRING = "Sync"; public final
	 * String ISSUE_MSG_LOST_SYNC_STRING = "LostSync";
	 */
	Logger							logger		= Logger.getLogger("ConfigurationMessage");
	
	public String	path;
	public String	module;
	public int		issue;
	public String	issuestring;
	public String	message;
	public long		input;
	public long		output;
	public int		percent;
	public int		good;
	public int		bad;
	public int		isinput;
	public int		isoutput;
	
	static final int ISSUE_MSG_ACTIVE = 1;
	static final int ISSUE_MSG_START = 2; 
	static final int ISSUE_MSG_PAUSE = 3; 
	static final int ISSUE_MSG_DONE = 4; 
	static final int ISSUE_MSG_RESTART = 5; 
	static final int ISSUE_MSG_PING = 6; 
	static final int ISSUE_MSG_UNPAUSE = 7; 
	static final int ISSUE_MSG_SYNC = 8; 
	static final int ISSUE_MSG_LOST_SYNC = 9; 
	static final int ISSUE_MSG_FATAL = 100; 
	static final int ISSUE_MSG_ERROR = 110; 
	static final int ISSUE_MSG_WARNING = 120; 
	static final int ISSUE_MSG_NOTICE = 130; 
	static final int ISSUE_MSG_INFO = 140; 
	static final int ISSUE_MSG_DEBUG = 150; 
	static final int ISSUE_MSG_TRACE = 160;

	public ConfigurationMessage()
	{
	}

	public ConfigurationMessage(int t, String Path)
	{
		if (Path == null) Path = "1";

		this.path = Path;
		this.module = "ProcessBlock";
		this.issue = t;
		this.issuestring = "";
		this.message = "";
		this.input = 0;
		this.output = 0;
		this.percent = 0;
		this.good = 0;
		this.bad = 0;
		this.isinput = 0;
		this.isoutput = 0;
	}

	public String toJson() throws Exception
	{
		ObjectMapper mapper = new ObjectMapper();
		String object = mapper.writeValueAsString(this);
		return object;
	}

	public static ConfigurationMessage fromJson(String JasonString) throws Exception
	{
		ObjectMapper mapper = new ObjectMapper();
		ConfigurationMessage object = mapper.readValue(JasonString, ConfigurationMessage.class);
		return object;
	}
	
	public String StatusMessage()
	{
		String issuePrefix = "";
		switch (issue)
		{
			case ISSUE_MSG_ERROR:
			case ISSUE_MSG_FATAL:
				logger.error("Got error message from " + path + ", " + module + ":" + message);
				issuePrefix = "Error";
				break;
				
			case ISSUE_MSG_WARNING:
				logger.warn("Got warning message from " + path + ", " + module + ":" + message);
				issuePrefix = "Warning";
				break;
				
			case ISSUE_MSG_ACTIVE:
			case ISSUE_MSG_START:
			case ISSUE_MSG_DONE:
			case ISSUE_MSG_SYNC:
			case ISSUE_MSG_LOST_SYNC:
				break;
				

			case ISSUE_MSG_NOTICE: 
			case ISSUE_MSG_INFO: 
			case ISSUE_MSG_DEBUG: 
			case ISSUE_MSG_TRACE:
				logger.debug("Got message from " + path + ", " + module + ":" + message);
				return null;
				
				
			default:
				logger.debug("Got unknown message from " + path + ", " + module + ":" + message);
				return null;
		}
		
		String status = module + ": ";
		if (issuePrefix != null & issuePrefix != "")
		{
			status += issuePrefix + ": ";
		}
		status += issuestring;
		if (message != null & message != "")
		{
			status += " -> " + message;
		}
		
		
		
		return status;
	}

}
