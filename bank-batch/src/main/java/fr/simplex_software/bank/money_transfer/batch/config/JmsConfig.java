package fr.simplex_software.bank.money_transfer.batch.config;

import org.springframework.context.annotation.*;

import javax.jms.*;
import javax.naming.*;

@Configuration
public class JmsConfig
{
  @Bean
  public ConnectionFactory connectionFactory() throws NamingException
  {
    return InitialContext.doLookup("java:/ConnectionFactory");
  }
}
