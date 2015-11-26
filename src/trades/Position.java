package trades;

import org.joda.time.LocalDate;

import constants.TradeConstants;

public class Position {
	protected double tickValue;
	protected int contracts;
	protected double target;
	protected String type;
	protected LocalDate filled;
	protected double fillPrice;
	protected double initialStop;
	protected double stop;
	protected double profit;
	protected double premium;
	boolean checked = false;
	
	public Position(double tickValue, int contracts, 
			double target, String type, LocalDate filled,
			double fillPrice, double initialStop, double stop, double profit,
			double premium) {
		super();
		this.tickValue = tickValue;
		this.contracts = contracts;
		this.target = target;
		this.type = type;
		this.filled = filled;
		this.fillPrice = fillPrice;
		this.initialStop = initialStop;
		this.stop = stop;
		this.profit = profit;
		this.premium = premium;
	}

	public void setProfit(double profit) {
		this.profit = profit;
	}
	public double getTickValue() {
		return tickValue;
	}
	public double calculateProfit(char orderType, double price) {
		double profit = 0.0;
		
		if (orderType == TradeConstants.BUY) {
			profit = (price - this.getFillPrice() - this.getPremium()) 
						* (double)this.getContracts() * this.getTickValue();
		}
		else if (orderType == TradeConstants.SELL) {
			
			profit =(this.getFillPrice() - price + this.getPremium()) 
						* (double)this.getContracts() * this.getTickValue();
		}
		return profit;
	}
	
	public void setTickValue(long tickValue) {
		this.tickValue = tickValue;
	}

	public int getContracts() {
		return contracts;
	}

	public void setContracts(int contracts) {
		this.contracts = contracts;
	}

	public double getTarget() {
		return target;
	}

	public void setTarget(double target) {
		this.target = target;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public LocalDate getFilled() {
		return filled;
	}

	public void setFilled(LocalDate filled) {
		this.filled = filled;
	}

	public double getFillPrice() {
		return fillPrice;
	}

	public void setFillPrice(double fillPrice) {
		this.fillPrice = fillPrice;
	}

	public double getInitialStop() {
		return initialStop;
	}

	public void setInitialStop(double initialStop) {
		this.initialStop = initialStop;
	}

	public double getStop() {
		return stop;
	}

	public void setStop(double stop) {
		this.stop = stop;
	}

	public double getProfit() {
		return profit;
	}

	public double getPremium() {
		return premium;
	}

	public void setPremium(double premium) {
		this.premium = premium;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	@Override
	public String toString() {
		return "Trade [tickValue=" + tickValue
				+ ", contracts=" + contracts + ", type=" + type
				+ ", filled=" + filled + ", fillPrice=" + fillPrice
				+ ", initialStop=" + initialStop + ", stop=" + stop
				+ ", profit=" + profit + ", premium=" + premium + "]";
	}
}
