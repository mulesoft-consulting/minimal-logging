package org.mule.consulting.logging;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.LinkedHashMap;

import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class MinimalloggingOperationsTestCase extends MuleArtifactFunctionalTestCase {

  /**
   * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
   */
  @Override
  protected String getConfigFile() {
    return "test-mule-config.xml";
  }

  @Test
  public void executeSetTransactionProperties() throws Exception {
    LinkedHashMap<String, String> payloadValue = ((LinkedHashMap<String, String>) flowRunner("set-transaction-properties").run()
                                      .getMessage()
                                      .getPayload()
                                      .getValue());
    assertNotNull(payloadValue);
  }

  @Test
  public void executeLog() throws Exception {
    LinkedHashMap<String, String> payloadValue = ((LinkedHashMap<String, String>) flowRunner("log").run()
                                      .getMessage()
                                      .getPayload()
                                      .getValue());
  }

  @Test
  public void executePut() throws Exception {
	  LinkedHashMap<String, String> payloadValue = ((LinkedHashMap<String, String>) flowRunner("put")
                                      .run()
                                      .getMessage()
                                      .getPayload()
                                      .getValue());
    assertThat(payloadValue.toString(), is("{peters=piper}"));
  }

  @Test
  public void executePutAll() throws Exception {
	  LinkedHashMap<String, String> payloadValue = ((LinkedHashMap<String, String>) flowRunner("putAll")
                                      .run()
                                      .getMessage()
                                      .getPayload()
                                      .getValue());
    assertThat(payloadValue.toString(), is("{peters=piper, a=b, x=y}"));
  }

  @Test
  public void executeUseTimedScope() throws Exception {
    LinkedHashMap<String, String> payloadValue = ((LinkedHashMap<String, String>) flowRunner("use-timed-scope").run()
                                      .getMessage()
                                      .getPayload()
                                      .getValue());
    assertNotNull(payloadValue);
  }
}
