
# Using Java EE 7 Batch Procession with Wildfly

The master branch of this repository demonstrates the use of the JSR 352 
specifications in Java EE 7. The current branch implements the same business case
but uses Spring Batch 4.3.2 (the last release as per this writing).

Additionally, as opposed to the branch labeled spring-batch, the current branch 
is using Spring Boot instead of plain Spring.

Deploying Spring Boot applications and services in Wildfly might be tricky. Hence,
we completely modified the project structure such that to comply with Spring Boot.
While it seems that deploying Spring Boot applications in EAR is possible, we 
prefered a standard Spring Boot WAR.

Accordingly, this project is very different compared to the ones on the branches 
master and spring-batch. It uses Spring Boot main class and Spring Beans instead 
of CDI (Context and Dependency Injection). Also, instead of MDB (Message Driven Beans)
it uses Spring JMS Listener Beans.

Here is the content of the deployment:
  - the module `bank-batch`: it contains everything, including the batch definition,
    the JMS configuration and the batch execution logic (WAR)
  - the module `bank-jaxb`: the domain module (JAR)

In order to build an run the sample the same steps as the ones on the master branch apply.