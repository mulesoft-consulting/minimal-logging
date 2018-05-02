# minimal-logging
This provides the minimum logging functions in Mule 4.1 and later

This package provides a connector with operations that codify the minimal logging an application should produce. It can be modified to extend its capabilities. 
There is some overlapping of function with the "Dynamic Tracing" that is provided by the Anypoint Platform. Note however, that the x-transaction-id stamping provided in
this connector is entirely manually configured and coded by convention, whereas the Anypoint Platform solution requires no coding and is a superior solution.
 
## Installation Dependency
Add the following dependency to your Mule 4.1 project:

```
		<dependency>
			<groupId>org.mule.consulting.logging</groupId>
			<artifactId>minimal-logging</artifactId>
			<version>1.0.0</version>
			<classifier>mule-plugin</classifier>
		</dependency>
```
