package fr.simplex_software.bank.money_transfer.batch;

import fr.simplex_software.bank.money_transfer.jaxb.*;
import lombok.extern.slf4j.*;
import org.apache.commons.lang3.builder.*;
import org.springframework.batch.core.*;
import org.springframework.batch.core.scope.context.*;
import org.springframework.batch.core.step.tasklet.*;
import org.springframework.batch.repeat.*;

import javax.faces.bean.*;
import javax.jms.*;
import javax.naming.*;
import javax.xml.bind.*;

@ApplicationScoped
@Slf4j
public class MoneyTransferBatchlet implements Tasklet
{
  private static final String sourceFileName = "xml/money-transfer.xml";
  private static final String jmsDestinationJndiName ="java:/jms/queue/BanQ";
  private static final String jmsConnectionFactoryJndiName = "java:/ConnectionFactory";
  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception
  {
    MoneyTransfers mts = (MoneyTransfers) JAXBContext.newInstance(MoneyTransfers.class)
      .createUnmarshaller()
      .unmarshal(this.getClass().getClassLoader()
        .getResourceAsStream(sourceFileName));
    Queue queue = InitialContext.doLookup(jmsDestinationJndiName);
    JMSProducer producer = ((ConnectionFactory)InitialContext.doLookup(jmsConnectionFactoryJndiName)).createContext().createProducer();
    mts.getMoneyTransfers().forEach(mt -> producer.send(queue, new ReflectionToStringBuilder(mt, new MultilineRecursiveToStringStyle()).toString()));
    return RepeatStatus.FINISHED;
  }
}
