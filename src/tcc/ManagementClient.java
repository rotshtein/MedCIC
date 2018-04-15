package tcc;

import java.net.URI;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import medcic_proto.MedCic.AutomaticStartCommand;
import medcic_proto.MedCic.ENCAPSULATION;
import medcic_proto.MedCic.Header;
import medcic_proto.MedCic.OPCODE;
import medcic_proto.MedCic.STATUS;
import medcic_proto.MedCic.StartCommand;
import medcic_proto.MedCic.StatusMessage;
import medcic_proto.MedCic.StatusReplay;

public class ManagementClient extends WebSocketClient
{

	static Logger		logger				= Logger.getLogger("ManagementClient");
	GuiInterface		gui					= null;
	Boolean				connectionStatus	= false;
	ManagementClient	conn;
	Boolean				gotAck				= false;
	Boolean				gotNck				= false;

	public ManagementClient(URI serverUri, GuiInterface gui)
	{
		super(serverUri);
		this.gui = gui;
		this.connect();
	}

	@Override
	public void onOpen(ServerHandshake handshakedata)
	{
		gui.UpdateStatus("Connected to the server" + System.getProperty("line.separator"));
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
				StatusReplay sr = StatusReplay.parseFrom(h.getMessageData());

				gui.UpdateStatus(sr.getStatusDescription());

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
					gui.UpdateStatus(sr.getStatusDescription());
					gui.onConnectionChange(false);
				}
				else if (sr.getStatus() == STATUS.RUN)
				{
					gui.UpdateStatus(sr.getStatusDescription());
					gui.onConnectionChange(true);
				}
				break;

			case STATUS_MESSAGE:
				StatusMessage sm = StatusMessage.parseFrom(h.getMessageData());
				gui.UpdateStatus(sm.getMessage());
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

	public Boolean SendAutomatucStartCommand(String input1_url, String input2_url, String output1_url,
			String output2_url)
	{
		AutomaticStartCommand sc = AutomaticStartCommand.newBuilder().setInput1Url(input1_url).setInput2Url(input2_url)
				.setOutput1Url(output1_url).setOutput2Url(output2_url).build();

		send(0, OPCODE.AUTO_START_CMD, sc.toByteString());
		return true;
	}

	public Boolean SendStartCommand(ENCAPSULATION encap, String input1_url, String input2_url, String output1_url,
			String output2_url)
	{
		StartCommand sc = StartCommand.newBuilder().setEncapsulation(encap).setInput1Url(input1_url)
				.setInput2Url(input2_url).setOutput1Url(output1_url).setOutput2Url(output2_url).build();

		send(0, OPCODE.START_CMD, sc.toByteString());
		return true;
	}

	public Boolean SendStopCommand()
	{
		try
		{
			Header h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.STOP_CMD).build();

			this.send(h.toByteArray());
			send(0, OPCODE.STOP_CMD, null);
		}
		catch (Exception e)
		{
			logger.error("Send SendStopCommand error", e);
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

			this.send(h.toByteArray());
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