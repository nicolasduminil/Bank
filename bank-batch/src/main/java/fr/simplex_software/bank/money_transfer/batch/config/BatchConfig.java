package fr.simplex_software.bank.money_transfer.batch.config;

import fr.simplex_software.bank.money_transfer.batch.tasklets.*;
import lombok.extern.slf4j.*;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.launch.support.*;
import org.springframework.context.annotation.*;

import javax.sql.*;

@Configuration
@EnableBatchProcessing
@Slf4j
public class BatchConfig extends DefaultBatchConfigurer
{
  @Bean
  public Job moneyTransferJob(Step moneyTransferJobStep, JobBuilderFactory jobBuilderFactory)
  {
    return jobBuilderFactory.get("moneyTransferJob")
      .incrementer(new RunIdIncrementer())
      .flow(moneyTransferJobStep)
      .end()
      .build();
  }

  @Bean
  public Step moneyTransferJobStep(StepBuilderFactory stepBuilderFactory)
  {
    return stepBuilderFactory.get("moneyTransferJobStep")
      .tasklet(moneyTransferTasklet())
      .build();
  }

  @Bean
  public MoneyTransferTasklet moneyTransferTasklet()
  {
    return new MoneyTransferTasklet();
  }

  @Override
  public void setDataSource(DataSource dataSource){}
}
