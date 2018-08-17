package gov.usgs.wma.mlrauthserver.config;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@SpringBootConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = { 
		"security.jwt.key-store=classpath:mytest.jks", 
		"security.jwt.key-store-oauth-key=jwttestkey",
		"security.jwt.key-store-password=mypass", })
@ContextConfiguration(classes = { JwtConfig.class })

public class JwtConfigTest {

	@Autowired
	private JwtConfig jwtConfig;

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testKeyValueUsingAccessTokenConverter() {
		String keyValue = "-----BEGIN PUBLIC KEY-----\n"
				+ "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA8/WDeUkmxBBBZLuisp7aUi+9H5jfeuuALa5vZM2ORqu7XyayvLfw+oNMMman3sm1SteeND8ID/nD/cpOQYqiVGe5J/KYEE4YDrp0cxH+xI7nfCScIPPaslGYm6RX9FXge2jJn1kMzNP35z65wT+XRNNNhJQHdeUNxqicwoX43IQ7gdEBeAH6kgRDDKzD1LE3DjmXlZAIs/EokiewUEt1TCU5bJczSuVqKBykP9WapiV7IqIpu6t32Vq6dnYcBmN+ZxTkVfTXTtHjY2GlUkmkfvsyKoy36mqLgDHZSvg5BQcnHlL6JQcalOca6EXlGvY+6qTf31K5YPCAT5RoUsugpQIDAQAB\n"
				+ "-----END PUBLIC KEY-----";
		assertEquals(keyValue, jwtConfig.accessTokenConverter().getKey().get("value"));
	}
	
	@Test
	public void testKeyAlgorithmUsingAccessTokenConverter() {
		String keyAlg = "SHA256withRSA";
		assertEquals(keyAlg, jwtConfig.accessTokenConverter().getKey().get("alg"));
	}

}
