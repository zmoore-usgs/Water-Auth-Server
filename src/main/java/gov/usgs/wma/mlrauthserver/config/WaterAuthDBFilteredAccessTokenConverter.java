package gov.usgs.wma.mlrauthserver.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
        Map<String, Object> returnMap = new HashMap<>();
        Map<String, ?> defaultTokenMap = super.convertAccessToken(token, authentication);
        Set<String> defaultAuthSet = getAuthSetFromTokenMap(defaultTokenMap);
        
        // Build initial token map
        returnMap.putAll(defaultTokenMap);
        returnMap.put(AUTHORITIES, new HashSet<>());

        // Only filter Auths if User has any Auths to filter
        if(!defaultAuthSet.isEmpty()) {
            Set<String> filterAuths = authDao.getAuthListForResourceIdList(authentication.getOAuth2Request().getResourceIds());

            // Only do filtering if the resourceIDs are all present in the resourceId-auth filter table
            if(filterAuths != null && !filterAuths.isEmpty()) {
                // Filter user authorizations to only those in the include list
                Set<String> filteredAuthSet = new HashSet<>();
                for(String auth : defaultAuthSet) {
                    if(filterAuths.contains(auth)) {
                        filteredAuthSet.add(auth);
                    }
                }
                returnMap.put(AUTHORITIES, filteredAuthSet);
            }
        }

        return returnMap;
    }

    protected Set<String> getAuthSetFromTokenMap(Map<String, ?> tokenMap) {
        Set<String> returnSet = new HashSet<>();

        if(tokenMap.get(AUTHORITIES) != null) {
            try {
                Collection<?> objectCollection = Collection.class.cast(tokenMap.get(AUTHORITIES));

                if(!objectCollection.isEmpty()) {
                    returnSet = objectCollection.stream().map(o -> o.toString()).collect(Collectors.toSet());
                }
            } catch(ClassCastException c) {
                LOG.error("Unable to cast token authorities to a Collection for modification. Error: " + c.getMessage());
                throw(c);
            } catch(Exception e) {
                LOG.error("An unexpected error occurred while trying to read token authorities. Error: " + e.getMessage());
                throw(e);
            }
        }

        return returnSet;
    }
}