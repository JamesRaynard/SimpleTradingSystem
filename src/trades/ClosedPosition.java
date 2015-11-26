package trades;

import org.joda.time.LocalDate;

public class ClosedPosition extends Position {
	protected LocalDate closed;
	protected double closePrice;
	protected String closureReason;

	ClosedPosition(Long tickValue, int contracts, double target, String type,
			LocalDate filled, double fillPrice, double initialStop, double stop,
			double profit, double premium, LocalDate closed, double closePrice,
			String closureReason) {
		super(tickValue, contracts, target, type, filled, fillPrice, initialStop,
				stop, profit, premium);
		this.closed = closed;
		this.closePrice = closePrice;
		this.closureReason = closureReason;
	}
	
	ClosedPosition(Position position) {
		super(position.tickValue, position.contracts, position.target, position.type,
				position.filled, position.fillPrice, position.initialStop,
				position.stop, position.profit, position.premium);
	}

	public LocalDate getClosed() {
		return closed;
	}

	public void setClosed(LocalDate closed) {
		this.closed = closed;
	}

	public double getClosePrice() {
		return closePrice;
	}

	public void setClosePrice(double closePrice) {
		this.closePrice = closePrice;
	}

	public String getClosureReason() {
		return closureReason;
	}

	public void setClosureReason(String closureReason) {
		this.closureReason = closureReason;
	}

	@Override
	public String toString() {
		return "closed=" + closed + ", closePrice=" + closePrice
				+ ", closureReason=" + closureReason + ", tickValue="
				+ tickValue + ", contracts=" + contracts + ", target=" + target
				+ ", type=" + type + ", filled=" + filled + ", fillPrice="
				+ fillPrice + ", initialStop=" + initialStop + ", stop=" + stop
				+ ", profit=" + profit + ", premium=" + premium;
	}
	
}
