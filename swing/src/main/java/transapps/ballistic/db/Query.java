package transapps.ballistic.db;

import transapps.ballistic.app.BLog;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Query {
	private static final String TAG = "Query";

	private final PreparedStatement ps;
	public final ResultSet rs;

	public Query(PreparedStatement ps, ResultSet rs) {
		this.ps = ps;
		this.rs = rs;
	}

	public void close() {
		try { if (rs != null) rs.close(); } catch (SQLException ex) { BLog.e(TAG, "CLOSE 1 exception", ex); }
		try { if (ps != null) ps.close(); } catch (SQLException ex) { BLog.e(TAG, "CLOSE 2 exception", ex);}
	}
}
