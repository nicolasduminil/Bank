This blog entry demonstrates the use of the JSR 352 specifications in Java EE 7. The JSR 352 specs define the implementation and the management of the batch processing. Historically, the Java batch processing made the object of the Spring Batch framework. Now, with the JSR 352, the Java batch processing became a part of Java EE, meaning that it is standard and it is implemented by any compliant application server, without any add on or complementary libraries.

Our demo uses Wildfly 10.1.0, the community release of the famous RedHat JBoss EAP, but things should also work in a similar manner with any other Java EE 7 compliant application server. In this last case, some slight modifications in the associated maven POM files, are of course required.

The Batch Processing has this particularity of being able to process large quantities of data. They should be seen as long running processes, comparable with business processes, without the inherent heavyness of these last ones. Like business processes, batches are based on an XML notation describing workflows. But as opposed to the BPMN2 language, which is the XML notation describing business processes, the JSR 352 specifications are specifically targeting Java platforms and, as such, provide a complete Java API guaranteeing portability.

The BPMN2 specifications only cover the workflow design process, not the runtime, and each implemnation comes with its own set of tools which makes very difficult the migration process between different products. Converselly, using the a JSR 352 implemntation, a workflow designed to work on Wildfly or JBoss EAP may be very easily migated to Glassfish, WebLogic or WebSphere.

The implementation of the JSR 352 specifications used here is JBeret which comes out-of-the-box with Wildfly 10.1.0 or higher and JBoss EAP 7.

JSR 352 versus Spring Batch
As already mentioned, historically the Batch Processing in Java has been made the object of Spring Batch. So why would one need the JSR 352 ? Well, there is a long and old debate here between Spring and Java EE and, without pretending to bring anything new here or to close in anyway this debate, the idea is that, while Java EE is a set of standard specifications drafted by a non-profit international foundation, named JCP (Java Community Process), which Executive Commitee groups together some of the majors like IBM, Oracle, RedHat, HP, etc., Spring is an open-source Java framework owned by Pivotal Software, a services company based in California.

While developers are implementing since ages batch processing in Java with Spring Batch, this requires to integrate Spring in the application server's landscape which, depending on the application server, might be a more or less difficult process. In any case, this requires to download and install the hundreds of the Spring libraries and to answer a quite complex question: what happens if 100 hundreds components deployed on the application servers use Spring Batch ? Should these components embed in their own archives the Spring libraries or should kind of shared library be constructed and deploied only once on the application server, such that all the components can use it ? Depending on the strategy adopted here, integrating Spring with an application server could be a tough process. Not to mention any more the fact that, once having integrated Spring into an application server platform, this platform is not any more supported by its vendor, as considered not standard. From this point on, you're responsible of what ever may happen to your platform and, the day when some obscure conflict prevents your services to run, you have no one to legally turn to. The only support in this case is the community. 

Using JSR 352 is very different in the sense that the implementation comes with the application server and is a part of its binaries. There is no add-on to download, nothing to integrate, you don't need to define any pooling or sharing strategy. Accordingly, there couldn't be any conflicts preventing you to run your services in production and, if this happens nevertheless, in addition to the community support, your vendor is legally responsible to solve the issues within the time limt defined by your support contract.

Some Basic Concepts
The basic concepts proposed by the JSR 352 are very similar to those defined by Spring Batch. A batch is a job described in a specific XML notation named JSL (Job Specification Language). Each job consists of a set of steps which should be seen as atomic stages. JSL describes job's steps in terms of two main programming models:

Chunks: discrete parts of a step consisting in a reader-processor-writer design pattern. According to this design pattern, a chunck consists of a reader which provides chunk input data, a processor which transforms input data to output data, and a writer which provides the chunk's output data, i.e. the processin results. Each chunk output data is the next chunk input data.
Batchlets: atomic parts of a step, more discrete then chunks and not requiring a set of reading, processing and writing operations. A batchlet is more atomic the a chunk as it is not divided into different operations.
A job is executed through the JobOperation interface. Substitution properties may also be specified in the job's JSL definition for customization purposes. The runtime loads the batch artifacts described JSL and runs the job on a separate thread. All steps in the job run on the same thread unless partitions or splits are used. The JSL description of the job may contain conditional logic that controls the order in which the steps run. The runtime handles your job’s conditional logic, ensuring the correct execution sequence.

Another important basic notion is the one of partition. It is defined as a discrete functional closure allowing to run a separate instance of the step’s chunk or batchlet artifacts. For example, if we want to process 100 records in a databse table and our processing time is estimated to take 10 minutes, using partitioning, we can group by 10 our records such that to have 10 partitions and to reduce at 1 minute our processing time.

The Business Case
In order to illustrate our speech we will consider a vey classical busines case: the monet transfer. A bank eeds to perform massive money transfer. The information comes in XML files having an associated grammar described by an XSD. Our batch is then responsible of parsing the XML files, unmarshalling the XML payload to Java domain objects, converting these domain objects into text messages and sending them to an output JMS destination. The project serving to demonstrate the business case we are going to discuss can be found here. It is a maven multi-project divided in several modules, as follows:

bank-master: the master POM
bank-jaxb: this is the JAXB module which unmarshalls the money transfer oerations, described in an XML file, into Java domain objects
bank-facade: this project implements the facade design pattern. It is implemnted as a singleton automatically started at the deployment time and which starts the workflow
bank-batch: this project contains the batchlet required to performthe money transfer operations
bank-ear: this project aims at packaging the whole stuff as an EAR archive. It also contains the required scripts and plugins such that to create and run a docker container with the Wildfly server inside and with our EAR deployed.
Let's try to look in a more detailed manner to each individual module.

The bank-master module
This is the master module defining the dependecies and plugins to be used, together with their associated versions, as well as all the other modules.

The bank-facde module
This is an EJB module deployed as an EJB-JAR. It contains a singleton which starts the batch. Here is the code:

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
    slf4jLogger.debug("*** BankBatchStarter.onStartup(): starting job {} {}", jobID, mtb);
    BatchRuntime.getJobOperator().start(jobID, null);
  }
}

The code above is showing a Java EE singleton, which automatically starts as soon as the EAR is deployed. Once started, it executes the onStartup() method, annotated with the @PostConstruct annotation which, in turn, will instatiate the JobOperator interface and start the batch. The batch is identified by its ID. Here we are using the Apache Deltaspike CDI library to inject properties from a property file. The private attribute jobID will be injected the value  of the property named "bank.money-transfer.batch.starter.jobID" extracted from the apache-deltaspike.properties file. This value, which is the string "batch-job" is then used to identify the required job in the JSL file batch-jobs/bank-job.xml in the META-INF directory. Here is the JSL file:

<?xml version="1.0" encoding="UTF-8"?>
<job id="bank-job" xmlns="http://xmlns.jcp.org/xml/ns/javaee" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
  xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee 
  http://xmlns.jcp.org/xml/ns/javaee/jobXML_1_0.xsd" version="1.0">
  <flow id="money-transfer">
    <step id="unmarshall-files">
      <batchlet ref="moneyTransferBatchlet" />
    </step>
  </flow>
</job>

Our goal here being to demonstrate the general use of the batch processing in Java and not to go into all the subleties of the JSL, we present a very simplified JSL file defining a flow, named "money-transfer", having only one step. This step, which name is unmarshall-files, will run the batchlet identified on the behalf of the CDI name moneyTransferBatchlet. The batchlet will be shown below as it belongs to the batch module.

The bank-batch module
This is the batch project which contains the batchlet. Here is the code:

@Named
public class MoneyTransferBatchlet extends AbstractBatchlet implements Serializable
{
  private static final long serialVersionUID = 1L;

  private static final Logger slf4jLogger = 
    LoggerFactory.getLogger(MoneyTransferBatchlet.class);

  @Inject
  @ConfigProperty(name = "bank.money-transfer.source.file.name")
  private String sourceFileName;

  @Resource(name="jms/QueueConnectionFactory")
  private ConnectionFactory connectionFactory;
  @Resource(mappedName = "java:/jms/queue/BanQ")
  private Queue queue;

  public String process() throws Exception
  {
    slf4jLogger.debug("*** MoneyTransferBatchlet.process(): Running ...");
    JMSProducer producer = connectionFactory.createContext().createProducer();
    JAXBContext jaxbContext = JAXBContext.newInstance(MoneyTransfers.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    InputStream is = this.getClass().getClassLoader().getResourceAsStream(sourceFileName);
    MoneyTransfers mts = (MoneyTransfers) jaxbUnmarshaller.unmarshal(is);
    for (MoneyTransfer mt : mts.getMoneyTransfers())
      producer.send(queue, new ReflectionToStringBuilder(mt).toString());
    return BatchStatus.COMPLETED.name();
  }
}

Like any batch, the one above implemnts the inteface AbstarctBatchlet which defines the process() method. This is the place where evrything in the batch happens. After having instatiated a JMS producer, the code is retriving the XML input stream containing the money transfer to be done and it unmarshalls it into Java domain objects. Then for each money transfer in the input stream, a new text message is produced. Notice the way that the required JMS artifacts, the connection factory and the queue, are injected.

The code could have been even simpler by directly injecting the JMSContext instead of using the JMS connection factory to create the context and the producer. But this would have required a CDI request scope, which we don't have here as our starting point is a Java EE signleton, not a servlet.

Last but not least, here is the XML file containing the money transfer:

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xml>
<ss:moneyTransfers xmlns:ss="http://www.simplex-software.fr/money-transfer" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
  xsi:schemaLocation="http://www.simplex-software.fr/money-transfer ../xsd/money-transfer.xsd ">
  <ss:moneyTransfer>
    <ss:sourceAccount accountID="ID0198" accountNumber="4007654329" accountType="SAVINGS" 
      bankName="Société Générale" sortCode="AB234" transCode="XY">
      <ss:bankAddress cityName="Paris" countryName="France" poBox="PO1234" streetName="rue de 
        Londres" streetNumber="24" zipCode="75008"/>
    </ss:sourceAccount>
    <ss:targetAccount accountID="ID0298" accountNumber="4007654330" accountType="SAVINGS"  
      bankName="ING" sortCode="SC9821" transCode="18er670">
      <ss:bankAddress cityName="Brussels" countryName="Belgium" poBox="None" 
        streetName="Chaussée de Waterloo" streetNumber="49" zipCode="B1002"/>
    </ss:targetAccount>
    <ss:amount>51000.85</ss:amount>
  </ss:moneyTransfer>
</ss:moneyTransfers>

This is a very simple XML definition of some money transfer operations. It will corresponds to a simple gramma which is presented below.

The bank-jaxb module
This is a simple module which compiles the XSD grammar of the money transfer projects into Java domain objects. Everything is done by the jaxb2-maven-plugin plugin, as shown below:

<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>jaxb2-maven-plugin</artifactId>
  <executions>
    <execution>
      <goals>
        <goal>xjc</goal>
      </goals>
    </execution>
  </executions>
  <configuration>
    <packageName>fr.simplex_software.bank.money_transfer.jaxb</packageName>
    <outputDirectory>${basedir}/src/main/java</outputDirectory>
    <schemaDirectory>${basedir}/src/main/resources/xsd</schemaDirectory>
    <extension>true</extension>
  </configuration>
</plugin>

Here we are using xjc, the XSD to Java compiler, i order to compile the XSD file in the src/main/resources/xsd directory into Java domain objects belonging to the fr.simplex_software.bank.money_transfer.jaxb package. The XSD file is as follows:

<schema xmlns="http://www.w3.org/2001/XMLSchema" 
  targetNamespace="http://www.simplex-software.fr/money-transfer" 
  xmlns:ss="http://www.simplex-software.fr/money-transfer" 
  xmlns:xjc="http://java.sun.com/xml/ns/jaxb/xjc"
  xmlns:jaxb="http://java.sun.com/xml/ns/jaxb" 
  jaxb:extensionBindingPrefixes="xjc" jaxb:version="2.0" elementFormDefault="qualified">
  <annotation>
    <appinfo>
      <jaxb:globalBindings>
        <xjc:simple/>
      </jaxb:globalBindings>
    </appinfo>
  </annotation>
  <simpleType name="BankAccountType" final="restriction">
    <restriction base="string">
      <enumeration value="SAVINGS" />
      <enumeration value="CHECKING" />
    </restriction>
  </simpleType>

  <complexType name="BankAddress">
    <attribute name="streetName" type="string" />
    <attribute name="streetNumber" type="string" />
    <attribute name="poBox" type="string" />
    <attribute name="cityName" type="string" />
    <attribute name="zipCode" type="string" />
    <attribute name="countryName" type="string" />
  </complexType>

  <complexType name="BankAccount">
    <sequence>
      <element name="bankAddress" type="ss:BankAddress" maxOccurs="1" minOccurs="1" />
    </sequence>
    <attribute name="accountID" type="string" />
    <attribute name="accountType" type="ss:BankAccountType" />
    <attribute name="sortCode" type="string" />
    <attribute name="accountNumber" type="string" />
    <attribute name="transCode" type="string" />
    <attribute name="bankName" type="string" />
  </complexType>

  <complexType name="SourceAccount">
    <complexContent>
      <extension base="ss:BankAccount" />
    </complexContent>
  </complexType>

  <complexType name="TargetAccount">
    <complexContent>
      <extension base="ss:BankAccount" />
    </complexContent>
  </complexType>

  <complexType name="MoneyTransfer">
    <sequence>
      <element name="sourceAccount" type="ss:SourceAccount" maxOccurs="1" minOccurs="1" />
      <element name="targetAccount" type="ss:TargetAccount" maxOccurs="1" minOccurs="1" />
      <element name="amount" type="decimal" maxOccurs="1" minOccurs="1" />
    </sequence>
  </complexType>

  <complexType name="MoneyTransfers">
    <sequence>
      <element name="moneyTransfer" type="ss:MoneyTransfer" 
        maxOccurs="unbounded" minOccurs="1"/> 
    </sequence>
  </complexType>

  <element name="moneyTransfers" type="ss:MoneyTransfers"></element>
</schema>

The XML Schema above uses the JAXB global bindings in order to define the @XmlRootElement annotations. This is expressed here by the <xjc:simple/> construct and activated by the <extension>true</extension> parameter in the JAX-B plugin above. Beside that, the XSD defines some complex elements, named MoneyTransfer, SourceAccount, TargetAccount, BankAddress, together with an enumerated element named BankAccountType. Finally, it defines a list of MoneyTransfer complex elements.

The bank-ear module
This module is packaging the whole stuff as an EAR archiven using the maven-ear-plugin. It also uses the docker-maven-plugin to create a docker container running the jboss/wildfly:10.1.0.Final docker image from DockerHub. Here is the associated code:

<plugin>
  <groupId>io.fabric8</groupId>
  <artifactId>docker-maven-plugin</artifactId>
  <configuration>
    <images>
      <image>
        <alias>wfy10</alias>
        <name>jboss/wildfly:10.1.0.Final</name>
        <run>
          <namingStrategy>alias</namingStrategy>
          <cmd>/opt/jboss/wildfly/bin/standalone.sh -c standalone-full.xml 
            -b 0.0.0.0 -bmanagement 0.0.0.0</cmd>
            <volumes>
              <bind>
                <volume>/home/nicolas/workspace/bank-master/bank-
                  ear/src/main/docker/customization:/opt/jboss/wildfly/customization</volume>
                 <volume>/home/nicolas/workspace/bank-master/bank-
                   ear/target:/opt/jboss/wildfly/customization/target</volume>
              </bind>
            </volumes>
            <ports>
              <port>8080:8080</port>
              <port>9990:9990</port>
            </ports>
        </run>
      </image>
    </images>
  </configuration>
  <executions>
    <execution>
      <id>docker:start</id>
      <phase>install</phase>
      <goals>
        <goal>start</goal>
      </goals>
    </execution>
  </executions>
</plugin>

The code excerpt above will run the docker container in which it will start the Wildfly application server using the standalone-full profile. Two volumes containing a customization script and, respectivelly, our EAR, will be mounted. Here is the customization script:

#!/bin/bash

WILDFLY_HOME=/opt/jboss/wildfly
JBOSS_CLI=$WILDFLY_HOME/bin/jboss-cli.sh

echo $(date -u) "=> Creating a new JMS queue"
$JBOSS_CLI -c "jms-queue add --queue-address=BanQ --entries=java:/jms/queue/BanQ"

echo $(date -u) "=> Deploy application"
$JBOSS_CLI -c "deploy wildfly/customization/target/bank-ear.ear"

echo $(date -u) "=> Create user"
$WILDFLY_HOME/bin/add-user.sh admin admin

This script uses the jboss-cli utility to create a new JMS queue named BanQ, which JNDI name is java:/jms/queue/BanQ. After that it deploys the EAR and creates the Wildfly admin user.

Running the demo
Now, in order to run the demo, after having cloned the project, perform the following:

nicolas@BEL20:~/workspace/bank-master$ mvn clean install

[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO] 
[INFO] Bank :: The Master POM
[INFO] Bank :: The JAXB Module
[INFO] Bank :: The Batchlet Module
[INFO] Bank :: The EJB JAR Module
[INFO] Bank :: The EAR module
..........

[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO] 
[INFO] Bank :: The Master POM ............................. SUCCESS [ 0.181 s]
[INFO] Bank :: The JAXB Module ............................ SUCCESS [ 2.192 s]
[INFO] Bank :: The Batchlet Module ........................ SUCCESS [ 0.101 s]
[INFO] Bank :: The EJB JAR Module ......................... SUCCESS [ 0.247 s]
[INFO] Bank :: The EAR module ............................. SUCCESS [ 1.417 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 4.358 s
[INFO] Finished at: 2018-01-09T16:46:52+01:00
[INFO] Final Memory: 29M/417M
[INFO] ------------------------------------------------------------------------

The maven project has been installed, the EAR built and the docker container created and started. you can check that as follows:

nicolas@BEL20:~/workspace/bank-master$ docker ps
CONTAINER ID IMAGE COMMAND CREATED STATUS PORTS NAMES
08fdb4eea384 jboss/wildfly:10.1.0.Final "/opt/jboss/wildfly/…" About a minute ago Up About a minute 0.0.0.0:8080->8080/tcp, 0.0.0.0:9990->9990/tcp wfy10

This shows that the docker container named wfy10, based on the jboss/wildfly:10.1.0.Final image from DockerHub, is started. It listens on all the allocated IP adresses, on the ports 8080 and 9990. Now we need to customize our Wildfly installation, by creating the required JMS artifacts, by deploying our EAR and by creating th Wildfly admin user. This is done by executing the customize.sh script.

nicolas@BEL20:~/workspace/bank-master$ docker exec -ti wfy10 ./wildfly/customization/customize.sh
Tue Jan 9 15:49:06 UTC 2018 => Creating a new JMS queue
Tue Jan 9 15:49:07 UTC 2018 => Deploy application
Tue Jan 9 15:49:10 UTC 2018 => Create user
Added user 'admin' to file '/opt/jboss/wildfly/standalone/configuration/mgmt-users.properties'
Added user 'admin' to file '/opt/jboss/wildfly/domain/configuration/mgmt-users.properties'
nicolas@BEL20:~/workspace/bank-master$

Now, in order to check that evrything is working as expected, you can look in the Wildfly log file:

At the end of the log file we should find the following lines:

 15:49:10,578 INFO  [fr.simplex_software.bank.session.MessageReceiver] (Thread-0 (ActiveMQ-client-global-threads-331309011)) *** MessageReceiver.onMessage(): got message fr.simplex_software.bank.money_transfer.jaxb.MoneyTransfer@5aa6d308[sourceAccount=fr.simplex_software.bank.money_transfer.jaxb.SourceAccount@12218d3f,targetAccount=fr.simplex_software.bank.money_transfer.jaxb.TargetAccount@1f73e58e,amount=51000.85]

This shows that the batch has been successfuly executed. Congratulations, you got a docker container running a Wildfly server with our EAR inside, proving that the Java batchprocessing and JMS/ActiveMQ works as expected.
