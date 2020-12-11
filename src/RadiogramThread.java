import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

class RadiogramThread implements Runnable {
	private JProgressBar pb;
	private JLabel lb;
	private JButton bt;
	private ActionListener actionEx = new ActionException();
	private static ExceptionFrame excFrame; 
	private boolean error = false;
	
	
	public RadiogramThread(JProgressBar progB, JLabel label, JButton button) {
		pb = progB;
		lb = label;
		bt = button;
	}
	
	public void run() {	
		try {
			mask makeRadiogrmas = new mask(pb);
		}
		catch(Exception exc) {
			error = true;
			excFrame = new ExceptionFrame(exc.toString());
			lb.setText("Помилка!!!");
			bt.setEnabled(true);
			bt.setText("Докладніше");
			bt.addActionListener(actionEx);
		}
		if (!error) {
			lb.setText("Радіограми успішно створенні!");
			bt.setText("Вихід");
			bt.setEnabled(true);
			bt.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});
		}	
	}
		
	private class ActionException implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			excFrame.setVisible(true);
		}
	}
}