package tcc;

import java.io.File;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.concurrent.BlockingQueue;
import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import lego.MessageParser;
import lego.ProcMon;
import medcic_proto.MedCic.AutomaticStartCommand;
import medcic_proto.MedCic.CHANEL_STATUS;
import medcic_proto.MedCic.ENCAPSULATION;
import medcic_proto.MedCic.Header;
import medcic_proto.MedCic.OPCODE;
import medcic_proto.MedCic.STATUS;
import medcic_proto.MedCic.StartCommand;
import medcic_proto.MedCic.StatusMessage;
import medcic_proto.MedCic.StatusReplay;


public class ManagementParser extends Thread implements GuiInterface
{

	static final Logger											logger				= Logger
			.getLogger("ManagmentParser");
	ManagementServer											server				= null;
	ProcMon														procMon				= null;
	Boolean														connectionStatus	= false;
	BlockingQueue<AbstractMap.SimpleEntry<byte[], WebSocket>>	queue				= null;
	Thread														Cic1Thread			= null;
	Thread														Cic2Thread			= null;
	MessageParser												messageParser		= null;
	WebSocket													currentConn = null;
	public ManagementParser(BlockingQueue<AbstractMap.SimpleEntry<byte[], WebSocket>> queue, ManagementServer server)
			throws Exception
	{
		this.queue = queue;
		this.server = server;
		int ManagementPort = Integer.parseInt(Parameters.Get("ManagementPort", "11001"));
		try
		{
			if (messageParser != null)
			{
				Stop();
			}
 			messageParser = new MessageParser(this, ManagementPort);
			messageParser.start();
		}
		catch (Exception e1)
		{
			logger.error("Failed to run UDP server for messages from the modules", e1);
		}
	}

	public void Stop()
	{
		if (messageParser != null)
		{
			messageParser.Stop();
		}
		messageParser = null;
	}

	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				if (Thread.interrupted())
				{
					logger.debug("Management Parser thread interrupted");
					break;
				}

				AbstractMap.SimpleEntry<byte[], WebSocket> request = null;
				try
				{
					request = queue.take();
				}
				catch (InterruptedException e)
				{
					logger.error("Error getting request from queue", e);
					continue;
				}
				Parse(request.getKey(), request.getValue());
			}
			catch (Exception e)
			{
				logger.error("Thread exception", e);
			}
		}
		logger.debug("Management Parser thread exit");
	}

	public void Parse(byte[] buffer, WebSocket conn)
	{
		currentConn = conn;
		Header h = getHeader(buffer);
		if (h == null)
		{
			SendNck(h, conn);
			return;
		}
		try
		{
			switch (h.getOpcode())
			{
			case HEADER:
				SendNck(h, conn);
				break;
	
			case AUTO_START_CMD:
				AutomaticStartCommand AuroRun = null;
				try
				{
					AuroRun = AutomaticStartCommand.parseFrom(h.getMessageData());
				}
				catch (InvalidProtocolBufferException e)
				{
					logger.error("Failed to parse Automatic Start Command", e);
					SendNck(h, conn);
				}
	
				/*try
				{
					
					AutomaticStart as = AutomaticStart(this, 
							AuroRun.getInput1Url(), 
							AuroRun.getOutput1Url(), 
							AuroRun.getInput2Url(),
							AuroRun.getOutput2Url(), 
							"gg",
							Paths.get(ScriptPath, "cicScript.lego").toString(),
							"127.0.0.1", 11001,
							conn);
					
					ENCAPSULATION e = as.GetEncapsulation();
					
					// Get samples for ID process
					 * 
					 */
				/*
					try
					{
						String IdentificationExe = Parameters.Get("IdentificationExe", "C:\\bin\\lego\\bin\\IdBlock.exe");
						String ScriptPath = Parameters.Get("ScriptPath", "C:\\bin\\lego\\legoFiles");
						
						
						// Get sample file
						GetSamples gs = new GetSamples(this, "Getting Samples");
						procMon = gs.Start(AuroRun.getInput1Url(), IdentificationExe, ScriptPath, managementServer, managementPort);
						while (!procMon.isComplete())
						{
							if  (new File(idFile).length() > SAMPLE_FILE_SIZE) 	
							{
								procMon.Kill();
								break;
							}
							
							Thread.sleep(50);
						}
					}
					catch (Exception e1)
					{
						UpdateStatus("Failed to get samples for identification - Aborting");
						logger.error("Failed to get samples for identification", e1);
						Kill();
					}
					
					/*
					if (e != null & e != ENCAPSULATION.UNRECOGNIZED)
					{
						StartProduction(ENCAPSULATION.ESC_551, 
									AuroRun.getInput1Url(), 
									AuroRun.getOutput1Url(), 
									AuroRun.getInput2Url(),
									AuroRun.getOutput2Url(), 
									Paths.get(ScriptPath, "cicScript.lego").toString(),
									h,conn);
					}
				
				
					SendStatusMessage("Starting ...", conn);
					logger.info("Starting...");
		
				
					SendAck(h, conn);
					SendProcessStartMessage();
				}
				catch (Exception e)
				{
					SendStatusMessage("Executable not found. Please fix the configuration file", conn);
					logger.error("Executable not found. Please fix the configuration file", e);
					if (h != null & conn != null)
					{
						SendNck(h, conn);
					}
				}
				/*
				 * Start thread to: 1. Get sample file 2. Identify 3. Run production
				 * 
				 * Terminate the thread on processes on STOP if running run thread per CIC => 2
				 * threads
				 */

				break;
	
			case START_CMD:
				StartCommand p = null;
				try
				{
					p = StartCommand.parseFrom(h.getMessageData());
				}
				catch (InvalidProtocolBufferException e)
				{
					logger.error("Failed to parse Start Command", e);
					SendNck(h, conn);
				}
	
				String ScriptPath = Parameters.Get("ScriptPath", "C:\\bin\\lego\\legoFiles");
				StartProduction(p.getEncapsulation(), p.getInput1Url(), p.getOutput1Url(), p.getInput2Url(),
						p.getOutput2Url(), Paths.get(ScriptPath, "cicScript.lego").toString(),h,conn);
				break;
	
			case STOP_CMD:
				SendAck(h, conn);
				ProcMon.SendStop();
				if (procMon == null)
				{
					SendStatusMessage("Process not running", conn);
					return;
				}
	
				if (!procMon.isComplete())
				{
					logger.warn("Killing process. [ " + procMon.description + " ]");
					SendStatusMessage("Killing process. [ " + procMon.description + " ]", conn);
					procMon.Kill();
					OperationCompleted();
				}
				else
				{
					
					SendStatusMessage("Process not running", conn);
				}
				SendProcessStopMessage();
				procMon = null;
				break;
	
			case STATUS_REQUEST:
				StatusReplay sr = null;
				if (procMon != null)
				{
					sr = StatusReplay.newBuilder().setError(false).setErrorMMessage("").setWarning(false)
							.setWarningMessage("").setStatus(procMon.isComplete() ? STATUS.STOP : STATUS.RUN).build();
				}
				else
				{
					sr = StatusReplay.newBuilder().setStatus(STATUS.STOP).build();
				}
				Header hh = Header.newBuilder().setSequence(h.getSequence()).setMessageData(sr.getErrorMMessageBytes())
						.build();
				
				SendMessage(hh.toByteString(), conn);
				break;
	
			default:
				SendNck(h, conn);
				break;
			}
		}
		catch (Exception e)
		{
			logger.error("Something want wrong in the parser" , e);
		}
		currentConn = null;
	}

	public Boolean StartProduction (ENCAPSULATION encap, 
			String InputUrl1, String OutputUrl1,
			String InputUrl2, String OutputUrl2, 
			String ConfigFilename,
			Header h, 
			WebSocket conn)
	{
		try
		{
			String MediationExe = Parameters.Get("MediationExe", "c:\\bin\\lego\\bin\\ProcessBlock.exe");
			Mediate med = new Mediate(MediationExe, this, "CIC Production");

			String ScriptPath = Parameters.Get("ScriptPath", "C:\\bin\\lego\\legoFiles");
			Kill();
			procMon = med.Start(encap, 	InputUrl1, OutputUrl1, 
										InputUrl2, OutputUrl2, 
										Paths.get(ScriptPath, "cicScript.lego").toString());
			SendStatusMessage("Starting ...", conn);
			logger.info("Starting...");
			SendAck(h, conn);
			SendProcessStartMessage();
		}
		catch (Exception e)
		{
			SendStatusMessage("Executable not found. Please fix the configuration file", conn);
			logger.error("Executable not found. Please fix the configuration file", e);
			if (h != null & conn != null)
			{
				SendNck(h, conn);
			}
			return false;
		}
		return true;
	}
	
	private void SendAck(Header h, WebSocket conn)
	{
		try
		{
			Header hh = Header.newBuilder().setSequence(h.getSequence()).setOpcode(OPCODE.ACK).build();
			SendMessage(hh.toByteString(), conn);
		}
		catch (Exception e)
		{
			logger.error("Failed to send Ack to a connection", e);
		}

	}

	private void SendNck(Header h, WebSocket conn)
	{
		try
		{
			Header hh = Header.newBuilder().setSequence(h.getSequence()).setOpcode(OPCODE.NACK).build();
			SendMessage(hh.toByteString(), conn);
		}
		catch (Exception e)
		{
			logger.error("Failed to send Nack to a connection", e);
		}

	}

	private Header getHeader(byte[] buffer)
	{
		Header h = null;
		try
		{
			h = Header.parseFrom(buffer);
		}
		catch (InvalidProtocolBufferException e)
		{
			logger.error("Failed to parse message header", e);
		}

		return h;
	}

	private void SendProcessStopMessage()
	{
		StatusReplay r = StatusReplay.newBuilder().setStatus(STATUS.STOP).build();
		Header h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.STATUS_MESSAGE)
				.setMessageData(r.toByteString()).build();

		BroadcastMessage(h.toByteString());
	}
	
	private void SendProcessStartMessage()
	{
		try
		{
			StatusReplay r = StatusReplay.newBuilder().setStatus(STATUS.RUN).build();
			Header h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.STATUS_MESSAGE)
					.setMessageData(r.toByteString()).build();
	
			BroadcastMessage(h.toByteString());
		}
		catch (Exception e)
		{
			logger.error("Failed to send StatusMessage when process starts", e);
		}
	}
	
	private void SendStatusMessage(String message, WebSocket conn)
	{
		try
		{
			StatusMessage s = StatusMessage.newBuilder().setMessage(message).build();
			Header h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.STATUS_MESSAGE)
					.setMessageData(s.toByteString()).build();

			SendMessage(h.toByteString(), conn);
		}
		catch (Exception e)
		{
			logger.error("Failed to send StatusMessage to a connection", e);
		}
	}

/*
	private void SendMessage(ByteString buffer)
	{
		if (currentConn != null)
		{
			SendMessage(buffer, currentConn );
		}
	}
*/
	private void SendMessage(ByteString buffer, WebSocket conn )
	{
		try
		{
			if (conn.isOpen())
			{
				conn.send(buffer.toByteArray());
			}
		}
		catch (Exception e)
		{
			logger.error("Failed to send Message to a connection", e);
		}
	}
	
	private void BroadcastMessage(ByteString buffer)
	{
		//SendMessage(buffer);
		
		try
		{
			for (WebSocket conn : server.connections() )
			{
				if (conn.isOpen())
				{
					conn.send(buffer.toByteArray());
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Failed to Broadcast Message to a connection", e);
		}
	}

	public Boolean isConnected()
	{
		return connectionStatus;
	}

	public Boolean isRunning()
	{
		if (procMon != null)
		{
			if (!procMon.isComplete())
			{
				return !procMon.isComplete();
			}
		}

		return false;
	}

	public void Kill()
	{
		if (procMon == null)
		{
			return;
		}

		if (!procMon.isComplete())
		{
			procMon.Kill();
		}
	}

	@Override
	public void OperationCompleted()
	{
		try
		{
			StatusReplay s = StatusReplay.newBuilder().setStatus(STATUS.STOP).build();
			Header h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.STATUS_REPLAY)
					.setMessageData(s.toByteString()).build();

			BroadcastMessage(h.toByteString());
		}
		catch (Exception e)
		{
			logger.error("Failed to send StatusReplay on complition to a connection", e);
		}
	}

	public void OperationStarted()
	{
		StatusReplay s = StatusReplay.newBuilder().setStatus(STATUS.RUN).build();
		Header h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.STATUS_REPLAY).setMessageData(s.toByteString())
				.build();

		BroadcastMessage(h.toByteString());
	}


	@Override
	public void UpdateStatus(String status)
	{
		try
		{
			StatusMessage s = StatusMessage.newBuilder().setMessage(status).build();
			Header h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.STATUS_MESSAGE)
					.setMessageData(s.toByteString()).build();

			BroadcastMessage(h.toByteString());
		}
		catch (Exception e)
		{
			logger.error("Failed to send StatusMessage to a connection", e);
		}
	}

	@Override
	public void onConnectionChange(Boolean status)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void OperationInSync(Channel ch)
	{
		StatusReplay s = null;
		Header h = null;
		try
		{
			switch (ch)
			{
			case  INPUT1:
				s = StatusReplay.newBuilder()
					.setCic1Input(CHANEL_STATUS.SYNC)
					.setCic1Output(CHANEL_STATUS.UNKNOWN)
					.setCic2Input(CHANEL_STATUS.UNKNOWN)
					.setCic2Output(CHANEL_STATUS.UNKNOWN)
					.setStatusDescription("CIC 1 input synchronized").build();
				
				break;
			case  INPUT2:
				s = StatusReplay.newBuilder()
						.setCic1Input(CHANEL_STATUS.UNKNOWN)
						.setCic1Output(CHANEL_STATUS.UNKNOWN)
						.setCic2Input(CHANEL_STATUS.SYNC)
						.setCic2Output(CHANEL_STATUS.UNKNOWN)
						.setStatusDescription("CIC 2 input synchronized").build();
				break;
			case  OUTPUT1:
				s = StatusReplay.newBuilder()
						.setCic1Input(CHANEL_STATUS.UNKNOWN)
						.setCic1Output(CHANEL_STATUS.SYNC)
						.setCic2Input(CHANEL_STATUS.UNKNOWN)
						.setCic2Output(CHANEL_STATUS.UNKNOWN)
				.setStatusDescription("CIC 1 output synchronized").build();
				break;
			case  OUTPUT2:
				s = StatusReplay.newBuilder()
						.setCic1Input(CHANEL_STATUS.UNKNOWN)
						.setCic1Output(CHANEL_STATUS.UNKNOWN)
						.setCic2Input(CHANEL_STATUS.UNKNOWN)
						.setCic2Output(CHANEL_STATUS.SYNC)
						.setStatusDescription("CIC 2 output synchronized").build();
				break;
			default:
				return;
			}
			
			
			h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.STATUS_REPLAY)
					.setMessageData(s.toByteString()).build();
			BroadcastMessage(h.toByteString() );
		}	
		catch (Exception e)
		{
			logger.error("Failed to send StatusReplay in sync",e);
		}
	}

	@Override
	public void OperationOutOfSync(Channel ch)
	{
		StatusReplay s = null;
		Header h = null;
		try
		{
			switch (ch)
			{
			case  INPUT1:
				s = StatusReplay.newBuilder()
					.setCic1Input(CHANEL_STATUS.OUT_OF_SYNC)
					.setCic1Output(CHANEL_STATUS.UNKNOWN)
					.setCic2Input(CHANEL_STATUS.UNKNOWN)
					.setCic2Output(CHANEL_STATUS.UNKNOWN)
					.setStatusDescription("CIC 1 input NOT synchronized").build();
				
				break;
			case  INPUT2:
				s = StatusReplay.newBuilder()
						.setCic1Input(CHANEL_STATUS.UNKNOWN)
						.setCic1Output(CHANEL_STATUS.UNKNOWN)
						.setCic2Input(CHANEL_STATUS.OUT_OF_SYNC)
						.setCic2Output(CHANEL_STATUS.UNKNOWN)
						.setStatusDescription("CIC 2 input NOT synchronized").build();
				break;
			case  OUTPUT1:
				s = StatusReplay.newBuilder()
						.setCic1Input(CHANEL_STATUS.UNKNOWN)
						.setCic1Output(CHANEL_STATUS.OUT_OF_SYNC)
						.setCic2Input(CHANEL_STATUS.UNKNOWN)
						.setCic2Output(CHANEL_STATUS.UNKNOWN)
				.setStatusDescription("CIC 1 output NOT synchronized").build();
				break;
			case  OUTPUT2:
				s = StatusReplay.newBuilder()
						.setCic1Input(CHANEL_STATUS.UNKNOWN)
						.setCic1Output(CHANEL_STATUS.UNKNOWN)
						.setCic2Input(CHANEL_STATUS.UNKNOWN)
						.setCic2Output(CHANEL_STATUS.OUT_OF_SYNC)
						.setStatusDescription("CIC 2 output NOT synchronized").build();
			default:
				return;
			}
			
			
			h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.STATUS_REPLAY)
					.setMessageData(s.toByteString()).build();
			BroadcastMessage(h.toByteString() );
			
			
		}
		catch (Exception e)
		{
			logger.error("Failed to send StatusMessage to a connection", e);
		}
	}
}
