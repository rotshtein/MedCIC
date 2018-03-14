package tcc;

import java.io.File;
import java.util.AbstractMap;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;

import com.google.protobuf.InvalidProtocolBufferException;

import medcic_proto.MedCic.Header;
import medcic_proto.MedCic.OPCODE;
import medcic_proto.MedCic.STATUS;
import medcic_proto.MedCic.StartCommand;
import medcic_proto.MedCic.StatusMessage;
import medcic_proto.MedCic.StatusReplay;


public class ManagementParser extends Thread implements GuiInterface
{

	Logger														logger				= Logger
			.getLogger("ManagmentParser");
	ManagementServer											server				= null;
	Parameters													param				= null;
	ProcMon														procMon				= null;
	Boolean														connectionStatus	= false;
	BlockingQueue<AbstractMap.SimpleEntry<byte[], WebSocket>>	queue				= null;

	public ManagementParser(String ParametersFile, BlockingQueue<AbstractMap.SimpleEntry<byte[], WebSocket>> queue,
			ManagementServer server) throws Exception
	{
		param = new Parameters(ParametersFile);
		this.queue = queue;
		this.server = server;
	}

	@Override
	public void run()
	{

		while (true)
		{
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

	
		case START_CMD:
			StartCommand p = null;
			try
			{
				p = StartCommand.parseFrom(h.getMessageData());
			}
			catch (InvalidProtocolBufferException e)
			{
				logger.error("Failed to parse PlayCommand", e);
				SendNck(h, conn);
			}

			/*
			try
			{
				Kill();
				procMon = tx.Start(p.getFrequency(), p.getRate(), p.getGain(), p.getFilename(), p.getLoop());
				SendStatusMessage("Starting to transmirt " + p.getFilename(), conn);
				logger.info("Starting to record");
				SendAck(h, conn);
				OperationStarted();
			}
			catch (Exception e)
			{
				SendStatusMessage("Spectrum exec not found. Please fix the configuration file", conn);
				logger.error("Spectrum exec not found. Please fix the configuration file,e");
				SendNck(h, conn);
			}
*/
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
			break;

		case STATUS_REQUEST:
			StatusReplay sr = null;
			if (procMon != null)
			{
				sr = StatusReplay.newBuilder().setError(false).setErrorMMessage("").setWarning(false)
						.setWarningMessage("")
						.setStatus(procMon.isComplete() ? STATUS.STOP : STATUS.RUN).build();
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
		Header hh = Header.newBuilder().setSequence(h.getSequence()).setOpcode(OPCODE.ACK).build();
		conn.send(hh.toByteArray());

	}

	private void SendNck(Header h, WebSocket conn)
	{
		Header hh = Header.newBuilder().setSequence(h.getSequence()).setOpcode(OPCODE.NACK).build();
		conn.send(hh.toByteArray());

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
		if (conn != null)
		{
			StatusMessage s = StatusMessage.newBuilder().setMessage(message).build();
			Header h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.STATUS_MESSAGE)
					.setMessageData(s.toByteString()).build();

			conn.send(h.toByteArray());
		}
	}

	public Boolean isConnected()
	{
		return connectionStatus;
	}

	public Boolean isRunning()
	{
		if (procMon == null)
		{
			return false;
		}

		return !procMon.isComplete();
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
		StatusReplay s = StatusReplay.newBuilder().setStatus(STATUS.STOP).build();
		Header h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.STATUS_REPLAY).setMessageData(s.toByteString())
				.build();

		conn.send(h.toByteArray());
	}

	public void OperationStarted()
	{
		for (WebSocket conn : server.connections())
		{
			OperationStarted(conn);
		}
	}

	public void OperationStarted(WebSocket conn)
	{
		StatusReplay s = StatusReplay.newBuilder().setStatus(STATUS.RUN).build();
		Header h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.STATUS_REPLAY).setMessageData(s.toByteString())
				.build();

		conn.send(h.toByteArray());
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
