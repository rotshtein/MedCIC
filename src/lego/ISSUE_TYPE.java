package lego;

public enum ISSUE_TYPE
{
	ISSUE_MSG_ACTIVE(1, "Active"), ISSUE_MSG_START(2, "Start"), ISSUE_MSG_PAUSE(3, "Pause"), ISSUE_MSG_DONE(4,
			"Done"), ISSUE_MSG_RESTART(5, "Restart"), ISSUE_MSG_PING(6, "Ping"), ISSUE_MSG_UNPAUSE(7,
					"Unpause"), ISSUE_MSG_SYNC(8, "Sync"), ISSUE_MSG_LOST_SYNC(9, "Start lost"), ISSUE_MSG_FATAL(100,
							"Fatal"), ISSUE_MSG_ERROR(110, "Error"), ISSUE_MSG_WARNING(120,
									"Warmning"), ISSUE_MSG_NOTICE(130, "Notice"), ISSUE_MSG_INFO(140,
											"Info"), ISSUE_MSG_DEBUG(150, "Debug"), ISSUE_MSG_TRACE(160, "Trace");

	int		value	= -1;
	String	name	= null;

	ISSUE_TYPE(int v, String Name)
	{
		this.value = v;
		this.name = Name;
	}

	int Val()
	{
		return this.value;
	}

	String Name()
	{
		return this.name;
	}

};