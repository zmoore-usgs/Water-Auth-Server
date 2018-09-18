package gov.usgs.wma.mlrauthserver.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opensaml.saml2.core.Attribute;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSAnyImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@SpringBootConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SAMLUtilsTest.class })
public class SAMLUtilsTest {
    @Mock
    private SAMLCredential samlCredential;

    @Test
    public void getAttributeValueMapTest1() {
        XSString attr1Val1 = mock(XSString.class);
        when(attr1Val1.getValue()).thenReturn("test1@test.gov");
        XSString attr1Val2 = mock(XSString.class);
        when(attr1Val2.getValue()).thenReturn("test2@test.gov");
        XSString attr2Val1 = mock(XSString.class);
        when(attr2Val1.getValue()).thenReturn("test1");
        XSString attr3Val1 = mock(XSString.class);
        when(attr3Val1.getValue()).thenReturn("group1");
        XSString attr3Val2 = mock(XSString.class);
        when(attr3Val2.getValue()).thenReturn("group3");
        Attribute attr1 = mock(Attribute.class);
        when(attr1.getName()).thenReturn("emailkey1");
        when(attr1.getAttributeValues()).thenReturn(Arrays.asList(attr1Val1, attr1Val2));
        Attribute attr2 = mock(Attribute.class);
        when(attr2.getName()).thenReturn("usernamekey1");
        when(attr2.getAttributeValues()).thenReturn(Arrays.asList(attr2Val1));
        Attribute attr3 = mock(Attribute.class);
        when(attr3.getName()).thenReturn("groupkey1");
        when(attr3.getAttributeValues()).thenReturn(Arrays.asList(attr3Val1, attr3Val2));
        samlCredential = mock(SAMLCredential.class);
        when(samlCredential.getAttributes()).thenReturn(Arrays.asList(attr1, attr2, attr3));
        Map<String,List<String>> result = SAMLUtils.getAttributeValueMap(samlCredential);
        assertEquals(result.keySet().size(), 3);
        assertThat(result.get("emailkey1"), containsInAnyOrder("test1@test.gov", "test2@test.gov"));
        assertThat(result.get("usernamekey1"), containsInAnyOrder("test1"));
        assertThat(result.get("groupkey1"), containsInAnyOrder("group1", "group3"));
    }

    @Test
    public void getAttributeValueMapTest2() {
        XSString attr1Val1 = mock(XSString.class);
        when(attr1Val1.getValue()).thenReturn("test1@test.gov");
        XSString attr1Val2 = mock(XSString.class);
        when(attr1Val2.getValue()).thenReturn("test2@test.gov");
        XSString attr3Val1 = mock(XSString.class);
        when(attr3Val1.getValue()).thenReturn("group1");
        XSString attr3Val2 = mock(XSString.class);
        when(attr3Val2.getValue()).thenReturn("group3");
        Attribute attr1 = mock(Attribute.class);
        when(attr1.getName()).thenReturn("emailkey1");
        when(attr1.getAttributeValues()).thenReturn(Arrays.asList(attr1Val1, attr1Val2));
        Attribute attr2 = mock(Attribute.class);
        when(attr2.getName()).thenReturn("usernamekey1");
        when(attr2.getAttributeValues()).thenReturn(new ArrayList<>());
        Attribute attr3 = mock(Attribute.class);
        when(attr3.getName()).thenReturn("groupkey1");
        when(attr3.getAttributeValues()).thenReturn(Arrays.asList(attr3Val1, attr3Val2));
        samlCredential = mock(SAMLCredential.class);
        when(samlCredential.getAttributes()).thenReturn(Arrays.asList(attr1, attr2, attr3));
        Map<String,List<String>> result = SAMLUtils.getAttributeValueMap(samlCredential);
        assertEquals(result.keySet().size(), 3);
        assertThat(result.get("emailkey1"), containsInAnyOrder("test1@test.gov", "test2@test.gov"));
        assertTrue(result.get("usernamekey1").isEmpty());
        assertThat(result.get("groupkey1"), containsInAnyOrder("group1", "group3"));
    }

    @Test
    public void getAttributeValueMapTest3() {
        samlCredential = mock(SAMLCredential.class);
        when(samlCredential.getAttributes()).thenReturn(new ArrayList<>());
        Map<String,List<String>> result = SAMLUtils.getAttributeValueMap(samlCredential);
        assertEquals(result.keySet().size(), 0);
    }

    @Test
    public void getAttributeValueMapTest4() {
        Attribute attr1 = mock(Attribute.class);
        when(attr1.getName()).thenReturn("emailkey1");
        when(attr1.getAttributeValues()).thenReturn(null);
        Attribute attr2 = mock(Attribute.class);
        when(attr2.getName()).thenReturn("usernamekey1");
        when(attr2.getAttributeValues()).thenReturn(null);
        Attribute attr3 = mock(Attribute.class);
        when(attr3.getName()).thenReturn("groupkey1");
        when(attr3.getAttributeValues()).thenReturn(null);
        samlCredential = mock(SAMLCredential.class);
        when(samlCredential.getAttributes()).thenReturn(Arrays.asList(attr1, attr2, attr3));
        Map<String,List<String>> result = SAMLUtils.getAttributeValueMap(samlCredential);
        assertEquals(result.keySet().size(), 0);
    }

    @Test
    public void getAttributeTextValueTest() {
        XSString val1 = mock(XSString.class);
        when(val1.getValue()).thenReturn("test");
        XSAnyImpl val2 = mock(XSAnyImpl.class);
        when(val2.getTextContent()).thenReturn("test");
        XSAny val3 = mock(XSAny.class);
        when(val3.toString()).thenReturn("test");

        assertEquals(SAMLUtils.getAttributeTextValue(val1), "test");
        assertEquals(SAMLUtils.getAttributeTextValue(val2), "test");
        assertEquals(SAMLUtils.getAttributeTextValue(val3), "test");
    }

    @Test
    public void getFirstMatchingAttributeValueListTest() {
        Map<String,List<String>> attrMap = new HashMap<>();
        attrMap.put("key1", Arrays.asList("test1", "test2"));
        attrMap.put("key2", Arrays.asList("test3"));
        attrMap.put("key3", new ArrayList<>());

        List<String> result = SAMLUtils.getFirstMatchingAttributeValueList(attrMap, new String[]{"key1"});
        assertEquals(result.size(), 2);
        assertThat(result, containsInAnyOrder("test1", "test2"));
        
        result = SAMLUtils.getFirstMatchingAttributeValueList(attrMap, new String[]{"key1", "key2"});
        assertEquals(result.size(), 2);
        assertThat(result, containsInAnyOrder("test1", "test2"));

        result = SAMLUtils.getFirstMatchingAttributeValueList(attrMap, new String[]{"key2", "key1"});
        assertEquals(result.size(), 1);
        assertThat(result, containsInAnyOrder("test3"));

        result = SAMLUtils.getFirstMatchingAttributeValueList(attrMap, new String[]{"key2"});
        assertEquals(result.size(), 1);
        assertThat(result, containsInAnyOrder("test3"));

        result = SAMLUtils.getFirstMatchingAttributeValueList(attrMap, new String[]{"key3", "key1"});
        assertEquals(result.size(), 2);
        assertThat(result, containsInAnyOrder("test1", "test2"));

        result = SAMLUtils.getFirstMatchingAttributeValueList(attrMap, new String[]{"key1", "key3"});
        assertEquals(result.size(), 2);
        assertThat(result, containsInAnyOrder("test1", "test2"));
    }

    @Test
    public void getFirstMatchingAttributeValueListErrorTest() {
        Map<String,List<String>> attrMap = new HashMap<>();
        attrMap.put("key1", new ArrayList<>());

        try {
            SAMLUtils.getFirstMatchingAttributeValueList(attrMap, new String[]{"key1"});
            fail("Expected RuntimeException");
        } catch(RuntimeException e) {
            assertTrue(e.getMessage().contains("but none of the matching keys contained any data!"));
        }

        try {
            SAMLUtils.getFirstMatchingAttributeValueList(attrMap, new String[]{"key2"});
            fail("Expected RuntimeException");
        } catch(RuntimeException e) {
            assertTrue(e.getMessage().contains("SAML response had no key matching any of"));
        }
    }
}