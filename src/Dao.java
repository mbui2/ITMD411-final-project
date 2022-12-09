import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
		final String createTicketsTable = "CREATE TABLE bui_tickets(ticket_id INT AUTO_INCREMENT PRIMARY KEY, ticket_issuer VARCHAR(30), ticket_description VARCHAR(200), start_date DATE, end_date DATE)";
		final String createUsersTable = "CREATE TABLE bui_users(uid INT AUTO_INCREMENT PRIMARY KEY, uname VARCHAR(30), upass VARCHAR(30), admin int)";
		final String createClosedTicketsTable = "CREATE TABLE bui_closedT(ticket_id INT PRIMARY KEY, ticket_status VARCHAR(10), ticket_closer VARCHAR(30))";

		try {

			// execute queries to create tables

			statement = getConnection().createStatement();

			statement.executeUpdate(createClosedTicketsTable);
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
		// add list of users from userlist.csv file to users table

		// variables for SQL Query inserts
		String sql;

		Statement statement;
		BufferedReader br;
		List<List<String>> array = new ArrayList<>(); // list to hold (rows & cols)

		// read data from file
		try {
			br = new BufferedReader(new FileReader(new File("userlist.csv")));

			String line;
			while ((line = br.readLine()) != null) {
				array.add(Arrays.asList(line.split(",")));
			}
		} catch (Exception e) {
			System.out.println("There was a problem loading the file");
		}

		try {

			// Setup the connection with the DB

			statement = getConnection().createStatement();

			// create loop to grab each array index containing a list of values
			// and PASS (insert) that data into your User table
			for (List<String> rowData : array) {

				sql = "insert into bui_users(uname,upass,admin) " + "values('" + rowData.get(0) + "'," + " '" + rowData.get(1) + "','" + rowData.get(2) + "');";
				statement.executeUpdate(sql);
			}
			System.out.println("Inserts completed in the given database...");

			// close statement object
			statement.close();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public int insertRecords(String ticketName, String ticketDesc, String startDate) {
		int id = 0;
		try {
			statement = getConnection().createStatement();
			statement.executeUpdate("Insert into bui_tickets" + "(ticket_issuer, ticket_description, start_date) values(" + " '"
					+ ticketName + "','" + ticketDesc + "','" + startDate + "')", Statement.RETURN_GENERATED_KEYS);

			// retrieve ticket id number newly auto generated upon record insertion
			ResultSet resultSet = null;
			resultSet = statement.getGeneratedKeys();
			if (resultSet.next()) {
				// retrieve first field in table
				id = resultSet.getInt(1);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return id;

	}

	public ResultSet readRecords(boolean isAdmin, String user) { //passing in isAdmin boolean to see if we are dealing with an administrator, if not we will use the string username

		ResultSet results = null;
		try {
			statement = getConnection().createStatement();
			if(isAdmin) //if they're an admin, they can view any ticket, otherwise they can only view their own
				results = statement.executeQuery("SELECT * FROM bui_tickets");
			else
				results = statement.executeQuery("SELECT * FROM bui_tickets WHERE ticket_issuer = '" + user + "'");
			
			//connect.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return results;
	}
	
	public ResultSet ticketByNum(boolean isAdmin, int ticketNum, String user) {
		
		ResultSet results = null;
		try {
			statement = getConnection().createStatement();
			if(isAdmin) //this method will check if you are an admin, at which point it will return to you a ticket with that number regardless of who posted it
				results = statement.executeQuery("SELECT * FROM bui_tickets WHERE ticket_id = " + ticketNum);
			else //if you aren't an admin then you can only see the ticket if it belongs to you
				results = statement.executeQuery("SELECT * FROM bui_tickets WHERE ticket_id = '" + ticketNum + "' AND ticket_issuer = '" + user + "'");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return results;
	}
	// continue coding for updateRecords implementation

	// continue coding for deleteRecords implementation
	
	public void deleteRecords(int ticketNum) {
		
		try { //delete the record, don't need to check for admin priviliges because this is only visible to admins
			statement = connect.createStatement();
			int deleted = statement.executeUpdate("DELETE from bui_tickets WHERE ticket_id = " + ticketNum);
			
			if (deleted != 0)
				System.out.println("Ticket #" + ticketNum+ " Deleted");
			else
				System.out.println("No Ticket Deleted. Ticket #" + ticketNum + " does not exist");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	
	
	public void closeTicket(int ticketNum, String endDate) {
		
		try {
			statement = getConnection().createStatement();
			
			int updated = statement.executeUpdate("UPDATE bui_tickets SET end_date = '" + endDate + "' WHERE ticket_id = '" + ticketNum + "'");
			
			if (updated != 0)
				System.out.println("Ticket #" + ticketNum + " closed");
			else
				System.out.println("No Ticket Closed");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateDescription(boolean isAdmin, int ticketNum, String user, String desc) {
		
		try {
			statement = getConnection().createStatement();
			int updated;
			if (isAdmin)
				updated = statement.executeUpdate("UPDATE bui_tickets SET ticket_description = '" + desc + "' WHERE ticket_id = " + ticketNum);
			else 
				updated = statement.executeUpdate("UPDATE bui_tickets SET ticket_description = '" + desc + "' WHERE ticket_issuer = '" + user + "', AND ticket_num = " + ticketNum);
			
			if (updated != 0)
				System.out.println("Ticket #" + ticketNum + " updated. New Description: " + desc);
			else
				System.out.println("No Ticket Updated");
			
				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//For entering closed tickets into the closed table
	public void toClosedTable(int ticketNum, String admin) {
		try {
			statement = getConnection().createStatement();
			statement.executeUpdate("Insert into bui_closedT(ticket_id, ticket_status, ticket_closer) values('" + ticketNum + "', 'closed', '" + admin + "')");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	//For viewing closed tickets
	public ResultSet viewClosedTable() {
		ResultSet results = null;
		try {
			statement = getConnection().createStatement();
			results = statement.executeQuery("SELECT * FROM bui_closedT");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return results;
	}
}
