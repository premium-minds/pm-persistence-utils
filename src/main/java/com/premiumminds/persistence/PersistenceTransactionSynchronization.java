package com.premiumminds.persistence;

public interface PersistenceTransactionSynchronization {
	public enum Status {COMMITTED, ROLLEDBACK}
	
	public void afterTransaction(Status status);
}
