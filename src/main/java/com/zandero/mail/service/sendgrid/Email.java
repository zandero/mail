package com.zandero.mail.service.sendgrid;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.*;

/**
 * Sendgrid email address object
 */
@JsonInclude(Include.NON_DEFAULT)
public class Email {
    @JsonProperty("name")
    private String name;
    @JsonProperty("email")
    private String email;

    /**
     * Empty email address
     */
    public Email() {
    }

    /**
     * Email without name
     *
     * @param email address
     */
    public Email(String email) {
        this.setEmail(email);
    }

    /**
     * Email address with name and address
     *
     * @param email address
     * @param name  associated name
     */
    public Email(String email, String name) {
        this.setEmail(email);
        this.setName(name);
    }

    /**
     * Name associated with email (aka person)
     *
     * @return associated name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Set associated name
     *
     * @param name to be set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Email address
     *
     * @return email address
     */
    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    /**
     * Sets email address
     *
     * @param email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        Email other = (Email) obj;
        if (email == null) {
            if (other.email != null)
                return false;
        } else if (!email.equals(other.email))
            return false;
        if (name == null) {
            return other.name == null;
        } else return name.equals(other.name);
    }
}