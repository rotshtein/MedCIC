package tcc;


import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;


public class ManagementServer extends WebSocketServer
{
	Logger logger = Logger.getLogger("ManagementServer");
	BlockingQueue<AbstractMap.SimpleEntry<byte[], WebSocket>> queue = null;
	ManagementParser parser;
	public ManagementServer(InetSocketAddress address)
	{
		super(address);
		try
		{
			queue = new ArrayBlockingQueue<SimpleEntry<byte[], WebSocket>>(1);
			parser = new ManagementParser("/MedCic/config.properties", queue, this);
			parser.start();
		}
		catch (Exception e)
		{
			logger.error("Parse error", e);
		}
		
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake)
	{
		conn.send("Welcome to the server!"); // This method sends a message to the new client
		// broadcast( "new connection: " + handshake.getResourceDescriptor() ); //This
		// method sends a message to all clients connected
		logger.info("new connection to " + conn.getRemoteSocketAddress());
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote)
	{
		logger.info("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
	}

	@Override
	public void onMessage(WebSocket conn, String message)
	{
		logger.warn("received message from " + conn.getRemoteSocketAddress() + ": " + message);
		conn.send("Meesage should send as ByteBuffer in protocol buffer according MedCic.proto");
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer message)
	{
		parser.queue.add(new SimpleEntry<byte[], WebSocket>(message.array(), conn));
	}

	@Override
	public void onError(WebSocket conn, Exception ex)
	{
		logger.error("an error occured on connection " + conn.getRemoteSocketAddress(), ex);
	}

	@Override
	public void onStart()
	{
		logger.info("server started successfully");
	}

}
