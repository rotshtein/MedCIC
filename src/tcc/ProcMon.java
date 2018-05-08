package tcc;

import org.apache.log4j.Logger;

public class ProcMon implements Runnable
{

	final static Logger	logger	= Logger.getLogger("ProcMon");
	private Process		_proc;

	private volatile boolean	_complete	= false;
	String						description	= "";

	public ProcMon(Process proc, String description)
	{
		this(proc);
		this.description = description;
	}

	public ProcMon(Process proc)
	{
		_proc = proc;
		
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
			if (_proc != null)
			{
				_proc.destroy();
			}
			return true;
		}
		return false;
	}

	public void run()
	{
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
