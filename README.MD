
# Using Java EE 7 Batch Procession with Wildfly

The master branch of this repository demonstrates the use of the JSR 352 
specifications in Java EE 7. The current branch implements the same business case
but uses Spring Batch 4.3.2 (the last release as per this writing).

The project structure is the same as the one of the master branch, meaning that
the application is deployed as an EAR on Wildfly 23.0 application server. The 
thing which changes is that the batch execution is driven by Spring Batch API
instead of the JSR 352 (JBeret) API.

Here is the content of the deployment:
  - the module `bank-master`: the master module (POM)
  - the module `bank-batch`: the batch definition module (JAR)
  - the module `bank-facade`: the batch starter module (EJB JAR)
  - the module `bank-jaxb`: the domain module (JAR)

In order to build an run the sample the same steps as the ones on the master branch apply.