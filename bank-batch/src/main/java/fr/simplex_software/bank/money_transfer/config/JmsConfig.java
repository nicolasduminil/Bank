package fr.simplex_software.bank.money_transfer.config;

import lombok.extern.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.jms.annotation.*;
import org.springframework.jms.config.*;

import javax.jms.*;
import javax.naming.*;

@Configuration
@EnableJms
@Slf4j
public class JmsConfig
{
  @Value("${jms.connection.factory.jndi.name}")
  private String connectionFactoryJndiName;

  @Bean
  public ConnectionFactory connectionFactory() throws NamingException
  {
    return new InitialContext().doLookup(connectionFactoryJndiName);
  }

  @Bean
  public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() throws NamingException
  {
    DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory());
    return factory;
  }

  @JmsListener(destination = "${jms.destination.name}")
  public void onMessage(Message msg) throws JMSException
  {
    log.info("*** MessageReceiver.onMessage(): got message {}", ((TextMessage)msg).getText());
  }
}
