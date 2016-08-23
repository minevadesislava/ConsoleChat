package chat.db;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import chat.utils.Printer;

//TODO: code convention, refactoring(repeating code....)

public class DBManager {
	
   //  Database credentials
   private static final String USER = "root";
   private static final String PASS = "mysql5714"; 
   
   private Connection conn = null;
   private Statement stmt = null;
   
   public static void main(String[] args) throws SQLException {
	  DBManager dbm = new DBManager();
	  dbm.createDataBase();
	  dbm.createTables();	  
}
   private void createTables() throws SQLException {
	   try{
		   	 //Connecting to database
		      conn = DriverManager.getConnection("jdbc:mysql://localhost/CHAT_APPLICATION", USER, PASS);
		      
		     //Creating statement
		      stmt = conn.createStatement();
		      
		      //Creating USER table in database
		      String sql = "CREATE TABLE IF NOT EXISTS USERS " +
		      		       "(nickname VARCHAR(255) NOT NULL UNIQUE, " +
	                       " ip VARCHAR(255)," + 
		      		       "password VARCHAR(8), " + 
	                       " connectionTime TIMESTAMP, " + 
	                       " lastOnline DATE, "+
	                       "PRIMARY KEY(nickname))";
		      stmt.executeUpdate(sql);
		      
		     //Creating MESSAGE table in database
		      sql = "CREATE TABLE IF NOT EXISTS MESSAGES" + 
		    		"(ID int AUTO_INCREMENT, " +
	                " sender VARCHAR(255), " + 
	                " receiver VARCHAR(255), " + 
	                " sendingTime TIMESTAMP, " +
	                "message VARCHAR(255)," +
	                "PRIMARY KEY(ID))" +
	                "FOREIGN KEY(sender) REFERENCES users (nickname), " +
					"FOREIGN KEY(receiver) REFERENCES users (nickname))"; 
		      stmt.executeUpdate(sql);	      
		   } catch(SQLException se) {
			   System.err.println("Error while creating tables: " + Printer.getStackTrace(se));
		       throw new SQLException("Error while creating tables: ", se);
		   } finally {
			   closeResources();
		   }
}
   
   
   private void createDataBase() throws SQLException {
	   try {
		      conn = DriverManager.getConnection("jdbc:mysql://localhost", USER, PASS);
		      stmt = conn.createStatement();		      
		      String sql = "CREATE DATABASE IF NOT EXISTS CHAT_APPLICATION";
		      stmt.executeUpdate(sql);
		   } catch(SQLException se) {
			   System.err.println("Error while creating database: " + Printer.getStackTrace(se));
			   throw new SQLException("Eror while creating database", se);
		   } finally { 
			   closeResources();
		   }
   }
   
   private void insertUser(String user, String ip, String password, int connectionTime,int lastOnline, int connectedTime ) throws SQLException {
	   try {
		conn = DriverManager.getConnection("jdbc:mysql://localhost/CHAT_APPLICATION", USER, PASS);
		String updateString = "INSERT INTO CHAT_APPLICATION.USERS VALUES (?, ?, ?, ?, ?, ?);";
		java.sql.PreparedStatement ps = conn.prepareStatement(updateString);
		ps.setString(1, user);
		ps.setString(2, ip);
		ps.setString(3, password);		
		ps.setInt(4, connectionTime);
		ps.setInt(5, lastOnline);
		ps.setInt(6, connectedTime);
		ps.executeUpdate(updateString);
	} catch (SQLException se) {
		System.err.println("Error while inserting users: " + Printer.getStackTrace(se));
		throw new SQLException("Error while inserting users: ", se);
	} finally {
		closeResources();
	}
   }
   
   private void insertMessage(String sender, String receiver, String message) throws SQLException {
	   try {
		    conn = DriverManager.getConnection("jdbc:mysql://localhost/CHAT_APPLICATION", USER, PASS);
		    String updateString = "INSERT INTO MESSAGES (SENDER, RECEIVER, MESSAGE) VALUES (?, ?, ?)";
			java.sql.PreparedStatement ps = conn.prepareStatement(updateString);
			ps.setString(1, sender);
			ps.setString(2, receiver);
			ps.setString(3, message);		
			ps.executeUpdate(updateString);
		} catch (SQLException se) {
			System.err.println("Error while inserting message: " + Printer.getStackTrace(se));
			throw new SQLException("Error while inserting message: ", se);
		} finally {
			closeResources();
		}
   }
   
   protected String allHistory() throws SQLException {
	   String query = "SELECT * FROM chat_application.messages";
	   StringBuilder history= new StringBuilder();
	   try {
		    conn = DriverManager.getConnection("jdbc:mysql://localhost/CHAT_APPLICATION", USER, PASS);
	        stmt = conn.createStatement();
	        ResultSet rs = stmt.executeQuery(query);
	        while (rs.next()) {
	        	history.append("\n");
	        	history.append("sender: ");
	        	history.append(rs.getString(2));
	        	history.append(" | receiver: " + rs.getString(3));
	        	history.append(" | date: " + rs.getString(4));
	        	history.append(" | " + rs.getString(5));
	        }
	    } catch (SQLException se) {
	        System.err.println("SQLException in DBManager.allHistory(): " + Printer.getStackTrace(se));
	        throw new SQLException("SQLException in DBManager.allHistory(): ", se);
	    } finally {
	        closeResources();
	    }
   	return history.toString();
   }
   
   protected String userHistory(String user) throws SQLException {
	   String query = "SELECT * FROM chat_application.messages WHERE (sender = ? OR receiver = ?)";
	   StringBuilder userHistory = new StringBuilder();
	   java.sql.PreparedStatement ps = null;
	   try {
		    conn = DriverManager.getConnection("jdbc:mysql://localhost/CHAT_APPLICATION", USER, PASS);
	        ps = conn.prepareStatement(query);
	        ps.setString(1, user);
	        ps.setString(2, user);
	        ResultSet rs = ps.executeQuery(query);
	        userHistory = getHistoryMessages(rs);
	    } catch (SQLException se ) {
	        System.err.println("SQLException in DBManager.userHistory(): " + Printer.getStackTrace(se)); 
	        throw new SQLException("SQLException in DBManager.userHistory(): ", se);
	    } finally {
	        closeResources();
	    }
   	return userHistory.toString();
   }
   
   protected String periodAllHistory(String start, String end) throws SQLException {
	   ResultSet rs = null;
	   StringBuilder history = new StringBuilder();
	   java.sql.PreparedStatement ps = null;
	   try {
		    conn = DriverManager.getConnection("jdbc:mysql://localhost/CHAT_APPLICATION", USER, PASS);
		    String query = "SELECT * FROM chat_application.messages WHERE ( SENDINGTIME > ? AND SENDNGTIME < ?);";
	        ps = conn.prepareStatement(query);
	        ps.setString(1, start);
	        ps.setString(2, end);	        
	        rs = ps.executeQuery();
	        history = getHistoryMessages(rs);
	    } catch (SQLException se ) {
	    	 System.err.println("SQLException in DBManager.periodAllHistory(): " + Printer.getStackTrace(se)); 
		     throw new SQLException("SQLException in DBManager.periodAllHistory(): ", se);
	    } finally {
	        closeResources();
	    }
   	return history.toString();
   }
   
   protected String periodUserHistory(String user, String start, String end) throws SQLException {
	   ResultSet rs = null;
	   StringBuilder history = null;
	   java.sql.PreparedStatement ps = null;
	   try {
		    conn = DriverManager.getConnection("jdbc:mysql://localhost/CHAT_APPLICATION", USER, PASS);
		    String query = "SELECT * FROM chat_application.messages WHERE ( sendingtime > ? AND sendingTime < ?) AND (receiver = ? OR sender = ?)";
	        ps = conn.prepareStatement(query);
	        ps.setString(1, start);
	        ps.setString(2, end);	        
	        ps.setString(3, user);	        
	        ps.setString(4, user);	        
	        rs = ps.executeQuery();
	        history = getHistoryMessages(rs);
	    } catch (SQLException se ) {
	    	System.err.println("SQLException in DBManager.periodUserHistory(): " + Printer.getStackTrace(se)); 
		    throw new SQLException("SQLException in DBManager.periodUserHistory(): ", se);
	    } finally {
	       closeResources();
	    }
   	return history.toString();
   }
   
   private void closeResources() throws SQLException {
	   if (stmt != null) { try {
			stmt.close();
		} catch (SQLException se) {
			System.err.println("SQLException: " + Printer.getStackTrace(se));
			throw new SQLException("SQLException was catch while closing resources.", se);
		}
       try {
			conn.close();
		} catch (SQLException se) {
			System.err.println("SQLException: " + Printer.getStackTrace(se));
			throw new SQLException("SQLException was catch while closing resources.", se);
		}
   }
  }
   
   private StringBuilder getHistoryMessages(ResultSet rs) throws SQLException {
	   StringBuilder result = new StringBuilder();
	   while (rs.next()) {
       	result.append("\n");
       	result.append("sender: ");
       	result.append(rs.getString(2));
       	result.append(" | " + rs.getString(3));
       	result.append(" | date: " + rs.getString(4));
       	result.append(" | " + rs.getString(5));
       }
       return result;
   }
}