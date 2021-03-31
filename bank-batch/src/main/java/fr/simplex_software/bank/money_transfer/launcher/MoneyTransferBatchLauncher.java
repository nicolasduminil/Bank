package fr.simplex_software.bank.money_transfer.launcher;

import lombok.extern.slf4j.*;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import javax.annotation.*;
import java.util.*;

@Component
@Slf4j
public class MoneyTransferBatchLauncher
{
  private final Job job;
  private final JobLauncher jobLauncher;

  @Autowired
  public MoneyTransferBatchLauncher(Job job, JobLauncher jobLauncher)
  {
    this.job = job;
    this.jobLauncher = jobLauncher;
  }

  @PostConstruct
  public void runMoneyTransferBatchJob() throws Exception
  {
    log.info("### MoneyTransferBatchLauncher.runMoneyTransferBatchJob(): Spring Batch job {} is starting", job.getName());
    jobLauncher.run(job, getParams());
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
