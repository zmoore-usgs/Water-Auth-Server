package gov.usgs.wma.mlrauthserver.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.opensaml.saml2.core.Attribute;
import org.opensaml.xml.schema.XSString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.usgs.wma.mlrauthserver.model.WaterAuthUser;

@SpringBootTest
@SpringBootConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = { 
	"security.saml.attribute-names.username=usernamekey1",
	"security.saml.attribute-names.email=emailkey1",
	"security.saml.attribute-names.group=groupkey1,groupkey2", 
	"security.saml.attribute-names.details.office-state=statekey1,statekey2"
})
@ContextConfiguration(classes = { SAMLUserDetailsImpl.class})
public class SAMLUserDetailsImplTest {
	@Autowired
	private SAMLUserDetailsImpl sAMLUserDetailsImpl;
	
	@Mock
	private SAMLCredential samlCredential;
	
	@Test
	public void loadUserBySAMLTest1() {
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
		XSString attr4Val1 = mock(XSString.class);
		when(attr4Val1.getValue()).thenReturn("WI");
		XSString attr4Val2 = mock(XSString.class);
		when(attr4Val2.getValue()).thenReturn("MO");
		Attribute attr1 = mock(Attribute.class);
		when(attr1.getName()).thenReturn("emailkey1");
		when(attr1.getAttributeValues()).thenReturn(Arrays.asList(attr1Val1, attr1Val2));
		Attribute attr2 = mock(Attribute.class);
		when(attr2.getName()).thenReturn("usernamekey1");
		when(attr2.getAttributeValues()).thenReturn(Arrays.asList(attr2Val1));
		Attribute attr3 = mock(Attribute.class);
		when(attr3.getName()).thenReturn("groupkey1");
		when(attr3.getAttributeValues()).thenReturn(Arrays.asList(attr3Val1, attr3Val2));
		Attribute attr4 = mock(Attribute.class);
		when(attr4.getName()).thenReturn("statekey1");
		when(attr4.getAttributeValues()).thenReturn(Arrays.asList(attr4Val1, attr4Val2));
		samlCredential = mock(SAMLCredential.class);
		when(samlCredential.getAttributes()).thenReturn(Arrays.asList(attr1, attr2, attr3, attr4));
		WaterAuthUser result = sAMLUserDetailsImpl.loadUserBySAML(samlCredential);
		assertEquals(result.getUsername(), "test1");
		assertEquals(result.getEmail(), "test1@test.gov");
		assertEquals(result.getDetails().getOfficeState(), "WI");
		assertEquals(result.getPassword(), null);
		assertEquals(result.getAuthorities().size(), 2);
		assertEquals(((GrantedAuthority)result.getAuthorities().toArray()[0]).getAuthority(), "group1");
		assertEquals(((GrantedAuthority)result.getAuthorities().toArray()[1]).getAuthority(), "group3");
	}

	@Test
	public void loadUserBySAMLTest2() {
		XSString attr1Val1 = mock(XSString.class);
		when(attr1Val1.getValue()).thenReturn("test1@test.gov");
		XSString attr1Val2 = mock(XSString.class);
		when(attr1Val2.getValue()).thenReturn("test2@test.gov");
		XSString attr2Val1 = mock(XSString.class);
		when(attr2Val1.getValue()).thenReturn("test1");
		XSString attr4Val1 = mock(XSString.class);
		when(attr4Val1.getValue()).thenReturn("WI");
		Attribute attr1 = mock(Attribute.class);
		when(attr1.getName()).thenReturn("emailkey1");
		when(attr1.getAttributeValues()).thenReturn(Arrays.asList(attr1Val1, attr1Val2));
		Attribute attr2 = mock(Attribute.class);
		when(attr2.getName()).thenReturn("usernamekey1");
		when(attr2.getAttributeValues()).thenReturn(Arrays.asList(attr2Val1));
		Attribute attr3 = mock(Attribute.class);
		when(attr3.getName()).thenReturn("groupkey1");
		when(attr3.getAttributeValues()).thenReturn(new ArrayList<>());
		Attribute attr4 = mock(Attribute.class);
		when(attr4.getName()).thenReturn("statekey2");
		when(attr4.getAttributeValues()).thenReturn(Arrays.asList(attr4Val1));
		samlCredential = mock(SAMLCredential.class);
		when(samlCredential.getAttributes()).thenReturn(Arrays.asList(attr1, attr2, attr3, attr4));
		WaterAuthUser result = sAMLUserDetailsImpl.loadUserBySAML(samlCredential);
		assertEquals(result.getUsername(), "test1");
		assertEquals(result.getEmail(), "test1@test.gov");
		assertEquals(result.getDetails().getOfficeState(), "WI");
		assertEquals(result.getPassword(), null);
		assertEquals(result.getAuthorities().size(), 0);
	}
	
	@Test
	public void loadUserBySAMLTest3() {
		XSString attr1Val1 = mock(XSString.class);
		when(attr1Val1.getValue()).thenReturn("test1@test.gov");
		XSString attr1Val2 = mock(XSString.class);
		when(attr1Val2.getValue()).thenReturn("test2@test.gov");
		XSString attr2Val1 = mock(XSString.class);
		when(attr2Val1.getValue()).thenReturn("test1");
		XSString attr3Val1 = mock(XSString.class);
		when(attr3Val1.getValue()).thenReturn("group1");
		XSString attr3Val2 = mock(XSString.class);
		when(attr3Val2.getValue()).thenReturn("group2");
		Attribute attr1 = mock(Attribute.class);
		when(attr1.getName()).thenReturn("emailkey1");
		when(attr1.getAttributeValues()).thenReturn(Arrays.asList(attr1Val1, attr1Val2));
		Attribute attr2 = mock(Attribute.class);
		when(attr2.getName()).thenReturn("usernamekey1");
		when(attr2.getAttributeValues()).thenReturn(Arrays.asList(attr2Val1));
		Attribute attr3 = mock(Attribute.class);
		when(attr3.getName()).thenReturn("groupkey3");
		when(attr3.getAttributeValues()).thenReturn(Arrays.asList(attr3Val1, attr3Val2));
		samlCredential = mock(SAMLCredential.class);
		when(samlCredential.getAttributes()).thenReturn(Arrays.asList(attr1, attr2, attr3));
		WaterAuthUser result = sAMLUserDetailsImpl.loadUserBySAML(samlCredential);
		assertEquals(result.getUsername(), "test1");
		assertEquals(result.getEmail(), "test1@test.gov");
		assertEquals(result.getDetails().getOfficeState(), null);
		assertEquals(result.getPassword(), null);
		assertEquals(result.getAuthorities().size(), 0);
	}

	@Test
	public void loadUserBySAMLErrorTest1() {
		XSString attr1Val1 = mock(XSString.class);
		when(attr1Val1.getValue()).thenReturn("test1@test.gov");
		XSString attr1Val2 = mock(XSString.class);
		when(attr1Val2.getValue()).thenReturn("test2@test.gov");
		XSString attr2Val1 = mock(XSString.class);
		when(attr2Val1.getValue()).thenReturn("test1");
		XSString attr3Val1 = mock(XSString.class);
		when(attr3Val1.getValue()).thenReturn("group1");
		XSString attr3Val2 = mock(XSString.class);
		when(attr3Val2.getValue()).thenReturn("group2");
		Attribute attr1 = mock(Attribute.class);
		when(attr1.getName()).thenReturn("emailkey2");
		when(attr1.getAttributeValues()).thenReturn(Arrays.asList(attr1Val1, attr1Val2));
		Attribute attr2 = mock(Attribute.class);
		when(attr2.getName()).thenReturn("usernamekey1");
		when(attr2.getAttributeValues()).thenReturn(Arrays.asList(attr2Val1));
		Attribute attr3 = mock(Attribute.class);
		when(attr3.getName()).thenReturn("groupkey1");
		when(attr3.getAttributeValues()).thenReturn(Arrays.asList(attr3Val1, attr3Val2));
		samlCredential = mock(SAMLCredential.class);
		when(samlCredential.getAttributes()).thenReturn(Arrays.asList(attr1, attr2, attr3));
		
		try {
			sAMLUserDetailsImpl.loadUserBySAML(samlCredential);
			fail("Expected RuntimeException");
		} catch (RuntimeException e) { }
	}
	
	@Test
	public void loadUserBySAMLErrorTest2() {
		samlCredential = mock(SAMLCredential.class);
		when(samlCredential.getAttributes()).thenReturn(new ArrayList<>());
		try {
			sAMLUserDetailsImpl.loadUserBySAML(samlCredential);
			fail("Expected RuntimeException");
		} catch (RuntimeException e) { }
	}
}
