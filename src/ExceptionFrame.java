import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;

import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ExceptionFrame extends JFrame{
	
	private JPanel Expanel;
	private JPanel ButtonPanel;
	private JButton Exbutton;
	private JLabel Exlabel;;
	
	public ExceptionFrame(String exception) {
		setTitle("Error");
			
		Expanel = new JPanel();
		ButtonPanel = new JPanel();
		Exbutton = new JButton("Ok");
		
		Exlabel = new JLabel(exception);
		
		Exbutton.setVisible(true);
		Exbutton.setFont(new Font("Serif", Font.PLAIN, 16));
		Exbutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		
		ButtonPanel.add(Exbutton);
		Expanel.setLayout(new GridLayout(2,1));
		Expanel.add(Exlabel);
		Expanel.add(ButtonPanel);
		
		add(Expanel);
		pack();
		setToCenterOfScreen();
	}
	
	private void setToCenterOfScreen() {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int screenHeight = screenSize.height;
		int screenWidth =  screenSize.width;
		int x  = screenWidth/2 - this.getWidth()/2;
		int y = screenHeight/2 - this.getHeight()/2;
		setLocation(x,y);
	}
}