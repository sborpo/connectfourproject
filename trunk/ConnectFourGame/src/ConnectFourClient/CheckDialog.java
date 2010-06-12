package ConnectFourClient;

import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class CheckDialog extends JDialog implements MouseListener{
		
	public CheckDialog()
	{
		super();
		setModal(true);
		this.setTitle("Address Dialog");
	    this.setLayout(new GridLayout(4, 2));
	    this.add(new JLabel("blabla"));
	    JButton butt = new JButton("stam");
	    getContentPane().add(butt);
	    butt.addMouseListener(this);
		
		this.setVisible(true);
		
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		JDialog dialog2=new JDialog(this, true);
		dialog2.setVisible(true);
		
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
