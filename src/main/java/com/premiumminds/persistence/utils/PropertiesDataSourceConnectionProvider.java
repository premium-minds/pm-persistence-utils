package com.premiumminds.persistence.utils;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl;

public class PropertiesDataSourceConnectionProvider extends DatasourceConnectionProviderImpl {
	public static final String PROPERTY = "persistence.datasource";
	private static final long serialVersionUID = 7429653440977958032L;
	
	private Connection conn;
	
	@Override
	public void configure(@SuppressWarnings("rawtypes") Map configValues) {
		conn = (Connection) configValues.get(PROPERTY);
		setDataSource(new DataSource() {
			
			@Override
			public <T> T unwrap(Class<T> iface) throws SQLException {
				return null;
			}
			
			@Override
			public boolean isWrapperFor(Class<?> iface) throws SQLException {
				return false;
			}
			
			@Override
			public void setLoginTimeout(int seconds) throws SQLException {
			}
			
			@Override
			public void setLogWriter(PrintWriter out) throws SQLException {
			}
			
			@Override
			public Logger getParentLogger() throws SQLFeatureNotSupportedException {
				return null;
			}
			
			@Override
			public int getLoginTimeout() throws SQLException {
				return 0;
			}
			
			@Override
			public PrintWriter getLogWriter() throws SQLException {
				return null;
			}
			
			@Override
			public Connection getConnection(String username, String password)
					throws SQLException {
				return conn;
			}
			
			@Override
			public Connection getConnection() throws SQLException {
				return conn;
			}
		});
		super.configure(configValues);
	}
	
	@Override
	public Connection getConnection() throws SQLException {
		return conn;
	}
}
