import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import lego.MessageParser;
import medcic_proto.MedCic.ENCAPSULATION;
import medcic_proto.MedCic.OPCODE;
import tcc.GuiInterface;
import tcc.ManagementClient;
import tcc.ManagementServer;
import tcc.Parameters;

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
import javax.swing.JCheckBox;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JTextArea;


public class MainScreen implements GuiInterface
{
	Logger logger = Logger.getLogger("MainScreen");
	private JFrame		frame;
	JFormattedTextField	txtIn1;
	JFormattedTextField	txtIn2;
	JFormattedTextField	txtOut1;
	JFormattedTextField	txtOut2;
	JComboBox<String>	encap;
	JCheckBox			chkCic1;
	JCheckBox			chkCic2;
	JButton				btnStart;
	ManagementServer	server;
	ManagementClient	client;
	private final JButton btnStop = new JButton("Stop");
	static String configurationFilename = "config.properties";
	private final JTextArea textArea;// = new JTextArea();
	JScrollPane jsp;
	MessageParser messageParser = null;

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
		chkCic1.setSelected(Parameters.Get("Cic1", "0").equals("0") ? false : true);
		chkCic2.setSelected(Parameters.Get("Cic2", "0").equals("0") ? false : true);
		btnStop.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				client.send(0,OPCODE.STOP_CMD, null);
			}
		});
		btnStop.setBounds(292, 53, 57, 23);
		frame.getContentPane().add(btnStop);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setBounds(10, 92, 607, 208);
		
		textArea.setSize(607,208);
		textArea.setLineWrap(true);
	    textArea.setEditable(false);
	    textArea.setVisible(true);
		
	    //JScrollPane scroll = new JScrollPane (textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	   
	    
	    //jsp = new JScrollPane(textArea, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		//jsp = new JScrollPane(textArea);
		 //JScrollBar bar = new JScrollBar();  
		 //jsp.add(bar);  
		frame.getContentPane().add(textArea);
		//frame.getContentPane().add(scroll);

		String host = Parameters.Get("ListenAddress", "127.0.0.1");
		int port = Integer.parseInt(Parameters.Get("ListenPort", "8887"));

		server = new ManagementServer(new InetSocketAddress(host, port));
		server.start();
		String serverUri = Parameters.Get("ServerUri", "ws://127.0.0.1:8887");
		client = new ManagementClient(new URI(serverUri), this);
		
		int ManagementPort = Integer.parseInt(Parameters.Get("ManagementPort", "11001"));
		try
		{
			messageParser = new MessageParser(this, ManagementPort);
			messageParser.start();
		}
		catch (Exception e1)
		{
			logger.error("Failed to run UDP server for messages from the modules", e1);
		} 
	}

	class StartAction implements ActionListener
	{

		public void actionPerformed(ActionEvent arg0)
		{

			try
			{
				Parameters.Set("url-in-1", txtIn1.getText());
				Parameters.Set("url-in-2", txtIn2.getText());
				Parameters.Set("url-out-1", txtOut1.getText());
				Parameters.Set("url-out-2", txtOut2.getText());
				Parameters.Set("Cic1", (chkCic1.isSelected() ? "1" : "0"));
				Parameters.Set("Cic2", (chkCic2.isSelected() ? "1" : "0"));
			}
			catch (IOException e1)
			{
				logger.error("Failed to save parameters", e1);
			}

			String input1="" ,input2="", output1="", output2="";
			
			if (chkCic1.isSelected())
			{
				input1 = txtIn1.getText();
				output1 = txtOut1.getText();
			}
			
			if (chkCic2.isSelected())
			{
				input2 = txtIn2.getText();
				output2 = txtOut2.getText();
			}
			if (((String)(encap.getSelectedItem())).toLowerCase().startsWith("auto"))
			{
				client.SendAutomatucStartCommand(input1,input2, output1, output2);
			}
			else
			{
				client.SendStartCommand(toProtobuff((String) encap.getSelectedItem()),input1,input2, output1, output2);
			}
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
		frame.setBounds(100, 100, 633, 340);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		txtIn1 = new JFormattedTextField();
		txtIn1.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		txtIn1.setToolTipText("test tooltip");
		txtIn1.setText("udp://127.0.0.0.1:1000");
		txtIn1.setBounds(10, 11, 172, 20);
		frame.getContentPane().add(txtIn1);
		txtIn1.setInputVerifier(new UrlVerifier());

		txtIn2 = new JFormattedTextField();
		txtIn2.setToolTipText("test tooltip");
		txtIn2.setText("udp://127.0.0.0.2:1000");
		txtIn2.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		txtIn2.setBounds(10, 54, 172, 20);
		frame.getContentPane().add(txtIn2);
		txtIn2.setInputVerifier(new UrlVerifier());

		txtOut1 = new JFormattedTextField();
		txtOut1.setToolTipText("test tooltip");
		txtOut1.setText("udp://127.0.0.0.3:1000");
		txtOut1.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		txtOut1.setBounds(384, 11, 172, 20);
		frame.getContentPane().add(txtOut1);
		txtOut1.setInputVerifier(new UrlVerifier());

		txtOut2 = new JFormattedTextField();
		txtOut2.setToolTipText("test tooltip");
		txtOut2.setText("udp://127.0.0.0.4:1000");
		txtOut2.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		txtOut2.setBounds(384, 54, 172, 20);
		frame.getContentPane().add(txtOut2);
		txtOut2.setInputVerifier(new UrlVerifier());

		encap = new JComboBox<String>();
		encap.setBounds(214, 11, 135, 20);
		frame.getContentPane().add(encap);

		btnStart = new JButton("Start");
		btnStart.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnStart.addActionListener(new StartAction());

		btnStart.setBounds(214, 53, 57, 23);
		frame.getContentPane().add(btnStart);

		chkCic1 = new JCheckBox("CIC 1");
		chkCic1.setBounds(562, 10, 97, 23);
		frame.getContentPane().add(chkCic1);

		chkCic2 = new JCheckBox("CIC 2");
		chkCic2.setBounds(562, 53, 97, 23);
		frame.getContentPane().add(chkCic2);

		encap.addItem("Auto Detect");
		encap.addItem("D&I++");
		encap.addItem("EDMAC");
		encap.addItem("EDMAC-2 (2928)");
		encap.addItem("EDMAC-2 (3072)");
		encap.addItem("ESC++        (532)");
		encap.addItem("ESC++        (551)");
		encap.addItem("ESC++        (874)");
		encap.addItem("ESC++      (1104)");
		encap.addItem("ESC++      (1792)");
		encap.addItem("E2");

		frame.setResizable(false);
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
		if (status)
		{
			btnStart.setBackground(Color.GREEN);
		}
		else
		{
			btnStart.setBackground(Color.GRAY);
		}
		
		
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
		// Now edit your gui objects
		btnStart.setBackground(Color.GRAY);
	}
}
