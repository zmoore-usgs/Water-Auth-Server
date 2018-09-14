package gov.usgs.wma.mlrauthserver.config;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.when;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.test.context.junit4.SpringRunner;

import gov.usgs.wma.mlrauthserver.dao.WaterAuthResourceIdAuthsDAO;

@RunWith(SpringRunner.class)
public class WaterAuthDBFilteredAccessTokenConverterTest {
    @Mock
    private WaterAuthResourceIdAuthsDAO authDao;
    @Mock
    private OAuth2Authentication oauthAuth;
    @Mock
    private OAuth2AccessToken oauthToken;
    @Mock
    private OAuth2Request oauthRequest;
    @Mock
    private Authentication userAuth;
    @Mock
    private Authentication clientAuth;

    private WaterAuthDBFilteredAccessTokenConverter converter;
    private List<GrantedAuthority> fullUserAuthorityList;

    @Before
    public void setup() {
        converter = new WaterAuthDBFilteredAccessTokenConverter(authDao);
        when(oauthAuth.isClientOnly()).thenReturn(false);
        when(oauthAuth.getOAuth2Request()).thenReturn(oauthRequest);
        when(oauthAuth.getUserAuthentication()).thenReturn(userAuth);
        when(userAuth.getName()).thenReturn("test");
        fullUserAuthorityList = new ArrayList<>();
        fullUserAuthorityList.add(new SimpleGrantedAuthority("group1"));
        fullUserAuthorityList.add(new SimpleGrantedAuthority("group2"));
        fullUserAuthorityList.add(new SimpleGrantedAuthority("group3"));
        fullUserAuthorityList.add(new SimpleGrantedAuthority("group4"));
        fullUserAuthorityList.add(new SimpleGrantedAuthority("group5"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void convertAccessTokenTest1() {
        given(authDao.getAuthListForResourceIdList(any(Set.class))).willReturn(new HashSet<>(Arrays.asList("group1", "group2")));
        Mockito.doReturn(fullUserAuthorityList).when(userAuth).getAuthorities();
        Map<String, ?> result = converter.convertAccessToken(oauthToken, oauthAuth);
        Map<String, ?> defaultResult = new DefaultAccessTokenConverter().convertAccessToken(oauthToken, oauthAuth);
        assertThat(result.keySet(), containsInAnyOrder(defaultResult.keySet().toArray()));
        for(String key : result.keySet()) {
            if(!key.equals("authorities")) {
                assertEquals(result.get(key), defaultResult.get(key));
            }
        }
        assertEquals(((Set<?>)defaultResult.get("authorities")).size(), 5);
        assertEquals(((Set<?>)result.get("authorities")).size(), 2);
        assertThat((Set<?>)result.get("authorities"), containsInAnyOrder("group1", "group2"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void convertAccessTokenTest2() {
        given(authDao.getAuthListForResourceIdList(any(Set.class))).willReturn(new HashSet<>(Arrays.asList("group-1", "group-2")));
        Mockito.doReturn(fullUserAuthorityList).when(userAuth).getAuthorities();
        Map<String, ?> result = converter.convertAccessToken(oauthToken, oauthAuth);
        Map<String, ?> defaultResult = new DefaultAccessTokenConverter().convertAccessToken(oauthToken, oauthAuth);
        assertThat(result.keySet(), containsInAnyOrder(defaultResult.keySet().toArray()));
        for(String key : result.keySet()) {
            if(!key.equals("authorities")) {
                assertEquals(result.get(key), defaultResult.get(key));
            }
        }
        assertEquals(((Set<?>)defaultResult.get("authorities")).size(), 5);
        assertEquals(((Set<?>)result.get("authorities")).size(), 0);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void convertAccessTokenTest3() {
        given(authDao.getAuthListForResourceIdList(any(Set.class))).willReturn(new HashSet<>());
        Mockito.doReturn(fullUserAuthorityList).when(userAuth).getAuthorities();
        Map<String, ?> result = converter.convertAccessToken(oauthToken, oauthAuth);
        Map<String, ?> defaultResult = new DefaultAccessTokenConverter().convertAccessToken(oauthToken, oauthAuth);
        assertThat(result.keySet(), containsInAnyOrder(defaultResult.keySet().toArray()));
        for(String key : result.keySet()) {
            if(!key.equals("authorities")) {
                assertEquals(result.get(key), defaultResult.get(key));
            }
        }
        assertEquals(((Set<?>)defaultResult.get("authorities")).size(), 5);
        assertEquals(((Set<?>)result.get("authorities")).size(), 0);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void convertAccessTokenTest4() {
        given(authDao.getAuthListForResourceIdList(any(Set.class))).willReturn(null);
        Mockito.doReturn(fullUserAuthorityList).when(userAuth).getAuthorities();
        Map<String, ?> result = converter.convertAccessToken(oauthToken, oauthAuth);
        Map<String, ?> defaultResult = new DefaultAccessTokenConverter().convertAccessToken(oauthToken, oauthAuth);
        assertThat(result.keySet(), containsInAnyOrder(defaultResult.keySet().toArray()));
        for(String key : result.keySet()) {
            if(!key.equals("authorities")) {
                assertEquals(result.get(key), defaultResult.get(key));
            }
        }
        assertEquals(((Set<?>)defaultResult.get("authorities")).size(), 5);
        assertEquals(((Set<?>)result.get("authorities")).size(), 5);
        assertThat((Set<?>)result.get("authorities"), containsInAnyOrder("group1", "group2", "group3", "group4", "group5"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void convertAccessTokenTest5() {
        given(authDao.getAuthListForResourceIdList(any(Set.class))).willReturn(new HashSet<>(Arrays.asList("group-1", "group2")));
        Mockito.doReturn(fullUserAuthorityList).when(userAuth).getAuthorities();
        Map<String, ?> result = converter.convertAccessToken(oauthToken, oauthAuth);
        Map<String, ?> defaultResult = new DefaultAccessTokenConverter().convertAccessToken(oauthToken, oauthAuth);
        assertThat(result.keySet(), containsInAnyOrder(defaultResult.keySet().toArray()));
        for(String key : result.keySet()) {
            if(!key.equals("authorities")) {
                assertEquals(result.get(key), defaultResult.get(key));
            }
        }
        assertEquals(((Set<?>)defaultResult.get("authorities")).size(), 5);
        assertEquals(((Set<?>)result.get("authorities")).size(), 1);
        assertThat((Set<?>)result.get("authorities"), containsInAnyOrder("group2"));
    }
}