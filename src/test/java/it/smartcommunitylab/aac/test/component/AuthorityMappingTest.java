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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
}
