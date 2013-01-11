package com.premiumminds.persistence;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.log4j.Logger;

import com.google.inject.Provider;
import com.google.inject.persist.UnitOfWork;

public class JpaGuicePersistenceTransaction implements PersistenceTransaction {
	private static final Logger log = Logger.getLogger(JpaGuicePersistenceTransaction.class);

	private UnitOfWork unitOfWork;
	private Provider<EntityManager> emp;
	
	private ThreadLocal<Boolean> started = new ThreadLocal<Boolean>(){
		protected Boolean initialValue() { return false; };
	};

	@Inject
	public JpaGuicePersistenceTransaction(UnitOfWork unitOfWork, Provider<EntityManager> emp) {
		this.unitOfWork = unitOfWork;
		this.emp = emp;
	}

	public void start() {
		if(started.get()) throw new RuntimeException("transaction already started");
		started.set(true);
		unitOfWork.begin();
		emp.get().getTransaction().begin();
		
		log.trace("Jpa transaction started");
	}

	public void end() {
		if(!started.get()) throw new RuntimeException("transaction not started");
		try {
			EntityTransaction transaction = emp.get().getTransaction();
			if(transaction.getRollbackOnly()){
				transaction.rollback();
				log.trace("Jpa transaction committed");
			} else {
				transaction.commit();
				log.trace("Jpa transaction rolledback");
			}
		} finally {
			unitOfWork.end();
			started.remove();
		}
	}

	public void setRollbackOnly() {
		if(!started.get()) throw new RuntimeException("transaction not started");
		emp.get().getTransaction().setRollbackOnly();
	}

	public boolean isRollbackOnly() {
		if(!started.get()) throw new RuntimeException("transaction not started");
		return emp.get().getTransaction().getRollbackOnly();
	}

}
