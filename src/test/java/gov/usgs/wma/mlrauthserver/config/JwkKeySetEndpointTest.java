package gov.usgs.wma.mlrauthserver.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.security.KeyPair;
import javax.sql.DataSource;
import com.nimbusds.jose.jwk.JWKSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.usgs.wma.mlrauthserver.dao.WaterAuthResourceIdAuthsDAO;
import net.minidev.json.JSONObject;

@SpringBootTest
@SpringBootConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = { 
		"security.jwt.key-store=classpath:mytest.jks", 
		"security.jwt.key-store-oauth-key=jwttestkey",
		"security.jwt.key-store-password=mypass", })
@ContextConfiguration(classes = { JwtConfig.class, JwkKeySetEndpoint.class })
public class JwkKeySetEndpointTest {

	@MockBean
	WaterAuthResourceIdAuthsDAO authDao;

	@MockBean
	DataSource dataSource;

	@Autowired
	private JwkKeySetEndpoint jwkKeySetEndpoint;

	@Autowired
	private KeyPair keyPair;

	@Test
	public void getKeyTest() throws Exception {
		JWKSet jwkSet = JWKSet.parse(new JSONObject(jwkKeySetEndpoint.getKey(null)));
		KeyPair resultPair = jwkSet.getKeys().get(0).toRSAKey().toKeyPair();
		assertEquals(1, jwkSet.getKeys().size());
		assertNull(resultPair.getPrivate());
		assertNotNull(keyPair.getPrivate());
		assertFalse(resultPair.equals(keyPair));
		assertTrue(keyPair.getPublic().equals(resultPair.getPublic()));
		assertFalse(keyPair.getPrivate().equals(resultPair.getPrivate()));
	}
}