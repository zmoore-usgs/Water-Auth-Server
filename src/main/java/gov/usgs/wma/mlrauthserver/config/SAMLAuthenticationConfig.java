package gov.usgs.wma.mlrauthserver.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.commons.httpclient.HttpClient;

import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

import org.opensaml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.saml2.metadata.provider.ResourceBackedMetadataProvider;
import org.opensaml.util.resource.ClasspathResource;
import org.opensaml.util.resource.ResourceException;
import org.opensaml.xml.parse.StaticBasicParserPool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLBootstrap;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.SAMLLogoutFilter;
import org.springframework.security.saml.SAMLLogoutProcessingFilter;
import org.springframework.security.saml.SAMLProcessingFilter;
import org.springframework.security.saml.context.SAMLContextProviderImpl;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.saml.parser.ParserPoolHolder;
import org.springframework.security.saml.websso.WebSSOProfileOptions;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.metadata.MetadataDisplayFilter;
import org.springframework.security.saml.processor.HTTPRedirectDeflateBinding;
import org.springframework.security.saml.processor.SAMLBinding;
import org.springframework.security.saml.processor.SAMLProcessorImpl;
import org.springframework.security.saml.websso.SingleLogoutProfile;
import org.springframework.security.saml.websso.SingleLogoutProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfile;
import org.springframework.security.saml.websso.WebSSOProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfileConsumer;
import org.springframework.security.saml.websso.WebSSOProfileConsumerHoKImpl;
import org.springframework.security.saml.websso.WebSSOProfileConsumerImpl;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 *
 * @author zmoore
 */
@Configuration
@EnableWebSecurity
public class SAMLAuthenticationConfig extends WebSecurityConfigurerAdapter {
	/*
	* Configuration Properties
	*/
	@Value("${samlIDPMetadataLocation}")
	private String metadataLocation;
	
	@Value("${samlKeystoreLocation}")
	private String keystoreLocation;
	
	@Value("${samlKeystoreDefaultKey}")
	private String keystoreDefaultKey;
	
	@Value("${samlKeystorePassword}")
	private String keystorePassword;
	
	@Value("${samlAuthnRequestEntityId}")
	private String entityId;
	
	@Value("${samlAuthnRequestProviderName}")
	private String providerName;
		
	/*
	* Utility Objects
	*/
	
	private Timer metadataTimer;
	private MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager;
	
	/*
	* Utility Methods
	*/
	@PostConstruct
	public void init() {
		this.metadataTimer = new Timer(true);
		this.multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
	}
	
	@PreDestroy
	public void destroy() {
		this.metadataTimer.purge();
		this.metadataTimer.cancel();
		this.multiThreadedHttpConnectionManager.shutdown();
	}
	
	/*
	* Utility Beans
	*/
	@Bean(initMethod = "initialize")
	public StaticBasicParserPool parserPool() {
		return new StaticBasicParserPool();
	}

	@Bean(name = "parserPoolHolder")
	public ParserPoolHolder parserPoolHolder() {
		return new ParserPoolHolder();
	}
	
	@Bean
	public HttpClient httpClient() {
		return new HttpClient(this.multiThreadedHttpConnectionManager);
	}
	
	@Bean
	public KeyManager keyManager() {
		Resource storeFile = new ClassPathResource(this.keystoreLocation);
		Map<String, String> passwords = new HashMap<String, String>();
		passwords.put(this.keystoreDefaultKey, this.keystorePassword);
		return new JKSKeyManager(storeFile, this.keystorePassword, passwords, this.keystoreDefaultKey);
	}
	
	/*
	* SAMLEntryPoint and Related beans Construction
	*/
	@Bean
	public SAMLEntryPoint samlEntryPoint() {
		SAMLEntryPoint samlEntryPoint = new SAMLEntryPoint();
		
		samlEntryPoint.setDefaultProfileOptions(webSSOProfileOptions());
		
		return samlEntryPoint;
	}
	
	@Bean
	public WebSSOProfileOptions webSSOProfileOptions() {
		WebSSOProfileOptions webSSOProfileOptions = new WebSSOProfileOptions();
		
		webSSOProfileOptions.setIncludeScoping(false);
		webSSOProfileOptions.setBinding("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect");
		webSSOProfileOptions.setAuthnContextComparison(AuthnContextComparisonTypeEnumeration.EXACT);
		webSSOProfileOptions.setForceAuthN(false);
		webSSOProfileOptions.setAuthnContexts(authnContexts());
		webSSOProfileOptions.setProviderName(providerName);
		
		return webSSOProfileOptions;
	}
	
	@Bean
	public List<String> authnContexts() {
		List<String> authnContexts = new ArrayList<>();
		
		authnContexts.add("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");
		
		return authnContexts;
	}
	
	@Bean
	public SAMLAuthenticationProvider samlAuthenticationProvider() {
		SAMLAuthenticationProvider samlAuthenticationProvider = new SAMLAuthenticationProvider();
		samlAuthenticationProvider.setForcePrincipalAsString(false);
		return samlAuthenticationProvider;
	}

	@Bean
	public SAMLContextProviderImpl contextProvider() {
		return new SAMLContextProviderImpl();
	}

	@Bean
	public static SAMLBootstrap sAMLBootstrap() {
		return new SAMLBootstrap();
	}

	@Bean
	public SAMLDefaultLogger samlLogger() {
		return new SAMLDefaultLogger();
	}

	@Bean
	public WebSSOProfileConsumer webSSOprofileConsumer() {
		return new WebSSOProfileConsumerImpl();
	}

	 // SAML 2.0 Holder-of-Key WebSSO Assertion Consumer
	@Bean
	public WebSSOProfileConsumerHoKImpl hokWebSSOprofileConsumer() {
		return new WebSSOProfileConsumerHoKImpl();
	}

	// SAML 2.0 Web SSO profile
	@Bean
	public WebSSOProfile webSSOprofile() {
		return new WebSSOProfileImpl();
	}

	// SAML 2.0 Holder-of-Key Web SSO profile
	@Bean
	public WebSSOProfileConsumerHoKImpl hokWebSSOProfile() {
		return new WebSSOProfileConsumerHoKImpl();
	}

	@Bean
	public SingleLogoutProfile logoutprofile() {
		return new SingleLogoutProfileImpl();
	}
	
	@Bean
	public HTTPRedirectDeflateBinding httpRedirectDeflateBinding() {
		return new HTTPRedirectDeflateBinding(parserPool());
	}
	
	@Bean
	public SAMLProcessorImpl processor() {
		Collection<SAMLBinding> bindings = new ArrayList<SAMLBinding>();
		bindings.add(httpRedirectDeflateBinding());
		return new SAMLProcessorImpl(bindings);
	}
	
	/*
	 * MetadataGeneratorFilter and Related Beans Construction
	 */
	@Bean
	public MetadataGeneratorFilter metadataGeneratorFilter() {
		MetadataGeneratorFilter metadataGeneratorFilter = new MetadataGeneratorFilter(metadataGenerator());
		
		return metadataGeneratorFilter;
	}
	
	@Bean
	public MetadataGenerator metadataGenerator() {
		MetadataGenerator metadataGenerator = new MetadataGenerator();
		
		metadataGenerator.setEntityId(entityId);
		metadataGenerator.setRequestSigned(false);
		metadataGenerator.setExtendedMetadata(extendedMetadata());
		 metadataGenerator.setKeyManager(keyManager()); 
		metadataGenerator.setIncludeDiscoveryExtension(false);
		
		return metadataGenerator;
	}
	
	@Bean
	public ExtendedMetadata extendedMetadata() {
		ExtendedMetadata extendedMetadata = new ExtendedMetadata();
		
		extendedMetadata.setSignMetadata(false);
		extendedMetadata.setIdpDiscoveryEnabled(false);
		extendedMetadata.setEcpEnabled(false);
		
		return extendedMetadata;
	}
	
	/*
	* CachingMetadataManager and Related Beans Construction
	*/
	@Bean
	@Qualifier("metadata")
	public CachingMetadataManager metadata() throws MetadataProviderException, ResourceException {
		CachingMetadataManager cachingMetadataManager = new CachingMetadataManager(metadataProviders());
				
		return cachingMetadataManager;
	} 
	
	@Bean
	public List<MetadataProvider> metadataProviders() throws MetadataProviderException, ResourceException {
		List<MetadataProvider> metadataProviders = new ArrayList<>();
		
		if(isLocalMetadata()){
			metadataProviders.add(doiSAMLExtendedMetadataProviderLocal());
		} else {
			metadataProviders.add(doiSAMLExtendedMetadataProvider());
		}
		
		return metadataProviders;
	}
	
	//Determines whether or not the provided metadata location is a local file or remote URL
	private boolean isLocalMetadata() {
		if(this.metadataLocation != null){
			if(this.metadataLocation.toLowerCase().startsWith("http://") || this.metadataLocation.toLowerCase().startsWith("https://")) {
				return false;
			}
		}
		
		return true;
	}
	
	//Local Filesystem Metadata Provider
	@Bean
	@Qualifier("idp-doi-saml-local")
	public ExtendedMetadataDelegate doiSAMLExtendedMetadataProviderLocal() throws MetadataProviderException, ResourceException {
		ResourceBackedMetadataProvider resourceMetadataProvider = new ResourceBackedMetadataProvider(
				this.metadataTimer, new ClasspathResource(this.metadataLocation));
		
		resourceMetadataProvider.setParserPool(parserPool());
		
		ExtendedMetadataDelegate extendedMetadataDelegate = 
				new ExtendedMetadataDelegate(resourceMetadataProvider, extendedMetadata());
		
		extendedMetadataDelegate.setMetadataTrustCheck(false);
		extendedMetadataDelegate.setMetadataRequireSignature(false);
		this.metadataTimer.purge();
		
		return extendedMetadataDelegate;
	}
	
	//Remote HTTP Metadata Provider
	@Bean
	@Qualifier("idp-doi-saml-http")
	public ExtendedMetadataDelegate doiSAMLExtendedMetadataProvider() throws MetadataProviderException {
		HTTPMetadataProvider httpMetadataProvider = new HTTPMetadataProvider(
				this.metadataTimer, httpClient(), this.metadataLocation);
		
		httpMetadataProvider.setParserPool(parserPool());
		
		ExtendedMetadataDelegate extendedMetadataDelegate = 
				new ExtendedMetadataDelegate(httpMetadataProvider, extendedMetadata());
		
		extendedMetadataDelegate.setMetadataTrustCheck(false);
		extendedMetadataDelegate.setMetadataRequireSignature(false);
		this.metadataTimer.purge();
		
		return extendedMetadataDelegate;
	}
	
	/*
	* SAML Security Filter
	*/
	@Bean
	public FilterChainProxy samlFilter() throws Exception {
		List<SecurityFilterChain> chains = new ArrayList<SecurityFilterChain>();
		chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/login/**"),
				samlEntryPoint()));
		chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/logout/**"),
				samlLogoutFilter()));
		chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/metadata/**"),
				metadataDisplayFilter()));
		chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSO/**"),
				samlWebSSOProcessingFilter()));
		chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SingleLogout/**"),
				samlLogoutProcessingFilter()));
		return new FilterChainProxy(chains);
	}
	
	@Bean
	public SimpleUrlLogoutSuccessHandler successLogoutHandler() {
		SimpleUrlLogoutSuccessHandler successLogoutHandler = new SimpleUrlLogoutSuccessHandler();
		successLogoutHandler.setDefaultTargetUrl("/");
		return successLogoutHandler;
	}
	
	@Bean
	public SecurityContextLogoutHandler logoutHandler() {
	   SecurityContextLogoutHandler logoutHandler = 
			   new SecurityContextLogoutHandler();
	   logoutHandler.setInvalidateHttpSession(true);
	   logoutHandler.setClearAuthentication(true);
	   return logoutHandler;
	}

	@Bean
	public SAMLLogoutProcessingFilter samlLogoutProcessingFilter() {
	   return new SAMLLogoutProcessingFilter(successLogoutHandler(),
			   logoutHandler());
	}

	@Bean
	public SAMLLogoutFilter samlLogoutFilter() {
	   return new SAMLLogoutFilter(successLogoutHandler(),
			   new LogoutHandler[] { logoutHandler() },
			   new LogoutHandler[] { logoutHandler() });
	}
	
	@Bean
	public MetadataDisplayFilter metadataDisplayFilter() {
		return new MetadataDisplayFilter();
	}
	
	@Bean
	public SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler() {
		SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler =
				new SavedRequestAwareAuthenticationSuccessHandler();
		successRedirectHandler.setDefaultTargetUrl("/");
		return successRedirectHandler;
	}

	@Bean
	public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
		SimpleUrlAuthenticationFailureHandler failureHandler =
				new SimpleUrlAuthenticationFailureHandler();
		failureHandler.setUseForward(true);
		failureHandler.setDefaultFailureUrl("/error");
		return failureHandler;
	}

	@Bean
	public SAMLProcessingFilter samlWebSSOProcessingFilter() throws Exception {
		SAMLProcessingFilter samlWebSSOProcessingFilter = new SAMLProcessingFilter();
		samlWebSSOProcessingFilter.setAuthenticationManager(authenticationManager());
		samlWebSSOProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
		samlWebSSOProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
		return samlWebSSOProcessingFilter;
	}

	/*
	* Web Security Overrides
	*/
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
	   return super.authenticationManagerBean();
	}

	@Override  
	protected void configure(HttpSecurity http) throws Exception {
	   http
		   .httpBasic()
			   .authenticationEntryPoint(samlEntryPoint());
	   http
		   .csrf()
			   .disable();
	   http
		   .addFilterBefore(metadataGeneratorFilter(), ChannelProcessingFilter.class)
		   .addFilterAfter(samlFilter(), BasicAuthenticationFilter.class);
	   http        
		   .authorizeRequests()
		   .antMatchers("/").permitAll()
		   .antMatchers("/error").permitAll()
		   .antMatchers("/saml/**").permitAll()
		   .anyRequest().authenticated();
	   http
		   .logout()
			   .logoutSuccessUrl("/");
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
	   auth
		   .authenticationProvider(samlAuthenticationProvider());
	}
}
