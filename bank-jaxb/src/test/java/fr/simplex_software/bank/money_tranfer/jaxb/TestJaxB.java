package fr.simplex_software.bank.money_tranfer.jaxb;


import static org.junit.Assert.*;

import java.io.*;
import java.math.*;

import javax.xml.bind.*;

import org.junit.*;

import fr.simplex_software.bank.money_transfer.jaxb.*;

public class TestJaxB
{
  @Test
  public void test() throws JAXBException
  {
    JAXBContext jaxbContext = JAXBContext.newInstance(MoneyTransfers.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    MoneyTransfers mt = (MoneyTransfers) jaxbUnmarshaller.unmarshal(new File ("src/main/resources/xml/money-transfer.xml"));
    assertNotNull(mt);
    assertTrue (mt.getMoneyTransfers().size() > 0);
    assertEquals (mt.getMoneyTransfers().get(0).getAmount(), new BigDecimal("51000.85"));
  }
}
