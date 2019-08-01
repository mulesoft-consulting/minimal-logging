# minimal-logging
The minimum logging operations a Mule 4.1 and later application should have.

This package provides a Mule SDK built connector with operations that encapsulates the minimal logging an application should produce. It can be modified to extend its capabilities. 

There is some overlapping of function with the standard Mule logger component and the "Dynamic Tracing" feature provided by the Anypoint Platform. While the "Dynamic Tracing" feature requires not coding, The minimal-logging operations must be added to the Mule code.

This connector can be used concurrently with the standard Mule logging component as well as the "Dynamic Tracing" feature.

## Initialize the transaction properties
Initialize the transaction properties at the beginning of the flow. This assumes some values will be coming from the inbound
request, in the form of headers. The client_id and the x-transaction-id headers are used to initialize the transaction properties. An example of this is shown below:

```
	<min-log:new target="transactionProperties" headers="#[attributes.headers]" doc:name="Initialize transaction properties" doc:id="03a758b5-7519-4a26-b745-754aebe0c195"/>
```

This takes the headers from the inbound request's attributes.headers and searches for the client_id and client_secret headers which are placed in a new LinkedHashMap<String, String> object and stored in the target variable named transactionProperties. It an x-transaction-id is not found in the headers, it will be generated and added to the transaction properties.

The x-transaction-id is used to track a message from one application to the next in the log messages. All Mule applications should include the min-log:new operation for all inbound transaction processors. In addition, the x-transaction-id should included in all outbound messages so that other systems have the option to include it in their log messages.

Using external log aggregators and analysis tools will be able to use the x-transaction-id to tie all a transaction's log events together. A very useful item for any problem-solving required on a transaction.

It's important to remember to specify the target="", otherwise the new LinkedHashMap will replace the inbound request's payload.

Generate an x-job-id or an x-record-id in the specified transaction properties using these tags. The current job or record id will be replaced:

```
	<min-log:new-job target="transactionProperties" transactionProperties="#[vars.transactionProperties]" doc:id="c5bb0d4e-3d86-4325-bf30-6b702502b6a9" doc:name="new-job" />
```

```
	<min-log:new-record target="transactionProperties" transactionProperties="#[vars.transactionProperties]" doc:id="c5bb0d4e-3d86-4325-bf30-6b702502b6a9" doc:name="new-record" />
```


## Logging Enter and Exit Points
The minimal logging standard is to log a message when a major flow starts and an similar message when the flow stops. The timed scope component will provide this enter and exit message. In addition, the elapsed time in milliseconds will be added to the exit message. 

The timed scope should surround the major flow elements that are to be included in the timing. Usually this is all the components of the flow after the transaction properties have been initialized. This is shown below where the flow components appear between the start and end tags:

```
	<min-log:new target="transactionProperties" headers="#[attributes.headers]" doc:name="Initialize transaction properties" doc:id="03a758b5-7519-4a26-b745-754aebe0c195"/>
	
 	<min-log:timed transactionProperties="#[vars.transactionProperties]">
		<!-- the flow components go here -->
 	</min-log:timed>
```
Make sure to specify the transactionProperties to be used in the log messages. A snapshot of the transactionProperties specified will be used for the enter and exit log messages. The values must all be String, the transactionProperties is defined as a LinkedHashMap <String, String> java type.

The timed scope does not return any values so specifying the target= property is unnecessary. Also note that Studio versions before 7.3 will not correctly display the scope in its graphical view. The Mule runtime does correctly process the scope.

## Adding to the Transaction Properties
During the flow processing, it may be necessary to add additional properties to the transactionProperties. This can be done by either adding a single key/value pair or by adding all the properties from another LinkedHashMap. Use the put and put-all operations to perform the additions. Here is an example of using put:

```
	<min-log:put key="order-no" value="88983dc3" target="transactionProperties" transactionProperties="#[vars.transactionProperties]" doc:id="c5bb0d4e-3d86-4325-bf30-6b702502b6a9" doc:name="Put" />
```
An example of putAll is:

```
	<min-log:put-all newProperties="#[{description:&quot;Peter's Pipers&quot;, date : &quot;2018-01-01&quot;}]" target="transactionProperties" transactionProperties="#[vars.transactionProperties]" doc:name="Put all" doc:id="3a845daf-c981-4bfb-b16f-d08b287ce261" />
```
With both these operations, it's important to remember to specify the target="", otherwise the new LinkedHashMap will replace the current payload.

## Logging a Message
The advantage of the log operation is that it will add the transaction properties to the log message as a JSON payload. The logging level can be INFO, WARN, ERROR or DEBUG. Any other level will not generate a log message. Here is an example of using the log operation:

```
	<min-log:log level="WARN" msg="A Test warning" transactionProperties="#[vars.transactionProperties]" doc:name="Example WARN"/>
```
or use the level operations:

```
	<min-log:info msg="A Test info" transactionProperties="#[vars.transactionProperties]" doc:name="Example INFO"/>
```

```
	<min-log:warn msg="A Test warning" transactionProperties="#[vars.transactionProperties]" doc:name="Example WARN"/>
```

```
	<min-log:error msg="A Test error" transactionProperties="#[vars.transactionProperties]" doc:name="Example ERROR"/>
```

```
	<min-log:debug msg="A Test debug" transactionProperties="#[vars.transactionProperties]" doc:name="Example DEBUG"/>
```

```
	<min-log:trace msg="A Test trace" transactionProperties="#[vars.transactionProperties]" doc:name="Example TRACE log message"/>
```
The log operation(s) does not return any values so specifying the target= property is unnecessary.
 
## Installation Dependency
To use the minimal-logging, add the following dependency to your Mule 4.1 (or greater) project:

```
		<dependency>
			<groupId>org.mule.consulting.logging</groupId>
			<artifactId>minimal-logging</artifactId>
			<version>1.0.4</version>
			<classifier>mule-plugin</classifier>
		</dependency>
```
