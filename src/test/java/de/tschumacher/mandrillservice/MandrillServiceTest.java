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
import java.util.Arrays;
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
    this.config = Mockito.mock(MandrillServiceConfig.class);
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

    Assert.assertEquals(expectedEmailAddress, firstRecipient.getEmail());
    Assert.assertEquals(MandrillMessage.Recipient.Type.TO, firstRecipient.getType());
    Assert.assertNull(firstRecipient.getName());
  }

  @Test
  public void shouldUseEmailsPropertyForMultipleSender() throws MandrillApiError, IOException {
    String firstExpectedEmailAddress = "email@example.com";
    String secondExpectedEmailAddress = "email@example.com";
    String expectedSubject = "subject";
    String expectedTemplate = "template";

    final MandrillServiceMessage message =
        MandrillServiceMessage.newBuilder()
            .withEmails(Arrays.asList(firstExpectedEmailAddress, secondExpectedEmailAddress))
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

    Assert.assertEquals(2, actualMandrillMessage.getTo().size());

    MandrillMessage.Recipient firstRecipient = actualMandrillMessage.getTo().get(0);

    Assert.assertEquals(firstExpectedEmailAddress, firstRecipient.getEmail());
    Assert.assertEquals(MandrillMessage.Recipient.Type.TO, firstRecipient.getType());
    Assert.assertNull(firstRecipient.getName());

    MandrillMessage.Recipient secondRecipient = actualMandrillMessage.getTo().get(1);

    Assert.assertEquals(secondExpectedEmailAddress, secondRecipient.getEmail());
    Assert.assertEquals(MandrillMessage.Recipient.Type.TO, secondRecipient.getType());
    Assert.assertNull(secondRecipient.getName());
  }

  @Test
  public void shouldUseRecipientsObjectForSenderIfDefined() throws IOException, MandrillApiError {
    Mockito.when(this.config.isDebug()).thenReturn(false);

    String firstExpectedEmailAddress = "test+recipient1@example.com";
    String firstExpectedName = "Test Recipient 1";
    String secondExpectedEmailAddress = "test+recipient2@example.com";
    String secondExpectedName = "Test Recipient 2";

    Recipient firstExpectedRecipient = new Recipient();
    firstExpectedRecipient.setEmail(firstExpectedEmailAddress);
    firstExpectedRecipient.setName(firstExpectedName);
    firstExpectedRecipient.setType(Recipient.Type.TO);

    Recipient secondExpectedRecipient = new Recipient();
    secondExpectedRecipient.setEmail(secondExpectedEmailAddress);
    secondExpectedRecipient.setName(secondExpectedName);
    secondExpectedRecipient.setType(Recipient.Type.CC);

    final MandrillServiceMessage message =
        MandrillServiceMessage.newBuilder()
            .withEmail("test+email@example.com")
            .withRecipients(Arrays.asList(firstExpectedRecipient, secondExpectedRecipient))
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

    Assert.assertEquals(2, actualMandrillMessage.getTo().size());

    MandrillMessage.Recipient firstRecipient = actualMandrillMessage.getTo().get(0);

    Assert.assertEquals(firstExpectedEmailAddress, firstRecipient.getEmail());
    Assert.assertEquals(firstExpectedName, firstRecipient.getName());
    Assert.assertEquals(MandrillMessage.Recipient.Type.TO, firstRecipient.getType());

    MandrillMessage.Recipient secondRecipient = actualMandrillMessage.getTo().get(1);

    Assert.assertEquals(secondExpectedEmailAddress, secondRecipient.getEmail());
    Assert.assertEquals(secondExpectedName, secondExpectedRecipient.getName());
    Assert.assertEquals(MandrillMessage.Recipient.Type.CC, secondRecipient.getType());
  }

  @Test
  public void shouldUseDefinedRecipientsDebug() throws IOException, MandrillApiError {
    Mockito.when(this.config.isDebug()).thenReturn(true);
    Mockito.when(this.config.getDebugRegex()).thenReturn(".*@example.com|.*@google.com");
    Mockito.when(this.config.getDebugMail()).thenReturn("debug@test.com");

    String firstExpectedEmailAddress = "test+recipient1@example.com";
    String firstExpectedName = "Test Recipient 1";
    String secondExpectedEmailAddress = "test+recipient2@google.com";
    String secondExpectedName = "Test Recipient 2";
    String thirdExpectedEmailAddress = "test+recipient3@bing.com";
    String thirdExpectedName = "Test Recipient 3";

    Recipient firstExpectedRecipient = new Recipient();
    firstExpectedRecipient.setEmail(firstExpectedEmailAddress);
    firstExpectedRecipient.setName(firstExpectedName);
    firstExpectedRecipient.setType(Recipient.Type.TO);

    Recipient secondExpectedRecipient = new Recipient();
    secondExpectedRecipient.setEmail(secondExpectedEmailAddress);
    secondExpectedRecipient.setName(secondExpectedName);
    secondExpectedRecipient.setType(Recipient.Type.CC);

    Recipient thirdExpectedRecipient = new Recipient();
    thirdExpectedRecipient.setEmail(thirdExpectedEmailAddress);
    thirdExpectedRecipient.setName(thirdExpectedName);
    thirdExpectedRecipient.setType(Recipient.Type.TO);

    final MandrillServiceMessage message =
        MandrillServiceMessage.newBuilder()
            .withEmail("test+email@example.com")
            .withRecipients(Arrays.asList(firstExpectedRecipient, secondExpectedRecipient, thirdExpectedRecipient))
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

    Assert.assertEquals(3, actualMandrillMessage.getTo().size());

    MandrillMessage.Recipient firstRecipient = actualMandrillMessage.getTo().get(0);

    Assert.assertEquals(firstExpectedEmailAddress, firstRecipient.getEmail());
    Assert.assertEquals(firstExpectedName, firstRecipient.getName());
    Assert.assertEquals(MandrillMessage.Recipient.Type.TO, firstRecipient.getType());

    MandrillMessage.Recipient secondRecipient = actualMandrillMessage.getTo().get(1);

    Assert.assertEquals(secondExpectedEmailAddress, secondRecipient.getEmail());
    Assert.assertEquals(secondExpectedName, secondExpectedRecipient.getName());
    Assert.assertEquals(MandrillMessage.Recipient.Type.CC, secondRecipient.getType());

    MandrillMessage.Recipient thirdRecipient = actualMandrillMessage.getTo().get(2);

    Assert.assertEquals("debug@test.com", thirdRecipient.getEmail());
    Assert.assertEquals(thirdExpectedName, thirdRecipient.getName());
    Assert.assertEquals(MandrillMessage.Recipient.Type.TO, thirdRecipient.getType());
  }

  @Test
  public void shouldUseDefinedEmailsDebug() throws IOException, MandrillApiError {
    Mockito.when(this.config.isDebug()).thenReturn(true);
    Mockito.when(this.config.getDebugRegex()).thenReturn(".*@example.com|.*@google.com");
    Mockito.when(this.config.getDebugMail()).thenReturn("debug@test.com");

    String firstExpectedEmailAddress = "test+recipient1@example.com";
    String secondExpectedEmailAddress = "test+recipient2@google.com";
    String thirdExpectedEmailAddress = "test+recipient3@bing.com";

    final MandrillServiceMessage message =
        MandrillServiceMessage.newBuilder()
            .withEmail("test+email@example.com")
            .withEmails(Arrays.asList(firstExpectedEmailAddress, secondExpectedEmailAddress, thirdExpectedEmailAddress))
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

    Assert.assertEquals(3, actualMandrillMessage.getTo().size());

    MandrillMessage.Recipient firstRecipient = actualMandrillMessage.getTo().get(0);

    Assert.assertEquals(firstExpectedEmailAddress, firstRecipient.getEmail());
    Assert.assertEquals(MandrillMessage.Recipient.Type.TO, firstRecipient.getType());

    MandrillMessage.Recipient secondRecipient = actualMandrillMessage.getTo().get(1);

    Assert.assertEquals(secondExpectedEmailAddress, secondRecipient.getEmail());
    Assert.assertEquals(MandrillMessage.Recipient.Type.TO, secondRecipient.getType());

    MandrillMessage.Recipient thirdRecipient = actualMandrillMessage.getTo().get(2);

    Assert.assertEquals("debug@test.com", thirdRecipient.getEmail());
    Assert.assertEquals(MandrillMessage.Recipient.Type.TO, thirdRecipient.getType());
  }

  @Test
  public void shouldUseDebugRecipient() throws MandrillApiError, IOException {
    Mockito.when(this.config.isDebug()).thenReturn(true);
    Mockito.when(this.config.getDebugMail()).thenReturn("debug@example.net");

    final MandrillServiceMessage message =
        MandrillServiceMessage.newBuilder()
            .withEmail("email")
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
    List<MandrillMessage.Recipient> recipients = actualMandrillMessage.getTo();
    Assert.assertEquals(1, recipients.size());
    Assert.assertEquals("debug@example.net", recipients.get(0).getEmail());
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
            .withReplacements(replacements)
            .build();
    this.service.sendMail(message);

    ArgumentCaptor<MandrillMessage> captor = ArgumentCaptor.forClass(MandrillMessage.class);

    Mockito.verify(this.messageApi, Mockito.times(1)).sendTemplate(
        Matchers.eq("template"),
        Matchers.anyMapOf(String.class, String.class),
        captor.capture(),
        Matchers.eq(false));

    MandrillMessage actualMandrillMessage = captor.getValue();
    MandrillMessage.MergeVar mergeVar = actualMandrillMessage.getGlobalMergeVars().get(0);

    Assert.assertEquals(expectedName, mergeVar.getName());
    Assert.assertEquals(expectedContent, mergeVar.getContent());
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
