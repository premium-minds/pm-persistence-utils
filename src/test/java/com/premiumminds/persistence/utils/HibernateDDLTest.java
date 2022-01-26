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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HibernateDDLTest {

	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	
	@BeforeEach
	public void setUpStreams() {
	    System.setOut(new PrintStream(outContent));
	}
	
	@AfterEach
	public void cleanUpStreams() {
	    System.setOut(null);
	}
	
	@Test
	public void testNoArgs() throws Exception {

		String[] args = new String[]{ };
		
		HibernateDDL.main(args);
		
		String result = outContent.toString().replaceAll("\r", "");
		assertEquals("Usage: \n" +  
					"	--create unitName [filename] - Create table commands\n" + 
					"	--create-drop unitName [filename] - Create table and drop commands\n" + 
					"	--update unitName jdbcUrl jdbcUsername jdbcPassword [filename] - Alter table commands based on your database\n" + 
					"\n" + 
					"	[filename] is the name of the file where to write (it's optional)\n", result);
	}

	@Test
	public void testUpdateCommand() throws Exception {

		String url = "jdbc:h2:/tmp/test" + UUID.randomUUID();
		String username = "foo";
		String password = "bar";
		createDataBase(url, username, password);

		String[] args = new String[]{   "--update",
				"application-data-unit-export-test",
				url,
				username,
				password};
		
		HibernateDDL.main(args);
		
		String result = outContent.toString().replaceAll("\r", "");
		assertTrue(result.contains("\n    alter table FooBar \n       add column bar integer not null;\n"));
		assertTrue(result.contains("\n    alter table FooBar_AUD \n       add column bar integer;"));
	}

	@Test
	public void testUpdateCommandWithFile() throws Exception {

		File file = File.createTempFile("updatedb", ".sql");
		file.deleteOnExit();

		String url = "jdbc:h2:/tmp/test" + UUID.randomUUID();
		String username = "foo";
		String password = "bar";
		createDataBase(url, username, password);

		String[] args = new String[] {  "--update", 
										"application-data-unit-export-test",
										url,
										username,
										password,
										file.getAbsolutePath()};
		
		HibernateDDL.main(args);

		assertTrue(outContent.toString().isEmpty());
		
		String result = new String(Files.readAllBytes(file.toPath()));
		assertTrue(result.contains("\n    alter table FooBar \n       add column bar integer not null;\n"));
		assertTrue(result.contains("\n    alter table FooBar_AUD \n       add column bar integer;"));
	}

	@Test
	public void testCreateCommand() throws Exception {

		String url = "jdbc:h2:/tmp/test" + UUID.randomUUID();
		String username = "foo";
		String password = "bar";
		createDataBase(url, username, password);

		String[] args = new String[]{ "--create", "application-data-unit-export-test",
				url,
				username,
				password};
		
		HibernateDDL.main(args);
		
		String result = outContent.toString().replaceAll("\r", "");
		assertTrue(result.startsWith("\n" +
				"    create table FooBar (\n" +
				"       id integer not null,\n" +
				"        bar integer not null,\n" +
				"        foo varchar(255),\n" +
				"        primary key (id)\n" +
				"    );\n"));
		assertTrue(result.contains("\n" + 
				"    create table FooBar_AUD (\n" + 
				"       id integer not null,\n" + 
				"        REV integer not null,\n" + 
				"        REVTYPE tinyint,\n" + 
				"        bar integer,\n" + 
				"        foo varchar(255),\n" + 
				"        primary key (id, REV)\n" + 
				"    );\n"));
	}
	
	@Test
	public void testCreateCommandWithFile() throws Exception {

		File file = File.createTempFile("updatedb", ".sql");
		file.deleteOnExit();

		String url = "jdbc:h2:/tmp/test" + UUID.randomUUID();
		String username = "foo";
		String password = "bar";
		createDataBase(url, username, password);

		String[] args = new String[]{ "--create", "application-data-unit-export-test",
				url,
				username,
				password, file.getAbsolutePath()};
		
		HibernateDDL.main(args);
		
		assertTrue(outContent.toString().isEmpty());

		String result = new String(Files.readAllBytes(file.toPath()));
		assertTrue(result.startsWith("\n" +
				"    create table FooBar (\n" +
				"       id integer not null,\n" +
				"        bar integer not null,\n" +
				"        foo varchar(255),\n" +
				"        primary key (id)\n" +
				"    );\n"));
		assertTrue(result.contains("\n" + 
				"    create table FooBar_AUD (\n" + 
				"       id integer not null,\n" + 
				"        REV integer not null,\n" + 
				"        REVTYPE tinyint,\n" + 
				"        bar integer,\n" + 
				"        foo varchar(255),\n" + 
				"        primary key (id, REV)\n" + 
				"    );\n"));
	}
	
	@Test
	public void testCreateDropCommand() throws Exception {

		String url = "jdbc:h2:/tmp/test" + UUID.randomUUID();
		String username = "foo";
		String password = "bar";
		createDataBase(url, username, password);

		String[] args = new String[]{ "--create-drop", "application-data-unit-export-test",
				url,
				username,
				password};
		
		HibernateDDL.main(args);
		
		String result = outContent.toString().replaceAll("\r", "");
		assertTrue(result.startsWith("\n" +
				"    drop table if exists FooBar CASCADE ;\n"));
		assertTrue(result.contains("\n" +
				"    drop table if exists FooBar_AUD CASCADE ;\n"));
		assertTrue(result.contains("\n" +
				"    create table FooBar (\n" +
				"       id integer not null,\n" +
				"        bar integer not null,\n" +
				"        foo varchar(255),\n" +
				"        primary key (id)\n" +
				"    );\n"));
		assertTrue(result.contains("\n" + 
				"    create table FooBar_AUD (\n" + 
				"       id integer not null,\n" + 
				"        REV integer not null,\n" + 
				"        REVTYPE tinyint,\n" + 
				"        bar integer,\n" + 
				"        foo varchar(255),\n" + 
				"        primary key (id, REV)\n" + 
				"    );\n"));
	}

	@Test
	public void testCreateDropCommandWithFile() throws Exception {

		File file = File.createTempFile("updatedb", ".sql");
		file.deleteOnExit();

		String url = "jdbc:h2:/tmp/test" + UUID.randomUUID();
		String username = "foo";
		String password = "bar";
		createDataBase(url, username, password);

		String[] args = new String[]{ "--create-drop", "application-data-unit-export-test",
				url,
				username,
				password, file.getAbsolutePath()};
		
		HibernateDDL.main(args);
		
		assertTrue(outContent.toString().isEmpty());

		String result = new String(Files.readAllBytes(file.toPath()));
		assertTrue(result.startsWith("\n" +
				"    drop table if exists FooBar CASCADE ;\n"));
		assertTrue(result.contains("\n" +
				"    drop table if exists FooBar_AUD CASCADE ;\n"));
		assertTrue(result.contains("\n" +
				"    create table FooBar (\n" +
				"       id integer not null,\n" +
				"        bar integer not null,\n" +
				"        foo varchar(255),\n" +
				"        primary key (id)\n" +
				"    );\n"));
		assertTrue(result.contains("\n" + 
				"    create table FooBar_AUD (\n" + 
				"       id integer not null,\n" + 
				"        REV integer not null,\n" + 
				"        REVTYPE tinyint,\n" + 
				"        bar integer,\n" + 
				"        foo varchar(255),\n" + 
				"        primary key (id, REV)\n" + 
				"    );\n"));
	}

	private void createDataBase(String url, String username, String password) throws SQLException {
		try (Connection conn = DriverManager.getConnection(url, username, password)) {
			try (Statement statement = conn.createStatement()) {
				statement.executeUpdate("CREATE SEQUENCE \"PUBLIC\".\"SYSTEM_SEQUENCE_EDACC4F5_A213_4A1E_9499_E4C8964302C2\" START WITH 1 BELONGS_TO_TABLE; ");
				statement.executeUpdate("CREATE TABLE PUBLIC.FOOBAR ( " +
						" ID INTEGER NOT NULL, " +
						" FOO VARCHAR(255), " +
						" CONSTRAINT CONSTRAINT_7 PRIMARY KEY (ID) " +
						"); ");
				statement.executeUpdate("CREATE TABLE PUBLIC.REVINFO ( " +
						" REV INTEGER DEFAULT (NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_EDACC4F5_A213_4A1E_9499_E4C8964302C2) NOT NULL AUTO_INCREMENT, " +
						" REVTSTMP BIGINT, " +
						" CONSTRAINT CONSTRAINT_6 PRIMARY KEY (REV) " +
						"); ");
				statement.executeUpdate("CREATE TABLE PUBLIC.FOOBAR_AUD ( " +
						" ID INTEGER NOT NULL, " +
						" REV INTEGER NOT NULL, " +
						" REVTYPE TINYINT, " +
						" FOO VARCHAR(255), " +
						" CONSTRAINT CONSTRAINT_B PRIMARY KEY (ID,REV), " +
						" CONSTRAINT FK_HQ6LVB9TWE0IDLWIWQ4LOCY79 FOREIGN KEY (REV) REFERENCES PUBLIC.REVINFO(REV) ON DELETE RESTRICT ON UPDATE RESTRICT " +
						"); ");
			}
		}
	}
}
