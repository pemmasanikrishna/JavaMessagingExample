import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class QueueConsumer implements MessageListener {
	public final static String JMS_CONNECTION_FACTORY_JNDI = "jms/RemoteConnectionFactory";
	public final static String JMS_Queue_JNDI = "jms/queue/testQueue";
	public final static String JMS_USERNAME = "admin"; // The role for this user
	// is "guest" in
	// ApplicationRealm
	public final static String JMS_PASSWORD = "Password"; // Password
	public final static String WILDFLY_REMOTING_URL = "http-remoting://localhost:8080";

	private QueueConnectionFactory queueConnectionFactory;
	private QueueConnection queueConnection;
	private QueueSession queueSession;
	private QueueReceiver queueReceiver;
	private Queue queue;
	private TextMessage msg;
	private boolean quit = false;

	public static void main(String[] args) throws Exception {
		System.out.println("Starting Jms Queue consumer");
		InitialContext ic = getInitialContext();
		QueueConsumer wildflyJmsQueueReceive = new QueueConsumer();
		wildflyJmsQueueReceive.init(ic, JMS_Queue_JNDI);
		synchronized(wildflyJmsQueueReceive) {
		while (!wildflyJmsQueueReceive.quit) {
			try {
				wildflyJmsQueueReceive.wait();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		wildflyJmsQueueReceive.close();

		System.out.println("End of Jms Queue Consumer");
		}

	}

	@Override
	public void onMessage(Message message) {
		try {
			System.out.println("Message : " + ((TextMessage) message).getText());

		} catch (JMSException jmse) {
			jmse.printStackTrace();
		}

	}

	public static InitialContext getInitialContext() throws NamingException {
		InitialContext context = null;
		try {
			Properties props = new Properties();
			props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
			props.put(Context.PROVIDER_URL, WILDFLY_REMOTING_URL); // NOTICE:
			// "http-remoting"
			// and port
			// "8080"
			props.put(Context.SECURITY_PRINCIPAL, JMS_USERNAME);
			props.put(Context.SECURITY_CREDENTIALS, JMS_PASSWORD);
			context = new InitialContext(props);
			System.out.println("\n\tGot initial Context: " + context);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return context;
	}

	public void close() throws JMSException {
		queueReceiver.close();
		queueSession.close();
		queueConnection.close();
	}

	public void init(Context ctx, String queueName) throws NamingException, JMSException {
		queueConnectionFactory = (QueueConnectionFactory) ctx.lookup(JMS_CONNECTION_FACTORY_JNDI);

		// If you won't pass jms credential here then you will get
		// [javax.jms.JMSSecurityException: HQ119031: Unable to validate user:
		// null]
		queueConnection = queueConnectionFactory.createQueueConnection(this.JMS_USERNAME, this.JMS_PASSWORD);

		queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		queue = (Queue) ctx.lookup(queueName);
		queueReceiver = queueSession.createReceiver(queue);
		queueReceiver.setMessageListener(this);
		queueConnection.start();
	}
}
