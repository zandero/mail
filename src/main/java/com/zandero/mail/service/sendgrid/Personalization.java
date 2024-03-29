package com.zandero.mail.service.sendgrid;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.*;

import java.util.*;

/**
 * Send grid personalization
 */
@JsonInclude(Include.NON_DEFAULT)
public class Personalization {

    /**
     * Send grid personalization
     */
    public Personalization() {    }

    @JsonProperty("to")
    private List<Email> tos;
    @JsonProperty("cc")
    private List<Email> ccs;
    @JsonProperty("bcc")
    private List<Email> bccs;
    @JsonProperty("subject")
    private String subject;
    @JsonProperty("headers")
    private Map<String, String> headers;
    @JsonProperty("substitutions")
    private Map<String, String> substitutions;
    @JsonProperty("custom_args")
    private Map<String, String> customArgs;
    @JsonProperty("send_at")
    private long sendAt;

    /**
     * To emails
     *
     * @return list of TO emails
     */
    @JsonProperty("to")
    public List<Email> getTos() {
        if (tos == null)
            return Collections.<Email>emptyList();
        return tos;
    }

    /**
     * Adds TO email
     *
     * @param email address
     */
    public void addTo(Email email) {
        Email newEmail = new Email();
        newEmail.setName(email.getName());
        newEmail.setEmail(email.getEmail());
        if (tos == null) {
            tos = new ArrayList<Email>();
            tos.add(newEmail);
        } else {
            tos.add(newEmail);
        }
    }

    /**
     * List of CC email addresses
     *
     * @return list of CC email addresses
     */
    @JsonProperty("cc")
    public List<Email> getCcs() {
        if (ccs == null)
            return Collections.<Email>emptyList();
        return ccs;
    }

    /**
     * Adds CC email address
     *
     * @param email address
     */
    public void addCc(Email email) {
        Email newEmail = new Email();
        newEmail.setName(email.getName());
        newEmail.setEmail(email.getEmail());
        if (ccs == null) {
            ccs = new ArrayList<Email>();
            ccs.add(newEmail);
        } else {
            ccs.add(newEmail);
        }
    }

    /**
     * List of BCC email addresses
     *
     * @return list of BCC email addresses
     */
    @JsonProperty("bcc")
    public List<Email> getBccs() {
        if (bccs == null)
            return Collections.<Email>emptyList();
        return bccs;
    }

    /**
     * Adds new BBC email address
     *
     * @param email address
     */
    public void addBcc(Email email) {
        Email newEmail = new Email();
        newEmail.setName(email.getName());
        newEmail.setEmail(email.getEmail());
        if (bccs == null) {
            bccs = new ArrayList<Email>();
            bccs.add(newEmail);
        } else {
            bccs.add(newEmail);
        }
    }

    /**
     * Email subject
     *
     * @return subject
     */
    @JsonProperty("subject")
    public String getSubject() {
        return subject;
    }

    /**
     * Sets email subject
     *
     * @param subject subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Map of email headers
     *
     * @return header pairs name=value
     */
    @JsonProperty("headers")
    public Map<String, String> getHeaders() {
        if (headers == null)
            return Collections.<String, String>emptyMap();
        return headers;
    }

    /**
     * Adds new email header
     *
     * @param key   header name
     * @param value header value
     */
    public void addHeader(String key, String value) {
        if (headers == null) {
            headers = new HashMap<String, String>();
            headers.put(key, value);
        } else {
            headers.put(key, value);
        }
    }

    /**
     * Email substitutions
     *
     * @return list of substitutions
     */
    @JsonProperty("substitutions")
    public Map<String, String> getSubstitutions() {
        if (substitutions == null)
            return Collections.<String, String>emptyMap();
        return substitutions;
    }

    /**
     * Adds substitution
     *
     * @param key   name
     * @param value value
     */
    public void addSubstitution(String key, String value) {
        if (substitutions == null) {
            substitutions = new HashMap<String, String>();
            substitutions.put(key, value);
        } else {
            substitutions.put(key, value);
        }
    }

    /**
     * Custom arguments
     *
     * @return map of custom arguments
     */
    @JsonProperty("custom_args")
    public Map<String, String> getCustomArgs() {
        if (customArgs == null)
            return Collections.<String, String>emptyMap();
        return customArgs;
    }

    /**
     * Adds new custom argument
     *
     * @param key   name
     * @param value value
     */
    public void addCustomArg(String key, String value) {
        if (customArgs == null) {
            customArgs = new HashMap<String, String>();
            customArgs.put(key, value);
        } else {
            customArgs.put(key, value);
        }
    }

    /**
     * Sent at time stamp
     * @return send at time
     */
    @JsonProperty("send_at")
    public long sendAt() {
        return sendAt;
    }

    /**
     * Sets sen at time
     * @param sendAt timestamp
     */
    public void setSendAt(long sendAt) {
        this.sendAt = sendAt;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bccs == null) ? 0 : bccs.hashCode());
        result = prime * result + ((ccs == null) ? 0 : ccs.hashCode());
        result = prime * result + ((customArgs == null) ? 0 : customArgs.hashCode());
        result = prime * result + ((headers == null) ? 0 : headers.hashCode());
        result = prime * result + (int) (sendAt ^ (sendAt >>> 32));
        result = prime * result + ((subject == null) ? 0 : subject.hashCode());
        result = prime * result + ((substitutions == null) ? 0 : substitutions.hashCode());
        result = prime * result + ((tos == null) ? 0 : tos.hashCode());
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
        Personalization other = (Personalization) obj;
        if (bccs == null) {
            if (other.bccs != null)
                return false;
        } else if (!bccs.equals(other.bccs))
            return false;
        if (ccs == null) {
            if (other.ccs != null)
                return false;
        } else if (!ccs.equals(other.ccs))
            return false;
        if (customArgs == null) {
            if (other.customArgs != null)
                return false;
        } else if (!customArgs.equals(other.customArgs))
            return false;
        if (headers == null) {
            if (other.headers != null)
                return false;
        } else if (!headers.equals(other.headers))
            return false;
        if (sendAt != other.sendAt)
            return false;
        if (subject == null) {
            if (other.subject != null)
                return false;
        } else if (!subject.equals(other.subject))
            return false;
        if (substitutions == null) {
            if (other.substitutions != null)
                return false;
        } else if (!substitutions.equals(other.substitutions))
            return false;
        if (tos == null) {
            return other.tos == null;
        } else return tos.equals(other.tos);
    }
}