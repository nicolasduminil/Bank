package fr.simplex_software.bank.money_transfer;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.autoconfigure.jdbc.*;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MoneyTransferApplication
{
  public static void main(String[] args)
  {
    SpringApplication.run(MoneyTransferApplication.class, args);
  }
}
