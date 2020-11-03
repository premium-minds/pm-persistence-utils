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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HibernateDDLTest {

	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	
	@Before
	public void setUpStreams() {
	    System.setOut(new PrintStream(outContent));
	}
	
	@After
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

		String[] args = new String[]{   "--update", 
										"application-data-unit-export-test", 
										"jdbc:h2:" + getClass().getResource("/foobar.mv.db").getFile().replace(".mv.db", ""), 
										"foo", 
										"bar"};
		
		HibernateDDL.main(args);
		
		String result = outContent.toString().replaceAll("\r", "");
		assertTrue(result.contains("\n    alter table FooBar \n       add column bar integer not null;\n"));
		assertTrue(result.contains("\n    alter table FooBar_AUD \n       add column bar integer;"));
	}
	
	@Test
	public void testUpdateCommandWithFile() throws Exception {

		File file = File.createTempFile("updatedb", ".sql");
		file.deleteOnExit();
		
		String[] args = new String[] {  "--update", 
										"application-data-unit-export-test",
										"jdbc:h2:" + getClass().getResource("/foobar.mv.db").getFile().replace(".mv.db", ""), 
										"foo", 
										"bar",
										file.getAbsolutePath()};
		
		HibernateDDL.main(args);

		assertTrue(outContent.toString().isEmpty());
		
		String result = IOUtils.toString(new FileReader(file)).replaceAll("\r", "");
		assertTrue(result.contains("\n    alter table FooBar \n       add column bar integer not null;\n"));
		assertTrue(result.contains("\n    alter table FooBar_AUD \n       add column bar integer;"));
	}

	@Test
	public void testCreateCommand() throws Exception {

		String[] args = new String[]{ "--create", "application-data-unit-export-test", 
				"jdbc:h2:" + getClass().getResource("/foobar.mv.db").getFile().replace(".mv.db", ""), 
				"foo", 
				"bar"};
		
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
		
		String[] args = new String[]{ "--create", "application-data-unit-export-test", 
				"jdbc:h2:" + getClass().getResource("/foobar.mv.db").getFile().replace(".mv.db", ""), 
				"foo", 
				"bar", file.getAbsolutePath()};
		
		HibernateDDL.main(args);
		
		assertTrue(outContent.toString().isEmpty());

		String result = IOUtils.toString(new FileReader(file)).replaceAll("\r", "");
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

		String[] args = new String[]{ "--create-drop", "application-data-unit-export-test", 
				"jdbc:h2:" + getClass().getResource("/foobar.mv.db").getFile().replace(".mv.db", ""), 
				"foo", 
				"bar"};
		
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
		
		String[] args = new String[]{ "--create-drop", "application-data-unit-export-test", 
				"jdbc:h2:" + getClass().getResource("/foobar.mv.db").getFile().replace(".mv.db", ""), 
				"foo", 
				"bar", file.getAbsolutePath()};
		
		HibernateDDL.main(args);
		
		assertTrue(outContent.toString().isEmpty());

		String result = IOUtils.toString(new FileReader(file)).replaceAll("\r", "");
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
}
