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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.model.MandrillApiError;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.MergeVar;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.Recipient;

import de.tschumacher.mandrillservice.configuration.MandrillConfig;
import de.tschumacher.mandrillservice.domain.MandrillServiceMessage;
import de.tschumacher.mandrillservice.exception.MandrillServiceException;


public class DefaultMadrillService implements MandrillService {

  private final MandrillConfig config;
  private final MandrillApi mandrillApi;



  public DefaultMadrillService(final MandrillConfig config) {
    super();
    this.config = config;
    this.mandrillApi = new MandrillApi(config.getMandrillKey());
  }

  // for testing
  public DefaultMadrillService(MandrillConfig config, MandrillApi mandrillApi) {
    super();
    this.config = config;
    this.mandrillApi = mandrillApi;
  }


  @Override
  public void sendMail(MandrillServiceMessage message) {

    final MandrillMessage mandrillMessage = createMessage(message);

    try {
      this.mandrillApi.messages().sendTemplate(message.getTemplate(), null, mandrillMessage, false);
    } catch (MandrillApiError | IOException e) {
      throw new MandrillServiceException(e);
    }
  }



  private MandrillMessage createMessage(MandrillServiceMessage message) {
    final MandrillMessage mandrillMessage = createDefaultMessage();

    mandrillMessage.setSubject(message.getSubject());
    final Recipient recipient = new Recipient();

    recipient.setEmail(message.getEmail());
    if (this.config.isDebug()) {
      recipient.setEmail(this.config.getDebugMail());
    }
    mandrillMessage.setTo(Arrays.asList(recipient));


    if (message.getFromEmail() != null) {
      mandrillMessage.setFromEmail(message.getFromEmail());
    }

    if (message.getFromName() != null) {
      mandrillMessage.setFromName(message.getFromName());
    }

    mandrillMessage.setGlobalMergeVars(createMergeVars(message.getReplacements()));
    return mandrillMessage;
  }


  private List<MergeVar> createMergeVars(final Map<String, String> replacements) {
    if (replacements == null)
      return null;

    final List<MergeVar> mergeVars = new ArrayList<MergeVar>();
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
    return message;
  }
}
