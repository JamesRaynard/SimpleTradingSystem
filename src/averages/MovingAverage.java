package averages;

import java.util.ArrayList;
import java.util.List;

import constants.MovingAveConstants;
import loader.Ohlcv;

abstract public class MovingAverage {
	protected int period;
	protected double alpha;
	protected List<Double> values = null;
	
	MovingAverage(int period) throws Exception {
		if (period < 0) {
			throw new Exception("Moving average period cannot be negative");
		}
		this.period = period;
		values = new ArrayList<Double>();
	}
	
	// Calculates new value and appends it to the list
	public abstract void update(double data);
	
	// Calculate all values from closing prices (overwrites existing ones)
	public List<Double> calculateAll(List<Ohlcv> ohlcvList) {
		values.clear();
		
		for (int i = 0; i <= ohlcvList.size(); i++) {
			update(ohlcvList.get(i).getClose());
		}
		return values;
	}
	
	public double getCurrent() {
		double retval = 0.0;
		
		if (!values.isEmpty()) {
			retval = values.get(values.size()-1);
		}
		return retval;
	}
	public double getPrevious() {
		double retval;
		if (values.size() < 2) {
			retval = getCurrent();
		}
		else {
			retval = values.get(values.size()-2);
		}
		return retval;
	}
	
	public int getCurrentDirection() {
		int direction = MovingAveConstants.NONE;
		double current = getCurrent();
		double previous = getPrevious();
		
		if (current > previous) {
			direction = MovingAveConstants.UP;
		}
		if (current < previous) {
			direction = MovingAveConstants.DOWN;
		}
		return direction;
	}
	
	public int getPreviousDirection() {
		int direction = MovingAveConstants.NONE;
		
		if (values.size() >= 2) {
			double previous = getPrevious();
			double lastButOne = values.get(values.size()-2);
		
			if (previous > lastButOne) {
				direction = MovingAveConstants.UP;
			}
			if (previous < lastButOne) {
				direction = MovingAveConstants.DOWN;
			}
		}
		return direction;
	}

	public int getPeriod() {
		return period;
	}

	public List<Double> getValues() {
		return values;
	}
}
