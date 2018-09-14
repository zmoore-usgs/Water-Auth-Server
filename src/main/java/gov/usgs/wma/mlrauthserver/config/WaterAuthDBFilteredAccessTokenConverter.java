package gov.usgs.wma.mlrauthserver.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;

import gov.usgs.wma.mlrauthserver.dao.WaterAuthResourceIdAuthsDAO;

public class WaterAuthDBFilteredAccessTokenConverter extends DefaultAccessTokenConverter {
    private static final Logger LOG = LoggerFactory.getLogger(WaterAuthDBFilteredAccessTokenConverter.class);
    private WaterAuthResourceIdAuthsDAO authDao;
    
    public WaterAuthDBFilteredAccessTokenConverter(WaterAuthResourceIdAuthsDAO authDao) {
        super();
        this.authDao = authDao;
    }

    @Override
    public Map<String, ?> convertAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        // Build default token map
        Map<String, ?> defaultTokenMap = super.convertAccessToken(token, authentication);
        Map<String, Object> returnMap = new HashMap<>();
        returnMap.putAll(defaultTokenMap);
        
        // Fetch included Auth Set for resource IDs
        Set<String> filterAuths = authDao.getAuthListForResourceIdList(authentication.getOAuth2Request().getResourceIds());
        
        // Only do filtering if the resourceIDs are all present in the resourceId-auth filter table
        if(filterAuths != null) {
            // Filter user authorizations to only those in the include list
            if(!filterAuths.isEmpty()) {
                try {
                    Set<?> objectSet = Set.class.cast(returnMap.get(AUTHORITIES));
                    Set<String> authSet = objectSet.stream().map(o -> (String) o).collect(Collectors.toSet());
                    Set<String> filteredAuthSet = new HashSet<>();
                    for(String auth : authSet) {
                        if(filterAuths.contains(auth)) {
                            filteredAuthSet.add(auth);
                        }
                    }
                    returnMap.put(AUTHORITIES, filteredAuthSet);
                } catch(ClassCastException c) {
                    LOG.error("Unable to cast token authorities to String Set for modification. Error: " + c.getMessage());
                    throw(c);
                } catch(Exception e) {
                    LOG.error("An unexpected error occurred while trying to filter user authorities. Error: " + e.getMessage());
                    throw(e);
                }
            } else {
                returnMap.put(AUTHORITIES, new HashSet<>());
            }
        }

        return returnMap;
    }
}