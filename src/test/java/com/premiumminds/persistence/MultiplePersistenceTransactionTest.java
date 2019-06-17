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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

public class MultiplePersistenceTransactionTest extends EasyMockSupport {

	@Test
	public void testStarts() throws Exception {
		
		PersistenceTransaction t1 = createMock(PersistenceTransaction.class);
		PersistenceTransaction t2 = createMock(PersistenceTransaction.class);
		
		// start
		t1.start();
		EasyMock.expectLastCall().once();
		t2.start();
		EasyMock.expectLastCall().once();
				
		replayAll();
		
		MultiplePersistenceTransaction tr = new MultiplePersistenceTransaction();
		tr.add(t1);
		tr.add(t2);
		
		tr.start();
		
		verifyAll();
		
	}

	@Test
	public void testStartsAndEnds() throws Exception {
		
		PersistenceTransaction t1 = createMock(PersistenceTransaction.class);
		PersistenceTransaction t2 = createMock(PersistenceTransaction.class);
		
		// start
		t1.start();
		EasyMock.expectLastCall().once();
		t2.start();
		EasyMock.expectLastCall().once();
		
		// end
		EasyMock.expect(t1.isRollbackOnly()).andReturn(false);
		EasyMock.expect(t2.isRollbackOnly()).andReturn(false);
		t1.end();
		EasyMock.expectLastCall().once();
		t2.end();
		EasyMock.expectLastCall().once();
		
		replayAll();
		
		MultiplePersistenceTransaction tr = new MultiplePersistenceTransaction();
		tr.add(t1);
		tr.add(t2);
		
		tr.start();
		tr.end();
		
		verifyAll();
		
	}
	
	@Test
	public void testStartsAndEndsWithRollback() throws Exception {
		
		PersistenceTransaction t1 = createMock(PersistenceTransaction.class);
		PersistenceTransaction t2 = createMock(PersistenceTransaction.class);
		
		// start
		t1.start();
		EasyMock.expectLastCall().once();
		t2.start();
		EasyMock.expectLastCall().once();
		
		// end
		EasyMock.expect(t1.isRollbackOnly()).andReturn(false);
		EasyMock.expect(t2.isRollbackOnly()).andReturn(true);
		t1.end();
		EasyMock.expectLastCall().once();
		t2.end();
		EasyMock.expectLastCall().once();
		
		replayAll();
		
		MultiplePersistenceTransaction tr = new MultiplePersistenceTransaction();
		tr.add(t1);
		tr.add(t2);
		
		tr.start();
		tr.end();
		
		verifyAll();
		
	}

	@Test
	public void testStartsAndEndsWithRollbackException() throws Exception {
		
		PersistenceTransaction t1 = createMock(PersistenceTransaction.class);
		PersistenceTransaction t2 = createMock(PersistenceTransaction.class);
		
		// start
		t1.start();
		EasyMock.expectLastCall().once();
		t2.start();
		EasyMock.expectLastCall().once();
		
		EasyMock.expect(t1.isRollbackOnly()).andThrow(new RuntimeException("some exception in isRollbackOnly"));
		
		// previous exception requires set rollback afterwards
		t1.setRollbackOnly();
		EasyMock.expectLastCall().once();
		t2.setRollbackOnly();
		EasyMock.expectLastCall().once();
		
		// end
		t1.end();
		EasyMock.expectLastCall().once();
		t2.end();
		EasyMock.expectLastCall().once();
		
		replayAll();
		
		MultiplePersistenceTransaction tr = new MultiplePersistenceTransaction();
		tr.add(t1);
		tr.add(t2);
		
		tr.start();
		try {
			tr.end();
		} catch(Exception e) {
			fail("should not throw exception anymore");
		}
		
		verifyAll();
	}
	
	@Test
	public void testSetRollbackOnly() throws Exception {
		
		PersistenceTransaction t1 = createMock(PersistenceTransaction.class);
		PersistenceTransaction t2 = createMock(PersistenceTransaction.class);
		
		// start
		t1.start();
		EasyMock.expectLastCall().once();
		t2.start();
		EasyMock.expectLastCall().once();
		
		// set rollback
		t1.setRollbackOnly();
		EasyMock.expectLastCall().once();
		t2.setRollbackOnly();
		EasyMock.expectLastCall().once();
		
		// end
		EasyMock.expect(t1.isRollbackOnly()).andReturn(true);
		t1.end();
		EasyMock.expectLastCall().once();
		t2.end();
		EasyMock.expectLastCall().once();
		
		replayAll();
		
		MultiplePersistenceTransaction tr = new MultiplePersistenceTransaction();
		tr.add(t1);
		tr.add(t2);
		
		tr.start();
		tr.setRollbackOnly();
		tr.end();
		
		verifyAll();
	}
	
	@Test
	public void testSetRollbackOnlyThrowsException() throws Exception {
		
		PersistenceTransaction t1 = createMock(PersistenceTransaction.class);
		PersistenceTransaction t2 = createMock(PersistenceTransaction.class);
		
		// start
		t1.start();
		EasyMock.expectLastCall().once();
		t2.start();
		EasyMock.expectLastCall().once();
		
		// set rollback
		t1.setRollbackOnly();
		EasyMock.expectLastCall().andThrow(new RuntimeException("some exception in setRollbackOnly"));
		t2.setRollbackOnly();
		EasyMock.expectLastCall().once();
		
		// end
		EasyMock.expect(t1.isRollbackOnly()).andReturn(true);
		t1.end();
		EasyMock.expectLastCall().once();
		t2.end();
		EasyMock.expectLastCall().once();
		
		replayAll();
		
		MultiplePersistenceTransaction tr = new MultiplePersistenceTransaction();
		tr.add(t1);
		tr.add(t2);
		
		tr.start();
		try {
			tr.setRollbackOnly();
		} catch (Exception e) {
			fail("should not throw exception anymore");
		}
		tr.end();
		
		verifyAll();
	}
	
	@Test
	public void testMultipleStartsAndEnds() throws Exception {
		
		PersistenceTransaction t1 = createMock(PersistenceTransaction.class);
		PersistenceTransaction t2 = createMock(PersistenceTransaction.class);
		
		// start
		t1.start();
		EasyMock.expectLastCall().times(2);
		t2.start();
		EasyMock.expectLastCall().times(2);
		
		// end
		EasyMock.expect(t1.isRollbackOnly()).andReturn(false).times(2);
		EasyMock.expect(t2.isRollbackOnly()).andReturn(true).times(2);
		t1.end();
		EasyMock.expectLastCall().times(2);
		t2.end();
		EasyMock.expectLastCall().times(2);
		
		replayAll();
		
		MultiplePersistenceTransaction tr = new MultiplePersistenceTransaction();
		tr.add(t1);
		tr.add(t2);
		
		tr.start();
		tr.end();
		tr.start();
		tr.end();
		
		verifyAll();
	}
	
	@Test
	public void testMultipleStartsAndEndsThrowsException() throws Exception {
		
		PersistenceTransaction t1 = createMock(PersistenceTransaction.class);
		PersistenceTransaction t2 = createMock(PersistenceTransaction.class);
		
		// start 1
		t1.start();
		t2.start();
		
		// end 1
		EasyMock.expect(t1.isRollbackOnly()).andReturn(false);
		EasyMock.expect(t2.isRollbackOnly()).andReturn(false);
		t1.end();
		EasyMock.expectLastCall().andThrow(new RuntimeException("some exception in end transaction"));
		// set rollback on second transaction since first ending failed
		t2.setRollbackOnly();
		t2.end();
		
		// start 2
		t1.start();
		EasyMock.expectLastCall().andThrow(new RuntimeException("transaction already started"));
		t1.setRollbackOnly();
		t1.end();
		t2.setRollbackOnly();
		t2.end();
		
		replayAll();
		
		MultiplePersistenceTransaction tr = new MultiplePersistenceTransaction();
		tr.add(t1);
		tr.add(t2);
		
		tr.start();
		try {
			tr.end();
		} catch (Exception e) {
			assertEquals("java.lang.RuntimeException: some exception in end transaction", e.getMessage());
		}
		try {
			tr.start();
		} catch (Exception e) {
			assertEquals("Aborting transaction start", e.getMessage());
		}
		
		verifyAll();
	}
	
}
