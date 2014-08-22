/**
 * Copyright (C) 2014 Premium Minds.
 *
 * This file is part of pm-persistence-utils.
 *
 * pm-persistence-utils is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * pm-persistence-utils is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with pm-persistence-utils. If not, see <http://www.gnu.org/licenses/>.
 */
package com.premiumminds.persistence.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;

@SuppressWarnings("deprecation")
public class HibernateDDL {
	private final static Formatter formatter = FormatStyle.DDL.getFormatter();

	/**
	 * --create unitName [filename]
	 * --create-drop unitName [filename]
	 * --update unitName jdbcUrl jdbcUsername jdbcPassword [filename]
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length==0){
			System.out.println("Usage: ");
			System.out.println("\t--create unitName [filename] - Create table commands");
			System.out.println("\t--create-drop unitName [filename] - Create table and drop commands");
			System.out.println("\t--update unitName jdbcUrl jdbcUsername jdbcPassword [filename] - Alter table commands based on your database");
			System.out.println("\n\t[filename] is the name of the file where to write (it's optional)");
		} else {
			if("--create".equals(args[0].toLowerCase())) createCommand(args);
			if("--create-drop".equals(args[0].toLowerCase())) createDropCommand(args);
			if("--update".equals(args[0].toLowerCase())) updateCommand(args);
			
		}
	}

	protected static void updateCommand(String[] args) {
		String unitName, filename=null, url, username, password;
		if(args.length<5) System.out.println("Expected unitName jdbcUrl jdbcUsername jdbcPassword");
		else {
			unitName = args[1];
			url = args[2];
			username = args[3];
			password = args[4];
			if(args.length>5) filename = args[5];
			
			Configuration config = getConfiguration(unitName);
			Dialect dialect = Dialect.getDialect(config.getProperties());
			
			try {
				Connection conn = DriverManager.getConnection(url, username, password);
				
				DatabaseMetadata meta = new DatabaseMetadata(conn, dialect, true);
				
				String[] updateSQL = config.generateSchemaUpdateScript(dialect, meta);
				
				stringToStream(updateSQL, filename);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	protected static void createDropCommand(String[] args) {
		String unitName;
		String filename=null;
		if(args.length<2) System.out.println("Expected unitName");
		else {
			unitName = args[1];
			if(args.length>2) filename = args[2];
			
			Configuration config = getConfiguration(unitName);
			
			String[] dropSQL = config.generateDropSchemaScript(Dialect.getDialect(config.getProperties()));
			String[] createSQL = config.generateSchemaCreationScript(Dialect.getDialect(config.getProperties()));
			
			stringToStream(concat(dropSQL, createSQL), filename);
		}
	}

	protected static void createCommand(String[] args) {
		String unitName;
		String filename=null;
		if(args.length<2) System.out.println("Expected unitName");
		else {
			unitName = args[1];
			if(args.length>2) filename = args[2];
			
			Configuration config = getConfiguration(unitName);
			
			String[] createSQL = config.generateSchemaCreationScript(Dialect.getDialect(config.getProperties()));
			
			stringToStream(createSQL, filename);
		}
	}

	protected static Configuration getConfiguration(String unitName){
		Ejb3Configuration jpaConfiguration = new Ejb3Configuration().configure(unitName, null);
		return jpaConfiguration.getHibernateConfiguration();
	}
	
	protected static void stringToStream(String[] sql, String filename){
		PrintWriter writer;
		try {
			if(filename==null) writer = new PrintWriter(System.out);
			else writer = new PrintWriter(new File(filename));
			
			for (String string : sql) {
				writer.print(formatter.format(string) + ";\n");
			}
			
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}	
}
