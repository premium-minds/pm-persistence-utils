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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.premiumminds.persistence.PersistenceTransactionSynchronization.Status;

public class MultiplePersistenceTransaction implements PersistenceTransaction {
	private static final Logger log = LoggerFactory.getLogger(MultiplePersistenceTransaction.class);
	
	private List<PersistenceTransaction> persistenceTransactions = new ArrayList<PersistenceTransaction>();
	private ThreadLocal<List<PersistenceTransactionSynchronization>> sync = new ThreadLocal<List<PersistenceTransactionSynchronization>>();

	public void add(PersistenceTransaction persistenceTransaction){
		persistenceTransactions.add(persistenceTransaction);
	}
	
	@Override
	public void start() {
		log.debug("Starting application transaction");
		try{
			for(PersistenceTransaction pt : persistenceTransactions){
				pt.start();
			}
		} catch(Exception e){
			
			for(PersistenceTransaction pt : persistenceTransactions){
				try{
					pt.setRollbackOnly();
					pt.end();
				} catch(Throwable t){ }
			}
			throw new RuntimeException("Aborting transaction start", e);
		}		
	}

	@Override
	public void end() {
		try {
			boolean rollback = isRollbackOnly();
			Exception caughtException = null;
			for(PersistenceTransaction pt : persistenceTransactions){
				try {
					if (caughtException!=null) {
						pt.setRollbackOnly();
						rollback = true;
					}
				} catch (Exception e) { }
				try {
					pt.end();
				} catch (Exception e) {
					log.warn("Failed to end transaction at "+pt.getClass().getSimpleName()+": "+e.getMessage());
					caughtException = e;
				}
			}
			log.debug("Ending application transaction");
			if(sync.get()!=null){
				PersistenceTransactionSynchronization.Status status = Status.COMMITTED;
				if(rollback) status = Status.ROLLEDBACK;
				for(PersistenceTransactionSynchronization s : sync.get()){
					s.afterTransaction(status);
				}
			}
			if(caughtException!=null) throw new RuntimeException(caughtException);
		} finally {
			sync.remove();
		}
	}

	@Override
	public void setRollbackOnly() {
		log.debug("Marking application transaction for rollback");
		for(PersistenceTransaction pt : persistenceTransactions){
			try {
				pt.setRollbackOnly();
			} catch (Exception e) {
				log.warn("Failed to mark transaction "+pt.getClass().getSimpleName()+" for rollback: "+e.getMessage());
			}
		}
	}

	@Override
	public boolean isRollbackOnly() {
		for(PersistenceTransaction pt : persistenceTransactions){
			try {
				if(pt.isRollbackOnly()) return true;
			} catch (Exception e) {
				log.warn("Failed to determine rollback status for transaction "+pt.getClass().getSimpleName()+" for rollback: "+e.getMessage());
			}
		}
		return false;
	}

	@Override
	public void registerSynchronization(
			PersistenceTransactionSynchronization synchronization) {
		if(sync.get()==null) sync.set(new ArrayList<PersistenceTransactionSynchronization>());
		sync.get().add(synchronization);
	}

}
