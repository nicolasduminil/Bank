package fr.simplex_software.bank.money_transfer.batch;

import java.io.*;

import javax.annotation.*;
import javax.batch.api.*;
import javax.batch.runtime.*;
import javax.inject.*;
import javax.jms.*;
import javax.xml.bind.*;

import org.apache.commons.lang3.builder.*;
import org.apache.deltaspike.core.api.config.*;
import org.slf4j.*;

import fr.simplex_software.bank.money_transfer.jaxb.*;

@Named
public class MoneyTransferBatchlet extends AbstractBatchlet implements Serializable
{
  private static final long serialVersionUID = 1L;

  private static final Logger slf4jLogger = LoggerFactory.getLogger(MoneyTransferBatchlet.class);

  @Inject
  @ConfigProperty(name = "bank.money-transfer.source.file.name")
  private String sourceFileName;

  @Resource(name="jms/QueueConnectionFactory")
  private ConnectionFactory connectionFactory;
  @Resource(mappedName = "java:/jms/queue/BanQ")
  private Queue queue;

  public String process() throws Exception
  {
    slf4jLogger.debug("*** MoneyTransferBatchlet.process(): Running ...");
    JMSProducer producer = connectionFactory.createContext().createProducer();
    JAXBContext jaxbContext = JAXBContext.newInstance(MoneyTransfers.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    InputStream is = this.getClass().getClassLoader().getResourceAsStream(sourceFileName);
    MoneyTransfers mts = (MoneyTransfers) jaxbUnmarshaller.unmarshal(is);
    for (MoneyTransfer mt : mts.getMoneyTransfers())
      producer.send(queue, new ReflectionToStringBuilder(mt).toString());
    return BatchStatus.COMPLETED.name();
  }
}
