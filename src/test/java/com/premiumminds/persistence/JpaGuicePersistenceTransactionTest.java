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
package com.premiumminds.persistence;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.google.inject.Provider;
import com.google.inject.persist.UnitOfWork;

public class JpaGuicePersistenceTransactionTest extends EasyMockSupport {

	@Test
	public void testStartEndSuccess() {
		
		UnitOfWork unitOfWork = createMock(UnitOfWork.class);
		@SuppressWarnings("unchecked")
		Provider<EntityManager> emp = createMock(Provider.class);
		EntityManager em = createMock(EntityManager.class);
		EntityTransaction et = createMock(EntityTransaction.class);
		
		// start
		unitOfWork.begin();
		expect(emp.get()).andReturn(em);
		expect(em.getTransaction()).andReturn(et);
		et.begin();

		// end
		expect(emp.get()).andReturn(em);
		expect(em.getTransaction()).andReturn(et);
		expect(et.getRollbackOnly()).andReturn(false);
		et.commit();
		unitOfWork.end();

		replayAll();
		
		PersistenceTransaction pr = new JpaGuicePersistenceTransaction(unitOfWork, emp);

		pr.start();
		
		pr.end();
		
		verifyAll();
	}
	
	@Test
	public void testStartEndRollbackSuccess() {
		
		UnitOfWork unitOfWork = createMock(UnitOfWork.class);
		@SuppressWarnings("unchecked")
		Provider<EntityManager> emp = createMock(Provider.class);
		EntityManager em = createMock(EntityManager.class);
		EntityTransaction et = createMock(EntityTransaction.class);
		
		// start
		unitOfWork.begin();
		expect(emp.get()).andReturn(em);
		expect(em.getTransaction()).andReturn(et);
		et.begin();

		//rollback
		expect(emp.get()).andReturn(em);
		expect(em.getTransaction()).andReturn(et);
		et.setRollbackOnly();
		
		// end
		expect(emp.get()).andReturn(em);
		expect(em.getTransaction()).andReturn(et);
		expect(et.getRollbackOnly()).andReturn(true);
		et.rollback();
		unitOfWork.end();

		replayAll();
		
		PersistenceTransaction pr = new JpaGuicePersistenceTransaction(unitOfWork, emp);

		pr.start();
		pr.setRollbackOnly();
		pr.end();
		
		verifyAll();
	}
	
	@Test
	public void testStartEndWithSynchronization() {
		
		UnitOfWork unitOfWork = createMock(UnitOfWork.class);
		@SuppressWarnings("unchecked")
		Provider<EntityManager> emp = createMock(Provider.class);
		EntityManager em = createMock(EntityManager.class);
		EntityTransaction et = createMock(EntityTransaction.class);
		
		// start
		unitOfWork.begin();
		expect(emp.get()).andReturn(em);
		expect(em.getTransaction()).andReturn(et);
		et.begin();

		// end
		expect(emp.get()).andReturn(em);
		expect(em.getTransaction()).andReturn(et);
		expect(et.getRollbackOnly()).andReturn(false);
		et.commit();
		unitOfWork.end();

		replayAll();
		
		PersistenceTransaction pr = new JpaGuicePersistenceTransaction(unitOfWork, emp);

		AtomicBoolean called = new AtomicBoolean(false);
		pr.registerSynchronization(new PersistenceTransactionSynchronization() {
			@Override
			public void afterTransaction(Status status) {
				called.set(true);
			}
		});
		
		pr.start();
		
		pr.end();
		
		assertTrue(called.get());
		
		verifyAll();
	}
	
	@Test
	public void testStartFailsOneTimeOnly() {
		
		UnitOfWork unitOfWork = createMock(UnitOfWork.class);
		@SuppressWarnings("unchecked")
		Provider<EntityManager> emp = createMock(Provider.class);
		EntityManager em = createMock(EntityManager.class);
		EntityTransaction et = createMock(EntityTransaction.class);
		
		// start - first time
		unitOfWork.begin();
		expect(emp.get()).andReturn(em);
		expect(em.getTransaction()).andReturn(et);
		et.begin();
		EasyMock.expectLastCall().andThrow(new RuntimeException("some problem"));
		unitOfWork.end();

		// start - second time
		unitOfWork.begin();
		expect(emp.get()).andReturn(em);
		expect(em.getTransaction()).andReturn(et);
		et.begin();
		
		replayAll();
		
		PersistenceTransaction pr = new JpaGuicePersistenceTransaction(unitOfWork, emp);

		try {
			pr.start();
			fail("should not reach here");
		} catch (Exception e) {
			assertEquals("some problem",  e.getMessage());
		}
		
		pr.start();
		
		verifyAll();
	}
	
}
