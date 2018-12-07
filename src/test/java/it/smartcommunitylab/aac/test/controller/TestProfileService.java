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

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import it.smartcommunitylab.aac.controller.BasicProfileController;
import it.smartcommunitylab.aac.manager.ProviderServiceAdapter;
import it.smartcommunitylab.aac.profile.model.BasicProfile;
import it.smartcommunitylab.aac.profile.model.BasicProfiles;
import it.smartcommunitylab.aac.repository.UserRepository;

/**
 * @author raman
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")
@WebAppConfiguration
public class TestProfileService {

	private static final String EMAIL_ATTRIBUTE = "OIDC_CLAIM_email";
	private static final String AUTHORITY = "google";
	private static final String NAME = "mario";
	private static final String SURNAME = "rossi";
	private static final String EMAIL = "mario.rossi@gmail.com";

	private MockMvc mockMvc;
	
	private ObjectMapper jsonMapper = new ObjectMapper();

	@Autowired
	private ProviderServiceAdapter providerServiceAdapter;
	@Autowired
	private UserRepository userRepository;
	
	private it.smartcommunitylab.aac.model.User user;
	
	@Autowired
	private BasicProfileController controller;

	@Before
    public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
		userRepository.delete(userRepository.findAll());
		user = createUser();
		mockAuthentication();
	}

	@After
	public void tearDown() {
		if (user != null) {
			userRepository.delete(user);
		}
	}

	private void mockAuthentication() {
		Authentication authentication = Mockito.mock(Authentication.class);
		Mockito.when(authentication.getPrincipal()).thenReturn(new User(user.getId().toString(), "", Collections.<GrantedAuthority>emptyList()));
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}

	private void mockNoAuthentication() {
		Authentication authentication = Mockito.mock(Authentication.class);
		Mockito.when(authentication.getPrincipal()).thenReturn(null);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);
	}

	private it.smartcommunitylab.aac.model.User createUser() {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter("OIDC_CLAIM_given_name", NAME);
		req.addParameter("OIDC_CLAIM_family_name", SURNAME);
		req.addParameter(EMAIL_ATTRIBUTE, EMAIL);
		
		it.smartcommunitylab.aac.model.User user = providerServiceAdapter.updateUser(AUTHORITY, new HashMap<String, String>(), req);
		return user;
	}
	
	
	@Test
	public void testCurrentUserBasicProfile() throws Exception {
		RequestBuilder request = MockMvcRequestBuilders.get("/basicprofile/me")
				.header("Accept", MediaType.APPLICATION_JSON_VALUE);
		ResultActions result = mockMvc.perform(request);
		result.andExpect(MockMvcResultMatchers.status().isOk());
		String string = result.andReturn().getResponse().getContentAsString();
		BasicProfile profile = jsonMapper.readValue(string, BasicProfile.class);
		Assert.assertNotNull(profile);
		Assert.assertEquals(SURNAME, profile.getSurname());
		Assert.assertEquals(NAME, profile.getName());
		Assert.assertEquals(user.getId().toString(), profile.getUserId());
		
		// no auth
		mockNoAuthentication();
		request = MockMvcRequestBuilders.get("/basicprofile/me")
				.header("Accept", MediaType.APPLICATION_JSON_VALUE);
		result = mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isForbidden());
	}

	@Test
	public void testBasicProfile() throws Exception {
		// existing
		RequestBuilder request = MockMvcRequestBuilders.get("/basicprofile/all/{userId}",user.getId())
				.header("Accept", MediaType.APPLICATION_JSON_VALUE);
		ResultActions result = mockMvc.perform(request);
		result.andExpect(MockMvcResultMatchers.status().isOk());
		String string = result.andReturn().getResponse().getContentAsString();
		BasicProfile profile = jsonMapper.readValue(string, BasicProfile.class);
		Assert.assertNotNull(profile);
		Assert.assertEquals(SURNAME, profile.getSurname());
		Assert.assertEquals(NAME, profile.getName());
		Assert.assertEquals(user.getId().toString(), profile.getUserId());
		// non existing
		request = MockMvcRequestBuilders.get("/basicprofile/all/NONE")
				.header("Accept", MediaType.APPLICATION_JSON_VALUE);
		mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().isInternalServerError());
	}
	
	@Test
	public void testSearchBasicProfile() throws Exception {
		RequestBuilder request = MockMvcRequestBuilders.get("/basicprofile/all")
				.header("Accept", MediaType.APPLICATION_JSON_VALUE);
		ResultActions result = mockMvc.perform(request);
		result.andExpect(MockMvcResultMatchers.status().isOk());
		String string = result.andReturn().getResponse().getContentAsString();
		BasicProfiles profiles = jsonMapper.readValue(string, BasicProfiles.class);
		Assert.assertNotNull(profiles);
		Assert.assertEquals(1, profiles.getProfiles().size());
		// matching filter
		request = MockMvcRequestBuilders.get("/basicprofile/all")
				.param("filter", "ROSS")
				.header("Accept", MediaType.APPLICATION_JSON_VALUE);
		result = mockMvc.perform(request);
		result.andExpect(MockMvcResultMatchers.status().isOk());
		string = result.andReturn().getResponse().getContentAsString();
		profiles = jsonMapper.readValue(string, BasicProfiles.class);
		Assert.assertNotNull(profiles);
		Assert.assertEquals(1, profiles.getProfiles().size());
		// non-matching filter
		request = MockMvcRequestBuilders.get("/basicprofile/all")
				.param("filter", "other")
				.header("Accept", MediaType.APPLICATION_JSON_VALUE);
		result = mockMvc.perform(request);
		result.andExpect(MockMvcResultMatchers.status().isOk());
		string = result.andReturn().getResponse().getContentAsString();
		profiles = jsonMapper.readValue(string, BasicProfiles.class);
		Assert.assertNotNull(profiles);
		Assert.assertEquals(0, profiles.getProfiles().size());
		// empty filter
		request = MockMvcRequestBuilders.get("/basicprofile/all")
				.param("filter", "")
				.header("Accept", MediaType.APPLICATION_JSON_VALUE);
		result = mockMvc.perform(request);
		result.andExpect(MockMvcResultMatchers.status().isOk());
		string = result.andReturn().getResponse().getContentAsString();
		profiles = jsonMapper.readValue(string, BasicProfiles.class);
		Assert.assertNotNull(profiles);
		Assert.assertEquals(1, profiles.getProfiles().size());

		
		request = MockMvcRequestBuilders.get("/basicprofile/profiles")
				.param("userIds", user.getId().toString())
				.header("Accept", MediaType.APPLICATION_JSON_VALUE);
		result = mockMvc.perform(request);
		result.andExpect(MockMvcResultMatchers.status().isOk());
		string = result.andReturn().getResponse().getContentAsString();
		profiles = jsonMapper.readValue(string, BasicProfiles.class);
		Assert.assertNotNull(profiles);
		Assert.assertEquals(1, profiles.getProfiles().size());

		request = MockMvcRequestBuilders.get("/basicprofile/profiles")
				.param("userIds", "")
				.header("Accept", MediaType.APPLICATION_JSON_VALUE);
		result = mockMvc.perform(request);
		result.andExpect(MockMvcResultMatchers.status().isOk());
		string = result.andReturn().getResponse().getContentAsString();
		profiles = jsonMapper.readValue(string, BasicProfiles.class);
		Assert.assertNotNull(profiles);
		Assert.assertEquals(0, profiles.getProfiles().size());

	}

}
