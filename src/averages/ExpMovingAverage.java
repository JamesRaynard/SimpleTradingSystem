package averages;

import java.util.List;

import loader.Ohlcv;

public class ExpMovingAverage extends MovingAverage {
	public ExpMovingAverage(int period) throws Exception {
		super(period);
		alpha = 2.0/(1.0 + period);
	}
	
	public void update(double data) {
		double ema = 0.0;
		int size = values.size();
		
		if (size == 0) {
			ema = data;
		}
		else {
			ema = alpha*data + (1.0-alpha)*values.get(size-1);
		}
		values.add(ema);
	}
	@Override
	public List<Double> calculateAll(List<Ohlcv> ohlcvList) {
		return super.calculateAll(ohlcvList);
	}

}
