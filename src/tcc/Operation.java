package tcc;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;

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
	Boolean 		stopThread	= false;

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
				builder.redirectInput(Redirect.INHERIT)
				   .redirectOutput(Redirect.INHERIT)
				   .redirectError(Redirect.INHERIT);
				
				builder.redirectOutput(new File("out-"+ Thread.currentThread().getId()+".txt"));
				builder.redirectError(new File("out-"+ Thread.currentThread().getId()+".txt"));
				p = builder.start(); // may throw IOException

				procMon = new ProcMon(p, operation);
				procMonThread = new Thread(procMon, operation + " procMon");
				procMonThread.start();

				//feedbackFileThread = new Thread(this, operation + " feedback reader");
				//feedbackFileThread.start();
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
		stopThread = false;
		
		while (!p.isAlive() & !stopThread);
		
		while (p.isAlive() & !stopThread)
		{
			// call complete and exit when ended
			if (isComplete())
			{
				gui.UpdateStatus(operation + "ended");
				logger.info(operation + "ended");
				break;
			}

			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				logger.error("InterruptedException received", e);
			}
		}
		logger.info("Exiting feednbak file thread");
	}
}
