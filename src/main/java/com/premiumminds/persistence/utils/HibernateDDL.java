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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.internal.PersistenceXmlParser;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;
import org.hibernate.jpa.boot.spi.ProviderChecker;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.hibernate.tool.hbm2ddl.SchemaUpdateScript;

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
				
				DatabaseMetadata meta = new DatabaseMetadata(conn, dialect, config, true);

				List<SchemaUpdateScript> updateScriptList = config.generateSchemaUpdateScriptList(dialect, meta);
				String[] updateSQL = SchemaUpdateScript.toStringArray(updateScriptList);

				stringToStream(updateSQL, filename);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	protected static EntityManagerFactoryBuilderImpl getEntityManagerFactoryBuilderOrNull(String persistenceUnitName, Map<String, Object> properties) {
		return getEntityManagerFactoryBuilderOrNull( persistenceUnitName, properties, null );
	}
	
	protected static EntityManagerFactoryBuilderImpl getEntityManagerFactoryBuilderOrNull(String persistenceUnitName, Map<String, Object> properties, ClassLoader providedClassLoader) {

		final Map<String, Object> integration = wrap( properties );
		final List<ParsedPersistenceXmlDescriptor> units;
		try {
			units = PersistenceXmlParser.locatePersistenceUnits( integration );
		}
		catch (Exception e) {
			throw new PersistenceException( "Unable to locate persistence units", e );
		}

		if ( persistenceUnitName == null && units.size() > 1 ) {
			throw new PersistenceException( "No name provided and multiple persistence units found" );
		}

		for ( ParsedPersistenceXmlDescriptor persistenceUnit : units ) {

			final boolean matches = persistenceUnitName == null || persistenceUnit.getName().equals( persistenceUnitName );
			if ( !matches ) {
				continue;
			}

			// See if we (Hibernate) are the persistence provider
			if ( ! ProviderChecker.isProvider( persistenceUnit, properties ) ) {
				continue;
			}

			return getEntityManagerFactoryBuilder( persistenceUnit, integration, providedClassLoader );
		}

		return null;
	}

	protected static EntityManagerFactoryBuilderImpl getEntityManagerFactoryBuilder(PersistenceUnitDescriptor persistenceUnitDescriptor,
			Map<String, Object> integration, ClassLoader providedClassLoader) {
		return new EntityManagerFactoryBuilderImpl( persistenceUnitDescriptor, integration, providedClassLoader );
	}

	protected static Map<String, Object> wrap(Map<String, Object> properties) {
		return properties == null ? Collections.<String, Object>emptyMap() : Collections.unmodifiableMap( properties );
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
		EntityManagerFactoryBuilderImpl entityManagerFactoryBuilder = getEntityManagerFactoryBuilderOrNull(unitName, null);
		
		ServiceRegistry serviceRegistry = entityManagerFactoryBuilder.buildServiceRegistry();
		Configuration config = entityManagerFactoryBuilder.buildHibernateConfiguration(serviceRegistry);

		return config;
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
