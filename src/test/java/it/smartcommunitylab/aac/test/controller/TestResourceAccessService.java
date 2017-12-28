/*******************************************************************************
 * Copyright 2015 Fondazione Bruno Kessler
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/
package it.smartcommunitylab.aac.test.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.DefaultAuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import it.smartcommunitylab.aac.Config;
import it.smartcommunitylab.aac.controller.ResourceAccessController;
import it.smartcommunitylab.aac.manager.ProviderServiceAdapter;
import it.smartcommunitylab.aac.model.ClientAppInfo;
import it.smartcommunitylab.aac.model.ClientDetailsEntity;
import it.smartcommunitylab.aac.oauth.NonRemovingTokenServices;
import it.smartcommunitylab.aac.repository.ClientDetailsRepository;
import it.smartcommunitylab.aac.repository.ResourceRepository;
import it.smartcommunitylab.aac.repository.UserRepository;

/**
 * @author raman
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")
@WebAppConfiguration
public class TestResourceAccessService {

	private static final String SCOPE = "profile.basicprofile.all";
	
	private static final String EMAIL_ATTRIBUTE = "OIDC_CLAIM_email";
	private static final String AUTHORITY = "google";
	private static final String NAME = "mario";
	private static final String SURNAME = "rossi";
	private static final String EMAIL = "mario.rossi@gmail.com";

	private MockMvc mockMvc;

	@Autowired
	private ProviderServiceAdapter providerServiceAdapter;
	@Autowired
	private UserRepository userRepository;
	
	private it.smartcommunitylab.aac.model.User user;
	
	@Autowired
	private ResourceAccessController controller;
	@Autowired
	private ClientDetailsRepository clientDetailsRepository;
	@Autowired
	private ResourceRepository resourceRepository;

	private ClientDetails client;
	private String token;

	NonRemovingTokenServices tokenServices;
	
	@Before
    public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		userRepository.delete(userRepository.findAll());
		user = createUser();
		
		client = createTestClient("TEST", user.getId());
		token = getToken(client);
	}

	@After
	public void tearDown() {
		if (user != null) {
			userRepository.delete(user);
		}
	}

	@Test
	public void testToken() throws Exception {
		// correct token correct scope
		RequestBuilder request = MockMvcRequestBuilders.get("/resources/access")
				.header("Authorization", "Bearer "+token)
				.header("Accept", MediaType.APPLICATION_JSON_VALUE)
				.param("scope", SCOPE);
		ResultActions result = mockMvc.perform(request);
		result.andExpect(MockMvcResultMatchers.status().isOk());
		String string = result.andReturn().getResponse().getContentAsString();
		Assert.assertEquals("true", string);
		
		// correct token incorrect scope
		request = MockMvcRequestBuilders.get("/resources/access")
				.header("Authorization", "Bearer "+token)
				.header("Accept", MediaType.APPLICATION_JSON_VALUE)
				.param("scope", "test");
		result = mockMvc.perform(request);
		result.andExpect(MockMvcResultMatchers.status().isOk());
		string = result.andReturn().getResponse().getContentAsString();
		Assert.assertEquals("false", string);

		// incorrect token
		request = MockMvcRequestBuilders.get("/resources/access")
				.header("Authorization", "Bearer TOKEN")
				.header("Accept", MediaType.APPLICATION_JSON_VALUE)
				.param("scope", "test");
		result = mockMvc.perform(request);
		result.andExpect(MockMvcResultMatchers.status().isOk());
		string = result.andReturn().getResponse().getContentAsString();
		Assert.assertEquals("false", string);
	}

	/**
	 * Create test client
	 * @param client
	 * @param developerId
	 * @return
	 * @throws Exception
	 */
	private ClientDetails createTestClient(String client, long developerId) throws Exception {
		ClientDetailsEntity entity = new ClientDetailsEntity();
		ClientAppInfo info = new ClientAppInfo();
		info.setName(client);
		entity.setAdditionalInformation(info.toJson());
		entity.setClientId(client);
		entity.setAuthorities(Config.AUTHORITY.ROLE_CLIENT_TRUSTED.name());
		entity.setAuthorizedGrantTypes("password,client_credentials,implicit");
		entity.setDeveloperId(developerId);
		entity.setClientSecret(UUID.randomUUID().toString());
		entity.setClientSecretMobile(UUID.randomUUID().toString());
		entity.setScope("profile.basicprofile.all");
		String resourcesId = ""+ resourceRepository.findByResourceUri("profile.basicprofile.all").getResourceId();
		entity.setResourceIds(resourcesId);

		entity = clientDetailsRepository.save(entity);
		return entity;
	}

	/**
	 * Create token for the client
	 * @param client
	 * @return
	 * @throws Exception
	 */
	private String getToken(ClientDetails client) throws Exception {
		NonRemovingTokenServices tokenServices = (NonRemovingTokenServices) unwrapProxy(ReflectionTestUtils.getField(controller, "resourceServerTokenServices"));
		Authentication userAuthentication = new UsernamePasswordAuthenticationToken(user.getId().toString(), "");
		AuthorizationRequest authorizationRequest = new DefaultAuthorizationRequest(client.getClientId(),Collections.singleton(SCOPE));
		OAuth2Authentication auth = new OAuth2Authentication(authorizationRequest, userAuthentication);
		OAuth2AccessToken createAccessToken = tokenServices.createAccessToken(auth);
		return createAccessToken.getValue();
	}

	/**
	 * Create test user
	 * @return
	 */
	private it.smartcommunitylab.aac.model.User createUser() {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter("OIDC_CLAIM_given_name", NAME);
		req.addParameter("OIDC_CLAIM_family_name", SURNAME);
		req.addParameter(EMAIL_ATTRIBUTE, EMAIL);
		
		it.smartcommunitylab.aac.model.User user = providerServiceAdapter.updateUser(AUTHORITY, new HashMap<String, String>(), req);
		return user;
	}
	public static final Object unwrapProxy(Object bean) throws Exception {
	    /*
	     * If the given object is a proxy, set the return value as the object
	     * being proxied, otherwise return the given object.
	     */
	    if (AopUtils.isAopProxy(bean) && bean instanceof Advised) {
	        Advised advised = (Advised) bean;
	        bean = advised.getTargetSource().getTarget();
	    }
	    return bean;
	}

}
