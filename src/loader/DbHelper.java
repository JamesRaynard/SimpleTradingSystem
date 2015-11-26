package loader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;

import constants.DbConstants;

public class DbHelper {	
	protected Connection conn;
	protected static final Calendar calendarUTC = Calendar.getInstance(TimeZone
			.getTimeZone("UTC"));

	public void connect() throws Exception {
		if (conn != null) {
			return;
		}
		
		conn = DriverManager.getConnection(DbConstants.URL, DbConstants.USER, DbConstants.PASSWORD);
	}

	public void disconnect() {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			System.out.println("Could not close database connection");
		}
	}
}
