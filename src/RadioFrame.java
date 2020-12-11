import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class RadioFrame extends JFrame {
	
	public RadioFrame() {
		setTitle("МАСКИРКИ");
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		add(new RadioComponent());
		setSize(450,200);
		setToCenterOfScreen();	
	}
	
	private void setToCenterOfScreen() {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int screenWidth = screenSize.width;
		int screenHeight = screenSize.height;
		int x = screenWidth/2 - getWidth()/2;
		int y = screenHeight/2 - getHeight()/2;
		setLocation(x,y);
	}
}

class RadioComponent extends JComponent {
	
	private JPanel panelOk;
	private JPanel panelProgressBar;
	private JButton buttonOk;
	private JLabel labelQuestion;
	private static JProgressBar progressBar;
	private ActionListener start; 
	private ExecutorService executor;
	
	
	public RadioComponent() {
		setLayout(new GridLayout(3,1));
		
		start = new ActionStart();
		executor = Executors.newSingleThreadExecutor();
		
		panelOk = new JPanel();
		buttonOk = new JButton("Так");
		buttonOk.setFont(new Font("Serif", Font.BOLD, 20));
		buttonOk.setVerticalAlignment(JButton.CENTER);
		buttonOk.setVisible(true);
		buttonOk.addActionListener(start);
		panelOk.add(buttonOk);
		
		labelQuestion = new JLabel("Створити радіограми на місяць?");
		labelQuestion.setFont(new Font("Serif", Font.CENTER_BASELINE, 24));
		labelQuestion.setForeground(Color.red);
		labelQuestion.setHorizontalAlignment(JLabel.CENTER);
		
		panelProgressBar = new JPanel();
		
		progressBar = new JProgressBar();
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setStringPainted(true);
		progressBar.setVisible(false);
		panelProgressBar.add(progressBar);
		
		add(labelQuestion);
		add(panelProgressBar);
		add(panelOk);
	}
	
	private class ActionStart implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {						
			progressBar.setVisible(true);					
			RadiogramThread rt = new RadiogramThread(progressBar, labelQuestion, buttonOk);
			executor.execute(rt);
			buttonOk.setEnabled(false);
		}
	}
}