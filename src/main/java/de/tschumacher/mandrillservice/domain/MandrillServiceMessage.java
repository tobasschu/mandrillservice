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
package de.tschumacher.mandrillservice.domain;

import java.util.Map;

public class MandrillServiceMessage {
  private final String email;
  private final String subject;
  private final Map<String, String> replacements;
  private final String template;
  private final String fromEmail;
  private final String fromName;

  public String getEmail() {
    return email;
  }

  public String getSubject() {
    return subject;
  }

  public Map<String, String> getReplacements() {
    return replacements;
  }

  public String getTemplate() {
    return template;
  }

  public String getFromEmail() {
    return fromEmail;
  }

  public String getFromName() {
    return fromName;
  }

  private MandrillServiceMessage(Builder builder) {
    this.email = builder.email;
    this.subject = builder.subject;
    this.replacements = builder.replacements;
    this.template = builder.template;
    this.fromEmail = builder.fromEmail;
    this.fromName = builder.fromName;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private String email;
    private String subject;
    private Map<String, String> replacements;
    private String template;
    private String fromEmail;
    private String fromName;

    public Builder withEmail(String email) {
      this.email = email;
      return this;
    }

    public Builder withSubject(String subject) {
      this.subject = subject;
      return this;
    }

    public Builder withReplacements(Map<String, String> replacements) {
      this.replacements = replacements;
      return this;
    }

    public Builder withTemplate(String template) {
      this.template = template;
      return this;
    }

    public Builder withFromEmail(String fromEmail) {
      this.fromEmail = fromEmail;
      return this;
    }

    public Builder withFromName(String fromName) {
      this.fromName = fromName;
      return this;
    }

    public MandrillServiceMessage build() {
      return new MandrillServiceMessage(this);
    }
  }


}
