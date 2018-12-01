package tcc;

import java.net.URI;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import lego.Statistics;
import medcic_proto.MedCic.AutomaticStartCommand;
import medcic_proto.MedCic.CHANEL_STATUS;
import medcic_proto.MedCic.ENCAPSULATION;
import medcic_proto.MedCic.Header;
import medcic_proto.MedCic.IdentifiedEncapsulation;
import medcic_proto.MedCic.OPCODE;
import medcic_proto.MedCic.STATUS;
import medcic_proto.MedCic.StartCommand;
import medcic_proto.MedCic.StatisticsReplay;
import medcic_proto.MedCic.StatusMessage;
import medcic_proto.MedCic.StatusReplay;
import tcc.GuiInterface.Channel;

public class ManagementClient extends WebSocketClient
{

	static final Logger	logger				= Logger.getLogger("ManagementClient");
	GuiInterface		gui					= null;
	Boolean				connectionStatus	= false;
	Boolean				gotAck				= false;
	Boolean				gotNck				= false;
	Boolean				operationStarted	= false;
	Boolean				syncTimeout			= false;
	long				lastSyncTime		= 0;
	Thread				syncTimeoutThread	= null;
	Boolean				runThread			= true;
	long				cic1LastLostSync	= 0;
	long				cic2LastLostSync	= 0;

	public ManagementClient(URI serverUri, GuiInterface gui)
	{
		super(serverUri);
		this.gui = gui;
		this.connect();
		syncTimeoutThread = new Thread(() -> SyncTimeoutThread());
		syncTimeoutThread.start();
		logger.debug("ManagementClient and the SyncTimeoutThread started");
	}

	public void Stop()
	{
		// syncTimeoutThread.stop();
		runThread = false;
		logger.debug("Stop ManagementClient and the SyncTimeoutThread");
		this.close();
	}

	void SyncTimeoutThread()
	{
		runThread = true;
		while (runThread)
		{
			try
			{
				Thread.sleep(1000);
				if (System.currentTimeMillis() - lastSyncTime > 5000)
				{
					if (syncTimeout == false)
					{
						syncTimeout = true;
						gui.OperationOutOfSync(Channel.INPUT1);
						gui.OperationOutOfSync(Channel.INPUT2);
						gui.OperationOutOfSync(Channel.OUTPUT1);
						gui.OperationOutOfSync(Channel.OUTPUT2);
					}
				}
				else
				{
					syncTimeout = false;
				}
			}
			catch (InterruptedException e)
			{
				logger.error("sleep fialed", e);
			}
		}
	}

	@Override
	public void onOpen(ServerHandshake handshakedata)
	{
		gui.UpdateStatus("Connected to the server");
		logger.info("Connected");
	}

	@Override
	public void onMessage(String message)
	{
		logger.info("got message: " + message);
	}

	@Override
	public void onMessage(ByteBuffer buffer)
	{
		Header h = null;
		try
		{
			h = Header.parseFrom(buffer.array());

			if (h != null)
			{
				// logger.debug("Got header. Command = " + h.getOpcode());
			}
			// int i = h.getOpcodeValue();
			switch (h.getOpcode())
			{
			case HEADER:
				logger.error("Got header only");
				break;

			case STOP_CMD:
				logger.error("Client got Stop command");
				break;

			case STATUS_REQUEST:
				logger.error("Client got Status request");
				break;

			case ACK:
				gotAck = true;
				break;

			case NACK:
				gotNck = true;
				break;

			case STATUS_REPLAY:
				lastSyncTime = System.currentTimeMillis();
				StatusReplay sr = StatusReplay.parseFrom(h.getMessageData());

				// gui.UpdateStatus(sr.getStatusDescription());

				if (sr.getError())
				{
					gui.UpdateStatus(sr.getErrorMMessage());
				}
				else if (sr.getWarning())
				{
					gui.UpdateStatus(sr.getWarningMessage());
				}

				if (sr.getStatus() == STATUS.STOP)
				{
					// gui.UpdateStatus(sr.getStatusDescription());
					gui.OperationCompleted();
					operationStarted = false;

				}
				else if (sr.getStatus() == STATUS.RUN)
				{
					// gui.UpdateStatus(sr.getStatusDescription());
					if (operationStarted == false)
					{
						gui.OperationStarted();
						operationStarted = true;
					}
				}

				if (sr.getCic1Input() == CHANEL_STATUS.SYNC)
				{
					gui.OperationInSync(Channel.INPUT1);
				}
				else if (sr.getCic1Input() == CHANEL_STATUS.OUT_OF_SYNC)
				{
					gui.OperationOutOfSync(Channel.INPUT1);
				}

				if (sr.getCic2Input() == CHANEL_STATUS.SYNC)
				{
					gui.OperationInSync(Channel.INPUT2);
				}
				else if (sr.getCic2Input() == CHANEL_STATUS.OUT_OF_SYNC)
				{
					gui.OperationOutOfSync(Channel.INPUT2);
				}

				if (sr.getCic1Output() == CHANEL_STATUS.SYNC)
				{
					if (System.currentTimeMillis() - cic1LastLostSync > 500)
					{
						gui.OperationInSync(Channel.OUTPUT1);
					}
				}
				else if (sr.getCic1Output() == CHANEL_STATUS.OUT_OF_SYNC)
				{
					gui.OperationOutOfSync(Channel.OUTPUT1);
					cic1LastLostSync = System.currentTimeMillis();
				}

				if (sr.getCic2Output() == CHANEL_STATUS.SYNC)
				{
					if (System.currentTimeMillis() - cic2LastLostSync > 500)
					{
						gui.OperationInSync(Channel.OUTPUT2);
					}
				}
				else if (sr.getCic2Output() == CHANEL_STATUS.OUT_OF_SYNC)
				{
					gui.OperationOutOfSync(Channel.OUTPUT2);
					cic2LastLostSync = System.currentTimeMillis();
				}
				break;

			case STATUS_MESSAGE:
				StatusMessage sm = StatusMessage.parseFrom(h.getMessageData());
				gui.UpdateStatus(sm.getMessage());
				break;

			case IDENTYPIED_ENCAPSULATION:
				IdentifiedEncapsulation ie = IdentifiedEncapsulation.parseFrom(h.getMessageData());
				gui.SetEncapsulation(ie.getEncapsulation());
				break;

			case STATISTICS_REPLAY:
				StatisticsReplay stat = StatisticsReplay.parseFrom(h.getMessageData());
				gui.UpdateCounters(new Statistics(stat.getCic1InputByteCounter(), stat.getCic2InputByteCounter(),
						stat.getCic1OutputByteCounter(), stat.getCic2OutputByteCounter()));
				break;

			default:
				logger.error("Unknown command.");
				break;
			}

		}
		catch (InvalidProtocolBufferException e)
		{
			logger.error("Protocol buffer Header parsing error", e);
		}

	}

	@Override
	public void onClose(int code, String reason, boolean remote)
	{
		logger.info("Disconnected");
		gui.UpdateStatus("Connection to the server closed!");
	}

	@Override
	public void onError(Exception ex)
	{
		gui.UpdateStatus("Websocket error received from the server");
		logger.error("Wensocket error", ex);
	}

	public Boolean SendAutomaticStartCommand(String input1_url, String input2_url, String output1_url,
			String output2_url)
	{
		try
		{
			AutomaticStartCommand sc = AutomaticStartCommand.newBuilder().setInput1Url(input1_url)
					.setInput2Url(input2_url).setOutput1Url(output1_url).setOutput2Url(output2_url).build();

			send(0, OPCODE.AUTO_START_CMD, sc.toByteString());
			return true;
		}
		catch (Exception e)
		{
			logger.error("Failed to send Automatic start command", e);
		}
		return false;
	}

	public Boolean SendStartCommand(ENCAPSULATION encap, String input1_url, String input2_url, String output1_url,
			String output2_url)
	{
		try
		{
			StartCommand sc = StartCommand.newBuilder().setEncapsulation(encap).setInput1Url(input1_url)
					.setInput2Url(input2_url).setOutput1Url(output1_url).setOutput2Url(output2_url).build();

			send(0, OPCODE.START_CMD, sc.toByteString());
			return true;
		}
		catch (Exception e)
		{
			logger.error("Failed to send Start command", e);
		}
		return false;
	}

	public Boolean SendStopCommand()
	{
		try
		{
			Header h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.STOP_CMD).build();
			if (this.isOpen())
			{
				this.send(h.toByteArray());
			}
			send(0, OPCODE.STOP_CMD, null);
		}
		catch (Exception e)
		{
			logger.error("Send SendStopCommand error", e);
			return false;
		}
		return true;
	}

	public Boolean SendStatus(String status)
	{
		try
		{
			StatusMessage sm = StatusMessage.newBuilder().setMessage(status).build();
			Header h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.STATUS_MESSAGE)
					.setMessageData(sm.toByteString()).build();

			if (this.isOpen())
			{
				this.send(h.toByteArray());
			}
		}
		catch (Exception e)
		{
			logger.error("Send SendStatus error", e);
			return false;
		}
		return true;
	}

	public void send(int Sequence, OPCODE opcode, ByteString data)
	{
		gotAck = false;
		gotNck = false;

		Header h = null;
		try
		{
			if (data != null)
			{
				h = Header.newBuilder().setSequence(Sequence).setOpcode(opcode).setMessageData(data).build();
			}
			else
			{
				h = Header.newBuilder().setSequence(Sequence).setOpcode(opcode).build();
			}
			if (this.isOpen())
			{
				this.send(h.toByteArray());
			}
		}
		catch (Exception e)
		{
			logger.error("Send error", e);
		}

	}

	public Boolean WaitForAck(long milliseconds)
	{
		long Start = System.currentTimeMillis();

		while (gotAck != true)
		{
			if (System.currentTimeMillis() - Start > milliseconds)
			{
				logger.warn("Timeout in getting Ack");
				return false;
			}
		}
		return true;
	}

	public Boolean isAck()
	{
		return gotAck;
	}
}