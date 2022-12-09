import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;

@SuppressWarnings("serial")
public class Tickets extends JFrame implements ActionListener {

	// class level member objects
	Dao dao = new Dao(); // for CRUD operations
	Boolean chkIfAdmin = null;
	String username;

	// Main menu object items
	private JMenu mnuFile = new JMenu("File");
	private JMenu mnuAdmin = new JMenu("Admin");
	private JMenu mnuTickets = new JMenu("Tickets");

	// Sub menu item objects for all Main menu item objects
	JMenuItem mnuItemExit;
	JMenuItem mnuItemDelete;
	JMenuItem mnuItemOpenTicket;
	JMenuItem mnuItemViewTicket;
	JMenuItem mnuItemViewTicketByNum; // for use in viewing tickets by a number
	JMenuItem mnuItemCloseTicket; // for use in closing tickets
	JMenuItem mnuItemUpdateTicketDesc; //for use in updating ticket description
	JMenuItem mnuItemClosedTickets; //for use in viewing the closed ticket menu

	public Tickets(Boolean isAdmin, String user) {

		chkIfAdmin = isAdmin;
		username = user;
		createMenu();
		prepareGUI();

	}

	private void createMenu() {

		/* Initialize sub menu items **************************************/

		// initialize sub menu item for File main menu
		mnuItemExit = new JMenuItem("Exit");
		// add to File main menu item
		mnuFile.add(mnuItemExit);

		// initialize second sub menu items for Admin main menu
		mnuItemDelete = new JMenuItem("Delete Ticket");
		// add to Admin main menu item
		mnuAdmin.add(mnuItemDelete);

		// initialize first sub menu item for Tickets main menu
		mnuItemOpenTicket = new JMenuItem("Open Ticket");
		// add to Ticket Main menu item
		mnuTickets.add(mnuItemOpenTicket);

		// initialize second sub menu item for Tickets main menu
		mnuItemViewTicket = new JMenuItem("View Tickets");
		// add to Ticket Main menu item
		mnuTickets.add(mnuItemViewTicket);
		
		//Adding this view by ticket number in ticket menu
		mnuItemViewTicketByNum = new JMenuItem("View Ticket by Ticket Numbers");
		mnuTickets.add(mnuItemViewTicketByNum);
		
		//This will be used to close tickets, only available to admins
		mnuItemCloseTicket = new JMenuItem("Close Ticket");
		mnuAdmin.add(mnuItemCloseTicket);
		
		//This will be used to update ticket descriptions
		mnuItemUpdateTicketDesc = new JMenuItem("Update Ticket Description");
		mnuTickets.add(mnuItemUpdateTicketDesc);
		
		//This will be used to view the closed tickets menu
		mnuItemClosedTickets = new JMenuItem("View Closed Tickets");
		mnuAdmin.add(mnuItemClosedTickets);

		// initialize any more desired sub menu items below

		/* Add action listeners for each desired menu item *************/
		mnuItemExit.addActionListener(this);
		mnuItemDelete.addActionListener(this);
		mnuItemOpenTicket.addActionListener(this);
		mnuItemViewTicket.addActionListener(this);
		mnuItemViewTicketByNum.addActionListener(this);
		mnuItemCloseTicket.addActionListener(this);
		mnuItemUpdateTicketDesc.addActionListener(this);
		mnuItemClosedTickets.addActionListener(this);

		 /*
		  * continue implementing any other desired sub menu items (like 
		  * for update and delete sub menus for example) with similar 
		  * syntax & logic as shown above*
		 */


	}

	private void prepareGUI() {

		// create JMenu bar
		JMenuBar bar = new JMenuBar();
		bar.add(mnuFile); // add main menu items in order, to JMenuBar
		if (chkIfAdmin)
			bar.add(mnuAdmin);
		bar.add(mnuTickets);
		// add menu bar components to frame
		setJMenuBar(bar);

		addWindowListener(new WindowAdapter() {
			// define a window close operation
			public void windowClosing(WindowEvent wE) {
				System.exit(0);
			}
		});
		// set frame options
		setSize(400, 400);
		getContentPane().setBackground(Color.LIGHT_GRAY);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// implement actions for sub menu items
		if (e.getSource() == mnuItemExit) {
			System.exit(0);
		} else if (e.getSource() == mnuItemOpenTicket) {

			// get ticket information
			String ticketDesc = JOptionPane.showInputDialog(null, "Enter a ticket description");
			String startDate = JOptionPane.showInputDialog(null, "Enter a ticket start date in YYYY-MM-DD form");

			// insert ticket information to database

			int id = dao.insertRecords(username, ticketDesc, startDate);

			// display results if successful or not to console / dialog box
			if (id != 0) {
				System.out.println("Ticket ID : " + id + " created successfully!!!");
				JOptionPane.showMessageDialog(null, "Ticket id: " + id + " created");
			} else
				System.out.println("Ticket cannot be created!!!");
		} else if(e.getSource() == mnuItemCloseTicket) { 
			
			
			//get end date
			String ticketNum = JOptionPane.showInputDialog(null, "Enter the ticket number you want to close");
			String endDate = JOptionPane.showInputDialog(null, "Enter the end date for the ticket in YYYY-MM-DD form");
			int confirmation = JOptionPane.showConfirmDialog(null, ("You want to close ticket #" + ticketNum), "Confirm?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			
			if (confirmation == JOptionPane.NO_OPTION)
				System.out.println("Ticket Closing Cancelled");
			else {
				dao.closeTicket(Integer.valueOf(ticketNum), endDate);
				dao.toClosedTable(Integer.valueOf(ticketNum), username);
			}
			
		} else if (e.getSource() == mnuItemViewTicket) {

			// retrieve all tickets details for viewing in JTable
			try {

				// Use JTable built in functionality to build a table model and
				// display the table model off your result set!!!
				JTable jt = new JTable(ticketsJTable.buildTableModel(dao.readRecords(chkIfAdmin, username))); //if we are dealing with an administrator, then there should be something different printed to them
				jt.setBounds(30, 40, 200, 400);
				JScrollPane sp = new JScrollPane(jt);
				add(sp);
				setVisible(true); // refreshes or repaints frame on screen

			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} else if (e.getSource() == mnuItemViewTicketByNum) {
			// retrieve all tickets details for viewing in JTable
			try {
				
				int ticketNum = Integer.valueOf(JOptionPane.showInputDialog(null, "Enter the ticket number you want to view"));

				// Use JTable built in functionality to build a table model and
				// display the table model off your result set!!!
				JTable jt = new JTable(ticketsJTable.buildTableModel(dao.ticketByNum(chkIfAdmin, ticketNum, username))); //if we are dealing with an administrator, then there should be something different printed to them
				jt.setBounds(30, 40, 200, 400);
				JScrollPane sp = new JScrollPane(jt);
				add(sp);
				setVisible(true); // refreshes or repaints frame on screen

			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} else if (e.getSource() == mnuItemDelete) {
			String ticketNum = JOptionPane.showInputDialog(null, "Enter the ticket number you want to delete");
			int confirmation = JOptionPane.showConfirmDialog(null, "Delete ticket # " + ticketNum + "?","Confirm",  JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); //confirmation menu for deleting
			
			if (confirmation == JOptionPane.NO_OPTION)
				System.out.println("Deletion cancelled");
			else
				dao.deleteRecords(Integer.valueOf(ticketNum));
		} else if(e.getSource() == mnuItemUpdateTicketDesc) {
			String ticketNum = JOptionPane.showInputDialog(null, "Enter the ticket number you want to update");
			String ticketDesc = JOptionPane.showInputDialog(null, "Enter the new ticket description");
			
			int confirmation = JOptionPane.showConfirmDialog(null, "Update ticket # " + ticketNum + " description?","Confirm",  JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); //confirmation menu for updating ticket number
			
			if (confirmation == JOptionPane.NO_OPTION)
				System.out.println("Update cancelled");
			else
				dao.updateDescription(chkIfAdmin, Integer.valueOf(ticketNum), username, ticketDesc);
		} else if (e.getSource() == mnuItemClosedTickets) {
			// Use JTable built in functionality to build a table model and
			// display the table model off your result set!!!
			JTable jt;
			try {
				jt = new JTable(ticketsJTable.buildTableModel(dao.viewClosedTable()));
				jt.setBounds(30, 40, 200, 400);
				JScrollPane sp = new JScrollPane(jt);
				add(sp);
				setVisible(true); // refreshes or repaints frame on screen
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} //if we are dealing with an administrator, then there should be something different printed to them
		}
		
	}

}
