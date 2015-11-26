package loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

class OhlcvPopulatorDbHelper extends DbHelper {
	public void clear() throws Exception {  	
    	String ohlcvDelete = "DELETE from ohlcv";
    	PreparedStatement ohlcvDeleteStatement = conn.prepareStatement(ohlcvDelete);
    	ohlcvDeleteStatement.executeUpdate();
    	ohlcvDeleteStatement.close();
    }
    
    public void populateOhlcv(List<Ohlcv> ohlcvList) throws SQLException {
    	String ohlcvSql = "INSERT into ohlcv(date, open, high, low, close, atr) " +
    							"values(?, ?, ?, ?, ?, ?)";
    	PreparedStatement ohlcvStatement = conn.prepareStatement(ohlcvSql);
    	
    	for (Ohlcv ohlcv: ohlcvList) {
    		int col = 1;
    		ohlcvStatement.setDate(col++, Date.valueOf(ohlcv.getDate().toString()));
    		ohlcvStatement.setDouble(col++, ohlcv.getOpen());
    		ohlcvStatement.setDouble(col++, ohlcv.getHigh());
    		ohlcvStatement.setDouble(col++, ohlcv.getLow());
    		ohlcvStatement.setDouble(col++, ohlcv.getClose());
    		ohlcvStatement.setDouble(col++, ohlcv.getTr());
    		ohlcvStatement.executeUpdate();
    	}
    	
		ohlcvStatement.close();
    }
}

public class OhlcvLoader {
	private String filename;
	private OhlcvPopulatorDbHelper db;
	
	public OhlcvLoader(String filename) {
		this.filename = filename;
		this.db = new OhlcvPopulatorDbHelper();
	}
	
	public void load() throws Exception {
		File file;
		FileReader fr = null;
		BufferedReader br = null;
		
		List<Ohlcv> list = new ArrayList<Ohlcv>();
				
		try {
			db.connect();
			db.clear();
			
			file = new File(filename);
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			

			String line = "";
			while ((line = br.readLine()) != null) {
				String[] columns = line.split(",");
				
				LocalDate date = new LocalDate(columns[0]);
				double open = Double.parseDouble(columns[1]);
				double high = Double.parseDouble(columns[2]);
				double low = Double.parseDouble(columns[3]);
				double close = Double.parseDouble(columns[4]);
				double tr = high - low;
				
				Ohlcv ohlcv = new Ohlcv();
				ohlcv.setDate(date);
				ohlcv.setOpen(open);
				ohlcv.setHigh(high);
				ohlcv.setLow(low);
				ohlcv.setClose(close);
				ohlcv.setTr(tr);
				
				list.add(ohlcv);
			}
			
			db.populateOhlcv(list);
		} catch (IOException e) {
			throw new Exception("Could not read from " + filename + ": " + e.getMessage());
		}
		finally {
			if (fr != null) {
				fr.close();
			}
			if (br != null) {
				br.close();
			}
		}
	}
	
	/*
	 * Program to load "filename" in OHLCV format into a table "ohlcv"
	 * in an MySQl database "simple".
	 * 
	 * The first part of "filename" (up to the first ".") will be
	 * used as the symbol name. e.g. ixic.ohlcv -> ixic
	 * 
	 * First argument must be full path to input file in format
	 * YYYY-MM-DD,open,high,low,close
	 */
	public static void main(String[] args) throws Exception {
		String filename = "";
		
		if (args.length > 0) {
			filename = args[0];
			if (filename == null || filename.equals("")) {
				throw new Exception("No file specfied in program arguments");
			} else {
				//System.out.println("Filename=" + filename);
			}
		}
		else {
			throw new Exception("No arguments");
		}

		// Keep this for reports etc
		String[] names = filename.split("/");
		String name=names[names.length-1];
		String[] parts = name.split("\\.");
		String symbol = parts[0];
		
		OhlcvLoader loader = new OhlcvLoader(filename);
		loader.load();
	}
}
