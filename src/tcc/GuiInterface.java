package tcc;

import medcic_proto.MedCic.ENCAPSULATION;

public interface GuiInterface
{
	
	public enum Channel
	{
		INPUT1,
		INPUT2,
		OUTPUT1,
		OUTPUT2
	}

	void onConnectionChange(Boolean status);

	void UpdateStatus(final String status);

	void OperationCompleted();
	
	void OperationStarted();
	
	void OperationInSync(Channel ch);
	
	void OperationOutOfSync(Channel ch);
	
	void SetEncapsulation(ENCAPSULATION encap);
	
	void UpdateCounters(Statistics statistics);
	
}
