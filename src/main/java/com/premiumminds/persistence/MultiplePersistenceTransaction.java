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
		for(PersistenceTransaction pt : persistenceTransactions){
			pt.start();
		}
	}

	@Override
	public void end() {
		try {
			boolean rollback = isRollbackOnly();
			for(PersistenceTransaction pt : persistenceTransactions){
				pt.end();
			}
			log.debug("Ending application transaction");
			if(sync.get()!=null){
				PersistenceTransactionSynchronization.Status status = Status.COMMITTED;
				if(rollback) status = Status.ROLLEDBACK;
				for(PersistenceTransactionSynchronization s : sync.get()){
					s.afterTransaction(status);
				}
			}
		} finally {
			sync.remove();
		}
	}

	@Override
	public void setRollbackOnly() {
		log.debug("Marking application transaction for rollback");
		for(PersistenceTransaction pt : persistenceTransactions){
			pt.setRollbackOnly();
		}
	}

	@Override
	public boolean isRollbackOnly() {
		for(PersistenceTransaction pt : persistenceTransactions){
			if(pt.isRollbackOnly()) return true;
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
