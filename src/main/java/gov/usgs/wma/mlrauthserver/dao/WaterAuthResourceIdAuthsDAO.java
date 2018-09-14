package gov.usgs.wma.mlrauthserver.dao;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("default")
public class WaterAuthResourceIdAuthsDAO {
    private static final Logger LOG = LoggerFactory.getLogger(WaterAuthResourceIdAuthsDAO.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Set<String> getAuthListForResourceId(String resourceId) {
        Set<String> returnSet = null;
        try {
            String authsString = jdbcTemplate.queryForObject("SELECT authorities FROM oauth_resource_id_auths WHERE resource_id = ?", 
            new Object[]{resourceId}, String.class);
            
            returnSet = new HashSet<>();
            if(authsString != null && !authsString.isEmpty()) {
                returnSet = new HashSet<>(Arrays.asList(authsString.split(",")));
            }
            return returnSet;
        } catch(EmptyResultDataAccessException e) {
            LOG.debug("No matching entry in oauth_resource_id_auths for resource_id=" + resourceId);
            return null;
        }
    }

    public Set<String> getAuthListForResourceIdList(Set<String> resourceIds) {
        Set<String> resultSet = new HashSet<>();
        for(String resourceId : resourceIds) {
            Set<String> result = getAuthListForResourceId(resourceId);
            // If any of our resource IDs has no entry in the auth list table then return null because we need all auths
            if(result != null) {
                resultSet.addAll(result);
            } else {
                return null;
            }
        }
        return resultSet;
    }
}