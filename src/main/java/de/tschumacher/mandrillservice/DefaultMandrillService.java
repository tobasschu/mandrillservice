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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;

import com.google.common.io.Files;
import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.model.MandrillApiError;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.MergeVar;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.MessageContent;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.Recipient;

import de.tschumacher.mandrillservice.configuration.MandrillServiceConfig;
import de.tschumacher.mandrillservice.domain.MandrillServiceAttachment;
import de.tschumacher.mandrillservice.domain.MandrillServiceMessage;
import de.tschumacher.mandrillservice.exception.MandrillServiceException;


public class DefaultMandrillService implements MandrillService {

  private final MandrillServiceConfig config;
  private final MandrillApi mandrillApi;



  public DefaultMandrillService(final MandrillServiceConfig config) {
    super();
    this.config = config;
    this.mandrillApi = new MandrillApi(config.getMandrillKey());
  }

  // for testing
  public DefaultMandrillService(MandrillServiceConfig config, MandrillApi mandrillApi) {
    super();
    this.config = config;
    this.mandrillApi = mandrillApi;
  }


  @Override
  public void sendMail(MandrillServiceMessage message) {
    try {
      final MandrillMessage mandrillMessage = createMessage(message);
      this.mandrillApi.messages().sendTemplate(message.getTemplate(), null, mandrillMessage, false);
    } catch (MandrillApiError | IOException e) {
      throw new MandrillServiceException(e);
    }
  }



  private MandrillMessage createMessage(MandrillServiceMessage message) throws IOException {
    final MandrillMessage mandrillMessage = createDefaultMessage();

    mandrillMessage.setSubject(message.getSubject());
    mandrillMessage.setTo(createRecipients(message));

    if (message.getFromEmail() != null) {
      mandrillMessage.setFromEmail(message.getFromEmail());
    }

    if (message.getFromName() != null) {
      mandrillMessage.setFromName(message.getFromName());
    }

    mandrillMessage.setPreserveRecipients(message.getPreserveRecipients());
    mandrillMessage.setHeaders(message.getHeaders());
    mandrillMessage.setGlobalMergeVars(createMergeVars(message.getReplacements()));
    mandrillMessage.setAttachments(createAttachments(message.getAttachments()));
    return mandrillMessage;
  }

  private List<MessageContent> createAttachments(List<MandrillServiceAttachment> attachments)
      throws IOException {
    if (attachments == null)
      return null;
    final List<MessageContent> messageContentList = new ArrayList<>();
    for (final MandrillServiceAttachment attachment : attachments) {
      messageContentList.add(createAttachment(attachment));
    }
    return messageContentList;
  }

  private MessageContent createAttachment(MandrillServiceAttachment attachment) throws IOException {
    final MessageContent messageContent = new MessageContent();
    messageContent.setBinary(true);
    messageContent.setName(attachment.getName());
    messageContent.setType(attachment.getType());
    messageContent.setContent(Base64.encodeBase64String(Files.toByteArray(attachment.getFile())));
    return messageContent;
  }

  private List<Recipient> createRecipients(MandrillServiceMessage message) {
    if (this.config.isDebug()) {
      return createDebugRecipients(message);
    }

    return createActualRecipients(message);
  }

  private List<Recipient> createDebugRecipients(MandrillServiceMessage message) {
    if (this.config.getDebugRegex() != null) {
      return createDebugRecipientsWithDebugRegex(message);
    } else {
      return createDebugRecipient();
    }
  }

  private List<Recipient> createDebugRecipientsWithDebugRegex(
      MandrillServiceMessage message
  ) {
    List<de.tschumacher.mandrillservice.domain.Recipient> messageRecipients = message.getRecipients();

    if (messageRecipients != null && !messageRecipients.isEmpty()) {
      return createDebugRecipientsFromRecipients(messageRecipients);
    } else {
      return createDebugRecipientsFromEmails(message.getEmails());
    }
  }

  private List<Recipient> createDebugRecipient() {
    final Recipient recipient = new Recipient();
    recipient.setEmail(this.config.getDebugMail());
    return Collections.singletonList(recipient);
  }

  private List<Recipient> createDebugRecipientsFromRecipients(List<de.tschumacher.mandrillservice.domain.Recipient> recipients) {
    return recipients.stream().map(recipient -> {
      if (emailAddressMatchesDebugRegex(recipient.getEmail())) {
        Recipient mandrillRecipient = new Recipient();

        mandrillRecipient.setType(Recipient.Type.valueOf(recipient.getType().name()));
        mandrillRecipient.setEmail(recipient.getEmail());
        mandrillRecipient.setName(recipient.getName());

        return mandrillRecipient;
      } else {
        Recipient mandrillRecipient = new Recipient();

        mandrillRecipient.setType(Recipient.Type.valueOf(recipient.getType().name()));
        mandrillRecipient.setEmail(this.config.getDebugMail());
        mandrillRecipient.setName(recipient.getName());

        return mandrillRecipient;
      }
    }).collect(Collectors.toList());
  }

  private List<Recipient> createDebugRecipientsFromEmails(List<String> emailAddresses) {
    return emailAddresses.stream().map(emailAddress -> {
      Recipient recipient = new Recipient();
      if (emailAddressMatchesDebugRegex(emailAddress)) {
        recipient.setEmail(emailAddress);
      } else {
        recipient.setEmail(this.config.getDebugMail());
      }
      return recipient;
    }).collect(Collectors.toList());
  }

  private boolean emailAddressMatchesDebugRegex(String emailAddress) {
    return emailAddress.matches(this.config.getDebugRegex());
  }

  private List<Recipient> createActualRecipients(MandrillServiceMessage message) {
    List<de.tschumacher.mandrillservice.domain.Recipient> messageRecipients = message.getRecipients();

    if (messageRecipients != null && !messageRecipients.isEmpty()) {
      return createRecipientsFromRecipients(messageRecipients);
    } else {
      return createRecipientsFromEmails(message.getEmails());
    }
  }

  private List<Recipient> createRecipientsFromEmails(List<String> emailAddresses) {
    return emailAddresses.stream().map(emailAddress ->  {
      final Recipient recipient = new Recipient();
      recipient.setEmail(emailAddress);
      return recipient;
    }).collect(Collectors.toList());
  }

  private List<Recipient> createRecipientsFromRecipients(List<de.tschumacher.mandrillservice.domain.Recipient> recipients) {
    return recipients.stream().map(recipient -> {
      Recipient mandrillRecipient = new Recipient();

      mandrillRecipient.setType(Recipient.Type.valueOf(recipient.getType().name()));
      mandrillRecipient.setEmail(recipient.getEmail());
      mandrillRecipient.setName(recipient.getName());

      return mandrillRecipient;
    }).collect(Collectors.toList());
  }

  private List<MergeVar> createMergeVars(final Map<String, String> replacements) {
    if (replacements == null)
      return null;

    final List<MergeVar> mergeVars = new ArrayList<>();
    for (final String key : replacements.keySet()) {
      final MergeVar mergeVar = new MergeVar(key, replacements.get(key));
      mergeVars.add(mergeVar);
    }
    return mergeVars;
  }


  private MandrillMessage createDefaultMessage() {
    final MandrillMessage message = new MandrillMessage();
    message.setFromEmail(this.config.getDefaultFromMail());
    message.setFromName(this.config.getDefaultFromName());
    message.setInlineCss(true);
    return message;
  }
}
