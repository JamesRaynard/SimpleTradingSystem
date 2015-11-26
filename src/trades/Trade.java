package trades;

import loader.Ohlcv;
import loader.OhlcvFetcher;

import org.joda.time.LocalDate;

import constants.OhlcvConstants;
import constants.TradeConstants;

public class Trade {
	protected char orderType;
	protected LocalDate filled;
	protected double target;
	protected int unit;
	protected double initialStop;
	protected String type;
	protected double tickValue;
	protected double stop;
	protected double fillPrice;
	protected double profit;
	protected double premium;
	
	public Trade(char orderType, LocalDate date, double target, int unit, double initialStop, String type,
			double tickValue) {
		this.orderType = orderType;
		this.filled = date;
		this.target = target;
		this.unit = unit;
		this.initialStop = initialStop;
		this.type = type;
		this.tickValue = tickValue;
		this.stop = initialStop;
		this.fillPrice = target;
		this.profit = 0.0;
		this.premium = 0.0;
	}

	public Position perform(LocalDate minDate) throws Exception {
		OhlcvFetcher fetcher = new OhlcvFetcher();
		fetcher.connect();
		
		Ohlcv ohlcv = fetcher.fetchLatest(this.filled);
		fetcher.disconnect();
		
		if (minDate.compareTo(ohlcv.getDate()) > 0) {
			return null;
		}
		
		if (this.fill(ohlcv)) {
			return new Position(tickValue, unit, target, type, filled,
				fillPrice, initialStop, stop, profit, premium);
		}
		else
			return null;
	}
	
	protected boolean fill(Ohlcv ohlcv) {
		double open = ohlcv.getOpen();
		double high = ohlcv.getHigh();
		double low = ohlcv.getLow();
		
		boolean traded = false;
		
		if (this.orderType == TradeConstants.BUY) {
			if (target == OhlcvConstants.LowestLow || open >= target) {
				this.fillPrice = open;
				this.target = open;
				traded = true;
			} else if (high > target) {
				this.fillPrice = target;				
				traded = true;
			}
			if (traded) {
				this.filled = ohlcv.getDate();
				this.stop = this.fillPrice - this.stop;
				this.initialStop = this.stop;
			}
		} 
		else if (this.orderType == TradeConstants.SELL) {
			if (target == OhlcvConstants.HighestHigh || open <= target) {
				this.fillPrice = open;
				this.target = open;
				traded = true;
			} else if (low < target) {
				this.fillPrice = target;
				traded = true;
			}
			if (traded) {
				this.filled = ohlcv.getDate();
				this.stop = this.fillPrice + this.stop;
				this.initialStop = this.stop;
			}
		}
		return traded;
	}
	
	public LocalDate getFilled() {
		return this.filled;
	}
}
