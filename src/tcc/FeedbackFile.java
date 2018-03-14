package tcc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.apache.log4j.Logger;

public class FeedbackFile
{

	final static Logger	logger	= Logger.getLogger("FeedbackFile");
	BufferedReader		reader;

	public FeedbackFile(String Filename) throws Exception
	{
		try
		{
			reader = new BufferedReader(new FileReader(Filename));
		}
		catch (IOException e)
		{
			logger.error("Can't open feedback file", e);
			throw new Exception("Can't open feedback file");
		}
	}

	public String GetNext()
	{
		try
		{
			return reader.readLine();
		}
		catch (IOException e)
		{
			return null;
		}
	}

	public void Close()
	{
		try
		{
			reader.close();
		}
		catch (IOException e)
		{
			logger.error("Failed to close the reader", e);
		}
	}
}
