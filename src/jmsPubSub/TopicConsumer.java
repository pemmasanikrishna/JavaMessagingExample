package jmsPubSub;

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
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class TopicConsumer implements MessageListener {

	 public final static String JMS_CONNECTION_FACTORY_JNDI="jms/RemoteConnectionFactory";
	  public final static String JMS_Topic_JNDI="jms/topic/testTopic";
	  public final static String JMS_USERNAME="admin";       //  The role for this user is "guest" in ApplicationRealm
	  public final static String JMS_PASSWORD="password";    //  Password for the username.
	  public final static String WILDFLY_REMOTING_URL="http-remoting://localhost:8080";
	 
	 
	  private TopicConnectionFactory topicConnectionFactory;
	  private TopicConnection topicConnection;
	  private TopicSession topicSession;
	  private TopicSubscriber topicSubscriber;
	  private Topic topic;
	  private TextMessage msg;
	  private boolean quit = false;
	 
	  public static void main(String[] args) throws Exception {
	    InitialContext ic = getInitialContext();
	    TopicConsumer wildflyJmsQueueReceive = new TopicConsumer();
	    wildflyJmsQueueReceive.init(ic, JMS_Topic_JNDI);
	    System.out.println("JMS Ready To Receive Messages (To quit, send a \"quit\" message from TopicProducer.class).");
	    // Waiting until a "quit" message has been received.
	    synchronized(wildflyJmsQueueReceive) {
	         while (! wildflyJmsQueueReceive.quit) {
	             try {
	                   wildflyJmsQueueReceive.wait();
	             }
	             catch (InterruptedException ie) {
	                   ie.printStackTrace();
	             }
	         }
	     }
	     wildflyJmsQueueReceive.close();
	  }
	   
	  public void init(Context ctx, String queueName) throws NamingException, JMSException {
	    topicConnectionFactory = (TopicConnectionFactory) ctx.lookup(JMS_CONNECTION_FACTORY_JNDI);
	 
	    //  If you won't pass jms credential here then you will get 
	    // [javax.jms.JMSSecurityException: HQ119031: Unable to validate user: null]    
	    topicConnection = topicConnectionFactory.createTopicConnection(this.JMS_USERNAME, this.JMS_PASSWORD);   
	     
	    topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
	    topic = (Topic) ctx.lookup(queueName);
	    topicSubscriber = topicSession.createSubscriber(topic);
	    topicSubscriber.setMessageListener(this);
	    topicConnection.start();
	  }
	 
	  public void onMessage(Message message) {
	     try {
	          System.out.println("Message : " + ((TextMessage)message).getText());

	      } catch (JMSException jmse) {
	          jmse.printStackTrace();
	     }
	  }
	 
	  public void close() throws JMSException {
	    topicSubscriber.close();
	    topicSession.close();
	    topicConnection.close();
	  }
	   
	  public static InitialContext getInitialContext() throws NamingException {
	     InitialContext context=null;
	     try {
	           Properties props = new Properties();
	           props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
	           props.put(Context.PROVIDER_URL,  WILDFLY_REMOTING_URL );   // NOTICE: "http-remoting" and port "8080"
	           props.put(Context.SECURITY_PRINCIPAL, JMS_USERNAME);
	           props.put(Context.SECURITY_CREDENTIALS, JMS_PASSWORD);
	           //props.put("jboss.naming.client.ejb.context", true);
	           context = new InitialContext(props); 
	       System.out.println("\n\tGot initial Context: "+context);     
	      } catch (Exception e) {
	           e.printStackTrace();
	      }
	    return context;
	  }
}
