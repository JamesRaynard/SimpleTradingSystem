package constants;

import org.joda.time.LocalDate;

public class TimeConstants {
	public static final String MINDATE = "1900-01-01";
	public static final String MAXDATE = "2099-12-31";
	public static final LocalDate MINLDATE = new LocalDate(MINDATE);
	public static final LocalDate MAXLDATE = new LocalDate(MAXDATE);
}
