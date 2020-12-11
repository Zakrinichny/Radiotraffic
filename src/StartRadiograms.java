import javax.swing.SwingUtilities;

public class StartRadiograms {
	
	public static void main (String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				RadioFrame frame = new RadioFrame();
			}
		});		 
	}
}
