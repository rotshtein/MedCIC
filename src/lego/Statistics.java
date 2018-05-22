package lego;


public class Statistics
{
	
	private long cic1In = 0;
	private long cic2In = 0;
	private long cic1Out = 0;
	private long cic2Out = 0;
	
	public Statistics() 
	{
		this(0,0,0,0);
	}

	public Statistics(long in1, long in2, long out1, long out2) 
	{
		cic1In = in1;
		cic2In = in2;
		cic1Out = out1;
		cic2Out = out2;
	}
	
	public void Clear()
	{
		cic1In = 0;
		cic2In = 0;
		cic1Out = 0;
		cic2Out = 0;
	}
	
	public long getCic1In()
	{
		return cic1In;
	}

	public void setCic1In(long cic1In)
	{
		this.cic1In = cic1In;
	}

	public long getCic2In()
	{
		return cic2In;
	}

	public void setCic2In(long cic2In)
	{
		this.cic2In = cic2In;
	}

	public long getCic1Out()
	{
		return cic1Out;
	}

	public void setCic1Out(long cic1Out)
	{
		this.cic1Out = cic1Out;
	}

	public long getCic2Out()
	{
		return cic2Out;
	}

	public void setCic2Out(long cic2Out)
	{
		this.cic2Out = cic2Out;
	}
}
