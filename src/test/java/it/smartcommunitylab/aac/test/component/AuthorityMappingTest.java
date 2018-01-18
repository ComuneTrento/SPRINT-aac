/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
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
package it.smartcommunitylab.aac.test.component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import it.smartcommunitylab.aac.authority.AuthorityHandlerContainer;
import it.smartcommunitylab.aac.authority.DefaultAuthorityHandler;
import it.smartcommunitylab.aac.jaxbmodel.AuthorityMapping;
import it.smartcommunitylab.aac.manager.AttributesAdapter;
import it.smartcommunitylab.aac.manager.ProviderServiceAdapter;
import it.smartcommunitylab.aac.model.Attribute;
import it.smartcommunitylab.aac.model.Authority;
import it.smartcommunitylab.aac.model.User;
import it.smartcommunitylab.aac.repository.AuthorityRepository;
import it.smartcommunitylab.aac.repository.UserRepository;

/**
 * Test Class
 * 
 * @author raman
 * 
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")
public class AuthorityMappingTest {
	
	@Autowired
	private ProviderServiceAdapter providerServiceAdapter;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AuthorityRepository authRepository;
	@Autowired
	private AttributesAdapter attributesAdapter;
	@Autowired
	private AuthorityHandlerContainer handlerContainer;
	
	
	@Before
	public void init() {
		List<User> users = userRepository.findByFullNameIgnoreCaseLike("mario rossi");
		if (users != null) {
			userRepository.delete(users);
		}
	}
	
	@Test
	public void testUsers() {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter("OIDC_CLAIM_given_name", "mario");
		req.addParameter("OIDC_CLAIM_family_name", "rossi");
		req.addParameter("OIDC_CLAIM_email", "mario.rossi@gmail.com");

		providerServiceAdapter.updateUser("google", new HashMap<String, String>(), req);
		List<User> users = userRepository.findByFullNameIgnoreCaseLike("mario rossi");
		Assert.assertNotNull(users);
		Assert.assertEquals(1, users.size());
		
		// repeating update
		providerServiceAdapter.updateUser("google", new HashMap<String, String>(), req);
		users = userRepository.findByFullNameIgnoreCaseLike("mario rossi");
		Assert.assertNotNull(users);
		Assert.assertEquals(1, users.size());
		
		req = new MockHttpServletRequest();
		req.addParameter("OIDC_CLAIM_given_name", "mario");
		req.addParameter("OIDC_CLAIM_family_name", "rossi");
		req.addParameter("OIDC_CLAIM_email", "mario.rossi2@gmail.com");
		
		providerServiceAdapter.updateUser("google", new HashMap<String, String>(), req);
		users = userRepository.findByFullNameIgnoreCaseLike("mario rossi");
		Assert.assertNotNull(users);
		Assert.assertEquals(2, users.size());
		
	}
	
	@Test
	public void testAutohrities() {
		Assert.assertTrue(attributesAdapter.getAuthorityUrls().size() > 0);
		Assert.assertTrue(attributesAdapter.getWebAuthorityUrls().size() > 0);
		Assert.assertNotNull(attributesAdapter.getAuthority("google"));
		
		Attribute a = new Attribute();
		
		Authority authority = authRepository.findByRedirectUrl("google");
		a.setAuthority(authority);
		a.setKey("OIDC_CLAIM_email");
		Assert.assertTrue(attributesAdapter.isIdentityAttr(a));
		
		List<Attribute> found = attributesAdapter.findAllIdentityAttributes(authority, Collections.singletonMap("OIDC_CLAIM_email", "mario.rossi2@gmail.com"), true);
		Assert.assertNotNull(found);
		Assert.assertTrue(found.size() > 0);
	}
	
	@Test
	public void testInternalMapper() {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter("email", "mario.rossi2@gmail.com");
		try {
			attributesAdapter.getAttributes("internal", new HashMap<String, String>(), req);
			Assert.assertTrue(false);
		} catch (Exception e) {
		}
		try {
			attributesAdapter.getAttributes("internal", Collections.singletonMap("email", "mario.rossi2@gmail.com"), null);
			Assert.assertTrue(false);
		} catch (Exception e) {
		}
		
	}
	
	@Test
	public void testDefaultMapper() {
		MockHttpServletRequest req = new MockHttpServletRequest();
		req.addParameter("OIDC_CLAIM_email", "mario.rossi2@gmail.com");
		Map<String, String> attributes = attributesAdapter.getAttributes("google", new HashMap<String, String>(), req);
		Assert.assertTrue(attributes.size() > 0);

		attributes = attributesAdapter.getAttributes("google", Collections.singletonMap("OIDC_CLAIM_email", "mario.rossi2@gmail.com"), null);
		Assert.assertTrue(attributes.size() > 0);

		DefaultAuthorityHandler handler = (DefaultAuthorityHandler) handlerContainer.getAuthorityHandler("google");
		handler.setTestMode(true);
		req = new MockHttpServletRequest();
		req.addParameter("OIDC_CLAIM_email", "mario.rossi2@gmail.com");
		attributes = attributesAdapter.getAttributes("google", new HashMap<String, String>(), req);
		Assert.assertTrue(attributes.size() > 0);

		attributes = attributesAdapter.getAttributes("google", Collections.singletonMap("OIDC_CLAIM_email", "mario.rossi2@gmail.com"), null);
		Assert.assertTrue(attributes.size() > 0);
		
		AuthorityMapping mapping = attributesAdapter.getAuthority("google");
		handler.setTestMode(false);
		mapping.setUseParams(false);
		req = new MockHttpServletRequest();
		req.setAttribute("OIDC_CLAIM_email", "mario.rossi2@gmail.com");
		attributes = handler.extractAttributes(req, new HashMap<String, String>(), mapping);
		Assert.assertTrue(attributes.size() > 0);
		
		
	}
}

