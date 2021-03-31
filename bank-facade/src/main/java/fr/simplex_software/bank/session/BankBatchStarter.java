package fr.simplex_software.bank.session;

import fr.simplex_software.bank.money_transfer.batch.*;
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
public class BankBatchStarter extends DefaultBatchConfigurer
{
  @PostConstruct
  public void runMoneyTransferBatchJob()
  {
    MoneyTransferBatchlet moneyTransferTasklet = new MoneyTransferBatchlet();
    JobBuilderFactory jobBuilderFactory = new JobBuilderFactory(this.getJobRepository());
    StepBuilderFactory stepBuilderFactory = new StepBuilderFactory(this.getJobRepository(), this.getTransactionManager());
    Step moneyTransferJobStep = stepBuilderFactory.get("moneyTransferJobStep")
      .tasklet(moneyTransferTasklet)
      .build();
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
