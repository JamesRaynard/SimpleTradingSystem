package reports;

public enum Account {
    INSTANCE;

	private double balance;
	private double initBalance;
	
	public double getFactor() throws Exception{
		double netBalance = balance - initBalance;
		double factor;
		
		if (netBalance + initBalance < 0) {
			throw new Exception("bust!");
		}
		if (netBalance >= 0) {
			factor = 1 + Math.log1p(netBalance/initBalance);
			//factor = 1 + netBalance/initBalance;
		}
		else {
			factor = 1 - Math.log1p(netBalance/initBalance);
			//factor = 1 - netBalance/initBalance;
		}

		return factor;
	}
	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public double getInitBalance() {
		return initBalance;
	}

	public void setInitBalance(double initBalance) {
		this.initBalance = initBalance;
	}
}
