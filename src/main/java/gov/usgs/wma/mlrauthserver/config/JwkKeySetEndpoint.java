package gov.usgs.wma.mlrauthserver.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.KeyPair;
import java.security.Principal;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@FrameworkEndpoint
class JwkKeySetEndpoint {	
	private KeyPair keyPair;

	@Autowired
	public JwkKeySetEndpoint(KeyPair keyPair) {
		this.keyPair = keyPair;
	}

	@GetMapping("/oauth/jwks.json")
	@ResponseBody
	public Map<String, Object> getKey(Principal principal) {
		RSAPublicKey publicKey = (RSAPublicKey) this.keyPair.getPublic();
		RSAKey key = new RSAKey.Builder(publicKey).build();
		return new JWKSet(key).toJSONObject();
	}
}