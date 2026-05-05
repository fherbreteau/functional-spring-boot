package io.github.fherbreteau.functional.driven;

import java.util.List;

public interface PasswordProtector {

    /**
     * Protect the password of the user
     * @param password the user raw password
     * @return a protected version of the password
     */
    String protect(String password);

    /**
     * Validate the password of a specific user
     * @param username the user name
     * @param password the user password
     * @return the list of Validation errors in the user password
     */
    List<String> validate(String username, String password);
}
