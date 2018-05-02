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
  public void executeRetrieveInfoOperation() throws Exception {
    String payloadValue = ((String) flowRunner("retrieveInfoFlow")
                                      .run()
                                      .getMessage()
                                      .getPayload()
                                      .getValue());
    assertThat(payloadValue, is("Using Configuration [configId] with Connection id [aValue:100]"));
  }
}
