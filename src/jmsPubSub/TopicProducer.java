package jmsPubSub;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.NamingException;

public class TopicProducer {

	public static void main(String[] args) throws NamingException, JMSException {
		// TODO Auto-generated method stub

		System.out.println("Starting jms Topic Producer");
		Context context = TopicConsumer.getInitialContext();
		TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) context.lookup(TopicConsumer.JMS_CONNECTION_FACTORY_JNDI);
		TopicConnection topicConnection = topicConnectionFactory.createTopicConnection(TopicConsumer.JMS_USERNAME, TopicConsumer.JMS_PASSWORD);   
	     
	    TopicSession topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		Topic topic = (Topic) context.lookup(TopicConsumer.JMS_Topic_JNDI);
		topicConnection.start();
		TopicProducer topicProducer = new TopicProducer();
		topicProducer.sendMessage("Message from producer",topicSession,topic);
		System.out.println("Message sent from producer");
	}

	private void sendMessage(String message, TopicSession topicSession, Topic topic) throws JMSException {
		// TODO Auto-generated method stub
		TopicPublisher topicPublisher = topicSession.createPublisher(topic);
		TextMessage textMessage = topicSession.createTextMessage(message);
		topicPublisher.publish(textMessage);
		topicPublisher.close();
		
	}

}
