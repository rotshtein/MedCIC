import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import javafx.util.Pair;
import medcic_proto.MedCic.ENCAPSULATION;
import medcic_proto.MedCic.OPCODE;
import tcc.GuiInterface;
import tcc.ManagementClient;
import tcc.ManagementServer;
import tcc.Parameters;
import tcc.Statistics;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;

import java.awt.Color;
import java.awt.ComponentOrientation;
import javax.swing.JComboBox;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JTextArea;


public class MainScreen implements GuiInterface
{
	static final Logger logger = Logger.getLogger("MainScreen");
	private JFrame		frame;
	JFormattedTextField	txtIn1;
	JFormattedTextField	txtIn2;
	JFormattedTextField	txtOut1;
	JFormattedTextField	txtOut2;
	JComboBox<String>	cmbEncap;
	JButton				btnStart;
	ManagementServer	server;
	ManagementClient	client;
	private final JButton btnStop = new JButton("Stop");
	static String configurationFilename = "config.properties";
	JScrollPane jsp;
	//MessageParser messageParser = null;
	private final JTextArea textArea = new JTextArea();
	private JScrollPane scrollPane;
	private final JButton btnClear = new JButton("Clear");
	private final JButton btnSave = new JButton("Save");
	String serverUri = null;
	Boolean isRunning = false;
	long lastUpdateTimeSync = System.currentTimeMillis();
	long lastUpdateTimeOutofSync = System.currentTimeMillis();
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		
		if (args.length > 2)
		{
			if (args[0].equals("-c"))
			{
				configurationFilename = args[1];
			}
		}

		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					MainScreen window = new MainScreen();
					window.frame.setVisible(true);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	
	
	/**
	 * Create the application.
	 * @throws URISyntaxException 
	 */
	public MainScreen() throws URISyntaxException
	{
		initialize();
		// txtIn1 ****://###.###.###.###:#####
		// MaskFormatter formatter = new MaskFormatter("****://###.###.###.###:#####");
		// txtIn1.setFormatterFactory(forrmatter);
		
		
		txtIn1.setText(Parameters.Get("url-in-1", "udp://127.0.0.1:5001"));
		txtIn2.setText(Parameters.Get("url-in-2", "udp://127.0.0.1:5002"));
		txtOut1.setText(Parameters.Get("url-out-1", "udp://127.0.0.1:5003"));
		txtOut2.setText(Parameters.Get("url-out-2", "udp://127.0.0.1:5004"));
		btnStop.setBounds(328, 55, 57, 23);
		btnStop.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				if (client != null)
				{
					client.send(0,OPCODE.STOP_CMD, null);
					try
					{
						Thread.sleep(200);
					}
					catch (InterruptedException e1)
					{
						
					}
					client.Stop();
					client = null;
				}
				isRunning = false;
				OperationCompleted();
				btnStart.setEnabled(true);
			}
		});
		frame.getContentPane().add(btnStop);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 89, 597, 215);
		frame.getContentPane().add(scrollPane);
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		btnClear.setBounds(81, 315, 74, 23);
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.setText("");
			}
		});
		btnClear.setFont(new Font("Tahoma", Font.PLAIN, 10));
		
		frame.getContentPane().add(btnClear);
		btnSave.setBounds(465, 315, 89, 23);
		btnSave.setEnabled(false);
		btnSave.setFont(new Font("Tahoma", Font.PLAIN, 10));
		
		frame.getContentPane().add(btnSave);
		//frame.getContentPane().add(scroll);

		String host = Parameters.Get("ListenAddress", "127.0.0.1");
		int port = Integer.parseInt(Parameters.Get("ListenPort", "8887"));

		server = new ManagementServer(new InetSocketAddress(host, port));
		server.start();
		serverUri = Parameters.Get("ServerUri", "ws://127.0.0.1:8887");
		client = null;// new ManagementClient(new URI(serverUri), this);
		
		/*
		ScriptFile sf = null;
		ENCAPSULATION e = null;
		
		try
		{
			sf = new ScriptFile("c:\\bin\\lego\\legoFiles\\cicScript.lego");
			e = sf.getEncapsolation();
		}
		catch (FileNotFoundException e2)
		{
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		catch (IOException e2)
		{
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}*/
		
		
		/*
		int ManagementPort = Integer.parseInt(Parameters.Get("ManagementPort", "11001"));
		try
		{
			messageParser = new MessageParser(this, ManagementPort);
			messageParser.start();
		}
		catch (Exception e1)
		{
			logger.error("Failed to run UDP server for messages from the modules", e1);
		} */
	}

	class StartAction implements ActionListener
	{

		public void actionPerformed(ActionEvent arg0)
		{
			isRunning = true;
			try
			{
				client = new ManagementClient(new URI(serverUri), MainScreen.this);
			}
			catch (URISyntaxException e)
			{
				logger.error("Failed to create client" ,e);
				client = null;
			}
			try
			{
				Parameters.Set("url-in-1", txtIn1.getText());
				Parameters.Set("url-in-2", txtIn2.getText());
				Parameters.Set("url-out-1", txtOut1.getText());
				Parameters.Set("url-out-2", txtOut2.getText());
			}
			catch (IOException e1)
			{
				logger.error("Failed to save parameters", e1);
			}

			String input1="" ,input2="", output1="", output2="";
			
			input1 = txtIn1.getText();
			output1 = txtOut1.getText();
			input2 = txtIn2.getText();
			output2 = txtOut2.getText();
			
			if (((String)(cmbEncap.getSelectedItem())).toLowerCase().startsWith("auto"))
			{
				client.SendAutomatucStartCommand(input1,input2, output1, output2);
			}
			else
			{
				client.SendStartCommand(toProtobuff((String) cmbEncap.getSelectedItem()),input1,input2, output1, output2);
			}
			btnStart.setEnabled(false);
		}
	}

	class UrlVerifier extends InputVerifier
	{
		public boolean verify(JComponent input)
		{
			if (!(input instanceof JFormattedTextField)) return true;
			String first = ((JFormattedTextField) input).getText();
			if (first.startsWith("udp://") || first.startsWith("file://"))
			{
				String[] splitted = first.split(":");
				if (splitted[0].equals("udp"))
				{
					if (splitted.length == 3)
					{
						try
						{
							int port = Integer.parseInt(splitted[2]);
							if (port > 0 && port < 0xFFFF)
							{
								return true;
							}

						}
						catch (Exception ex)
						{
						}
						JOptionPane.showMessageDialog(null,"Wrong URL format.:port should be a number between 1 and 65535");
						return false;
					}
				}
				else
				{
					return true;
				}
			}
			JOptionPane.showMessageDialog(null,"Wrong URL format. Should be udp://ip:port or file://<file path and name>");
			return false;
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize()
	{
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) 
			{
				Stop();
			}
		});
		frame.setBounds(100, 100, 633, 384);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		txtIn1 = new JFormattedTextField();
		txtIn1.setBackground(Color.WHITE);
		txtIn1.setBounds(10, 13, 197, 20);
		txtIn1.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		txtIn1.setToolTipText("test tooltip");
		txtIn1.setText("udp://127.0.0.0.1:1000");
		frame.getContentPane().add(txtIn1);
		txtIn1.setInputVerifier(new UrlVerifier());

		txtIn2 = new JFormattedTextField();
		txtIn2.setBounds(10, 56, 197, 20);
		txtIn2.setToolTipText("test tooltip");
		txtIn2.setText("udp://127.0.0.0.2:1000");
		txtIn2.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		frame.getContentPane().add(txtIn2);
		txtIn2.setInputVerifier(new UrlVerifier());

		txtOut1 = new JFormattedTextField();
		txtOut1.setBounds(420, 11, 187, 20);
		txtOut1.setToolTipText("test tooltip");
		txtOut1.setText("udp://127.0.0.0.3:1000");
		txtOut1.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		frame.getContentPane().add(txtOut1);
		txtOut1.setInputVerifier(new UrlVerifier());

		txtOut2 = new JFormattedTextField();
		txtOut2.setBounds(420, 56, 187, 20);
		txtOut2.setToolTipText("test tooltip");
		txtOut2.setText("udp://127.0.0.0.4:1000");
		txtOut2.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		frame.getContentPane().add(txtOut2);
		txtOut2.setInputVerifier(new UrlVerifier());

		cmbEncap = new JComboBox<String>();
		cmbEncap.setBounds(250, 11, 135, 22);
		frame.getContentPane().add(cmbEncap);

		btnStart = new JButton("Start");
		btnStart.setBounds(250, 55, 57, 23);
		btnStart.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnStart.addActionListener(new StartAction());
		frame.getContentPane().add(btnStart);

		cmbEncap.addItem("Auto Detect");
		cmbEncap.addItem("D&I++");
		cmbEncap.addItem("EDMAC");
		cmbEncap.addItem("EDMAC-2 (2928)");
		cmbEncap.addItem("EDMAC-2 (3072)");
		cmbEncap.addItem("ESC++        (532)");
		cmbEncap.addItem("ESC++        (551)");
		cmbEncap.addItem("ESC++        (874)");
		cmbEncap.addItem("ESC++      (1104)");
		cmbEncap.addItem("ESC++      (1792)");
		cmbEncap.addItem("E2");
		frame.setVisible(true);
		// create the status bar panel and shove it down the bottom of the frame

	}
	
	private ENCAPSULATION toProtobuff(String encap)
	{
		switch (encap)
		{
		case "D&I++":
			return ENCAPSULATION.DI_PLUS;

			
		case "EDMAC":
			return ENCAPSULATION.EDMAC;
			
		case "EDMAC-2 (2928)":
			return ENCAPSULATION.EDMAC2_2928;
			
		case "EDMAC-2 (3072)":
			return ENCAPSULATION.EDMAC2_3072;
			
		case "ESC++        (532)":
			return ENCAPSULATION.ESC_532;
			
		case "ESC++        (551)":
			return ENCAPSULATION.ESC_551;
			
		case "ESC++        (874)":
			return ENCAPSULATION.ESC_874;
			
		case "ESC++      (1104)":
			return ENCAPSULATION.ESC_1104;
			
		case "ESC++      (1792)":
			return ENCAPSULATION.ESC_1792;
			
		case "E2":
			return ENCAPSULATION.E2;
			
		default:
			return ENCAPSULATION.DI_PLUS;
		}
	}

	private void Stop()
	{
		if (server != null)
		{
			server.Stop();
		}
		
		if (client != null)
		{
			client.Stop();
		}
	}

	@Override
	public void UpdateStatus(String status)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new Runnable()
			{

				@Override
				public void run()
				{
					UpdateStatus(status + System.getProperty("line.separator"));
				}
			});
			return;
		}
		// Now edit your gui objects
		textArea.append(status);
	}

	@Override
	public void onConnectionChange(Boolean status)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new Runnable()
			{

				@Override
				public void run()
				{
					onConnectionChange(status);
				}
			});
			return;
		}
		// Now edit your gui objects
		/*
		if (status)
		{
			btnStart.setBackground(Color.GREEN);
		}
		else
		{
			btnStart.setBackground(Color.GRAY);
		}*/
		
		
	}

	@Override
	public void OperationCompleted()
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					OperationCompleted();
				}
			});
			return;
		}
		isRunning = false;
		// Now edit your gui objects
		txtIn1.setBackground(Color.WHITE);
		txtIn2.setBackground(Color.WHITE);
		txtOut1.setBackground(Color.WHITE);
		txtOut2.setBackground(Color.WHITE);
	}
	
	public JTextArea getTextArea() 
	{
		return textArea;
	}



	@Override
	public void OperationStarted()
	{
		if (isRunning)
		{
			if (!SwingUtilities.isEventDispatchThread())
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						OperationStarted();
					}
				});
				return;
			}
			// Now edit your gui objects
			txtIn1.setBackground(Color.ORANGE);
			txtIn2.setBackground(Color.ORANGE);
			txtOut1.setBackground(Color.ORANGE);
			txtOut2.setBackground(Color.ORANGE);
		}
	}



	@Override
	public void OperationInSync(Channel ch)
	{
		if (isRunning)
		{
			if (!SwingUtilities.isEventDispatchThread())
			{
				switch (ch)
				{
				case INPUT1:
					if (txtIn1.getBackground() == Color.GREEN)
					{
						return;
					}
					break;
					
				case INPUT2:
					if (txtIn2.getBackground() == Color.GREEN)
					{
						return;
					}
					break;
					
				case OUTPUT1:
					if (txtOut1.getBackground() == Color.GREEN)
					{
						return;
					}
					break;
					
				case OUTPUT2:
					if (txtOut2.getBackground() == Color.GREEN)
					{
						return;
					}
					break;
					
				default:
					return;
				}
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						OperationInSync(ch);
					}
				});
				return;
			}
			// Now edit your gui objects
			//if (System.currentTimeMillis() - lastUpdateTimeSync > 200)
			{
				switch (ch)
				{
				case INPUT1:
					txtIn1.setBackground(Color.GREEN);
					break;
					
				case INPUT2:
					txtIn2.setBackground(Color.GREEN);
					break;
					
				case OUTPUT1:
					txtOut1.setBackground(Color.GREEN);
					UpdateStatus("CIC-1 is synchronized\n\r");
					break;
					
				case OUTPUT2:
					txtOut2.setBackground(Color.GREEN);
					UpdateStatus("CIC-2 is synchronized\n\r");
					break;
					
				default:
					return;
				}
				lastUpdateTimeSync = System.currentTimeMillis();
			}
		}
	}

	
	@Override
	public void OperationOutOfSync(Channel ch)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			switch (ch)
			{
			case INPUT1:
				if (!(txtIn1.getBackground() != Color.RED))
				{
					return;
				}
				break;
				
			case INPUT2:
				if (!(txtIn2.getBackground() != Color.RED))
				{
					return;
				}
				break;
				
			case OUTPUT1:
				if (!(txtOut1.getBackground() != Color.RED))
				{
					return;
				}
				break;
				
			case OUTPUT2:
				if (!(txtOut2.getBackground() != Color.RED))
				{
					return;
				}
				break;
				
			default:
				return;
			}
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					OperationOutOfSync(ch);
				}
			});
			return;
		}
		//if (System.currentTimeMillis() - lastUpdateTimeOutofSync > 200)
		{
		// Now edit your gui objects
			switch (ch)
			{
			case INPUT1:
				txtIn1.setBackground(Color.RED);
				break;
				
			case INPUT2:
				txtIn2.setBackground(Color.RED);
				break;
				
			case OUTPUT1:
				txtOut1.setBackground(Color.RED);
				UpdateStatus("CIC-1 is out of synchronized\n\r");
				break;
				
			case OUTPUT2:
				txtOut2.setBackground(Color.RED);
				UpdateStatus("CIC-2 is out of synchronized\n\r");
				break;
				
			default:
				return;
			}
			lastUpdateTimeOutofSync = System.currentTimeMillis();
		}

	}



	@Override
	public void SetEncapsulation(ENCAPSULATION encap)
	{
		
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					SetEncapsulation(encap);
				}
			});
			return;
		}
		// Now edit your gui objects
		int Index = Encapsulation2Index(encap);
		if (Index > 0)
		{
			cmbEncap.setSelectedIndex(Index);
		}
	}
	
	public int Encapsulation2Index(ENCAPSULATION EncapsolationName)
	{
		int index = -1;
	
		switch (EncapsolationName)
		{
		case DI:
			logger.warn("DI - encapsulation not supporeted");
			index = -1;
			break;

		case DI_PLUS: // "DI++":
			index = 1;
			break;

		case EDMAC: // "EDMAC":
			index = 2;
			break;

		case EDMAC2_2928: // "EDMAC-2 (2928)":
			index = 3;
			break;

		case EDMAC2_3072: // "EDMAC-2 (3072)":
			index = 4;
			break;

		case ESC_532: // "ESC++ (532)":
			index = 5;
			break;

		case ESC_551: // "ESC++ (551)":
			index = 6;
			break;

		case ESC_874:// "ESC++ (874)":
			index = 7;
			break;

		case ESC_1104: // "ESC++ (1104)":
			index = 8;
			break;

		case ESC_1792: // "ESC++ (1792)":
			index = 9;
			break;

		case E2:// "E2":
			logger.warn("E2 - encapsulation not supporeted");
			index = 10;
			break;

		case UNRECOGNIZED:
		default:
			logger.warn("UNRECOGNIZED - encapsulation not supporeted");
			index = -1;
			break;
		}
		return index;
	}



	@Override
	public void UpdateCounters(Statistics stat)
	{
		UpdateStatus("CIC 1 < input byte count: " + stat.getCic1In());
		UpdateStatus("CIC 2 < input byte count: " + stat.getCic1In());
		UpdateStatus("CIC 1 > output byte count: " + stat.getCic1In());
		UpdateStatus("CIC 2 > outinput byte count: " + stat.getCic1In());
		UpdateStatus("======================================================");
	}
}
