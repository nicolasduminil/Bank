package fr.simplex_software.bank.session;

import javax.ejb.*;
import javax.jms.*;

import org.slf4j.*;

@MessageDriven(activationConfig = @ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/jms/queue/BanQ"), mappedName = "java:/jms/queue/BanQ")
public class MessageReceiver implements MessageListener
{
  private static final Logger slf4jLogger = LoggerFactory.getLogger(MessageReceiver.class);

  public void onMessage(Message message)
  {
    try
    {
      slf4jLogger.info("*** MessageReceiver.onMessage(): got message {}", ((TextMessage)message).getText());
    }
    catch (JMSException e)
    {
      e.printStackTrace();
    }
  }
}
