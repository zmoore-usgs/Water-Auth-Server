/**
 * Class  structure modeled after: https://github.com/vdenotaris/spring-boot-security-saml-sample/blob/master/src/main/java/com/vdenotaris/spring/boot/security/saml/web/config/WebSecurityConfig.java
 */

package gov.usgs.wma.mlrauthserver.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.saml2.metadata.provider.ResourceBackedMetadataProvider;
import org.opensaml.util.resource.ClasspathResource;
import org.opensaml.util.resource.FilesystemResource;
import org.opensaml.util.resource.ResourceException;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResourceLoader;
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
import org.springframework.security.saml.SAMLWebSSOHoKProcessingFilter;
import org.springframework.security.saml.context.SAMLContextProviderImpl;
import org.springframework.security.saml.context.SAMLContextProviderLB;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.security.saml.metadata.MetadataDisplayFilter;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.saml.parser.ParserPoolHolder;
import org.springframework.security.saml.processor.HTTPPAOS11Binding;
import org.springframework.security.saml.processor.HTTPPostBinding;
import org.springframework.security.saml.processor.HTTPRedirectDeflateBinding;
import org.springframework.security.saml.processor.HTTPSOAP11Binding;
import org.springframework.security.saml.processor.SAMLBinding;
import org.springframework.security.saml.processor.SAMLProcessorImpl;
import org.springframework.security.saml.trust.httpclient.TLSProtocolConfigurer;
import org.springframework.security.saml.trust.httpclient.TLSProtocolSocketFactory;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.security.saml.websso.SingleLogoutProfile;
import org.springframework.security.saml.websso.SingleLogoutProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfile;
import org.springframework.security.saml.websso.WebSSOProfileConsumer;
import org.springframework.security.saml.websso.WebSSOProfileConsumerHoKImpl;
import org.springframework.security.saml.websso.WebSSOProfileConsumerImpl;
import org.springframework.security.saml.websso.WebSSOProfileECPImpl;
import org.springframework.security.saml.websso.WebSSOProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfileOptions;
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

import gov.usgs.wma.mlrauthserver.service.SAMLUserDetailsImpl;

@Configuration
@EnableWebSecurity
@Profile("default")
public class SAMLSecurityConfig extends WebSecurityConfigurerAdapter {
	//Keystore Configuration
	@Value("${security.jwt.key-store}")
	private String keystorePath;
	@Value("${security.jwt.key-store-saml-key}")
	private String keystoreSAMLKey;
	@Value("${security.jwt.key-store-password}")
	private String keystorePassword;

	//Server URL Configuration
	private boolean waterAuthUrlIncludePort;
	private String waterAuthUrlScheme;
	@Value("${security.require-ssl}")
	private boolean waterAuthUrlSSL;
	@Value("${server.implementation.port}")
	private int waterAuthUrlServerPort;
	@Value("${server.implementation.server}")
	private String waterAuthUrlServerName;
	@Value("${server.implementation.context}")
	private String waterAuthUrlContextPath;

	//SAML IDP Configuration
	@Value("${security.saml.idp.metadata-location}")
	private String metadataLocation;
	@Value("${security.saml.idp.provider}")
	private String providerName;
	@Value("${security.saml.idp.entity-id}")
	private String entityId;
	@Value("${security.saml.idp.passive}")
	private boolean passiveAuth;
	@Value("${security.saml.idp.force-auth}")
	private boolean forceAuth;
	@Value("${security.saml.idp.auth-contexts}")
	private String csvAuthContexts;

	//Local SAML Endpoint Configuration
	@Value("${security.saml.endpoint.base}")
	private String samlBaseEndpoint;
	@Value("${security.saml.endpoint.login}")
	private String samlLoginEndpoint;
	@Value("${security.saml.endpoint.logout}")
	private String samlLogoutEndpoint;
	@Value("${security.saml.endpoint.single-logout}")
	private String samlSingleLogoutEndpoint;
	@Value("${security.saml.endpoint.sso}")
	private String samlSSOEndpoint;
	@Value("${security.saml.endpoint.sso-hok}")
	private String samlSSOHOKEndpoint;
	@Value("${security.saml.endpoint.metadata}")
	private String samlMetadataEndpoint;

	//Login/Logout Routing Configuration
	@Value("${server.routing.login-success}")
	private String loginSuccessTargetUrl;
	@Value("${server.routing.logout-success}")
	private String logoutSuccessTargetUrl;
	@Value("${server.routing.login-error}")
	private String loginErrorTargetUrl;

	//Logging Configuration
	@Value("${security.saml.logging.messages:false}")
	private Boolean logMessages;
	@Value("${security.saml.logging.errors:true}")
	private Boolean logErrors;

	private Timer metadataTimer;
	private MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager;

	@Autowired
	SAMLUserDetailsImpl sAMLUserDetailsImpl;

	@PostConstruct
	public void init() {
		this.waterAuthUrlIncludePort = (waterAuthUrlServerPort != 80 && waterAuthUrlServerPort != 443);
		this.waterAuthUrlScheme = waterAuthUrlSSL ? "https" : "http";
		this.metadataTimer = new Timer(true);
		this.multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
	}

	@PreDestroy
	public void destroy() {
		this.metadataTimer.purge();
		this.metadataTimer.cancel();
		this.multiThreadedHttpConnectionManager.shutdown();
	}

	@Bean
	public VelocityEngine velocityEngine() {
		return VelocityFactory.getEngine();
	}

	@Bean(initMethod = "initialize")
	public StaticBasicParserPool parserPool() {
		return new StaticBasicParserPool();
	}

	@Bean(name = "parserPoolHolder")
	public ParserPoolHolder parserPoolHolder() {
		return new ParserPoolHolder();
	}

	@Bean
	public HTTPSOAP11Binding soapBinding() {
		return new HTTPSOAP11Binding(parserPool());
	}

	@Bean
	public HTTPPostBinding httpPostBinding() {
		return new HTTPPostBinding(parserPool(), velocityEngine());
	}

	@Bean
	public HTTPRedirectDeflateBinding httpRedirectDeflateBinding() {
		return new HTTPRedirectDeflateBinding(parserPool());
	}

	@Bean
	public HTTPSOAP11Binding httpSOAP11Binding() {
		return new HTTPSOAP11Binding(parserPool());
	}

	@Bean
	public HTTPPAOS11Binding httpPAOS11Binding() {
		return new HTTPPAOS11Binding(parserPool());
	}

	@Bean
	public HttpClient httpClient() {
		return new HttpClient(this.multiThreadedHttpConnectionManager);
	}

	@Bean
	public SAMLAuthenticationProvider samlAuthenticationProvider() {
		SAMLAuthenticationProvider samlAuthenticationProvider = new SAMLAuthenticationProvider();
		samlAuthenticationProvider.setUserDetails(sAMLUserDetailsImpl);
		samlAuthenticationProvider.setForcePrincipalAsString(false);
		return samlAuthenticationProvider;
	}

	@Bean
	public SAMLContextProviderImpl contextProvider() {
		if(waterAuthUrlServerName != null && waterAuthUrlServerName.length() > 0)
		{
			SAMLContextProviderLB context = new SAMLContextProviderLB();
			context.setServerName(waterAuthUrlServerName);
			context.setServerPort(waterAuthUrlServerPort);
			context.setContextPath(waterAuthUrlContextPath);
			context.setScheme(waterAuthUrlScheme);
			context.setIncludeServerPortInRequestURL(waterAuthUrlIncludePort);
			return context;
		} else {
			return new SAMLContextProviderImpl();
		}
	}

	@Bean
	public static SAMLBootstrap samlBootstrap() {
		return new SAMLBootstrap();
	}

	@Bean
	public SAMLDefaultLogger samlLogger() {
		SAMLDefaultLogger logger = new SAMLDefaultLogger();
		logger.setLogMessages(this.logMessages);
		logger.setLogErrors(this.logErrors);
		return logger;
	}

	@Bean
	public WebSSOProfileConsumer webSSOprofileConsumer() {
		return new WebSSOProfileConsumerImpl();
	}

	@Bean
	public WebSSOProfileConsumerHoKImpl hokWebSSOprofileConsumer() {
		return new WebSSOProfileConsumerHoKImpl();
	}

	@Bean
	public WebSSOProfile webSSOprofile() {
		return new WebSSOProfileImpl();
	}

	@Bean
	public WebSSOProfileConsumerHoKImpl hokWebSSOProfile() {
		return new WebSSOProfileConsumerHoKImpl();
	}

	@Bean
	public WebSSOProfileECPImpl ecpprofile() {
		return new WebSSOProfileECPImpl();
	}

	@Bean
	public SingleLogoutProfile logoutprofile() {
		return new SingleLogoutProfileImpl();
	}

	@Bean
	public KeyManager keyManager() {
		Resource storeFile;

		if(this.keystorePath.toLowerCase().startsWith("classpath:")){
			DefaultResourceLoader loader = new DefaultResourceLoader();
			String classpathLocation = this.keystorePath.replaceFirst("classpath:", "");
			storeFile = loader.getResource(classpathLocation);
		} else {
			FileSystemResourceLoader loader = new FileSystemResourceLoader();
			storeFile = loader.getResource(this.keystorePath);
		}

		Map<String, String> passwords = new HashMap<>();
		passwords.put(this.keystoreSAMLKey,this.keystorePassword);

		return new JKSKeyManager(storeFile, this.keystorePassword, passwords, this.keystoreSAMLKey);
	}

	@Bean
	public TLSProtocolConfigurer tlsProtocolConfigurer() {
		return new TLSProtocolConfigurer();
	}

	@Bean
	public ProtocolSocketFactory socketFactory() {
		return new TLSProtocolSocketFactory(keyManager(), null, "default");
	}

	@Bean
	public Protocol socketFactoryProtocol() {
		return new Protocol("https", socketFactory(), 443);
	}

	@Bean
	public MethodInvokingFactoryBean socketFactoryInitialization() {
		MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
		methodInvokingFactoryBean.setTargetClass(Protocol.class);
		methodInvokingFactoryBean.setTargetMethod("registerProtocol");
		Object[] args = {"https", socketFactoryProtocol()};
		methodInvokingFactoryBean.setArguments(args);
		return methodInvokingFactoryBean;
	}

	@Bean
	public WebSSOProfileOptions webSSOProfileOptions() {
		WebSSOProfileOptions webSSOProfileOptions = new WebSSOProfileOptions();
		webSSOProfileOptions.setIncludeScoping(false);

		if(csvAuthContexts != null && csvAuthContexts.length() > 0) {
			webSSOProfileOptions.setAuthnContextComparison(AuthnContextComparisonTypeEnumeration.EXACT);
			webSSOProfileOptions.setAuthnContexts(authnContexts());
		}

		webSSOProfileOptions.setForceAuthN(forceAuth);
		webSSOProfileOptions.setPassive(passiveAuth);
		webSSOProfileOptions.setProviderName(providerName);

		return webSSOProfileOptions;
	}

	@Bean
	public List<String> authnContexts() {
		if(csvAuthContexts != null && csvAuthContexts.length() > 0) {
			return Arrays.asList(csvAuthContexts.split(","));
		} else {
			return new ArrayList<>();
		}
	}

	@Bean
	public SAMLEntryPoint samlEntryPoint() {
		SAMLEntryPoint samlEntryPoint = new SAMLEntryPoint();
		samlEntryPoint.setDefaultProfileOptions(webSSOProfileOptions());
		return samlEntryPoint;
	}

	@Bean
	public ExtendedMetadata extendedMetadata() {
		ExtendedMetadata extendedMetadata = new ExtendedMetadata();
		extendedMetadata.setIdpDiscoveryEnabled(false); 
		extendedMetadata.setSignMetadata(false);
		extendedMetadata.setEcpEnabled(true);
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
		metadataProviders.add(doiSAMLExtendedMetadataProvider());

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

	@Bean
	@Qualifier("idp-doi-saml")
	public ExtendedMetadataDelegate doiSAMLExtendedMetadataProvider() throws MetadataProviderException, ResourceException {
		ExtendedMetadataDelegate extendedMetadataDelegate;

		//Handle HTTP vs local metadata location
		if(isLocalMetadata()){
			ResourceBackedMetadataProvider resourceMetadataProvider;

			//Handle classpath or file system resources
			if(this.metadataLocation.toLowerCase().startsWith("classpath:")){
				String classpathLocation = this.metadataLocation.replaceFirst("classpath:", "");
				resourceMetadataProvider = new ResourceBackedMetadataProvider(this.metadataTimer, new ClasspathResource(classpathLocation));
			} else {
				resourceMetadataProvider = new ResourceBackedMetadataProvider(this.metadataTimer, new FilesystemResource(this.metadataLocation));
			}

			resourceMetadataProvider.setParserPool(parserPool());
			extendedMetadataDelegate = new ExtendedMetadataDelegate(resourceMetadataProvider, extendedMetadata());
		} else {
			HTTPMetadataProvider httpMetadataProvider = new HTTPMetadataProvider(this.metadataTimer, httpClient(), this.metadataLocation);
			httpMetadataProvider.setParserPool(parserPool());
			 extendedMetadataDelegate = new ExtendedMetadataDelegate(httpMetadataProvider, extendedMetadata());
		}

		extendedMetadataDelegate.setMetadataTrustCheck(false);
		extendedMetadataDelegate.setMetadataRequireSignature(false);

		this.metadataTimer.purge();
		return extendedMetadataDelegate;
	}

	@Bean
	public MetadataGenerator metadataGenerator() {
		MetadataGenerator metadataGenerator = new MetadataGenerator();
		metadataGenerator.setEntityId(entityId);
		metadataGenerator.setExtendedMetadata(extendedMetadata());
		metadataGenerator.setIncludeDiscoveryExtension(false);
		metadataGenerator.setKeyManager(keyManager()); 
		metadataGenerator.setRequestSigned(false);

		if(waterAuthUrlServerName != null && waterAuthUrlServerName.length() > 0) {
			String baseUrl = waterAuthUrlScheme + "://" + waterAuthUrlServerName +
					(waterAuthUrlIncludePort ? ":" + waterAuthUrlServerPort : "" ) + 
					(waterAuthUrlContextPath.length() > 1 ? waterAuthUrlContextPath : "");
			metadataGenerator.setEntityBaseURL(baseUrl);
		}
		return metadataGenerator;
	}

	@Bean
	public MetadataDisplayFilter metadataDisplayFilter() {
		return new MetadataDisplayFilter();
	}

	@Bean
	public SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler() {
		SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler = new SavedRequestAwareAuthenticationSuccessHandler();
		successRedirectHandler.setDefaultTargetUrl(this.loginSuccessTargetUrl);

		return successRedirectHandler;
	}

	@Bean
	public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
		SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();
		failureHandler.setUseForward(true);
		failureHandler.setDefaultFailureUrl(this.loginErrorTargetUrl);
		return failureHandler;
	}

	@Bean
	public SAMLWebSSOHoKProcessingFilter samlWebSSOHoKProcessingFilter() throws Exception {
		SAMLWebSSOHoKProcessingFilter samlWebSSOHoKProcessingFilter = new SAMLWebSSOHoKProcessingFilter();
		samlWebSSOHoKProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
		samlWebSSOHoKProcessingFilter.setAuthenticationManager(authenticationManager());
		samlWebSSOHoKProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
		return samlWebSSOHoKProcessingFilter;
	}

	@Bean
	public SAMLProcessingFilter samlWebSSOProcessingFilter() throws Exception {
		SAMLProcessingFilter samlWebSSOProcessingFilter = new SAMLProcessingFilter();
		samlWebSSOProcessingFilter.setAuthenticationManager(authenticationManager());
		samlWebSSOProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
		samlWebSSOProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
		return samlWebSSOProcessingFilter;
	}

	@Bean
	public MetadataGeneratorFilter metadataGeneratorFilter() {
		return new MetadataGeneratorFilter(metadataGenerator());
	}

	@Bean
	public SimpleUrlLogoutSuccessHandler successLogoutHandler() {
		SimpleUrlLogoutSuccessHandler successLogoutHandler = new SimpleUrlLogoutSuccessHandler();
		successLogoutHandler.setDefaultTargetUrl(this.logoutSuccessTargetUrl);
		return successLogoutHandler;
	}

	@Bean
	public SecurityContextLogoutHandler logoutHandler() {
		SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
		logoutHandler.setInvalidateHttpSession(true);
		logoutHandler.setClearAuthentication(true);
		return logoutHandler;
	}

	@Bean
	public SAMLLogoutProcessingFilter samlLogoutProcessingFilter() {
		return new SAMLLogoutProcessingFilter(successLogoutHandler(), logoutHandler());
	}

	@Bean
	public SAMLLogoutFilter samlLogoutFilter() {
		return new SAMLLogoutFilter(successLogoutHandler(), new LogoutHandler[] { logoutHandler() }, new LogoutHandler[] { logoutHandler() });
	}

	@Bean
	public SAMLProcessorImpl processor() {
		Collection<SAMLBinding> bindings = new ArrayList<>();
		bindings.add(httpRedirectDeflateBinding());
		bindings.add(httpPostBinding());
		bindings.add(httpSOAP11Binding());
		bindings.add(httpPAOS11Binding());
		return new SAMLProcessorImpl(bindings);
	}

	@Bean
	public FilterChainProxy samlFilter() throws Exception {
		List<SecurityFilterChain> chains = new ArrayList<>();
		chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(this.samlBaseEndpoint + this.samlLoginEndpoint + "/**"),
				samlEntryPoint()));
		chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(this.samlBaseEndpoint + this.samlLogoutEndpoint + "/**"),
				samlLogoutFilter()));
		chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(this.samlBaseEndpoint + this.samlMetadataEndpoint + "/**"),
				metadataDisplayFilter()));
		chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(this.samlBaseEndpoint + this.samlSSOEndpoint + "/**"),
				samlWebSSOProcessingFilter()));
		chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(this.samlBaseEndpoint + this.samlSSOHOKEndpoint + "/**"),
				samlWebSSOHoKProcessingFilter()));
		chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(this.samlBaseEndpoint + this.samlSingleLogoutEndpoint + "/**"),
				samlLogoutProcessingFilter()));
		return new FilterChainProxy(chains);
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.httpBasic()
				.authenticationEntryPoint(samlEntryPoint())
			.and()
				.csrf()
					.disable()
			.addFilterBefore(metadataGeneratorFilter(), ChannelProcessingFilter.class)
			.addFilterAfter(samlFilter(), BasicAuthenticationFilter.class)
			.authorizeRequests()
				.antMatchers(this.samlBaseEndpoint + "/**/").permitAll()
				.antMatchers(this.logoutSuccessTargetUrl).permitAll()
				.antMatchers(this.loginErrorTargetUrl).permitAll()
				.antMatchers("/health/").permitAll()
				.antMatchers("/login/").permitAll()
				.antMatchers("/oauth/authorize/").permitAll()
				.anyRequest().authenticated()
			.and()
				.logout()
					.logoutSuccessUrl(this.logoutSuccessTargetUrl);
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(samlAuthenticationProvider());
	}

}