package de.tschumacher.mandrillservice.domain;

import com.microtripit.mandrillapp.lutung.view.MandrillMessage;

public class Recipient {
  /**
   * <p>The Recipient type (To, Cc, Bcc, ...)
   */
  public enum Type {
    TO, BCC, CC
  }

  private String email, name;
  private Type type = Type.TO;

  /**
   * @return The type of the recipient.
   */
  public Type getType() {
    return type;
  }

  /**
   * @param type The type of the recipient.
   */
  public void setType(final Type type) {
    this.type = type;
  }

  public String getEmail() {
    return email;
  }
  /**
   * @param email The email address of the recipient.
   */
  public void setEmail(final String email) {
    this.email = email;
  }
  /**
   * @return The optional display name to use for the recipient.
   */
  public String getName() {
    return name;
  }
  /**
   * @param name The optional display name to use for the recipient
   */
  public void setName(final String name) {
    this.name = name;
  }
}