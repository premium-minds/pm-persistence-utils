package com.premiumminds.persistence;

public interface PersistenceTransaction {
	public void start();
	public void end();
	public void setRollbackOnly();
	public boolean isRollbackOnly();
	public void registerSynchronization(PersistenceTransactionSynchronization synchronization);
}
