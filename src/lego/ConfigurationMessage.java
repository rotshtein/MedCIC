/*
 * 
 */
package lego;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

// TODO: Auto-generated Javadoc
/**
 * The Class ConfigurationMessage.
 * 
 * serialise and deserialise lego jason based status messages
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigurationMessage
{

	/** The logger. */
	Logger logger = Logger.getLogger("ConfigurationMessage");

	/** The path as string of numbers 1.1.1 for instance. */
	@JsonIgnore
	public String address;
	
	/** The path as string of numbers 1.1.1 for instance. */
	public String path;

	/** The module name */
	public String module;

	/** The issue number. */
	public int issue;

	/** The issue description string. */
	public String issuestring;

	/** The status message. */
	public String message;

	/** The number of input bytes. */
	public long input;

	/** The number of output bytes. */
	public long output;

	/** The percent completed of a file. */
	public int percent;

	/** The number of good bytes. */
	public int good;

	/** The number of bad bytes. */
	public int bad;

	/** Boolean - True if this is an input port. */
	public int isinput;

	/** Boolean - True if this is an output port. */
	public int isoutput;
	
	/** Boolean - True (!0) - sent from the server. */
	public int clientserver; 

	/** The Constant ISSUE_MSG_ACTIVE. */
	public static final int ISSUE_MSG_ACTIVE = 1;

	/** The Constant ISSUE_MSG_START. */
	public static final int ISSUE_MSG_START = 2;

	/** The Constant ISSUE_MSG_PAUSE. */
	public static final int ISSUE_MSG_PAUSE = 3;

	/** The Constant ISSUE_MSG_DONE. */
	public static final int ISSUE_MSG_DONE = 4;

	/** The Constant ISSUE_MSG_RESTART. */
	public static final int ISSUE_MSG_RESTART = 5;

	/** The Constant ISSUE_MSG_PING. */
	public static final int ISSUE_MSG_PING = 6;

	/** The Constant ISSUE_MSG_UNPAUSE. */
	public static final int ISSUE_MSG_UNPAUSE = 7;

	/** The Constant ISSUE_MSG_SYNC. */
	public static final int ISSUE_MSG_SYNC = 8;

	/** The Constant ISSUE_MSG_LOST_SYNC. */
	public static final int ISSUE_MSG_LOST_SYNC = 9;
	
	public static final int  ISSUE_MSG_IS_SYNC = 10;

	/** The Constant ISSUE_MSG_FATAL. */
	public static final int ISSUE_MSG_FATAL = 100;

	/** The Constant ISSUE_MSG_ERROR. */
	public static final int ISSUE_MSG_ERROR = 110;

	/** The Constant ISSUE_MSG_WARNING. */
	public static final int ISSUE_MSG_WARNING = 120;

	/** The Constant ISSUE_MSG_NOTICE. */
	public static final int ISSUE_MSG_NOTICE = 130;

	/** The Constant ISSUE_MSG_INFO. */
	public static final int ISSUE_MSG_INFO = 140;

	/** The Constant ISSUE_MSG_DEBUG. */
	public static final int ISSUE_MSG_DEBUG = 150;

	/** The Constant ISSUE_MSG_TRACE. */
	public static final int ISSUE_MSG_TRACE = 160;

	/**
	 * Instantiates a new configuration message.
	 */
	public ConfigurationMessage()
	{
	}

	/**
	 * Instantiates a new configuration message.
	 *
	 * @param Path
	 *            the module path
	 * @param issue
	 *            the issue number
	 */
	public ConfigurationMessage(String Path, int issue)
	{
		this(Path, "", issue, GetIssueString(issue));
	}

	/**
	 * Instantiates a new configuration message.
	 *
	 * @param Path
	 *            the module path
	 * @param Module
	 *            the module name
	 * @param issue
	 *            the issue number
	 * @param IssueString
	 *            the issue string
	 */
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
		this.clientserver = 1;
	}

	/**
	 * To json.
	 *
	 * @return the string
	 * @throws Exception
	 *             the exception
	 */
	public String toJson() throws Exception
	{
		ObjectMapper mapper = new ObjectMapper();
		String object = mapper.writeValueAsString(this);
		return object;
	}

	/**
	 * From json.
	 *
	 * @param JasonString
	 *            the jason string
	 * @return the configuration message
	 * @throws Exception
	 *             the exception
	 */
	public static ConfigurationMessage fromJson(String JasonString) throws Exception
	{
		ObjectMapper mapper = new ObjectMapper();
		ConfigurationMessage object = mapper.readValue(JasonString, ConfigurationMessage.class);
		return object;
	}

	/**
	 * Reformat the status string by adding severity.
	 *
	 * @return the new string. Null represents unknown issue number
	 */
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
		case ISSUE_MSG_IS_SYNC:
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

	/**
	 * Gets the issue string name.
	 *
	 * @param issue
	 *            as int with the issue number
	 * @return the string containing the issue name. Empty string represents unknown
	 *         issue number
	 */
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
		case ISSUE_MSG_IS_SYNC:
			return "IsSync";
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
