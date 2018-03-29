package lego;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;



public class ConfigurationMessage
{
	/*
	public final String ISSUE_MSG_ACTIVE_STRING 	= "Active";
	public final String ISSUE_MSG_START_STRING 		= "Start";
	public final String ISSUE_MSG_PAUSE_STRING 		= "Pause";
	public final String ISSUE_MSG_DONE_STRING 		= "Done";
	public final String ISSUE_MSG_RESTART_STRING 	= "Restart";
	public final String ISSUE_MSG_PING_STRING 		= "Ping";
	public final String ISSUE_MSG_UNPAUSE_STRING 	= "Unpause";
	public final String ISSUE_MSG_SYNC_STRING 		= "Sync";
	public final String ISSUE_MSG_LOST_SYNC_STRING 	= "LostSync";
	*/
	
	public	String 		path;
	public	String 		module;
	public	int 		issue;
	public  String 		issuestring;
	public  String		message;
	public	long		input;
	public	long 		output;
	public	int			percent;
	public	int			good;
	public	int			bad;
	public	int 		isinput;
	public	int			isoutput; 
	
	public ConfigurationMessage() {}	
	
	public ConfigurationMessage(ISSUE_TYPE t, String Path) 
	{
		if (Path == null)
			Path = "1";
		
		this.path 		= Path;
		this.module 	= "ProcessBlock" ;
		this.issue 		= t.value;
		this.issuestring = t.name;
		this.message 	= "";
		this.input  	= 0;
		this.output  	= 0;
		this.percent  	= 0;
		this.good  		= 0;
		this.bad  		= 0;
		this.isinput  	= 0;
		this.isoutput  	= 0; 
 	}
	
	@JsonIgnore
	public ISSUE_TYPE getIssueType()
	{
		for (ISSUE_TYPE t : ISSUE_TYPE.values())
		{
			if (issue == t.value)
				return t;
		}
		return null;
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
	
}
