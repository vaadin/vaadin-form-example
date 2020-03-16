package org.vaadin.examples.form.data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class UserDetailsService implements Serializable {

    private String previousHandle;

    public void store(UserDetails userDetails) throws ServiceException {

        // Here you can store the object into the DB, call REST services, etc.

        // for demo purposes, always fail first try
        if (previousHandle == null || !previousHandle.equals(userDetails.getHandle())) {
            previousHandle = userDetails.getHandle();
            throw new ServiceException();
        }
    }

    /**
     * A validator method for User handles.
     *
     * @return <code>null</code> if the handle is OK to use or an error message if
     *         it is not OK.
     */
    public String validateHandle(String handle) {

        if (handle == null) {
            return "Handle can't be empty";
        }
        if (handle.length() < 4) {
            return "Handle can't be shorter than 4 characters";
        }
        List<String> reservedHandles = Arrays.asList("admin", "test", "null", "void");
        if (reservedHandles.contains(handle)) {
            return String.format("'%s' is not available as a handle", handle);
        }

        return null;
    }

    /**
     * Utility Exception class that we can use in the frontend to show that
     * something went wrong during save.
     */
    public static class ServiceException extends Exception {

    }
}
