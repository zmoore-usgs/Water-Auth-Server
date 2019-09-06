package gov.usgs.wma.mlrauthserver.config.local;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

import gov.usgs.wma.mlrauthserver.model.local.LocalWaterAuthUser;

/**
 * This is based on an example linked in the StackOverflow thread below. This
 * class is necessary because the default InMemoryUserDetailsManager doesn't
 * return custom user classes.
 * 
 * See: https://stackoverflow.com/questions/22564532/how-to-get-a-custom-user-logged-via-inmemoryauthentication-with-spring-security
 *
 * @see org.springframework.security.provisioning.InMemoryUserDetailsManager
 */
public class LocalInMemoryWaterAuthUserDetailsManager implements UserDetailsManager {

    private final Map<String, LocalWaterAuthUser> users = new HashMap<>();

    @Override
    public void createUser(UserDetails user) {
        Assert.isTrue(!userExists(user.getUsername()), "User with username '" + user.getUsername() + "' already exists.");

        users.put(user.getUsername().toLowerCase(), (LocalWaterAuthUser) user);
    }

    @Override
    public void updateUser(UserDetails user) {
        Assert.isTrue(userExists(user.getUsername()), "User with username '" + user.getUsername() + "' does not exists.");

        users.put(user.getUsername().toLowerCase(), (LocalWaterAuthUser) user);
    }

    @Override
    public void deleteUser(String username) {
        users.remove(username.toLowerCase());
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean userExists(String username) {
        return users.containsKey(username.toLowerCase());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LocalWaterAuthUser user = users.get(username.toLowerCase());

        if (user == null) {
            throw new UsernameNotFoundException(username);
        }

        return user;
    }
}