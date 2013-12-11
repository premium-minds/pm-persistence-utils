package com.premiumminds.persistence.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.google.inject.Provider;
import com.premiumminds.persistence.TransactionWrapper;

/**
 * Method or class annotation and its interceptor.
 * Wraps a methods or class methods into a transaction
 */
@Retention(RetentionPolicy.RUNTIME) @Target({ElementType.METHOD, ElementType.TYPE})
public @interface EnsureTransaction {

	public static class Interceptor implements MethodInterceptor {
		
		@Inject Provider<EntityManager> emProvider;
		
		public Object invoke(final MethodInvocation invocation) throws Throwable {
			return new TransactionWrapper<Object>(emProvider.get()) {

				@Override
				public Object runTransaction() throws Throwable {
					return invocation.proceed();
				}
			}.execute();
		}
	}

}
