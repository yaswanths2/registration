package io.mosip.registration.processor.core.spi.eventbus;

/**
 * Declares all the methods to be used by Processor stages.
 *
 * @author Pranav Kumar
 * @param <T>            The type of underlying Eventbus
 * @param <U>            The type of address for communication between stages
 * @param <V>            The type of Message for communication between stages
 * @since 0.0.1
 */
public interface EventBusManager<T, U, V> {

	/**
	 * This method returns the EventBus instance for the provided class.
	 *
	 * @param clazz the Object of class
	 * @param clusterManagerUrl the cluster address
	 * @return The EventBus instance
	 */
	public T getEventBus(Object clazz, String clusterManagerUrl);

	/**
	 * @param clazz
	 * @param clusterManagerUrl
	 * @param instanceNumber
	 * @return The EventBus instance
	 */
	public T getEventBus(Object clazz, String clusterManagerUrl, int instanceNumber);

	/**
	 * This method consumes a message from an address, processes it and forwards the
	 * message to next given address.
	 *
	 * @param eventBus            The Eventbus instance for communication
	 * @param fromAddress            The address from which message is to be consumed
	 * @param toAddress            The address to which message needs to be sent
	 * @param messageExpiryTimeLimit	The time limit in seconds, after which message should 
	 * 									considered as expired
	 */
	public void consumeAndSend(T eventBus, U fromAddress, U toAddress, long messageExpiryTimeLimit);

	/**
	 * This method processes on the supplied object and returns the modified object.
	 *
	 * @param object            The object for processing
	 * @return The modified object after processing
	 */
	public V process(V object);

}
