package transapps.ballistic.app;

/**
 * Simple log wrapper, just use println for now but epand later for better logging.
 * Looks like Android logging since this app started as an android app.
 * Created by sstanf on 10/16/13.
 */
public class BLog {
	public static void d(String tag, String msg) {
		d(tag, msg, null);
	}
	public static void d(String tag, String msg, Throwable tr) {
		System.out.println(tag+"/DEBUG : "+msg);
		if (tr != null) tr.printStackTrace();
	}

	public static void e(String tag, String msg) {
		e(tag, msg, null);
	}
	public static void e(String tag, String msg, Throwable tr) {
		System.out.println(tag+"/ERROR : "+msg);
		if (tr != null) tr.printStackTrace();
	}

	public static void i(String tag, String msg) {
		i(tag, msg, null);
	}
	public static void i(String tag, String msg, Throwable tr) {
		System.out.println(tag+"/INFO : "+msg);
		if (tr != null) tr.printStackTrace();
	}

	public static void v(String tag, String msg) {
		v(tag, msg, null);
	}
	public static void v(String tag, String msg, Throwable tr) {
		System.out.println(tag+"/VERBOSE : "+msg);
		if (tr != null) tr.printStackTrace();
	}

	public static void w(String tag, String msg) {
		w(tag, msg, null);
	}
	public static void w(String tag, String msg, Throwable tr) {
		System.out.println(tag+"/WARN : "+msg);
		if (tr != null) tr.printStackTrace();
	}
}
