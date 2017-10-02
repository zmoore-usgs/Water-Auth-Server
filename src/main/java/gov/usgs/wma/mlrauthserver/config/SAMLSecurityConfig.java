/*
 * Copyright 2017 Vincenzo De Notaris
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package gov.usgs.wma.mlrauthserver.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import gov.usgs.wma.mlrauthserver.service.SAMLUserDetailsImpl;

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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
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

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SAMLSecurityConfig extends WebSecurityConfigurerAdapter {
	//Keystore Configuration
	@Value("${samlKeystoreLocation}")
	private String keystorePath;
	@Value("${samlKeystoreDefaultKey}")
	private String keystoreDefaultKey;
	@Value("${samlKeystorePassword}")
	private String keystorePassword;
	
	//SAML IDP Configuration
	@Value("${samlIDPMetadataLocation}")
	private String metadataLocation;
	@Value("${samlAuthnRequestProviderName:}")
	private String providerName;
	@Value("${samlAuthnRequestEntityId}")
	private String entityId;
	
	//Local SAML Endpoint Configuration
	@Value("${samlBaseEndpoint:/saml}")
	private String samlBaseEndpoint;
	@Value("${samlLoginEndpoint:/login}")
	private String samlLoginEndpoint;
	@Value("${samlLogoutEndpoint:/logout}")
	private String samlLogoutEndpoint;
	@Value("${samlSingleLogoutEndpoint:/singlelogout}")
	private String samlSingleLogoutEndpoint;
	@Value("${samlSSOEndpoint:/sso}")
	private String samlSSOEndpoint;
	@Value("${samlSSOHOKEndpoint:/ssohok}")
	private String samlSSOHOKEndpoint;
	@Value("${samlMetadataEndpoint:/metadata}")
	private String samlMetadataEndpoint;
	
	//Login/Logout Routing Configuration
	@Value("${loginSuccessTargetUrl}")
	private String loginSuccessTargetUrl;
	@Value("${logoutSuccessTargetUrl}")
	private String logoutSuccessTargetUrl;
	@Value("${loginErrorTargetUrl:/error}")
	private String loginErrorTargetUrl;
	
	private Timer metadataTimer;
	private MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager;

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
		samlAuthenticationProvider.setUserDetails(new SAMLUserDetailsImpl());
		samlAuthenticationProvider.setForcePrincipalAsString(false);
		return samlAuthenticationProvider;
	}

	@Bean
	public SAMLContextProviderImpl contextProvider() {
		return new SAMLContextProviderImpl();
	}

	@Bean
	public static SAMLBootstrap samlBootstrap() {
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
		DefaultResourceLoader loader = new DefaultResourceLoader();
		Resource storeFile = loader.getResource(this.keystorePath);
		
		Map<String, String> passwords = new HashMap<String, String>();
		passwords.put(this.keystoreDefaultKey,this.keystorePassword );
		
		return new JKSKeyManager(storeFile, this.keystorePassword, passwords, this.keystoreDefaultKey);
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
		webSSOProfileOptions.setAuthnContextComparison(AuthnContextComparisonTypeEnumeration.EXACT);
		webSSOProfileOptions.setAuthnContexts(authnContexts());
		webSSOProfileOptions.setForceAuthN(false);
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
		ResourceBackedMetadataProvider resourceMetadataProvider;
		
		//Handle classpath or file system resources
		if(this.metadataLocation.toLowerCase().startsWith("classpath:")){
			String classpathLocation = this.metadataLocation.replaceFirst("classpath:", "");
			resourceMetadataProvider = new ResourceBackedMetadataProvider(this.metadataTimer, new ClasspathResource(classpathLocation));
		} else {
			resourceMetadataProvider = new ResourceBackedMetadataProvider(this.metadataTimer, new FilesystemResource(this.metadataLocation));
		}
		
		resourceMetadataProvider.setParserPool(parserPool());
		
		ExtendedMetadataDelegate extendedMetadataDelegate = new ExtendedMetadataDelegate(resourceMetadataProvider, extendedMetadata());
		extendedMetadataDelegate.setMetadataTrustCheck(false);
		extendedMetadataDelegate.setMetadataRequireSignature(false);
		
		this.metadataTimer.purge();
		return extendedMetadataDelegate;
	}
	
	//Remote HTTP Metadata Provider
	@Bean
	@Qualifier("idp-doi-saml-http")
	public ExtendedMetadataDelegate doiSAMLExtendedMetadataProvider() throws MetadataProviderException {
		HTTPMetadataProvider httpMetadataProvider = new HTTPMetadataProvider(this.metadataTimer, httpClient(), this.metadataLocation);
		httpMetadataProvider.setParserPool(parserPool());
		
		ExtendedMetadataDelegate extendedMetadataDelegate = new ExtendedMetadataDelegate(httpMetadataProvider, extendedMetadata());
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

	// Processor
	@Bean
	public SAMLProcessorImpl processor() {
		Collection<SAMLBinding> bindings = new ArrayList<SAMLBinding>();
		bindings.add(httpRedirectDeflateBinding());
		bindings.add(httpPostBinding());
		bindings.add(httpSOAP11Binding());
		bindings.add(httpPAOS11Binding());
		return new SAMLProcessorImpl(bindings);
	}
	
	@Bean
	public FilterChainProxy samlFilter() throws Exception {
		List<SecurityFilterChain> chains = new ArrayList<SecurityFilterChain>();
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
				.authenticationEntryPoint(samlEntryPoint());
		http
			.csrf()
				.disable();
		http
			.addFilterBefore(metadataGeneratorFilter(), ChannelProcessingFilter.class)
			.addFilterAfter(samlFilter(), BasicAuthenticationFilter.class);
		http        
			.authorizeRequests()
			.antMatchers(this.logoutSuccessTargetUrl).permitAll()
			.antMatchers(this.loginErrorTargetUrl).permitAll()
			.antMatchers(this.samlBaseEndpoint + "/**").permitAll()
			.anyRequest().authenticated();
		http
			.logout()
				.logoutSuccessUrl(this.logoutSuccessTargetUrl);
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth
			.authenticationProvider(samlAuthenticationProvider());
	}   

}