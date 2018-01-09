package fr.simplex_software.bank.session;

import javax.annotation.*;
import javax.batch.runtime.*;
import javax.ejb.*;
import javax.ejb.Singleton;
import javax.inject.*;

import org.apache.deltaspike.core.api.config.*;
import org.slf4j.*;

import fr.simplex_software.bank.money_transfer.batch.*;

@Singleton
@Startup
public class BankBatchStarter
{
  private static final Logger slf4jLogger = LoggerFactory.getLogger(BankBatchStarter.class);
  
  @Inject
  @ConfigProperty(name = "bank.money-transfer.batch.starter.jobID")
  private String jobID;
  
  @Inject
  private MoneyTransferBatchlet mtb;

  @PostConstruct
  public void onStartup()
  {
    slf4jLogger.info("*** BankBatchStarter.onStartup(): starting job {} {}", jobID, mtb);
    BatchRuntime.getJobOperator().start(jobID, null);
  }
}
