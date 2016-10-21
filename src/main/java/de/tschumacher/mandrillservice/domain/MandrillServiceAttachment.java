package de.tschumacher.mandrillservice.domain;

import java.io.File;

public class MandrillServiceAttachment {
  private final String name;
  private final String type;
  private final File file;

  public String getName() {
    return this.name;
  }

  public String getType() {
    return this.type;
  }

  public File getFile() {
    return this.file;
  }


  public static Builder newBuilder() {
    return new Builder();
  }

  private MandrillServiceAttachment(Builder builder) {
    this.name = builder.name;
    this.type = builder.type;
    this.file = builder.file;
  }

  public static class Builder {

    private String name;
    private String type;
    private File file;

    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    public Builder withType(String type) {
      this.type = type;
      return this;
    }

    public Builder withFile(File file) {
      this.file = file;
      return this;
    }

    public MandrillServiceAttachment build() {
      return new MandrillServiceAttachment(this);
    }
  }


}
