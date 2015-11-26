package criteria;


public class MovingAverageCriteria {
	public static boolean isBuySignal(double oldSlowMa, double oldFastMa,
											double slowMa, double fastMa) {
		return fastMa > slowMa && oldFastMa < oldSlowMa;
	}
	
	public static boolean isSellSignal(double oldSlowMa, double oldFastMa,
									double slowMa, double fastMa) {
		return fastMa < slowMa && oldFastMa > oldSlowMa;
	}
}