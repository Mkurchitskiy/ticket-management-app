package javaapplication1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Dao {
	// instance fields
	static Connection connect = null;
	Statement statement = null;

	// constructor
	public Dao() {
	  
	}

	public Connection getConnection() {
		// Setup the connection with the DB
		try {
			connect = DriverManager
					.getConnection("jdbc:mysql://www.papademas.net:3307/tickets?autoReconnect=true&useSSL=false"
							+ "&user=fp411&password=411");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return connect;
	}

	// CRUD implementation

	public void createTables() {
		
		// variables for SQL Query table creations
		final String createTicketsTable = "CREATE TABLE mkurc_tickets(ticket_id INT AUTO_INCREMENT PRIMARY KEY, ticket_issuer VARCHAR(30), ticket_description VARCHAR(200), start_date DATE, end_date DATE)";
    	final String createUsersTable = "CREATE TABLE mkurc_users(uid INT AUTO_INCREMENT PRIMARY KEY, uname VARCHAR(30), upass VARCHAR(30), admin int)";

		try {
			// execute queries to create tables

			statement = getConnection().createStatement();
			statement.executeUpdate(createTicketsTable);
			statement.executeUpdate(createUsersTable);
			System.out.println("Created tables in given database...");

			// end create table
			// close connection/statement object
			statement.close();
			connect.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		// add users to user table
		addUsers();
	}

	public void addUsers() {
		// Read user data from the file
		List<List<String>> users = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(new File("./userlist.csv")))) {
			String line;
			while ((line = br.readLine()) != null) {
				users.add(Arrays.asList(line.split(",")));
			}
		} catch (Exception e) {
			System.out.println("There was a problem loading the file: " + e.getMessage());
			return; // Exit if there's an error reading the file
		}

		// Prepare SQL statements for checking, updating, and inserting
		String queryCheck = "SELECT count(*) FROM mkurc_users WHERE uname = ?";
		String updateSql = "UPDATE mkurc_users SET upass = ?, admin = ? WHERE uname = ?";
		String insertSql = "INSERT INTO mkurc_users (uname, upass, admin) VALUES (?, ?, ?)";

		try (Connection conn = getConnection();
			PreparedStatement stmtCheck = conn.prepareStatement(queryCheck);
			PreparedStatement stmtUpdate = conn.prepareStatement(updateSql);
			PreparedStatement stmtInsert = conn.prepareStatement(insertSql)) {

			for (List<String> user : users) {
				String uname = user.get(0);
				String upass = user.get(1);
				int admin = Integer.parseInt(user.get(2));

				// Check if user exists
				stmtCheck.setString(1, uname);
				ResultSet rs = stmtCheck.executeQuery();
				rs.next();
				int count = rs.getInt(1);

				if (count > 0) {
					// User exists, update
					stmtUpdate.setString(1, upass);
					stmtUpdate.setInt(2, admin);
					stmtUpdate.setString(3, uname);
					stmtUpdate.executeUpdate();
				} else {
					// User does not exist, insert
					stmtInsert.setString(1, uname);
					stmtInsert.setString(2, upass);
					stmtInsert.setInt(3, admin);
					stmtInsert.executeUpdate();
				}
			}
			System.out.println("User updates and inserts completed in the given database.");
		} catch (SQLException e) {
			System.out.println("SQL Error during user update/insert: " + e.getMessage());
			e.printStackTrace();
		}
	}


	public int insertRecords(String ticketName, String ticketDesc, LocalDate startDate, LocalDate endDate) {
		int id = 0;
		String sql = "INSERT INTO mkurc_tickets (ticket_issuer, ticket_description, start_date, end_date) VALUES (?, ?, ?, ?)";
		try (PreparedStatement ps = connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			ps.setString(1, ticketName);
			ps.setString(2, ticketDesc);
			ps.setDate(3, Date.valueOf(startDate));
			ps.setDate(4, Date.valueOf(endDate));
			ps.executeUpdate();
	
			try (ResultSet resultSet = ps.getGeneratedKeys()) {
				if (resultSet.next()) {
					id = resultSet.getInt(1);
				}
			}
		} catch (SQLException e) {
			System.err.println("Error inserting records: " + e.getMessage());
		}
		return id;
	}

	public ResultSet readRecords(String username, boolean isAdmin) {
		ResultSet results = null;
		try {
			String query;
			if (isAdmin) {
				query = "SELECT * FROM mkurc_tickets";
			} else {
				query = "SELECT * FROM mkurc_tickets WHERE ticket_issuer = ?";
				System.out.println("Querying tickets for user: " + username);
			}
			PreparedStatement ps = connect.prepareStatement(query);
			if (!isAdmin) {
				ps.setString(1, username);
				System.out.println("Prepared statement: " + ps);
			}
			results = ps.executeQuery();
			if(!results.isBeforeFirst()) {
				System.out.println("No data fetched");
				System.out.println("No data fetched for user: " + username);
			} else {
				System.out.println("Data fetched for user: " + username);
			}
		} catch (SQLException e) {
			System.out.println("Error fetching tickets: " + e.getMessage());
			e.printStackTrace();
		}
		return results;
	}

	// continue coding for updateRecords implementation
	public int updateRecords(int ticketID, String ticketDesc, LocalDate startDate, LocalDate endDate) {
		try {
			String sql = "UPDATE mkurc_tickets SET ticket_description = ?, start_date = ?, end_date = ? WHERE ticket_id = ?";
			PreparedStatement ps = getConnection().prepareStatement(sql);
			ps.setString(1, ticketDesc);
			ps.setDate(2, Date.valueOf(startDate));
			ps.setDate(3, Date.valueOf(endDate));
			ps.setInt(4, ticketID);
			return ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	// continue coding for deleteRecords implementation
	public int deleteRecords(int ticketID) {
		try {
			String sql = "DELETE FROM mkurc_tickets WHERE ticket_id = ?;";
			PreparedStatement ps = getConnection().prepareStatement(sql);
			ps.setInt(1, ticketID);
			return ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public int closeRecords(int ticketID) {
		int result = 0;
		String sql = "UPDATE mkurc_tickets SET is_closed = TRUE WHERE ticketID = ?";
		try (PreparedStatement ps = getConnection().prepareStatement(sql)){
			result = ps.executeUpdate();
			if (result > 0) {
				System.out.println("Ticker " + ticketID + "has been closed successfully.");
			} else {
				System.out.println("Failed to close the ticket. No ticker found with ID: " + ticketID);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
		return result;
	}
}
