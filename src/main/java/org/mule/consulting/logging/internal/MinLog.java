package org.mule.consulting.logging.internal;

import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;

import java.util.LinkedHashMap;
import java.util.UUID;

import org.mule.runtime.api.util.MultiMap;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class MinLog {
	private final Logger LOGGER = LoggerFactory.getLogger(MinLog.class);

  /**
   * Example of an operation that uses the configuration and a connection instance to perform some action.
   */
  @MediaType(value = ANY, strict = false)
  public String retrieveInfo(@Config MinimalloggingConfiguration configuration, @Connection MinimalloggingConnection connection){
    return "Using Configuration [" + configuration.getConfigId() + "] with Connection id [" + connection.getId() + "]";
  }

	/**
	 * Generate a transaction id if required, otherwise return the current
	 * transaction id.
	 */
	@MediaType(value = ANY, strict = false)
	public LinkedHashMap<String, String> setTransactionProperties(@Optional MultiMap headers) {
		LinkedHashMap<String, String> retvalue = new LinkedHashMap<String, String>();

		if (headers != null) {
			if (headers.get("client_id") != null) {
				retvalue.put("client_id", (String) headers.get("client_id"));
			}
			if (headers.get("x-transaction-id") != null) {
				retvalue.put("x-transaction-id", (String) headers.get("x-transaction-id"));
			} else {
				retvalue.put("x-transaction-id", UUID.randomUUID().toString());
				logMessage("Generated x-transaction-id", retvalue);
			}
		}
		return retvalue;
	}
	
	private void logMessage(String msg, LinkedHashMap <String, String> transactionProperties) {
		ObjectMapper mapper = new ObjectMapper();
		String payload = null;
		try {
			payload = mapper.writeValueAsString(transactionProperties);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		sb.append(msg).append(" ").append(payload);
		LOGGER.info(sb.toString());
	}
}
