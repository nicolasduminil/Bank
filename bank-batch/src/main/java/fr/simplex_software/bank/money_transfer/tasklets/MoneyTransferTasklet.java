package fr.simplex_software.bank.money_transfer.tasklets;

import fr.simplex_software.bank.money_transfer.jaxb.*;
import org.apache.commons.lang3.builder.*;
import org.springframework.batch.core.*;
import org.springframework.batch.core.scope.context.*;
import org.springframework.batch.core.step.tasklet.*;
import org.springframework.batch.repeat.*;
import org.springframework.beans.factory.annotation.*;

import javax.annotation.*;
import javax.jms.*;
import javax.xml.bind.*;
import java.io.*;

public class MoneyTransferTasklet implements Tasklet
{
  @Value("${bank.money-transfer.source.file.name}")
  private String sourceFileName;
  @Autowired
  private ConnectionFactory connectionFactory;
  @Resource(mappedName = "java:/jms/queue/BanQ")
  private Queue queue;

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception
  {
    JAXBContext jaxbContext = JAXBContext.newInstance(MoneyTransfers.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    InputStream is = this.getClass().getClassLoader().getResourceAsStream(sourceFileName);
    MoneyTransfers mts = (MoneyTransfers) jaxbUnmarshaller.unmarshal(is);
    JMSProducer producer = connectionFactory.createContext().createProducer();
    mts.getMoneyTransfers().forEach(mt -> producer.send(queue, new ReflectionToStringBuilder(mt, new MultilineRecursiveToStringStyle()).toString()));
    return RepeatStatus.FINISHED;
  }
}
