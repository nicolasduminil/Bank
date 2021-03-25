package fr.simplex_software.bank.session;

import lombok.extern.slf4j.*;

import javax.ejb.*;
import javax.jms.*;

@MessageDriven(activationConfig = @ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/jms/queue/BanQ"), mappedName = "java:/jms/queue/BanQ")
@Slf4j
public class MessageReceiver implements MessageListener
{
  public void onMessage(Message message)
  {
    try
    {
      log.info("*** MessageReceiver.onMessage(): got message {}", ((TextMessage)message).getText());
    }
    catch (JMSException e)
    {
      e.printStackTrace();
    }
  }
}
