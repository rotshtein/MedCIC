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
	Logger logger = Logger.getLogger("ConfigurationMessage");

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

	public static final int	ISSUE_MSG_ACTIVE	= 1;
	public static final int	ISSUE_MSG_START		= 2;
	public static final int	ISSUE_MSG_PAUSE		= 3;
	public static final int	ISSUE_MSG_DONE		= 4;
	public static final int	ISSUE_MSG_RESTART	= 5;
	public static final int	ISSUE_MSG_PING		= 6;
	public static final int	ISSUE_MSG_UNPAUSE	= 7;
	public static final int	ISSUE_MSG_SYNC		= 8;
	public static final int	ISSUE_MSG_LOST_SYNC	= 9;
	public static final int	ISSUE_MSG_FATAL		= 100;
	public static final int	ISSUE_MSG_ERROR		= 110;
	public static final int	ISSUE_MSG_WARNING	= 120;
	public static final int	ISSUE_MSG_NOTICE	= 130;
	public static final int	ISSUE_MSG_INFO		= 140;
	public static final int	ISSUE_MSG_DEBUG		= 150;
	public static final int	ISSUE_MSG_TRACE		= 160;

	public ConfigurationMessage()
	{
	}

	public ConfigurationMessage(String Path, int issue)
	{
		this(Path, "", issue, GetIssueString(issue));
	}

	public ConfigurationMessage(String Path, String Module, int issue, String IssueString)
	{
		if (Path == null) Path = "1";

		this.path = Path;
		this.module = Module;
		this.issue = issue;
		this.issuestring = IssueString;
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

		String status = module;
		if (issuePrefix != null & issuePrefix != "")
		{
			status += " : " + issuePrefix;
		}
		status += " [" + issuestring + "]";
		if (message != null & message != "")
		{
			status += " input:" + input + " output:" + output + " >> " + message;
		}

		return status;
	}

	public static String GetIssueString(int issue)
	{
		switch (issue)
		{
		case ISSUE_MSG_ACTIVE:
			return "Active";
		case ISSUE_MSG_START:
			return "Start";
		case ISSUE_MSG_PAUSE:
			return "Pause";
		case ISSUE_MSG_DONE:
			return "Done";
		case ISSUE_MSG_RESTART:
			return "Restart";
		case ISSUE_MSG_PING:
			return "Ping";
		case ISSUE_MSG_UNPAUSE:
			return "Unpause";
		case ISSUE_MSG_SYNC:
			return "Sync";
		case ISSUE_MSG_LOST_SYNC:
			return "Lost sync";
		case ISSUE_MSG_FATAL:
			return "Fatal";
		case ISSUE_MSG_ERROR:
			return "Error";
		case ISSUE_MSG_WARNING:
			return "Warning";
		case ISSUE_MSG_NOTICE:
			return "Notice";
		case ISSUE_MSG_INFO:
			return "Info";
		case ISSUE_MSG_DEBUG:
			return "Debug";
		case ISSUE_MSG_TRACE:
			return "Trace";
		}
		return "";
	}

}
