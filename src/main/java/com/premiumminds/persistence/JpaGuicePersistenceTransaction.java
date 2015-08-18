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

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;
import com.google.inject.persist.UnitOfWork;
import com.premiumminds.persistence.PersistenceTransactionSynchronization.Status;

public class JpaGuicePersistenceTransaction implements PersistenceTransaction {
	private static final Logger log = LoggerFactory.getLogger(JpaGuicePersistenceTransaction.class);

	private UnitOfWork unitOfWork;
	private Provider<EntityManager> emp;
	private ThreadLocal<List<PersistenceTransactionSynchronization>> sync = new ThreadLocal<List<PersistenceTransactionSynchronization>>();

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
			PersistenceTransactionSynchronization.Status status;
			if(transaction.getRollbackOnly()){
				transaction.rollback();
				log.trace("Jpa transaction rolledback");
				status = Status.ROLLEDBACK;
			} else {
				transaction.commit();
				log.trace("Jpa transaction committed");
				status = Status.COMMITTED;
			}
			if(sync.get()!=null){
				for(PersistenceTransactionSynchronization s : sync.get()){
					s.afterTransaction(status);
				}
			}
		} finally {
			sync.remove();
			try {
				unitOfWork.end();
			} finally {
				started.remove();
			}
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

	public void registerSynchronization(
			PersistenceTransactionSynchronization synchronization) {
		if(sync.get()==null) sync.set(new ArrayList<PersistenceTransactionSynchronization>());
		sync.get().add(synchronization);
	}

}
