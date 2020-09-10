import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import lego.Statistics;
import medcic_proto.MedCic.ENCAPSULATION;
import medcic_proto.MedCic.OPCODE;
import tcc.GuiInterface;
import tcc.ManagementClient;
import tcc.ManagementServer;
import tcc.Parameters;

import javax.swing.BorderFactory;
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
import java.awt.Toolkit;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class MainScreen implements GuiInterface
{

	static final Logger		logger					= Logger.getLogger("MainScreen");
	private JFrame			frame;
	JFormattedTextField		txtIn1;
	JFormattedTextField		txtIn2;
	JFormattedTextField		txtOut1;
	JFormattedTextField		txtOut2;
	JComboBox<String>		cmbEncap;
	JButton					btnStart;
	ManagementServer		server;
	ManagementClient		client;
	private final JButton	btnStop					= new JButton("Stop");
	static String			configurationFilename	= "config.properties";
	JScrollPane				jsp;
	// MessageParser messageParser = null;
	private final JTextArea	textArea				= new JTextArea();
	private JScrollPane		scrollPane;
	private final JButton	btnClear				= new JButton("Clear");
	private final JButton	btnSave					= new JButton("Save");
	String					serverUri				= null;
	Boolean					isRunning				= false;
	long					lastUpdateTimeSync		= System.currentTimeMillis();
	long					lastUpdateTimeOutofSync	= System.currentTimeMillis();
	private final JPanel	pnlCounters				= new JPanel();
	private JLabel			lblCicInpoutbytes;
	private JLabel			lblIn1Counter;
	private final JPanel	pnlSetup				= new JPanel();
	private final JLabel	lblCic2InpoutBytes		= new JLabel("CIC 2 Inpout [Bytes]");
	private final JLabel	lblIn2Counter			= new JLabel("0");
	private final JLabel	lblCic1OutpoutBytes		= new JLabel("CIC 1 Outpout [Bytes]");
	private final JLabel	lblOut1Counter			= new JLabel("0");
	private final JLabel	lblOut2Counter			= new JLabel("0");
	private final JLabel	lblCic2OutpoutBytes		= new JLabel("CIC 2 Outpout [Bytes]");
	private final String	Version					= "1.1";
	private final JButton btnClearCounter = new JButton("Clear Counter");

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
					logger.error("Filed to start the application", e);
				}
			}
		});
	}

	/**
	 * Create the application.
	 * 
	 * @throws URISyntaxException
	 */
	public MainScreen() throws URISyntaxException
	{
		initialize();
		// txtIn1 ****://###.###.###.###:#####
		// MaskFormatter formatter = new MaskFormatter("****://###.###.###.###:#####");
		// txtIn1.setFormatterFactory(forrmatter);

		txtIn1.setText(Parameters.Get("url-in-1", "udp://127.0.0.1:5001"));
		txtOut1.setText(Parameters.Get("url-out-1", "udp://127.0.0.1:5003"));
		txtOut2.setText(Parameters.Get("url-out-2", "udp://127.0.0.1:5004"));
		btnStop.setToolTipText("Stop de-encapsulation process.");
		btnStop.setBounds(305, 60, 63, 23);
		pnlSetup.add(btnStop);
		btnStop.setFont(new Font("Dialog", Font.PLAIN, 12));
		txtIn2.setText(Parameters.Get("url-in-2", "udp://127.0.0.1:5002"));
		
		btnStop.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				if (client != null)
				{
					client.send(0, OPCODE.STOP_CMD, null);
					try
					{
						Thread.sleep(200);
					}
					catch (InterruptedException e1)
					{
						logger.error("Faild to stop", e1);
					}
					client.Stop();
					client = null;
				}
				isRunning = false;
				OperationCompleted();
				btnStart.setEnabled(true);
			}
		});

		scrollPane = new JScrollPane();
		scrollPane.setForeground(new Color(0, 0, 128));
		scrollPane.setEnabled(false);
		scrollPane.setBounds(10, 118, 597, 234);
		frame.getContentPane().add(scrollPane);
		textArea.setFocusable(false);
		textArea.setForeground(new Color(0, 0, 255));
		textArea.setToolTipText("Clear the message logger");
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		btnClear.setBounds(247, 457, 123, 23);
		btnClear.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				textArea.setText("");
			}
		});
		btnClear.setFont(new Font("Dialog", Font.PLAIN, 12));

		frame.getContentPane().add(btnClear);
		btnSave.setToolTipText("Save the message logger to a file");
		btnSave.setBounds(432, 457, 123, 23);
		btnSave.setEnabled(false);
		btnSave.setFont(new Font("Tahoma", Font.PLAIN, 10));

		frame.getContentPane().add(btnSave);
		pnlCounters.setForeground(Color.LIGHT_GRAY);
		pnlCounters.setBounds(10, 363, 597, 83);

		frame.getContentPane().add(pnlCounters);
		pnlCounters.setLayout(null);
		pnlCounters.setBorder(BorderFactory.createTitledBorder("Couters"));

		lblCicInpoutbytes = new JLabel("CIC 1 Inpout [Bytes]");
		lblCicInpoutbytes.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblCicInpoutbytes.setBounds(10, 22, 136, 20);
		pnlCounters.add(lblCicInpoutbytes);

		lblIn1Counter = new JLabel("0");
		lblCicInpoutbytes.setLabelFor(lblIn1Counter);
		lblIn1Counter.setBorder(new LineBorder(new Color(0, 0, 0)));
		lblIn1Counter.setHorizontalAlignment(SwingConstants.CENTER);
		lblIn1Counter.setBounds(156, 22, 129, 20);
		pnlCounters.add(lblIn1Counter);
		lblCic2InpoutBytes.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblCic2InpoutBytes.setBounds(10, 53, 136, 20);

		pnlCounters.add(lblCic2InpoutBytes);
		lblIn2Counter.setBorder(new LineBorder(new Color(0, 0, 0)));
		lblIn2Counter.setHorizontalAlignment(SwingConstants.CENTER);
		lblIn2Counter.setBounds(156, 53, 129, 20);

		pnlCounters.add(lblIn2Counter);
		lblCic1OutpoutBytes.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblCic1OutpoutBytes.setBounds(318, 22, 141, 20);

		pnlCounters.add(lblCic1OutpoutBytes);
		lblOut1Counter.setBorder(new LineBorder(new Color(0, 0, 0)));
		lblOut1Counter.setHorizontalAlignment(SwingConstants.CENTER);
		lblOut1Counter.setBounds(458, 22, 129, 20);

		pnlCounters.add(lblOut1Counter);
		lblCic2OutpoutBytes.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblCic2OutpoutBytes.setBounds(318, 53, 141, 20);
		
				pnlCounters.add(lblCic2OutpoutBytes);
		lblOut2Counter.setBorder(new LineBorder(new Color(0, 0, 0)));
		lblOut2Counter.setHorizontalAlignment(SwingConstants.CENTER);
		lblOut2Counter.setBounds(458, 53, 129, 20);

		pnlCounters.add(lblOut2Counter);
		btnClearCounter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lblIn1Counter.setText("0");
				lblIn2Counter.setText("0");
				lblOut1Counter.setText("0");
				lblOut2Counter.setText("0");
			}
			
		});
		btnClearCounter.setFont(new Font("Dialog", Font.PLAIN, 12));
		btnClearCounter.setBounds(62, 457, 123, 23);
		
		frame.getContentPane().add(btnClearCounter);
		// frame.getContentPane().add(scroll);

		String host = Parameters.Get("WebSocketListenAddress", "127.0.0.1");
		int port = Integer.parseInt(Parameters.Get("WebSocketListenPort", "8887"));

		server = new ManagementServer(new InetSocketAddress(host, port));
		server.start();
		serverUri = Parameters.Get("WebSocketServerUri", "ws://127.0.0.1") + ":" + Parameters.Get("WebSocketListenPort");
		
		client = null;// new ManagementClient(new URI(serverUri), this);
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
				logger.error("Failed to create client", e);
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

			String input1 = "", input2 = "", output1 = "", output2 = "";

			input1 = txtIn1.getText();
			output1 = txtOut1.getText();
			input2 = txtIn2.getText();
			output2 = txtOut2.getText();

			if (((String) (cmbEncap.getSelectedItem())).toLowerCase().startsWith("auto"))
			{
				client.SendAutomaticStartCommand(input1, input2, output1, output2);
			}
			else
			{
				client.SendStartCommand(toProtobuff((String) cmbEncap.getSelectedItem()), input1, input2, output1,
						output2);
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
							logger.error("Worng port number", ex);
						}
						JOptionPane.showMessageDialog(null,
								"Wrong URL format.:port should be a number between 1 and 65535");
						return false;
					}
				}
				else
				{
					return true;
				}
			}
			JOptionPane.showMessageDialog(null,
					"Wrong URL format. Should be udp://ip:port or file://<file path and name>");
			return false;
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize()
	{
		frame = new JFrame();
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(MainScreen.class.getResource("/tcc/mediation.png")));
		frame.addWindowListener(new WindowAdapter()
		{

			@Override
			public void windowClosing(WindowEvent e)
			{
				Stop();
			}
		});
		frame.setBounds(100, 100, 633, 530);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		pnlSetup.setBounds(10, 11, 597, 96);
		pnlSetup.setBorder(BorderFactory.createTitledBorder("Setup"));
		frame.getContentPane().add(pnlSetup);
		pnlSetup.setLayout(null);

		txtOut2 = new JFormattedTextField();
		txtOut2.setBounds(400, 63, 187, 20);
		pnlSetup.add(txtOut2);
		txtOut2.setToolTipText("CIC 2 de-encapsulated signal destination URI in the form of udp://<ip address>:<port>");
		txtOut2.setText("udp://127.0.0.0.4:1000");
		txtOut2.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		txtOut2.setInputVerifier(new UrlVerifier());

		txtIn2 = new JFormattedTextField();
		txtIn2.setBounds(10, 63, 197, 20);
		pnlSetup.add(txtIn2);
		txtIn2.setToolTipText("CIC 2 signal source URI in the form of udp://<ip address>:<port>");
		txtIn2.setText("udp://127.0.0.0.2:1000");
		txtIn2.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		txtIn2.setInputVerifier(new UrlVerifier());

		btnStart = new JButton("Start");
		btnStart.setToolTipText("Start de-encapsulation process.");
		btnStart.setBounds(233, 60, 68, 23);
		pnlSetup.add(btnStart);
		btnStart.setFont(new Font("Dialog", Font.PLAIN, 12));
		btnStart.addActionListener(new StartAction());

		cmbEncap = new JComboBox<String>();
		cmbEncap.setToolTipText("Choose encapsulation type. \"AUTO DETECT\" will initate detection process first and than start production");
		cmbEncap.setBounds(233, 17, 135, 22);
		pnlSetup.add(cmbEncap);

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
		cmbEncap.addItem("PLANE");

		txtOut1 = new JFormattedTextField();
		txtOut1.setBounds(400, 18, 187, 20);
		pnlSetup.add(txtOut1);
		txtOut1.setToolTipText("CIC 1 de-encapsulated signal destination URI in the form of udp://<ip address>:<port>");
		txtOut1.setText("udp://127.0.0.0.3:1000");
		txtOut1.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		txtOut1.setInputVerifier(new UrlVerifier());

		txtIn1 = new JFormattedTextField();
		txtIn1.setBounds(10, 18, 197, 20);
		pnlSetup.add(txtIn1);
		txtIn1.setBackground(Color.WHITE);
		txtIn1.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		txtIn1.setToolTipText("CIC 1 signal source URI in the form of udp://<ip address>:<port>");
		txtIn1.setText("udp://127.0.0.0.1:1000");
		txtIn1.setInputVerifier(new UrlVerifier());
		frame.setTitle("MedCic Version " + Version);
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
			
		case "PLANE":
		default:
			return ENCAPSULATION.UNKNOWN_ENCAPSULATION;
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
		 * if (status) { btnStart.setBackground(Color.GREEN); } else {
		 * btnStart.setBackground(Color.GRAY); }
		 */

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
			// if (System.currentTimeMillis() - lastUpdateTimeSync > 200)
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
		// if (System.currentTimeMillis() - lastUpdateTimeOutofSync > 200)
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
		int Index = encap.getNumber();
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
			
		case UNKNOWN_ENCAPSULATION:
			logger.warn("Unkow encapsulation");
			index = 11;

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
		/*
		 * UpdateStatus("CIC 1 < input byte count: " + stat.getCic1In());
		 * UpdateStatus("CIC 2 < input byte count: " + stat.getCic1In());
		 * UpdateStatus("CIC 1 > output byte count: " + stat.getCic1In());
		 * UpdateStatus("CIC 2 > outinput byte count: " + stat.getCic1In());
		 * UpdateStatus("======================================================");
		 */
		UpdateCic1InCouter(stat.getCic1In());
		UpdateCic2InCouter(stat.getCic2In());
		UpdateCic1OutCouter(stat.getCic1Out());
		UpdateCic2OutCouter(stat.getCic2Out());
	}

	private void UpdateCic1InCouter(long counter)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new Runnable()
			{

				@Override
				public void run()
				{
					UpdateCic1InCouter(counter);
				}
			});
			return;
		}
		lblIn1Counter.setText(String.valueOf(counter));
	}

	private void UpdateCic2InCouter(long counter)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new Runnable()
			{

				@Override
				public void run()
				{
					UpdateCic2InCouter(counter);
				}
			});
			return;
		}
		lblIn2Counter.setText(String.valueOf(counter));
	}

	private void UpdateCic1OutCouter(long counter)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new Runnable()
			{

				@Override
				public void run()
				{
					UpdateCic1OutCouter(counter);
				}
			});
			return;
		}
		lblOut1Counter.setText(String.valueOf(counter));
	}

	private void UpdateCic2OutCouter(long counter)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new Runnable()
			{

				@Override
				public void run()
				{
					UpdateCic2OutCouter(counter);
				}
			});
			return;
		}
		lblOut2Counter.setText(String.valueOf(counter));
	}
}
