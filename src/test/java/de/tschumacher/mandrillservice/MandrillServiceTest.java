/*
 * Copyright 2015 Tobias Schumacher
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package de.tschumacher.mandrillservice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.controller.MandrillMessagesApi;
import com.microtripit.mandrillapp.lutung.model.MandrillApiError;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;

import de.tschumacher.mandrillservice.configuration.MandrillServiceConfig;
import de.tschumacher.mandrillservice.domain.MandrillServiceMessage;


public class MandrillServiceTest {
  private MandrillApi api = null;
  private MandrillMessagesApi messageApi = null;
  private MandrillService service = null;
  private MandrillServiceConfig config;

  @Before
  public void setUp() {
    this.config = MandrillServiceConfig.newBuilder().build();
    this.messageApi = Mockito.mock(MandrillMessagesApi.class);
    this.api = Mockito.mock(MandrillApi.class);
    Mockito.when(this.api.messages()).thenReturn(this.messageApi);
    this.service = new DefaultMandrillService(this.config, this.api);
  }

  @After
  public void afterTest() {
    Mockito.verify(this.api).messages();
    Mockito.verifyNoMoreInteractions(this.api);
  }

  @Test
  public void sendMessageTest() throws MandrillApiError, IOException {

    final MandrillServiceMessage message =
        MandrillServiceMessage.newBuilder().withEmail("email").withSubject("subject")
            .withTemplate("template").build();
    this.service.sendMail(message);

    Mockito.verify(this.messageApi, Mockito.times(1)).sendTemplate(Matchers.anyString(),
        Matchers.anyMapOf(String.class, String.class), Matchers.any(MandrillMessage.class),
        Matchers.anyBoolean());

  }

  @Test
  public void sendMessageWithReplacementsTest() throws MandrillApiError, IOException {

    final Map<String, String> replacements = new HashMap<String, String>();
    replacements.put("test", "test");
    final MandrillServiceMessage message =
        MandrillServiceMessage.newBuilder().withEmail("email").withSubject("subject")
            .withTemplate("template").withReplacements(replacements).build();
    this.service.sendMail(message);

    Mockito.verify(this.messageApi, Mockito.times(1)).sendTemplate(Matchers.anyString(),
        Matchers.anyMapOf(String.class, String.class), Matchers.any(MandrillMessage.class),
        Matchers.anyBoolean());

  }

}
