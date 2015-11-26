package averages;

import java.util.ArrayList;
import java.util.List;

import loader.Ohlcv;

public class SimpleMovingAverage extends MovingAverage {
	private double total;
	private List<Double> dataList = new ArrayList<Double>();
	
	public SimpleMovingAverage(int period) throws Exception {
		super(period);
	}

	@Override
	public void update(double data) {		
		double sma = 0.0;
		total += data;
		
		int size = values.size();
		
		if (size >= period) {
			total -= dataList.get(size-period);
			sma = total/period;
		}
		else {
			sma = total/(size+1);
		}
		
		dataList.add(data);
		values.add(sma);
	}

	@Override
	public List<Double> calculateAll(List<Ohlcv> ohlcvList) {
		dataList.clear();
		return super.calculateAll(ohlcvList);
	}

}
