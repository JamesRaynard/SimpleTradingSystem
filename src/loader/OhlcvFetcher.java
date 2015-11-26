package loader;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.joda.time.LocalDate;

import constants.OhlcvConstants;

public class OhlcvFetcher extends DbHelper {	
	public Ohlcv fetch(LocalDate date) throws SQLException {
		Ohlcv ohlcv = new Ohlcv();
		
		String sql = "SELECT open, high, low, close, tr from ohlcv where date=?";
		PreparedStatement selectStatement = conn.prepareStatement(sql);
		selectStatement.setDate(1, Date.valueOf(date.toString()));
		ResultSet results = selectStatement.executeQuery();
		
		while (results.next()) {
			ohlcv.setDate(new LocalDate(date));
			ohlcv.setOpen(results.getDouble("open"));
			ohlcv.setHigh(results.getDouble("high"));
			ohlcv.setLow(results.getDouble("low"));
			ohlcv.setClose(results.getDouble("close"));
			ohlcv.setTr(results.getDouble("tr"));
		}

		results.close();
		selectStatement.close();
		return ohlcv;
	}
		
	public Ohlcv fetchPrevious(LocalDate date) throws SQLException {
		String sql = "SELECT open, high, low, close, tr from "
				+ "ohlcv where date <= ? order by date desc LIMIT 1,1";
		PreparedStatement selectStatement = conn.prepareStatement(sql);
		selectStatement.setString(1, date.toString());
		ResultSet results = selectStatement.executeQuery();
		Ohlcv ohlcv = new Ohlcv();
		
		while (results.next()) {
			ohlcv.setDate(new LocalDate(date));
			ohlcv.setOpen(results.getDouble("open"));
			ohlcv.setHigh(results.getDouble("high"));
			ohlcv.setLow(results.getDouble("low"));
			ohlcv.setClose(results.getDouble("close"));
			ohlcv.setTr(results.getDouble("tr"));
		}

		results.close();
		selectStatement.close();
		return ohlcv;
	}
	
	public Ohlcv fetchLatest(LocalDate date) throws SQLException {
		String sql = "SELECT open, high, low, close, tr from "
				+ " ohlcv where date <= ? order by date desc LIMIT 0,1";
		PreparedStatement selectStatement = conn.prepareStatement(sql);
		selectStatement.setString(1, date.toString());
		ResultSet results = selectStatement.executeQuery();
		Ohlcv ohlcv = new Ohlcv();
		
		while (results.next()) {
			ohlcv.setDate(new LocalDate(date));
			ohlcv.setOpen(results.getDouble("open"));
			ohlcv.setHigh(results.getDouble("high"));
			ohlcv.setLow(results.getDouble("low"));
			ohlcv.setClose(results.getDouble("close"));
			ohlcv.setTr(results.getDouble("tr"));
		}

		results.close();
		selectStatement.close();
		return ohlcv;
	}		

	public void fetchDates(List<LocalDate> dates) throws SQLException {				
		String sql = "SELECT distinct date from ohlcv";
		PreparedStatement selectStatement = conn.prepareStatement(sql);
		ResultSet results = selectStatement.executeQuery();
		
		while (results.next()) {
			Date date = results.getDate("date", calendarUTC);
			dates.add(new LocalDate(date));
		}
	}	
}
