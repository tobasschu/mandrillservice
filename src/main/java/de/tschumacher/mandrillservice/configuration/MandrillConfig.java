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
package de.tschumacher.mandrillservice.configuration;


public class MandrillConfig {

  private final String mandrillKey;
  private final boolean isDebug;
  private final String debugMail;
  private final String defaultFromMail;
  private final String defaultFromName;

  public String getMandrillKey() {
    return mandrillKey;
  }

  public boolean isDebug() {
    return isDebug;
  }

  public String getDebugMail() {
    return debugMail;
  }

  public String getDefaultFromMail() {
    return defaultFromMail;
  }

  public String getDefaultFromName() {
    return defaultFromName;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  private MandrillConfig(Builder builder) {
    this.mandrillKey = builder.mandrillKey;
    this.isDebug = builder.isDebug;
    this.debugMail = builder.debugMail;
    this.defaultFromMail = builder.defaultFromMail;
    this.defaultFromName = builder.defaultFromName;
  }

  public static class Builder {

    private String mandrillKey;
    private boolean isDebug;
    private String debugMail;
    private String defaultFromMail;
    private String defaultFromName;

    public Builder withMandrillKey(String mandrillKey) {
      this.mandrillKey = mandrillKey;
      return this;
    }

    public Builder withIsDebug(boolean isDebug) {
      this.isDebug = isDebug;
      return this;
    }

    public Builder withDebugMail(String debugMail) {
      this.debugMail = debugMail;
      return this;
    }

    public Builder withDefaultFromMail(String defaultFromMail) {
      this.defaultFromMail = defaultFromMail;
      return this;
    }

    public Builder withDefaultFromName(String defaultFromName) {
      this.defaultFromName = defaultFromName;
      return this;
    }

    public MandrillConfig build() {
      return new MandrillConfig(this);
    }
  }



}
