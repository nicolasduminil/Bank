package fr.simplex_software.bank.session;

import fr.simplex_software.bank.money_transfer.batch.tasklets.*;
import lombok.extern.slf4j.*;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.launch.support.*;

import javax.annotation.*;
import javax.ejb.*;
import java.util.*;

@Singleton
@Startup
@Slf4j
public class MoneyTransferBatchLauncher extends DefaultBatchConfigurer
{
  @PostConstruct
  public void runMoneyTransferBatchJob()
  {
    MoneyTransferTasklet moneyTransferTasklet = new MoneyTransferTasklet();
    JobBuilderFactory jobBuilderFactory = new JobBuilderFactory(this.getJobRepository());
    StepBuilderFactory stepBuilderFactory = new StepBuilderFactory(this.getJobRepository(), this.getTransactionManager());
    Step moneyTransferJobStep = stepBuilderFactory.get("moneyTransferJobStep")
      .tasklet(moneyTransferTasklet)
      .build();
    log.info("### MoneyTransferBatchLauncher.runMoneyTransferBatchJob(): jobBuilderFactory {}, platformTransactionManager {}, jobRepository {}, stepBuilderFactory {}", jobBuilderFactory, this.getTransactionManager(), this.getJobRepository(), stepBuilderFactory);
    Job job = jobBuilderFactory.get("moneyTransferJob")
      .incrementer(new RunIdIncrementer())
      .flow(moneyTransferJobStep)
      .end()
      .build();
    log.info("### MoneyTransferBatchLauncher.runMoneyTransferBatchJob(): Spring Batch job {} is starting", job.getName());
    try
    {
      this.getJobLauncher().run(job, getParams());
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    log.info("### MoneyTransferBatchLauncher.runMoneyTransferBatchJob(): Spring Batch job {} is stopping", job.getName());
  }

  private JobParameters getParams()
  {
    Map<String, JobParameter> parameters = new HashMap<>();
    JobParameter parameter = new JobParameter(new Date());
    parameters.put("currentTime", parameter);
    return new JobParameters(parameters);
  }
}
