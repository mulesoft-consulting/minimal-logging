package org.mule.consulting.logging.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mule.runtime.api.component.location.ComponentLocation;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.dsl.xml.ParameterDsl;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.runtime.process.CompletionCallback;
import org.mule.runtime.extension.api.runtime.route.Chain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;


/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class MinLog {
	private final Logger LOGGER = LoggerFactory.getLogger(MinLog.class);

	/**
	 * Generate a transaction id if required, otherwise return the current
	 * transaction id. Creates a transactionProperties (as a LinkedHashMap<String, String>) containing the x-transaction-id and optionally 
	 * any values from the specified headers (as a MultiMap).
	 */
	@MediaType(value = ANY, strict = false)
	@Alias("new")
	public LinkedHashMap<String, String> setTransactionProperties(@Optional Map headers, ComponentLocation location) {
		LinkedHashMap<String, String> transactionProperties = new LinkedHashMap<String, String>();

		addLocation("new", transactionProperties, location);

//		if (headers == null) {
//			System.out.println("headers are null");
//		} else {
//			System.out.println("headers: " + headers.toString());
//		}
		if (headers != null) {
			if (headers.get("client_id") != null) {
				transactionProperties.put("client_id", (String) headers.get("client_id"));
			}
			if (headers.get("x-transaction-id") != null) {
				transactionProperties.put("x-transaction-id", (String) headers.get("x-transaction-id"));
			} else if (headers.get("x_transaction_id") != null) {
				transactionProperties.put("x-transaction-id", (String) headers.get("x_transaction_id"));				
			} else {
				transactionProperties.put("x-transaction-id", UUID.randomUUID().toString());
				logMessage("INFO", "Generated x-transaction-id", transactionProperties);
			}
		} else {
			transactionProperties.put("x-transaction-id", UUID.randomUUID().toString());
			logMessage("INFO", "Generated x-transaction-id", transactionProperties);
		}
		return transactionProperties;
	}
	
	/**
	 * Add the specified key/value pair to the indicated transactionProperties LinkedHashMap
	 * 
	 * @param key
	 * @param value
	 * @param transactionProperties
	 * @param location
	 * @return
	 */
	@MediaType(value = ANY, strict = false)
	public LinkedHashMap<String, String> put(String key, String value, @Optional LinkedHashMap<String, String> transactionProperties, ComponentLocation location) {
		
		LinkedHashMap<String, String> tempMap = new LinkedHashMap<String, String>();
		if (transactionProperties != null) {
			tempMap.putAll(transactionProperties);
		}
		tempMap.put(key, value);
		return tempMap;
	}
	
	
	/**
	 * Add the all specified (newProperties) LinkedHashMap key/value pairs to the indicated transactionProperties LinkedHashMap
	 * 
	 * @param newProperties
	 * @param transactionProperties
	 * @param location
	 * @return
	 */
	@MediaType(value = ANY, strict = false)
	public LinkedHashMap<String, String> putAll(LinkedHashMap<String, String> newProperties, @Optional LinkedHashMap<String, String> transactionProperties, ComponentLocation location) {
		
		LinkedHashMap<String, String> tempMap = new LinkedHashMap<String, String>();
		if (transactionProperties != null) {
			tempMap.putAll(transactionProperties);
		}
		tempMap.putAll(newProperties);
		return tempMap;
	}

	/**
	 * Scope for generating enter and exit message with elapsed time
	 * 
	 * @param transactionProperties
	 * @param location
	 * @param operations
	 * @param callback
	 */
	public void timed(
			@Optional(defaultValue="#[{}]") @ParameterDsl(allowInlineDefinition=false) LinkedHashMap<String, String> transactionProperties, 
			ComponentLocation location,
			Chain operations,
			CompletionCallback<Object, Object> callback) {
		
		long startTime = System.currentTimeMillis();

		LinkedHashMap<String, String> tempMap = new LinkedHashMap<String, String>();
		if (transactionProperties != null) {
			for (String item : transactionProperties.keySet()) {
				tempMap.put(item, transactionProperties.get(item));
			}
		}

		addLocation("timer", tempMap, location);

		StringBuilder sb = new StringBuilder();
		sb.append("enter ");
		logMessage("INFO", sb.toString(), tempMap);

		operations.process(result -> {
			long elapsedTime = System.currentTimeMillis() - startTime;
			tempMap.put("elapsedMS", Long.toString(elapsedTime));
			StringBuilder sbsuccess = new StringBuilder();
			sbsuccess.append("exit ");
			logMessage("INFO", sbsuccess.toString(), tempMap);
			callback.success(result);
		}, (error, previous) -> {
			long elapsedTime = System.currentTimeMillis() - startTime;
			tempMap.put("elapsedMS", Long.toString(elapsedTime));
			StringBuilder sberror = new StringBuilder();
			sberror.append("exit with error ").append(error.getMessage());
			logMessage("INFO", sberror.toString(), tempMap);
			callback.error(error);
		});
	}

	/**
	 * Generate a log message of level INFO, WARN, ERROR or DEBUG.  All other levels result in no message generated.
	 * 
	 * @param level
	 * @param msg
	 * @param transactionProperties
	 * @param location
	 */
	public void log(@Optional(defaultValue="INFO") String level, 
			String msg,
					@Optional(defaultValue="#[{}]") @ParameterDsl(allowInlineDefinition=false) LinkedHashMap<String, String> transactionProperties,
					@Optional(defaultValue="#[{}]") @ParameterDsl(allowInlineDefinition=false) LinkedHashMap<String, String> eventData,
					ComponentLocation location) {

		LinkedHashMap<String, String> tempMap = new LinkedHashMap<String, String>();
		if (transactionProperties != null) {
			for (String item : transactionProperties.keySet()) {
				tempMap.put(item, transactionProperties.get(item));
			}
		}

		addLocation("log", tempMap, location);
		if(eventData != null) {
			logMessage(level.toUpperCase(), msg, tempMap, eventData);
		} else {
			logMessage(level.toUpperCase(), msg, tempMap);
		}

	}
	
	/*
	 * Add component location values to the transactionProperties
	 */
	private void addLocation(String sourcePrefix, LinkedHashMap <String, String> transactionProperties, ComponentLocation location) {
		if (location != null) {
			java.util.Optional<String> fileName = location.getFileName();
			java.util.Optional<Integer> lineNumber = location.getLineInFile();
			transactionProperties.put(sourcePrefix + ".flow", location.getRootContainerName());
			if (fileName.isPresent()) {
				transactionProperties.put(sourcePrefix + ".fileName", fileName.get());
			}
			if (lineNumber.isPresent()) {
				transactionProperties.put(sourcePrefix + "lineNumber", lineNumber.get().toString());
			}
		} else {
			LOGGER.debug("Missing location information");
		}
	}
	
	/*
	 * Write a log message
	 */
	private void logMessage(String level, String msg, LinkedHashMap<String, String> transactionProperties) {
		logMessage(level,msg,transactionProperties,Collections.emptyMap());
	}

	/*
	 * Write a log message
	 */
	private void logMessage(String level, String msg, LinkedHashMap<String, String> transactionProperties, Map<String, String> eventData) {

		switch (level) {
			case ("INFO"):
				LOGGER.info(formatLogMsg(msg, transactionProperties, eventData));
				break;
			case ("DEBUG"):
				LOGGER.debug(formatLogMsg(msg, transactionProperties, eventData));
				break;
			case ("ERROR"):
				LOGGER.error(formatLogMsg(msg, transactionProperties, eventData));
				break;
			case ("WARN"):
				LOGGER.warn(formatLogMsg(msg, transactionProperties, eventData));
				break;
			default:
				//do nothing
		}
	}

	/*
	 * Create the log message by adding the transactionProperties to the message as a JSON payload
	 */
	private String formatLogMsg(String msg, LinkedHashMap<String, String> transactionProperties) {
		return formatLogMsg(msg,transactionProperties, Collections.emptyMap());
	}
	
	/*
	 * Create the log message by adding the transactionProperties to the message as a JSON payload
	 */
	private String formatLogMsg(String msg, LinkedHashMap<String, String> transactionProperties, Map<String, String> eventData) {
		ObjectMapper mapper = new ObjectMapper();
		String logStatement = "";
		Map<String, Object> logData = new LinkedHashMap<>();
		try {
			if (transactionProperties != null) {
				logData.put("context",transactionProperties);
			}
			if (eventData != null && !eventData.isEmpty()) {
				logData.put("eventData",eventData);
			}
			if (msg != null) {
				logData.put("message",msg);
			}
			if(logData!=null) {
				logStatement = mapper.writeValueAsString(logData);
			}

		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder(logStatement);
		return sb.toString();
	}
}
