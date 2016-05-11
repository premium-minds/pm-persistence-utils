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
		
		// end
		EasyMock.expect(t1.isRollbackOnly()).andThrow(new RuntimeException("some exception in isRollbackOnly"));
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
		// start
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
		// start
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
		t2.end();
		
		// start 2
		t1.start();
		EasyMock.expectLastCall().andThrow(new RuntimeException("transaction already started"));
		
		// end 2
		EasyMock.expect(t1.isRollbackOnly()).andReturn(true);
		t1.end();
		t2.end();
		
		replayAll();
		
		MultiplePersistenceTransaction tr = new MultiplePersistenceTransaction();
		tr.add(t1);
		tr.add(t2);
		
		tr.start();
		try {
			tr.end();
		} catch (Exception e) {
			fail("should not throw exception anymore");
		}
		try {
			tr.start();
		} catch (Exception e) {
			assertEquals("Aborting transaction start", e.getMessage());
		}
		tr.end();
		
		verifyAll();
	}
	
}
