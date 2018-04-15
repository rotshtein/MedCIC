package lego;

import org.apache.log4j.Logger;

public class ProcMon implements Runnable
{

	final static Logger	logger	= Logger.getLogger("ProcMon");
	private Process		_proc;

	private volatile boolean	_complete	= false;
	String						description	= "";

	public ProcMon(String[] vars, String description) throws Exception
	{
		this(Runtime.getRuntime().exec(vars, null), description);
	}

	public ProcMon(Process proc)
	{
		this(proc, "No description");
	}

	public ProcMon(Process proc, String description)
	{
		_proc = proc;
		this.description = description;
		Thread t = new Thread(this);
		t.start();
	}

	public boolean isComplete()
	{
		return _complete;
	}

	public Boolean kill()
	{
		if (!_complete)
		{
			_proc.destroy();
			return true;
		}
		return false;
	}

	public void run()
	{
		_complete = false;
		try
		{
			_proc.waitFor();
		}
		catch (InterruptedException e)
		{
			logger.error("Failed to monitor process", e);

		}
		_complete = true;
		logger.info("Exiting procMon thread");
	}
}
