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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tschumacher.mandrillservice.domain.Recipient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.controller.MandrillMessagesApi;
import com.microtripit.mandrillapp.lutung.model.MandrillApiError;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;

import de.tschumacher.mandrillservice.configuration.MandrillServiceConfig;
import de.tschumacher.mandrillservice.domain.MandrillServiceAttachment;
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
  public void shouldUseEmailPropertyForSender() throws MandrillApiError, IOException {
    String expectedEmailAddress = "email@example.com";
    String expectedSubject = "subject";
    String expectedTemplate = "template";

    final MandrillServiceMessage message =
        MandrillServiceMessage.newBuilder()
            .withEmail(expectedEmailAddress)
            .withSubject(expectedSubject)
            .withTemplate(expectedTemplate)
            .build();

    this.service.sendMail(message);

    ArgumentCaptor<MandrillMessage> captor = ArgumentCaptor.forClass(MandrillMessage.class);

    Mockito.verify(this.messageApi, Mockito.times(1)).sendTemplate(
        Matchers.eq(expectedTemplate),
        Matchers.anyMapOf(String.class, String.class),
        captor.capture(),
        Matchers.eq(false)
    );

    MandrillMessage actualMandrillMessage = captor.getValue();
    Assert.assertEquals(actualMandrillMessage.getSubject(), expectedSubject);

    Assert.assertEquals(1, actualMandrillMessage.getTo().size());

    MandrillMessage.Recipient firstRecipient = actualMandrillMessage.getTo().get(0);

    Assert.assertEquals(firstRecipient.getEmail(), expectedEmailAddress);
    Assert.assertEquals(firstRecipient.getType(), MandrillMessage.Recipient.Type.TO);
    Assert.assertNull(firstRecipient.getName());
  }

  @Test
  public void shouldUseRecipientsObjectForSenderIfDefined() throws IOException, MandrillApiError {
    String expectedEmailAddress = "test+recipient@example.com";
    String expectedName = "Test Recipient";

    Recipient recipient = new Recipient();
    recipient.setEmail(expectedEmailAddress);
    recipient.setName(expectedName);
    recipient.setType(Recipient.Type.TO);

    final MandrillServiceMessage message =
        MandrillServiceMessage.newBuilder()
            .withEmail("test+email@example.com")
            .withRecipients(Collections.singletonList(recipient))
            .withSubject("subject")
            .withTemplate("template")
            .build();
    this.service.sendMail(message);

    ArgumentCaptor<MandrillMessage> captor = ArgumentCaptor.forClass(MandrillMessage.class);

    Mockito.verify(this.messageApi, Mockito.times(1)).sendTemplate(
        Matchers.eq("template"),
        Matchers.anyMapOf(String.class, String.class),
        captor.capture(),
        Matchers.eq(false)
    );

    MandrillMessage actualMandrillMessage = captor.getValue();
    Assert.assertEquals(actualMandrillMessage.getSubject(), "subject");

    Assert.assertEquals(1, actualMandrillMessage.getTo().size());

    MandrillMessage.Recipient firstRecipient = actualMandrillMessage.getTo().get(0);

    Assert.assertEquals(firstRecipient.getEmail(), expectedEmailAddress);
    Assert.assertEquals(firstRecipient.getName(), expectedName);
    Assert.assertEquals(firstRecipient.getType(), MandrillMessage.Recipient.Type.TO);
  }

  @Test
  public void sendMessageWithReplacementsTest() throws MandrillApiError, IOException {
    String expectedName = "name";
    String expectedContent = "content";

    final Map<String, String> replacements = new HashMap<String, String>();
    replacements.put(expectedName, expectedContent);
    final MandrillServiceMessage message =
        MandrillServiceMessage.newBuilder()
            .withEmail("email")
            .withSubject("subject")
            .withTemplate("template")
            .withReplacements(replacements).build();
    this.service.sendMail(message);

    ArgumentCaptor<MandrillMessage> captor = ArgumentCaptor.forClass(MandrillMessage.class);

    Mockito.verify(this.messageApi, Mockito.times(1)).sendTemplate(
        Matchers.eq("template"),
        Matchers.anyMapOf(String.class, String.class),
        captor.capture(),
        Matchers.eq(false));

    MandrillMessage actualMandrillMessage = captor.getValue();
    MandrillMessage.MergeVar mergeVar = actualMandrillMessage.getGlobalMergeVars().get(0);

    Assert.assertEquals(mergeVar.getName(), expectedName);
    Assert.assertEquals(mergeVar.getContent(), expectedContent);
  }


  @Test
  public void sendMessageWithAttachmentsTest() throws MandrillApiError, IOException {
    String expectedFilename = "myFilename";
    String expectedType = "Type";

    final List<MandrillServiceAttachment> attachments = new ArrayList<MandrillServiceAttachment>();

    attachments.add(MandrillServiceAttachment.newBuilder()
        .withFile(new File("src/test/resources/test.txt"))
        .withName(expectedFilename).withType(expectedType)
        .build());

    final MandrillServiceMessage message =
        MandrillServiceMessage.newBuilder()
            .withEmail("email")
            .withSubject("subject")
            .withTemplate("template")
            .withAttachments(attachments)
            .build();
    this.service.sendMail(message);

    ArgumentCaptor<MandrillMessage> captor = ArgumentCaptor.forClass(MandrillMessage.class);

    Mockito.verify(this.messageApi, Mockito.times(1)).sendTemplate(
        Matchers.eq("template"),
        Matchers.anyMapOf(String.class, String.class),
        captor.capture(),
        Matchers.eq(false)
    );

    MandrillMessage actualMandrillMessage = captor.getValue();
    MandrillMessage.MessageContent firstAttachment = actualMandrillMessage.getAttachments().get(0);

    Assert.assertEquals("dGVzdA==", firstAttachment.getContent());
    Assert.assertEquals(expectedFilename, firstAttachment.getName());
    Assert.assertEquals(expectedType, firstAttachment.getType());
  }

}
