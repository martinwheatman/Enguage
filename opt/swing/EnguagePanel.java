package opt.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.enguage.Enguage;
import org.enguage.sign.object.Variable;

public class EnguagePanel extends JPanel {
	static final long serialVersionUID = 0L;
	
	static JFrame frame;
	protected JTextField t1, t2;
	protected JButton b;
	protected Action buttonAction;
 
	public EnguagePanel() {
		super(new BorderLayout());
 
		buttonAction = new ButtonAction( "Click to 'say' what's above, e.g. 'set colour to red'", null,
							"Click to interpret what you type above",
							KeyEvent.VK_M );
		
		Font font1 = new Font("SansSerif", Font.PLAIN, 24);
		
		t1 = new JTextField("");
		t1.setFont( font1 );
		JPanel utterancePanel = new JPanel( new GridLayout( 1, 1 ));
		utterancePanel.add( t1 );

		b = new JButton( buttonAction );
		if (b.getIcon() != null) b.setText( "" );
		b.setFont( font1 );
		JPanel buttonPanel = new JPanel( new GridLayout( 1, 1 ));
		buttonPanel.add( b );

		t2 = new JTextField("Reply");
		t2.setEditable( false );
		t2.setFont( font1 );
		JPanel textFieldPanel = new JPanel( new GridLayout(1,1));
		textFieldPanel.add( t2 );
 
		add( utterancePanel, BorderLayout.PAGE_START );
		add( buttonPanel,	BorderLayout.CENTER	 );
		add( textFieldPanel, BorderLayout.PAGE_END   );
		setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 0 ));
	}
	
	private static Color whatColourShouldIbe( Color current ) {
		Color color = current;
		
		String colour = Variable.get( "COLOUR" );
		     if (colour == null); // ignore a null return
		else if (colour.equalsIgnoreCase(  "red" )) color = Color.RED;
		else if (colour.equalsIgnoreCase( "blue" )) color = Color.BLUE;
		else if (colour.equalsIgnoreCase( "green" )) color = Color.green;
		else if (colour.equalsIgnoreCase( "yellow" )) color = Color.yellow;
						
		return color;
	}
	
	public class ButtonAction extends AbstractAction {
		static final long serialVersionUID = 0L;
		public ButtonAction( String text, ImageIcon icon,
							 String desc, Integer mnemonic ) {
			super(text, icon);
			putValue( SHORT_DESCRIPTION, desc );
			putValue( MNEMONIC_KEY, mnemonic );
		}
		public void actionPerformed(ActionEvent e) {
			String uid= "swing"; // see ./var/swing
			String utterance = t1.getText();
			String reply = Enguage.get().mediateMulti( uid, utterance );
			t2.setText( reply );
			t2.setBackground( 
				whatColourShouldIbe( t2.getBackground() )
			);
	}	}
	
	private static void createAndShowGUI() {
		JComponent pane = new EnguagePanel();
		pane.setOpaque( true );
		
		frame = new JFrame("Enguage Panel");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane( pane );
		frame.pack();
		frame.setSize( 800, 150 );
		frame.setVisible( true );
	}
	public static void main(String[] args) {
		
		Enguage.set( new Enguage( Enguage.RW_SPACE ));
		
		javax.swing.SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
}   }
