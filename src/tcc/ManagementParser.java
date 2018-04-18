package tcc;

import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.concurrent.BlockingQueue;
import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import com.google.protobuf.InvalidProtocolBufferException;
import medcic_proto.MedCic.AutomaticStartCommand;
import medcic_proto.MedCic.Header;
import medcic_proto.MedCic.OPCODE;
import medcic_proto.MedCic.STATUS;
import medcic_proto.MedCic.StartCommand;
import medcic_proto.MedCic.StatusMessage;
import medcic_proto.MedCic.StatusReplay;

public class ManagementParser extends Thread implements GuiInterface
{

	Logger				logger	= Logger.getLogger("ManagmentParser");
	ManagementServer	server	= null;
	// Parameters param = null;
	ProcMon														procMon			= null;
	Boolean														connectionStatus	= false;
	BlockingQueue<AbstractMap.SimpleEntry<byte[], WebSocket>>	queue				= null;
	Thread														Cic1Thread			= null;
	Thread														Cic2Thread			= null;

	public ManagementParser(BlockingQueue<AbstractMap.SimpleEntry<byte[], WebSocket>> queue, ManagementServer server)
			throws Exception
	{
		this.queue = queue;
		this.server = server;
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
		Header h = getHeader(buffer);
		if (h == null)
		{
			SendNck(h, conn);
			return;
		}

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

			if (AuroRun.getInput1Url() != null & AuroRun.getInput1Url() != "")
			{

			}

			if (AuroRun.getInput2Url() != null & AuroRun.getInput2Url() != "")
			{

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

			try
			{
				String MediationExe = Parameters.Get("MediationExe", "notepad.exe");
				Mediate med = new Mediate(MediationExe, this,"CIC Production");
				
				String ScriptPath = Parameters.Get("ScriptPath", "C:\\programs\\lego\\config");
				Kill();
				procMon = med.Start(p.getEncapsulation(), p.getInput1Url(), p.getOutput1Url(),p.getInput2Url(), p.getOutput2Url(),
						Paths.get(ScriptPath, "cicScript.lego").toString());
				SendStatusMessage("Starting ...", conn);
				logger.info("Starting...");
				SendAck(h, conn);
			}
			catch (Exception e)
			{
				SendStatusMessage("Executable not found. Please fix the configuration file", conn);
				logger.error("Executable not found. Please fix the configuration file", e);
				SendNck(h, conn);
			}
			break;

		case STOP_CMD:
			SendAck(h, conn);
			if (procMon == null)
			{
				SendStatusMessage("Process not running", conn);
				return;
			}

			if (!procMon.isComplete())
			{
				logger.warn("Killing process. [ " + procMon.description + " ]");
				SendStatusMessage("Killing process. [ " + procMon.description + " ]", conn);
				procMon.kill();
				OperationCompleted();
			}
			else
			{
				SendStatusMessage("Process not running", conn);
			}
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
			conn.send(hh.toByteArray());
			break;

		default:
			SendNck(h, conn);
			break;
		}
	}

	private void SendAck(Header h, WebSocket conn)
	{
		try
		{
			Header hh = Header.newBuilder().setSequence(h.getSequence()).setOpcode(OPCODE.ACK).build();
			conn.send(hh.toByteArray());
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
			conn.send(hh.toByteArray());
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

	private void SendStatusMessage(String message, WebSocket conn)
	{
		try
		{
			StatusMessage s = StatusMessage.newBuilder().setMessage(message).build();
			Header h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.STATUS_MESSAGE)
					.setMessageData(s.toByteString()).build();

			conn.send(h.toByteArray());
		}
		catch (Exception e)
		{
			logger.error("Failed to send StatusMessage to a connection", e);
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
			procMon.kill();
		}
	}

	@Override
	public void OperationCompleted()
	{
		for (WebSocket conn : server.connections())
		{
			OperationCompleted(conn);
		}
	}

	public void OperationCompleted(WebSocket conn)
	{
		try
		{
			StatusReplay s = StatusReplay.newBuilder().setStatus(STATUS.STOP).build();
			Header h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.STATUS_REPLAY)
					.setMessageData(s.toByteString()).build();

			conn.send(h.toByteArray());
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

		for (WebSocket conn : server.connections())
		{
			OperationStarted(conn, h);
		}
	}

	public void OperationStarted(WebSocket conn)
	{
		OperationStarted(conn, null);
	}

	public void OperationStarted(WebSocket conn, Header h)
	{
		try
		{
			if (h == null)
			{
				StatusReplay s = StatusReplay.newBuilder().setStatus(STATUS.RUN).build();
				h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.STATUS_REPLAY).setMessageData(s.toByteString())
						.build();
			}

			conn.send(h.toByteArray());
		}
		catch (Exception e)
		{
			logger.error("Failed to send StatusReplay on starting operation to a connection", e);
		}
	}

	@Override
	public void UpdateStatus(String status)
	{
		for (WebSocket conn : server.connections())
		{
			SendStatusMessage(status, conn);
		}
	}

	@Override
	public void onConnectionChange(Boolean status)
	{
		// TODO Auto-generated method stub

	}
}
