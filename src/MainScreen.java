import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.java_websocket.server.WebSocketServer;

import com.google.protobuf.AbstractMessage.Builder;

import tcc.ManagementServer;
import tcc.MedCIC;
import tcc.MedCIC.STATUS;
import tcc.MedCIC.StatusReply;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import javax.swing.JComboBox;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;

public class MainScreen
{

	private JFrame		frame;
	JFormattedTextField	txtIn1;
	JFormattedTextField	txtIn2;
	JFormattedTextField	txtOut1;
	JFormattedTextField	txtOut2;
	JComboBox<String>	encap;
	JCheckBox			chkCic1;
	JCheckBox			chkCic2;
	JButton				btnStart;
	JLabel				lblStatusbar;
	Parameters			param;
	WebSocketServer		server;
	StatusReply			m;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
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
	 */
	public MainScreen()
	{
		initialize();
		// txtIn1 ****://###.###.###.###:#####
		// MaskFormatter formatter = new MaskFormatter("****://###.###.###.###:#####");
		// txtIn1.setFormatterFactory(forrmatter);
		try
		{
			param = new Parameters("config.ini");
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			return;
			// e.printStackTrace();
		}

		txtIn1.setText(param.Get("url-in-1", "udp://127.0.0.1:5001"));
		txtIn2.setText(param.Get("url-in-2", "udp://127.0.0.1:5002"));
		txtOut1.setText(param.Get("url-out-1", "udp://127.0.0.1:5003"));
		txtOut2.setText(param.Get("url-out-2", "udp://127.0.0.1:5004"));
		chkCic1.setSelected(param.Get("Cic1", "0").equals("0") ? false : true);
		chkCic2.setSelected(param.Get("Cic2", "0").equals("0") ? false : true);
		// webs = new Managment();
		// webs.Start("ws://echo.websocket.org/");
		// webs.Send("Hello");

		String host = param.Get("ListenAddress", "127.0.0.1");
		int port = Integer.parseInt(param.Get("ListenPort", "8887"));

		/*
		 * m = StatusReply.newBuilder() .setStatus(STATUS.RUN)
		 * .setStatusDescription("Seems OK") .setError(false) .setWarning(false)
		 * .build();
		 * 
		 * Boolean x = m.getError();
		 */
		server = new ManagementServer(new InetSocketAddress(host, port));
		server.start();

		m = StatusReply.newBuilder().setStatus(STATUS.RUN).setStatusDescription("Seems OK").setError(false)
				.setWarning(false).build();

		Boolean x = m.getError();

	}

	class StartAction implements ActionListener
	{

		public void actionPerformed(ActionEvent arg0)
		{

			try
			{
				param.Set("url-in-1", txtIn1.getText());
				param.Set("url-in-2", txtIn2.getText());
				param.Set("url-out-1", txtOut1.getText());
				param.Set("url-out-2", txtOut2.getText());
				param.Set("Cic1", (chkCic1.isSelected() ? "1" : "0"));
				param.Set("Cic2", (chkCic2.isSelected() ? "1" : "0"));
			}
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			String Protocol = (String) encap.getSelectedItem();
			int FrameSize = 0;
			Pattern pattern = Pattern.compile("(\\()([0-9]*)(\\))");
			Matcher matcher = pattern.matcher(Protocol);
			if (matcher.find())
			{
				try
				{
					FrameSize = Integer.parseInt(matcher.group(2));
				}
				catch (Exception e)
				{
					FrameSize = -1;
				}
			}
			Protocol = Protocol.split(" ")[0];
			lblStatusbar.setText("Protocol=" + Protocol + ", FrameSize=" + FrameSize + " CIC1 is "
					+ (chkCic1.isSelected() ? "ON" : "OFF") + ", CIC2 is " + (chkCic2.isSelected() ? "ON" : "OFF"));
			try
			{
				Runtime.getRuntime().exec("notepad.exe");
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			btnStart.setBackground(Color.GREEN);
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
		frame.setBounds(100, 100, 633, 143);
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
		btnStart.addActionListener(new StartAction());

		btnStart.setBounds(241, 53, 89, 23);
		frame.getContentPane().add(btnStart);

		chkCic1 = new JCheckBox("CIC 1");
		chkCic1.setBounds(562, 10, 97, 23);
		frame.getContentPane().add(chkCic1);

		chkCic2 = new JCheckBox("CIC 2");
		chkCic2.setBounds(562, 53, 97, 23);
		frame.getContentPane().add(chkCic2);

		lblStatusbar = new JLabel("Application Started");
		lblStatusbar.setBounds(0, 89, frame.getWidth() - 5, 14);
		frame.getContentPane().add(lblStatusbar, BorderLayout.SOUTH);

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
		// create the status bar panel and shove it down the bottom of the frame

	}
}
