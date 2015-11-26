package trades;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import loader.DbHelper;
import loader.Ohlcv;
import loader.OhlcvFetcher;

import org.joda.time.LocalDate;

import constants.OhlcvConstants;
import constants.TimeConstants;
import constants.TradeConstants;

class PositionDb extends DbHelper {
	public void saveClosedPositions(String orderType,
						List<ClosedPosition> closedList) throws SQLException {
		String sql = "INSERT into trading.closed_trades(symbol, tick_value, contracts, "
				+ " order_type, placed, price, type, filled, fill_price, initial_stop, "
				+ " stop, closed, close_price, closure_reason, profit, premium) "
				+ "values(?, ?, ?, "  
				+ "?, ?, ?, ?, ?, ?, ?, "
				+ "?, ?, ?, ?, ?, ?)";
		PreparedStatement statement = conn.prepareStatement(sql);

		for (ClosedPosition position: closedList) {
			int col = 1;
			statement.setString(col++, "XXX");
			statement.setDouble(col++, position.getTickValue());
			statement.setInt(col++, position.getContracts());
			
			statement.setString(col++, orderType);
			statement.setDate(col++, new Date(0));
			statement.setDouble(col++, position.getTarget());
			statement.setString(col++, position.getType());
			statement.setDate(col++, Date.valueOf(position.getFilled().toString()));
			statement.setDouble(col++, position.getFillPrice());
			statement.setDouble(col++, position.getInitialStop());
			
			statement.setDouble(col++, position.getStop());
			statement.setDate(col++, Date.valueOf(position.getClosed().toString()));
			statement.setDouble(col++, position.getClosePrice());
			statement.setString(col++, position.getClosureReason());
			statement.setDouble(col++, position.getProfit());
			statement.setDouble(col++, position.getPremium());

			statement.executeUpdate();
			System.out.println(position);
		}

		statement.close();
	}
	
	public void savePositions(String orderType,
			List<Position> positionList) throws SQLException {
		String sql = "INSERT into trading.trades(symbol, tick_value, contracts, "
				+ " order_type, placed, price, type, filled, fill_price, initial_stop, "
				+ " stop, profit, premium) "
				+ "values(?, ?, ?, "
				+ "?, ?, ?, ?, ?, ?, ?, "
				+ "?, ?, ?)";
		PreparedStatement statement = conn.prepareStatement(sql);

		for (Position position : positionList) {
			int col = 1;
			statement.setString(col++, "XXX");
			statement.setDouble(col++, position.getTickValue());
			statement.setInt(col++, position.getContracts());
			
			statement.setString(col++, orderType);
			statement.setDate(col++, new Date(0));
			statement.setDouble(col++, position.getTarget());
			statement.setString(col++, position.getType());
			statement.setDate(col++, Date.valueOf(position.getFilled().toString()));
			statement.setDouble(col++, position.getFillPrice());
			statement.setDouble(col++, position.getInitialStop());
			
			statement.setDouble(col++, position.getStop());
			statement.setDouble(col++, position.getProfit());
			statement.setDouble(col++, position.getPremium());

			statement.executeUpdate();
		}

		statement.close();
	}
	
	public void clearPositions(String orderType,
							List<Position> positionList) throws SQLException {
		String sql = "DELETE from trading.trades where "
					+ "order_type = ? and type = ? and filled = ?";

		PreparedStatement statement = conn.prepareStatement(sql);
		
		for (Position position : positionList) {
			int col = 1;
			statement.setString(col++, orderType);
			statement.setString(col++, position.getType());
			statement.setDate(col++, Date.valueOf(position.getFilled().toString()));
			statement.executeUpdate();
		}
		statement.close();
	}
	
	public void clearAll() throws SQLException {
		String sql = "DELETE from trading.trades";

		PreparedStatement statement = conn.prepareStatement(sql);	
		statement.executeUpdate();
		statement.close();
		
		sql = "DELETE from trading.closed_trades";
		statement = conn.prepareStatement(sql);	
		statement.executeUpdate();
		statement.close();
	}
}

public class PositionManager {
	protected List<Position> positions;
	protected char orderType;
	
	public PositionManager(Position position) {
		this.positions = new ArrayList<Position>();
		positions.add(position);
		orderType = TradeConstants.NONE;
	}

	public void closeAllPositions(LocalDate date, double closePrice, String reason,
								List<ClosedPosition> closedPositions) throws Exception {
		if (closePrice != OhlcvConstants.LowestLow) {
			for (Position position: this.positions) {
				ClosedPosition closedPosition = new ClosedPosition(position);
				closedPosition.setClosed(date);
				closedPosition.setClosePrice(closePrice);
				closedPosition.setClosureReason(reason);
				closedPositions.add(closedPosition);
			}
			clearAll();
		}
	}
	
	public ClosedPosition closePosition(Position position, LocalDate date,
								double closePrice, String reason) {
		if (closePrice != OhlcvConstants.LowestLow) {
			ClosedPosition closedPosition = new ClosedPosition(position);
			closedPosition.setClosed(date);
			closedPosition.setClosePrice(closePrice);
			closedPosition.setClosureReason(reason);	
			return closedPosition;
		}
		return null;
	}
	public void clearAll() throws Exception {
		PositionDb positionDb = new PositionDb();
		positionDb.connect();
		positionDb.clearPositions("" + this.orderType, positions);
		positions.clear();
		positionDb.disconnect();
	}
	public boolean isBuy() {
		return orderType == TradeConstants.BUY;
	}
	public boolean isSell() {
		return orderType == TradeConstants.SELL;
	}
	public void addBuy(Position position) {
		orderType = TradeConstants.BUY;
		positions.add(position);
	}
	
	public void addSell(Position position) {
		orderType = TradeConstants.SELL;
		positions.add(position);
	}
	
	public boolean hasPosition() {
		return orderType != TradeConstants.NONE;
	}
	
	public void setChecked() {
		for (Position position: positions) {
			position.setChecked(true);
		}
	}
	public void setUnchecked() {
		for (Position position: positions) {
			position.setChecked(false);
		}
	}
	public double getLatestPrice() {
		if (this.getCount() > 0) {
			return this.positions.get(positions.size()-1).getFillPrice();
		}
		return OhlcvConstants.LowestLow;
	}
	public void setStop(double newStop) {
		if (orderType == TradeConstants.BUY) {
			for (Position position: positions) {
				if (newStop > position.getStop()) {
					position.setStop(newStop);
				}
			}
		}
		else if (orderType == TradeConstants.SELL) {
			for (Position position: positions) {
				if (newStop < position.getStop()) {
					position.setStop(newStop);
				}
			}
		}
	}
	public boolean checkStop(Position position, LocalDate date, Ohlcv ohlcv, 
			List<ClosedPosition> closedPositions, 
			char orderType) throws Exception {
		boolean stopHit = false;
		LocalDate lastDate = TimeConstants.MINLDATE;
		
		if (position.isChecked()) {
			return false;
		}
		
		if (date.compareTo(position.getFilled()) <= 0) {
			return false;
		}
		
		if (orderType == TradeConstants.BUY) {	
			ClosedPosition closedTrade = null;
			
			double stop = position.getStop();
			
			if (stop > ohlcv.getLow()) {
				double closedPrice = OhlcvConstants.LowestLow;
				String closedReason = "";
				
				if (stop > ohlcv.getOpen()) {
					closedPrice = ohlcv.getOpen();
					closedReason = "Gap";
				}
				else {
					closedPrice = stop;
					closedReason = "Stop hit";
				}
				
				closedTrade = this.closePosition(position, date,
											closedPrice, closedReason);
				stopHit = true;
				
				if (date.compareTo(lastDate) > 0) {
					lastDate = date;
				}
			}
			
			if (stopHit) {
				closedPositions.add(closedTrade);
			}
		}
		else if (orderType == TradeConstants.SELL) {
			ClosedPosition closedPosition = null;
			
			double stop = position.getStop();
			
			if (stop < ohlcv.getHigh()) {
				double closedPrice = OhlcvConstants.LowestLow;
				String closedReason = "";
				
				if (stop < ohlcv.getOpen()) {
					closedPrice = ohlcv.getOpen();
					closedReason = "Gap";
				}
				else {
					closedPrice = stop;
					closedReason = "Stop hit";
				}
				
				closedPosition = this.closePosition(position, date,
											closedPrice, closedReason);
				stopHit = true;
				
				if (date.compareTo(lastDate) > 0) {
					lastDate = date;
				}
			}
			
			if (stopHit) {
				closedPositions.add(closedPosition);
			}
		}
		return stopHit;
	}
	public boolean checkStops(LocalDate date, Ohlcv ohlcv, 
								List<ClosedPosition> closedPositions) throws Exception {
		List<Position> openPositions = new ArrayList<>(positions.size());
		boolean stopHit = false;
	
		for (Position position: positions) {
			if (checkStop(position, date, ohlcv, closedPositions, 
										orderType)) {
				stopHit = true;
				openPositions.add(position);
			}
			position.setChecked(true);
		}

		if (stopHit) {
			this.finalizeProfits(closedPositions);

			if (openPositions.size() != 0) {
				this.clearAll();
				this.positions = openPositions;
				this.savePositions();
			}
		}
		return stopHit;
	}
	
	public void updateProfits() {
		for (Position position: positions) {
			position.setProfit(position.calculateProfit(this.orderType, position.getStop()));
		}
	}
	
	protected void finalizeProfits(List<ClosedPosition> positions) {
		for (ClosedPosition position: positions) {
			position.setProfit(position.calculateProfit(this.orderType,
					position.getClosePrice()));
		}
	}
	
	public void savePositions() throws Exception {
		String orderType = "" + this.orderType;
		
		PositionDb positionDb = new PositionDb();
		positionDb.connect();
		positionDb.clearPositions(orderType, this.positions);
		this.updateProfits();
		positionDb.savePositions(orderType, this.positions);
		positionDb.disconnect();
	}
	
	public void saveClosedPositions(List<ClosedPosition> closedList) throws Exception {
		String orderType = "" + this.orderType;
		
		PositionDb positionDb = new PositionDb();
		positionDb.connect();
		positionDb.clearPositions(orderType, new ArrayList<Position>(closedList));
		positionDb.saveClosedPositions(orderType, closedList);
		if (positions.size() == 0) {
			this.orderType = TradeConstants.NONE;
		}
		positionDb.disconnect();
	}
	public static void clearDb() throws Exception {
		PositionDb positionDb = new PositionDb();
		positionDb.connect();
		positionDb.clearAll();
		positionDb.disconnect();
	}
	public int getCount() {
		return positions.size();
	}
	
	public int getContracts() {
		int contracts = 0;
		
		for (Position position: positions) {
			contracts += position.getContracts();
		}
		return contracts;
	}
		
	public double getProfits(LocalDate date) throws Exception {
		double profits = 0.0;
		
		for (Position position: positions) {
			OhlcvFetcher ohlcvFetcher = new OhlcvFetcher();
			ohlcvFetcher.connect();
			Ohlcv ohlcv = ohlcvFetcher.fetchLatest(date);
			ohlcvFetcher.disconnect();
			profits += position.calculateProfit(orderType, ohlcv.getClose());
		}
		return profits;
	}
	
	public double getFillPrice() {
		if (positions.get(0) != null) {
			return positions.get(0).getFillPrice();
		}
		return OhlcvConstants.LowestLow;
	}
	public void setOrderType(char orderType) {
		this.orderType = orderType;
	}
	
	public char getOrderType() {
		return orderType;
	}

	public String toString() {
		String retval = "" + orderType;
		retval += "\n";
		
		for (Position position: positions) {
			retval += position.toString();
			retval += "\n";
		}
		return retval;
	}
}
