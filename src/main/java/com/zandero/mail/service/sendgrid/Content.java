package com.zandero.mail.service.sendgrid;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * An object in which you may specify the content of your email. 
 */
@JsonInclude(Include.NON_DEFAULT)
public class Content {
  @JsonProperty("type") private String type;
  @JsonProperty("value") private String  value;

  /**
   * Content (empty)
   */
  public Content() {  }

  /**
   * Content
   *
   * @param type of content
   * @param value content
   */
  public Content(String type, String value) {
    setType(type);
    setValue(value);
  }

  /**
   * Conent type
   * @return type of content
   */
  @JsonProperty("type")
  public String getType() {
    return type;
  }

  /**
   * Sets content type
   * @param type of content
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Content value
   * @return value as string
   */
  @JsonProperty("value")
  public String getValue() {
    return value;
  }

  /**
   * Setns content and checks if valid
   * @param value to be set
   * @throws IllegalArgumentException if content is invalid
   */
  public void setValue(String value) {
    ContentVerifier.verifyContent(value);
    this.value = value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Content other = (Content) obj;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }
}

/**
 * Content verifier utility
 */
class ContentVerifier {
  private static final List<Pattern> FORBIDDEN_PATTERNS = Collections.singletonList(
          Pattern.compile(".*SG\\.[a-zA-Z0-9(-|_)]*\\.[a-zA-Z0-9(-|_)]*.*")
  );

  static void verifyContent(String content) {
    for (Pattern pattern: FORBIDDEN_PATTERNS) {
      if (pattern.matcher(content).matches()) {
        throw new IllegalArgumentException("Found a Forbidden Pattern in the content of the email");
      }
    }
  }
}