package javaapplication1;

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
	String currentUser;

	// Main menu object items
	private JMenu mnuFile = new JMenu("File");
	private JMenu mnuAdmin = new JMenu("Admin");
	private JMenu mnuTickets = new JMenu("Tickets");

	// Sub menu item objects for all Main menu item objects
	JMenuItem mnuItemExit;
	JMenuItem mnuItemUpdate;
	JMenuItem mnuItemDelete;
	JMenuItem mnuItemOpenTicket;
	JMenuItem mnuItemViewTicket;

	public Tickets(Boolean isAdmin, String username) {
		chkIfAdmin = isAdmin;
		currentUser = username;
		createMenu();
		prepareGUI();
	}

	private void createMenu() {

		/* Initialize sub menu items **************************************/

		// initialize sub menu item for File main menu
		mnuItemExit = new JMenuItem("Exit");
		// add to File main menu item
		mnuFile.add(mnuItemExit);

		// initialize first sub menu items for Admin main menu
		mnuItemUpdate = new JMenuItem("Update Ticket");
		// add to Admin main menu item
		mnuAdmin.add(mnuItemUpdate);
		mnuItemUpdate.setVisible(chkIfAdmin);

		// initialize second sub menu items for Admin main menu
		mnuItemDelete = new JMenuItem("Delete Ticket");
		// add to Admin main menu item
		mnuAdmin.add(mnuItemDelete);
		mnuItemDelete.setVisible(chkIfAdmin);

		// initialize first sub menu item for Tickets main menu
		mnuItemOpenTicket = new JMenuItem("Open Ticket");
		// add to Ticket Main menu item
		mnuTickets.add(mnuItemOpenTicket);

		// initialize second sub menu item for Tickets main menu
		mnuItemViewTicket = new JMenuItem("View Ticket");
		// add to Ticket Main menu item
		mnuTickets.add(mnuItemViewTicket);

		// initialize any more desired sub menu items below

		/* Add action listeners for each desired menu item *************/
		mnuItemExit.addActionListener(this);
		mnuItemUpdate.addActionListener(this);
		mnuItemDelete.addActionListener(this);
		mnuItemOpenTicket.addActionListener(this);
		mnuItemViewTicket.addActionListener(this);

		 /*
		  * continue implementing any other desired sub menu items (like 
		  * for update and delete sub menus for example) with similar 
		  * syntax & logic as shown above
		 */

 
	}

	private void prepareGUI() {

		// create JMenu bar
		JMenuBar bar = new JMenuBar();
		bar.add(mnuFile); // Add File menu to JMenuBar
		if (chkIfAdmin) {
			bar.add(mnuAdmin); // Only add Admin menu to admin user menu
		}
		bar.add(mnuTickets); // Add Tickets menu to JMenuBar

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

	// Method to refresh the ticket view
    private void refreshTicketView() {
        try {
            JTable jt = new JTable(ticketsJTable.buildTableModel(dao.readRecords(currentUser, chkIfAdmin)));
            jt.setBounds(30, 40, 200, 400);
            JScrollPane sp = new JScrollPane(jt);
            getContentPane().removeAll(); // Remove all previous components
            add(sp); // Add new JScrollPane with updated JTable
            validate(); // Validate changes on the container
            repaint(); // Repaint the container
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to refresh tickets.");
        }
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		// implement actions for sub menu items
		if (e.getSource() == mnuItemExit) {
			System.exit(0);
		} else if (e.getSource() == mnuItemOpenTicket) {

			// get ticket information
			String ticketName = JOptionPane.showInputDialog(null, "Enter your name");
			String ticketDesc = JOptionPane.showInputDialog(null, "Enter a ticket description");

			// insert ticket information to database

			int id = dao.insertRecords(ticketName, ticketDesc);

			// display results if successful or not to console / dialog box
			if (id != 0) {
				System.out.println("Ticket ID : " + id + " created successfully!!!");
				JOptionPane.showMessageDialog(null, "Ticket id: " + id + " created");
				refreshTicketView(); // Refresh view to show new ticket
			} else
				System.out.println("Ticket cannot be created!!!");
		}

		else if (e.getSource() == mnuItemViewTicket) {
			// Assume 'currentUser' holds the username of the logged-in user.
			// This variable should be set during login.
			try {
				JTable jt = new JTable(ticketsJTable.buildTableModel(dao.readRecords(currentUser, chkIfAdmin)));
				jt.setBounds(30, 40, 200, 400);
				JScrollPane sp = new JScrollPane(jt);
				add(sp);
				setVisible(true); // Refreshes or repaints frame on screen
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		
		/*
		 * continue implementing any other desired sub menu items (like for update and
		 * delete sub menus for example) with similar syntax & logic as shown above
		 */

		else if (e.getSource() == mnuItemUpdate) { 
			// Get ticket ID from user
			String ticketIdString = JOptionPane.showInputDialog(null, "Enter the ID of the ticket to update:");
			if (ticketIdString != null && !ticketIdString.isEmpty() ) {
				int ticketId = Integer.parseInt(ticketIdString);
				// Get the new descriptionate for the ticket
				String newDescription = JOptionPane.showInputDialog(null, "Enter the new description for the ticket: ");
				if (newDescription != null && !newDescription.isEmpty()) {
					// Call update method
					int result = dao.updateRecords(ticketId, newDescription);
					if (result > 0) {
						JOptionPane.showMessageDialog(null, "Tickets updated successfully!");
						refreshTicketView(); // Refresh view to show new ticket

					} else {
						JOptionPane.showMessageDialog(null, "Failed to update tickets. Please try again.");
					}
				}
			}
		}

		else if (e.getSource() == mnuItemDelete) {
			// Get the ticket Id from user
			String ticketIdString = JOptionPane.showInputDialog(null, "Enter the ID of the ticket to delete:");
			if (ticketIdString != null && !ticketIdString.isEmpty()) {
				int ticketId = Integer.parseInt(ticketIdString);
				// Ask user for conformation
				int confirm = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this ticket?", "Confirm Deletion", JOptionPane.YES_NO_CANCEL_OPTION);
				if (confirm == JOptionPane.YES_OPTION) {
					// Call delete method
					int result = dao.deleteRecords(ticketId);
					if (result > 0) {
						JOptionPane.showMessageDialog(null, "Ticket deleted successfully!");
						refreshTicketView(); // Refresh view to show new ticket
					} else {
						JOptionPane.showMessageDialog(null, "Failed to delete ticket. Please check the ticket ID.");
					}
				}
			}
		}
	}
}