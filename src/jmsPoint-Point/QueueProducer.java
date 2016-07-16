import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;

public class QueueProducer {

	public static void main(String[] args) throws Exception {
		System.out.println("Entering the Queue Producer");
		
		Context context = QueueConsumer.getInitialContext();
		QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory) context.lookup(QueueConsumer.JMS_CONNECTION_FACTORY_JNDI);
		QueueConnection queueConnection = queueConnectionFactory.createQueueConnection(QueueConsumer.JMS_USERNAME, QueueConsumer.JMS_PASSWORD);   
	     
	    QueueSession queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = (Queue) context.lookup(QueueConsumer.JMS_Queue_JNDI);
		queueConnection.start();
		QueueProducer queueProducer = new QueueProducer();
		queueProducer.sendMessage("Message from producer",queueSession,queue);
		System.out.println("End of the Queue Producer");

	}

	private void sendMessage(String message, QueueSession queueSession, Queue queue) throws JMSException {
		QueueSender queueSender = queueSession.createSender(queue);
		TextMessage textMessage = queueSession.createTextMessage(message);
		queueSender.send(textMessage);
		queueSender.close();
		
	}

}
