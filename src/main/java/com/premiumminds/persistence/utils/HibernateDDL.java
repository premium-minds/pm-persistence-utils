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

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl;
import org.hibernate.jpa.boot.internal.ParsedPersistenceXmlDescriptor;
import org.hibernate.jpa.boot.internal.PersistenceXmlParser;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;
import org.hibernate.jpa.boot.spi.ProviderChecker;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.TargetType;

public class HibernateDDL {
	/**
	 * --create unitName [filename]
	 * --create-drop unitName [filename]
	 * --update unitName jdbcUrl jdbcUsername jdbcPassword [filename]
	 * 
	 * @param args usage arguments
	 */
	public static void main(String[] args) {
		if(args.length==0){
			System.out.println("Usage: ");
			System.out.println("\t--create unitName [filename] - Create table commands");
			System.out.println("\t--create-drop unitName [filename] - Create table and drop commands");
			System.out.println("\t--update unitName jdbcUrl jdbcUsername jdbcPassword [filename] - Alter table commands based on your database");
			System.out.println("\n\t[filename] is the name of the file where to write (it's optional)");
		} else if(args.length<5) {
			System.out.println("Expected unitName jdbcUrl jdbcUsername jdbcPassword [filename]");
		} else {
			String unitName, filename=null, url, username, password;
			unitName = args[1];
			url = args[2];
			username = args[3];
			password = args[4];
			if(args.length>5) filename = args[5];

			Map<String, Object>  properties = new HashMap<>();
			properties.put("javax.persistence.jdbc.url", url);
			properties.put("javax.persistence.jdbc.user", username);
			properties.put("javax.persistence.jdbc.password", password);

			if("--update".equals(args[0].toLowerCase())) {
				updateCommand(unitName, properties, filename);
			}
			if("--create".equals(args[0].toLowerCase())) {
				createCommand(unitName, properties, filename);
			}
			if("--create-drop".equals(args[0].toLowerCase())) {
				createDropCommand(unitName, properties, filename);
			}
		}
	}

	protected static void updateCommand(String unitName, Map<String, Object> properties, String filename) {
		EntityManagerFactoryBuilderImpl entityManagerFactoryBuilder = getEntityManagerFactoryBuilderOrNull(unitName, properties);
		EntityManagerFactory factory = entityManagerFactoryBuilder.build();

		MetadataImplementor metaData = entityManagerFactoryBuilder.getMetadata();

		SchemaUpdate update = new SchemaUpdate();
        update.setHaltOnError(true);
        update.setFormat(true);
        update.setDelimiter(";");
        if (filename != null) {
        	update.setOutputFile(filename);
        }
		update.execute(EnumSet.of(filename == null ? TargetType.STDOUT : TargetType.SCRIPT), metaData);

		factory.close();
	}

	protected static void createCommand(String unitName, Map<String, Object> properties, String filename) {
		EntityManagerFactoryBuilderImpl entityManagerFactoryBuilder = getEntityManagerFactoryBuilderOrNull(unitName, properties);
		EntityManagerFactory factory = entityManagerFactoryBuilder.build();

		MetadataImplementor metaData = entityManagerFactoryBuilder.getMetadata();

		SchemaExport export = new SchemaExport();
        export.setHaltOnError(true);
        export.setFormat(true);
        export.setDelimiter(";");
        if (filename != null) {
        	export.setOutputFile(filename);
        }
		export.execute(EnumSet.of(filename == null ? TargetType.STDOUT : TargetType.SCRIPT), SchemaExport.Action.CREATE, metaData);

		factory.close();
	}

	protected static void createDropCommand(String unitName, Map<String, Object> properties, String filename) {
		EntityManagerFactoryBuilderImpl entityManagerFactoryBuilder = getEntityManagerFactoryBuilderOrNull(unitName, properties);
		EntityManagerFactory factory = entityManagerFactoryBuilder.build();

		MetadataImplementor metaData = entityManagerFactoryBuilder.getMetadata();

		SchemaExport export = new SchemaExport();
        export.setHaltOnError(true);
        export.setFormat(true);
        export.setDelimiter(";");
        if (filename != null) {
        	export.setOutputFile(filename);
        }
		export.execute(EnumSet.of(filename == null ? TargetType.STDOUT : TargetType.SCRIPT), SchemaExport.Action.BOTH, metaData);

		factory.close();
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
}
