package fr.simplex_software.bank.money_transfer.batch.tasklets;

import fr.simplex_software.bank.money_transfer.jaxb.*;
import lombok.extern.slf4j.*;
import org.apache.commons.lang3.builder.*;
import org.springframework.batch.core.*;
import org.springframework.batch.core.scope.context.*;
import org.springframework.batch.core.step.tasklet.*;
import org.springframework.batch.repeat.*;
import org.springframework.beans.factory.annotation.*;

import javax.annotation.*;
import javax.inject.*;
import javax.jms.*;
import javax.xml.bind.*;
import java.io.*;

@Slf4j
@Named
public class MoneyTransferTasklet implements Tasklet
{
  private final String sourceFileName = "xml/money-transfer.xml";
  @Resource(name="java:/ConnectionFactory")
  private ConnectionFactory connectionFactory;
  @Resource(mappedName = "java:/jms/queue/BanQ")
  private Queue queue;

  @Override
  public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception
  {
    log.info("### MoneyTransferTasklet.execute(): Started");
    JAXBContext jaxbContext = JAXBContext.newInstance(MoneyTransfers.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    log.info("### MoneyTransferTasklet.execute(): Unmarshaler created");
    InputStream is = this.getClass().getClassLoader().getResourceAsStream(sourceFileName);
    log.info("### MoneyTransferTasklet.execute(): inputStream {}", is);
    MoneyTransfers mts = (MoneyTransfers) jaxbUnmarshaller.unmarshal(is);
    log.info("### MoneyTransferTasklet.execute(): Unmarshaling done");
    JMSProducer producer = connectionFactory.createContext().createProducer();
    mts.getMoneyTransfers().forEach(mt -> producer.send(queue, new ReflectionToStringBuilder(mt, new MultilineRecursiveToStringStyle()).toString()));
    return RepeatStatus.FINISHED;
  }
}
