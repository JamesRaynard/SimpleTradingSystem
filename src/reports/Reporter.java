package reports;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import loader.DbHelper;

import org.joda.time.LocalDate;

import trades.PositionManager;
import constants.TradeConstants;

class ReporterDb extends DbHelper {
	@Override
	public void connect() throws Exception {
		if (conn != null) {
			return;
		}

		String url = "jdbc:mysql://localhost:3306/trading?zeroDateTimeBehavior=convertToNull";
		String user = "root";
		String password = "";
		conn = DriverManager.getConnection(url, user, password);
	}
	public void clear(String runName) throws Exception {
		String sql = "DELETE from reports where run_name = ?";
		PreparedStatement statement = conn.prepareStatement(sql);
		
		statement.setString(1, runName);
		statement.executeUpdate();
		statement.close();
		
		sql = "DELETE from trades";
		statement = conn.prepareStatement(sql);
		statement.executeUpdate();
		statement.close();
		
		sql = "DELETE from closed_trades";
		statement = conn.prepareStatement(sql);
		statement.executeUpdate();
		statement.close();
	}
	public void updateReport(LocalDate date, String runName, double balance,
							int newPositions, int closedPositions, int totalPositions,
							int openPositions, double totalCommission,
							double grossBalance, double netBalance) 
									throws Exception {
		String sql = "INSERT into reports(date, run_name, balance, new_positions,"
						+ " closed_positions, total_positions, open_positions,"
						+ "commission, mtm_gross, mtm_net) " +
						"values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
		PreparedStatement statement = conn.prepareStatement(sql);

		int col = 1;
		statement.setDate(col++, Date.valueOf(date.toString()));
		statement.setString(col++, runName);
		statement.setDouble(col++, balance);
		statement.setInt(col++, newPositions);
		statement.setInt(col++, closedPositions);
		statement.setInt(col++, totalPositions);
		statement.setInt(col++, openPositions);
		statement.setDouble(col++, totalCommission);
		statement.setDouble(col++, grossBalance);
		statement.setDouble(col++, netBalance);
		statement.executeUpdate();
		statement.close();
	}

	int getOpenContracts(LocalDate date) throws SQLException {
		String sql = "SELECT sum(contracts) s from trades where filled<=?";
		PreparedStatement selectStatement = conn.prepareStatement(sql);
		selectStatement.setString(1, date.toString());
		ResultSet results = selectStatement.executeQuery();
		
		int retval = 0;
		while (results.next()) {
			retval = results.getInt("s");
		}

		return retval;
	}
	int getContractsOpened(LocalDate begin, LocalDate end) throws SQLException {
		String sql = "SELECT sum(contracts) s from trades where filled between ? and ?";
		PreparedStatement selectStatement = conn.prepareStatement(sql);
		selectStatement.setString(1, begin.toString());
		selectStatement.setString(2, end.toString());
		ResultSet results = selectStatement.executeQuery();
		
		int retval = 0;
		while (results.next()) {
			retval = results.getInt("s");
		}

		return retval;
	}
	int getContractsOpenedAndClosed(LocalDate begin, LocalDate end) throws SQLException {
		String sql = "SELECT sum(contracts) s from closed_trades where filled between ? and ?";
		PreparedStatement selectStatement = conn.prepareStatement(sql);
		selectStatement.setString(1, begin.toString());
		selectStatement.setString(2, end.toString());
		ResultSet results = selectStatement.executeQuery();
		
		int retval = 0;
		while (results.next()) {
			retval = results.getInt("s");
		}

		return retval;
	}
	int getClosedContracts(LocalDate date) throws SQLException {
		String sql = "SELECT sum(contracts) s from closed_trades where closed <= ?";
		PreparedStatement selectStatement = conn.prepareStatement(sql);
		selectStatement.setString(1, date.toString());
		ResultSet results = selectStatement.executeQuery();
		
		int retval = 0;
		while (results.next()) {
			retval = results.getInt("s");
		}

		return retval;
	}
	int getContractsClosed(LocalDate begin, LocalDate end) throws SQLException {
		String sql = "SELECT sum(contracts) s from closed_trades where closed between ? and ?";
		PreparedStatement selectStatement = conn.prepareStatement(sql);
		selectStatement.setString(1, begin.toString());
		selectStatement.setString(2, end.toString());
		ResultSet results = selectStatement.executeQuery();
		
		int retval = 0;
		while (results.next()) {
			retval = results.getInt("s");
		}

		return retval;
	}
	double getRisk(LocalDate date) throws SQLException {
		String sql = "SELECT sum(profit) s from trades where filled<=?";
		PreparedStatement selectStatement = conn.prepareStatement(sql);
		selectStatement.setString(1, date.toString());
		ResultSet results = selectStatement.executeQuery();
		
		int retval = 0;
		while (results.next()) {
			retval = results.getInt("s");
		}

		return retval;
	}
	double getClosedProfits(LocalDate date) throws SQLException {
		String sql = "SELECT sum(profit) s from closed_trades where filled<=?";
		PreparedStatement selectStatement = conn.prepareStatement(sql);
		selectStatement.setString(1, date.toString());
		ResultSet results = selectStatement.executeQuery();
		
		int retval = 0;
		while (results.next()) {
			retval = results.getInt("s");
		}

		return retval;
	}
	
}
public class Reporter {
	protected String runName;
	private ReporterDb reporterDb;
	
	public Reporter(String runName) throws Exception {
		this.runName = runName;
		reporterDb = new ReporterDb();
		reporterDb.connect();
		reporterDb.clear(runName);
	}
	
	public void clear(String runName) throws Exception {
		this.reporterDb.clear(runName);
	}
	public void report(LocalDate date, PositionManager positionManager) throws Exception {
		ReporterDb reporterDb = new ReporterDb();
		reporterDb.connect();

		double closedProfits = reporterDb.getClosedProfits(date);
		double balance = reporterDb.getRisk(date) + closedProfits;
		int totalContractsToday = reporterDb.getContractsOpened(date, date)
									+reporterDb.getContractsOpenedAndClosed(date, date);
		int totalOpenContracts = reporterDb.getOpenContracts(date);
		int closedContractsToday = reporterDb.getContractsClosed(date, date);
		int totalContracts = totalOpenContracts
								+ reporterDb.getClosedContracts(date);
		double totalCommission = totalContracts * TradeConstants.commission;
		
		double tmm = Account.INSTANCE.getInitBalance() + closedProfits;
		
		if (positionManager != null) {
			tmm += positionManager.getProfits(date);
		}
		
		double nmm = tmm - totalCommission;
		Account.INSTANCE.setBalance(nmm);
		
		//final PrintStream stream = new PrintStream(System.out);
		final PrintStream stream = new PrintStream(new FileOutputStream("/home/james/output/gs_daily/" + runName, true));
		stream.println("Date\t\tBalance\t\tOpened\tClosed\tTotal\tOpen\tComm\tMTM\t\tNMTM\n");
		stream.print(date  + "\t" + balance + "\t" + totalContractsToday
							+ "\t" + closedContractsToday + "\t"
							+ totalContracts + "\t" + totalOpenContracts
							+ "\t" + totalCommission);

		stream.printf("\t%.2f\t%.2f\n", tmm, nmm);
		stream.close();
		
		reporterDb.updateReport(date, runName, balance, totalContractsToday, 
								closedContractsToday, totalContracts,
								totalOpenContracts, totalCommission, tmm, nmm);
		reporterDb.disconnect();
	}
}
