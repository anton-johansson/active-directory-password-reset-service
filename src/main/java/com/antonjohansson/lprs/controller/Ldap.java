/**
 * Copyright (c) Anton Johansson <antoon.johansson@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.antonjohansson.lprs.controller;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static javax.naming.Context.INITIAL_CONTEXT_FACTORY;
import static javax.naming.Context.PROVIDER_URL;
import static javax.naming.Context.SECURITY_AUTHENTICATION;
import static javax.naming.Context.SECURITY_CREDENTIALS;
import static javax.naming.Context.SECURITY_PRINCIPAL;
import static javax.naming.directory.DirContext.REPLACE_ATTRIBUTE;
import static javax.naming.directory.SearchControls.SUBTREE_SCOPE;

import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.antonjohansson.lprs.model.User;

/**
 * Wraps the LDAP API.
 */
public class Ldap implements AutoCloseable
{
    private static final Logger LOG = LoggerFactory.getLogger(Ldap.class);
    private static final String[] USER_ATTRIBUTES = {"distinguishedName", "userPrincipalName", "name", "mail", "telephoneNumber"};

    private final InitialLdapContext context;
    private final String domain;

    /**
     * Constructs a new LDAP connection.
     */
    Ldap(String providerURL, String domain, String serviceUsername, String servicePassword)
    {
        String principal = format("%s@%s", serviceUsername, domain);

        Properties properties = new Properties();
        properties.put(INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        properties.put(PROVIDER_URL, providerURL);
        properties.put(SECURITY_PRINCIPAL, principal);
        properties.put(SECURITY_CREDENTIALS, servicePassword);
        properties.put(SECURITY_AUTHENTICATION, "simple");
        properties.put("java.naming.ldap.factory.socket", TrustAnythingSSLSocketFactory.class.getName());

        try
        {
            this.context = new InitialLdapContext(properties, null);
            this.domain = domain;
        }
        catch (NamingException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a user in the directory by its user name.
     *
     * @param username The user name of the user to get.
     * @return Returns the user.
     */
    public Optional<User> getUserByName(String username)
    {
        LOG.debug("Finding user with username '{}'", username);

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SUBTREE_SCOPE);
        controls.setReturningAttributes(USER_ATTRIBUTES);
        String filter = format("(& (userPrincipalName=%s@%s)(objectClass=user))", username, domain);

        try
        {
            NamingEnumeration<SearchResult> results = context.search(toDC(domain), filter, controls);
            while (results.hasMoreElements())
            {
                SearchResult result = results.nextElement();

                String distinguishedName = attribute(result, "distinguishedName");
                String userPrincipalName = attribute(result, "userPrincipalName");
                String name = attribute(result, "name");
                String mail = attribute(result, "mail");
                String telephoneNumber = attribute(result, "telephoneNumber");

                User user = new User();
                user.setDistinguishedName(distinguishedName);
                user.setUserPrincipalName(userPrincipalName);
                user.setName(name);
                user.setMail(mail);
                user.setTelephoneNumber(telephoneNumber);

                LOG.debug("Found user '{}'", name);
                return Optional.of(user);
            }

            LOG.debug("No user was found");
            return Optional.empty();
        }
        catch (NamingException e)
        {
            LOG.warn("Exception occurred when looking up user", e);
            return Optional.empty();
        }
    }

    /**
     * Sets a new password of a user in the directory.
     *
     * @param password The new password.
     * @return Returns whether or not the operation succeeded.
     */
    public boolean setPassword(String distinguishedName, String password)
    {
        LOG.debug("Setting password for user '{}'", distinguishedName);

        String quotedPassword = "\"" + password + "\"";
        char[] unicodePwd = quotedPassword.toCharArray();
        byte[] passwordArray = new byte[unicodePwd.length * 2];
        for (int i = 0; i < unicodePwd.length; i++)
        {
            // CSOFF
            passwordArray[i * 2 + 1] = (byte) (unicodePwd[i] >>> 8);
            passwordArray[i * 2 + 0] = (byte) (unicodePwd[i] & 0xff);
            // CSON
        }

        ModificationItem modification = new ModificationItem(REPLACE_ATTRIBUTE, new BasicAttribute("UnicodePwd", passwordArray));
        ModificationItem[] modifications = new ModificationItem[] {modification};

        try
        {
            context.modifyAttributes(distinguishedName, modifications);
            return true;
        }
        catch (NamingException e)
        {
            if (e.getMessage().startsWith("[LDAP: error code 53"))
            {
                LOG.debug("Password did not meet the requirements");
            }
            else
            {
                LOG.error("Exception occurred when setting password", e);
            }
            return false;
        }
    }

    private String attribute(SearchResult result, String name) throws NamingException
    {
        Attribute attribute = result.getAttributes().get(name);
        return attribute != null ? (String) attribute.get() : "";
    }

    private String toDC(String domain)
    {
        return Stream
                .of(domain.split("\\."))
                .filter(part -> !part.isEmpty())
                .map(part -> "DC=".concat(part))
                .collect(joining(","));
    }

    @Override
    public void close()
    {
        try
        {
            LOG.debug("Closing the LDAP connection");
            context.close();
        }
        catch (NamingException e)
        {
            LOG.error("Error occurred when closing the LDAP connection", e);
        }
    }
}
