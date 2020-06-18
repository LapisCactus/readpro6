package freesoftoriented.pro6.readpro6.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StdLog {

	private static Logger logger = LoggerFactory.getLogger("stdlog");

	public static void debug(String msg) {
		logger.debug(msg);
	}

	public static void info(String msg) {
		logger.info(msg);
	}

	public static void warn(String msg) {
		logger.warn(msg);
	}

	public static void warn(String msg, Exception e) {
		logger.warn(msg, e);
	}

	public static void error(String msg) {
		logger.error(msg);
	}

	public static void error(String msg, Exception e) {
		logger.error(msg, e);
	}

}
