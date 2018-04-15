package tcc;

import java.io.File;

import org.apache.log4j.Logger;

public abstract class Operation implements Runnable
{

	final static Logger logger = Logger.getLogger("Operation");

	String			exe_file	= "";
	Process			p			= null;
	ProcMon			procMon;
	GuiInterface	gui;
	String			operation;
	Thread			procMonThread;
	Thread			feedbackFileThread;

	public Operation(String Exe, GuiInterface gui, String Operation)
	{
		exe_file = Exe;
		this.gui = gui;
		operation = Operation;
	}

	public ProcMon StartAction(String[] vars) throws Exception
	{
		if (new File(exe_file).exists())
		{
			try
			{

				ProcessBuilder builder = new ProcessBuilder(vars);
				// builder.redirectOutput(new File("out.txt"));
				// builder.redirectError(new File("out.txt"));
				p = builder.start(); // may throw IOException

				// while (!p.isAlive());

				procMon = new ProcMon(p, operation);
				procMonThread = new Thread(procMon, operation + "procMon");
				procMonThread.start();

				feedbackFileThread = new Thread(this, operation + "feedback reader");
				feedbackFileThread.start();
			}
			catch (Exception ex)
			{
				logger.error("Failed to start " + operation + " process", ex);
				return null;
			}
			return procMon;
		}
		throw new Exception(exe_file + "Not fount. Please check the configuration file");
	}

	public boolean isComplete()
	{
		if (procMon != null)
		{
			return procMon.isComplete();
		}
		return false;
	}

	@Override
	public void run()
	{
		while (p.isAlive())
		{
			// call complete and exit when ended
			if (isComplete())
			{
				gui.UpdateStatus("End transmitting file");
				logger.info("End transmitting file");
				break;
			}

			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		logger.info("Exiting feednbak file thread");

	}
}
