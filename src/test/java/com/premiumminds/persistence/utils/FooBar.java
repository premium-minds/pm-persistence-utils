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

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.envers.Audited;

@Audited
@Entity
public class FooBar {

	@Id
	private int id;
	
	private String foo;
	private int bar;
	
	public String getFoo() {
		return foo;
	}
	public void setFoo(String foo) {
		this.foo = foo;
	}
	public int getBar() {
		return bar;
	}
	public void setBar(int bar) {
		this.bar = bar;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
}
