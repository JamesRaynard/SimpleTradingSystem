package loader;

import org.joda.time.LocalDate;

public class Ohlcv {
	protected LocalDate date;
	protected Double open;
	protected Double high;
	protected Double low;
	protected Double close;
	protected Double tr;

	public Ohlcv() {
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate localDate) {
		this.date = localDate;
	}

	public Double getOpen() {
		return open;
	}

	public void setOpen(Double open) {
		this.open = open;
	}

	public Double getHigh() {
		return high;
	}

	public void setHigh(Double high) {
		this.high = high;
	}

	public Double getLow() {
		return low;
	}

	public void setLow(Double low) {
		this.low = low;
	}

	public Double getClose() {
		return close;
	}

	public void setClose(Double close) {
		this.close = close;
	}
	public Double getTr() {
		return tr;
	}
	public void setTr(double tr) {
		this.tr = tr;
	}

	@Override
	public String toString() {
		return date + ", " + open + ", " + high + ", " + low
				+ ", " + close + ", " + tr;
	}

}