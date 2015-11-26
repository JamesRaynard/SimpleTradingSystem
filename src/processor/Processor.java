package processor;

import java.util.ArrayList;
import java.util.List;

import loader.Ohlcv;
import loader.OhlcvFetcher;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import reports.Account;
import reports.Reporter;
import trades.ClosedPosition;
import trades.Position;
import trades.PositionManager;
import trades.Trade;
import averages.ExpMovingAverage;
import averages.MovingAverage;
import constants.MovingAveConstants;
import constants.OhlcvConstants;
import constants.TradeConstants;
import criteria.MovingAverageCriteria;

public class Processor {
	protected Reporter reporter;
	protected PositionManager positionManager;

	boolean toClose;
	boolean toBuy;
	boolean toSell;
	
	MovingAverage slowMa;
	MovingAverage fastMa;
	MovingAverage atr;
	
	public Processor(Reporter reporter, MovingAverage slowMa,
				MovingAverage fastMa, MovingAverage atr) throws Exception {
		this.reporter = reporter;
		this.positionManager = null;

		toClose = toBuy = toSell = false;
		
		this.slowMa = slowMa;
		this.fastMa = fastMa;
		this.atr = atr;
	}
	
	public PositionManager buy(double target, LocalDate date, Ohlcv ohlcv) 
								throws Exception {
		char orderType = TradeConstants.BUY;
		String type = "" + orderType;

		double stop = atr.getCurrent() * MovingAveConstants.ATR_FACTOR;
		int unit = 1;

		Trade trade = new Trade(orderType, date, target, 
									unit, stop, type, TradeConstants.TICKVALUE);
		Position position = trade.perform(date);

		if (position == null) {
			return null;
		}

		this.positionManager = new PositionManager(position);
		positionManager.setOrderType(orderType);
		positionManager.updateProfits();

		System.out.println(unit + ", " 
				+ positionManager.getOrderType()
				+ " at " + position.getTarget()
				+ " on " + position.getFilled());

		return positionManager;
	}
	
	public PositionManager sell(double target, LocalDate date, Ohlcv ohlcv)
								throws Exception {
		// Force sell at open
		if (target == OhlcvConstants.LowestLow) {
			target = OhlcvConstants.HighestHigh;
		}
		char orderType = TradeConstants.SELL;
		String type = "" + orderType;

		double stop = atr.getCurrent() * MovingAveConstants.ATR_FACTOR;
		int unit = 1;

		Trade trade = new Trade(orderType, date, target, 
				unit, stop, type, TradeConstants.TICKVALUE);
		Position position = trade.perform(date);

		if (position == null) {
			return null;
		}

		this.positionManager = new PositionManager(position);
		positionManager.setOrderType(orderType);
		positionManager.updateProfits();

		System.out.println(unit + ", " 
				+ positionManager.getOrderType()
				+ " at " + position.getTarget()
				+ " on " + position.getFilled());

		return positionManager;
	}

	
	public void closePosition(LocalDate date, double closePrice,
			String reason) throws Exception {
		if (positionManager != null) {
			int positionSize = positionManager.getCount();
			List<ClosedPosition> closedPositions = new ArrayList<ClosedPosition>(positionSize);

			positionManager.closeAllPositions(date, closePrice, reason, closedPositions);
			positionManager.saveClosedPositions(closedPositions);

			if (!positionManager.hasPosition()) {
				positionManager = null;
			}
		}
	}
	
	public boolean checkStops(LocalDate date, Ohlcv ohlcv) throws Exception {
		boolean stopHit = false;

		if (positionManager != null) {
			int positionSize = positionManager.getCount();
			List<ClosedPosition> closedPositions = new ArrayList<ClosedPosition>(positionSize);
		
			stopHit = positionManager.checkStops(date, ohlcv, closedPositions);
			
			if (stopHit) {
				positionManager.saveClosedPositions(closedPositions);
				
				if (positionManager.hasPosition()) {
					positionManager = null;
				}
				else {
					positionManager.updateProfits();
				}
			}
		}
		
		toClose = false;
		return stopHit;
	}
	
	public void checkSignals(LocalDate date, Ohlcv ohlcv) throws Exception {
		if (toClose) {
			closePosition(date, ohlcv.getOpen(), "Reversed");
			toClose = false;
		}
		if (toBuy) {
			this.buy(ohlcv.getOpen(), date, ohlcv);
			toBuy = false;
		}
		else if (toSell) {
			this.sell(ohlcv.getOpen(), date, ohlcv);
			toSell = false;
		}
	}
	
	public void updateMas(LocalDate date, Ohlcv ohlcv) {
		slowMa.update(ohlcv.getClose());
		fastMa.update(ohlcv.getClose());
		atr.update(ohlcv.getTr());
	}	
	
	public void updateSignals(LocalDate date, Ohlcv ohlcv) {
		if (MovingAverageCriteria.isBuySignal(slowMa.getPrevious(),
				fastMa.getPrevious(), slowMa.getCurrent(), fastMa.getCurrent())) {
			if (positionManager != null) {
				toClose = true;
			}

			toBuy = true;
		}

		if (MovingAverageCriteria.isSellSignal(slowMa.getPrevious(),
				fastMa.getPrevious(), slowMa.getCurrent(), fastMa.getCurrent())) {
			if (positionManager != null) {
				toClose = true;
			}

			toSell = true;
		}
	}
	
	public void updateStops(LocalDate date, Ohlcv ohlcv) throws Exception {
		if (positionManager != null) {
			if (positionManager.isBuy()) {
				double stop = ohlcv.getLow() - atr.getCurrent();
				positionManager.setStop(stop);
			}
			else if (positionManager.isSell()) {
				double stop = ohlcv.getHigh() + atr.getCurrent();
				positionManager.setStop(stop);
			}
		}
	}
	
	public void Process(LocalDate begin, LocalDate end) throws Exception {
		PositionManager.clearDb();
		
		ArrayList<LocalDate> dates = new ArrayList<LocalDate>();
		OhlcvFetcher ohlcvFetcher = new OhlcvFetcher();
		ohlcvFetcher.connect();
		ohlcvFetcher.fetchDates(dates);

		for (int i = 0; i < dates.size(); i++) {
			LocalDate date = dates.get(i);

			if (date.compareTo(new LocalDate("2012-08-07")) == 0) {
				//System.out.println("here\n");
			}
			Ohlcv ohlcv = ohlcvFetcher.fetchLatest(date);

			if (i > slowMa.getPeriod()) {
				if (date.compareTo(begin) >= 0 && date.compareTo(end) <= 0) {
					checkSignals(date, ohlcv);
					checkStops(date, ohlcv);

					if (positionManager != null) {
						positionManager.savePositions();
					}

					updateMas(date, ohlcv);
					updateStops(date, ohlcv);
					updateSignals(date, ohlcv);
							
					this.reporter.report(date, positionManager);
				}
			}
			else {
				updateMas(date, ohlcv);
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		MovingAverage slowMa = new ExpMovingAverage(50);
		MovingAverage fastMa = new ExpMovingAverage(20);
		MovingAverage atr = new ExpMovingAverage(20);
		
		Account.INSTANCE.setInitBalance(100000.0);
		Account.INSTANCE.setBalance(100000.0);
		
		final LocalDate begin = new LocalDate("1998-09-04");
		final LocalDate end = new LocalDate("2015-12-31");
		final String runName = "DAILY_TEST_" + begin + "_" + end + "_" + new DateTime();
		
		Reporter reporter = new Reporter(runName);
		reporter.clear(runName);
		
		Processor processor = new Processor(reporter, slowMa, fastMa, atr);
		processor.Process(begin, end);
	}
}
