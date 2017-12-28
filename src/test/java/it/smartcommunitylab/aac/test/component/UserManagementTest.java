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

package it.smartcommunitylab.aac.test.component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import it.smartcommunitylab.aac.common.RegistrationException;
import it.smartcommunitylab.aac.manager.MailSender;
import it.smartcommunitylab.aac.manager.ProviderServiceAdapter;
import it.smartcommunitylab.aac.manager.RegistrationManager;
import it.smartcommunitylab.aac.manager.RegistrationService;
import it.smartcommunitylab.aac.model.Registration;
import it.smartcommunitylab.aac.model.User;
import it.smartcommunitylab.aac.repository.RegistrationRepository;
import it.smartcommunitylab.aac.repository.UserRepository;
import junit.framework.Assert;

/**
 * @author raman
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")
public class UserManagementTest {

	private static final String NAME = "TESTNAME";
	private static final String SURNAME = "TESTSURNAME";
	private static final String PWD = "123456";
	private static final String PWD2 = "12345678";
	private static final String EMAIL = "test.test.registration@test.com";

	@Mock
	private MailSender mailSender;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RegistrationRepository regRepository;

	@Autowired
	@InjectMocks
	private RegistrationManager regManager;

	@Autowired
	private RegistrationService service;
	
	@Autowired
	private ProviderServiceAdapter providerServiceAdapter;

	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
		RegistrationManager rm = (RegistrationManager) unwrapProxy(regManager);
		ReflectionTestUtils.setField(rm, "sender", mailSender);
		
		userRepository.delete(userRepository.findAll());
		regRepository.delete(regRepository.findAll());
	}


	@Test
	public void testLoadDefaults() throws InterruptedException, JAXBException, IOException {
		providerServiceAdapter.init();
		List<User> list = userRepository.findAll();
		Assert.assertTrue(list.size() > 0);
	}

	@Test
	public void testRegistration() throws InterruptedException {
		Registration register = null;
		try {
			register = service.register(NAME, SURNAME, EMAIL, PWD, null);
			Assert.assertNotNull(register);
			
			register = regRepository.findByEmail(EMAIL);
			Assert.assertNotNull(register);
		} catch (RegistrationException e) {
			Assert.assertTrue("Error testing registration", false);
			return;
		}

		// find user: not confirmed
		try {
			service.getUser(EMAIL, PWD);
			Assert.assertTrue(false);
		} catch (RegistrationException e1) {
		}
		
		// resend confirm
		try {
			service.resendConfirm(EMAIL);
		} catch (RegistrationException e1) {
			Assert.assertTrue("Error testing resend confirm", false);
		}
		try {
			Thread.sleep(3000);
			service.resendConfirm(EMAIL);
		} catch (RegistrationException e1) {
			Assert.assertTrue("Error testing resend confirm", false);
		}
		
		// confirm
		try {
			register = regRepository.findByEmail(EMAIL);
			service.confirm(register.getConfirmationKey());
		} catch (RegistrationException e1) {
			Assert.assertTrue("Error testing confirmation", false);
		}

		// find user
		try {
			register = service.getUser(EMAIL, PWD);
			Assert.assertNotNull(register);
		} catch (RegistrationException e1) {
			Assert.assertTrue("Error testing registration search", false);
		}
		
		// find user: wrong email
		try {
			register = service.getUser("wrong@email", PWD);
			Assert.assertTrue(false);
		} catch (RegistrationException e) {
		}

		// find user: wrong email
		try {
			register = service.getUser(EMAIL, "wrong password");
			Assert.assertTrue(false);
		} catch (RegistrationException e) {
		}
	}
	
	@Test
	public void testReset() throws InterruptedException {
		Registration register = null;
		try {
			register = service.register(NAME, SURNAME, EMAIL, PWD, null);
			register = regRepository.findByEmail(EMAIL);
			service.confirm(register.getConfirmationKey());
		} catch (RegistrationException e) {
			Assert.assertTrue("Error testing registration", false);
			return;
		}
		
		// reset
		try {
			service.resetPassword(EMAIL);
		} catch (RegistrationException e1) {
			Assert.assertTrue("Error testing reset password", false);
		}
		try {
			Thread.sleep(3000);
			service.resetPassword(EMAIL);
		} catch (RegistrationException e1) {
			Assert.assertTrue("Error testing reset password", false);
		}
		
		// find user
		try {
			register = service.getUser(EMAIL, PWD);
			Assert.assertNotNull(register);
		} catch (RegistrationException e1) {
			Assert.assertTrue("Error testing update search", false);
		}

		// reset
		try {
			service.updatePassword(EMAIL, PWD2);
		} catch (RegistrationException e1) {
			Assert.assertTrue("Error testing update password", false);
		}

		// find user
		try {
			register = service.getUser(EMAIL, PWD);
			Assert.assertTrue(false);
		} catch (RegistrationException e1) {
		}

		// find user
		try {
			register = service.getUser(EMAIL, PWD2);
			Assert.assertNotNull(register);
		} catch (RegistrationException e1) {
			Assert.assertTrue("Error testing update search", false);
		}

	}

	@Test
	public void testRegistrationInvalid() {
		try {
			service.register(NAME, SURNAME, EMAIL, null, null);
			Assert.assertTrue(false);
		} catch (RegistrationException e) {
		}
		try {
			service.register(null, SURNAME, EMAIL, PWD, null);
			Assert.assertTrue(false);
		} catch (RegistrationException e) {
		}
		try {
			service.register(NAME, null, EMAIL, PWD, null);
			Assert.assertTrue(false);
		} catch (RegistrationException e) {
		}
		try {
			service.register(NAME, SURNAME, null, PWD, null);
			Assert.assertTrue(false);
		} catch (RegistrationException e) {
		}

		
	}
	
	@Test
	public void testRegistrationFailureMail() {
		try {
			Registration register = regRepository.findByEmail(EMAIL);
			Assert.assertNull(register);

			Mockito.doThrow(new RegistrationException()).when(mailSender);
			register = service.register(NAME, SURNAME, EMAIL, PWD, null);
			
			
		} catch (RegistrationException e) {
			Registration register = regRepository.findByEmail(EMAIL);
			Assert.assertNull(register);
		}
	}
	@Test
	public void testRegistrationInParallel() {
		final CountDownLatch lock = new CountDownLatch(2);
		
		Runnable body = new Runnable(){
			@Override
			public void run() {
				try {
					service.register(NAME, SURNAME, EMAIL, PWD, null);
					List<Registration> list = regRepository.findAllByEmail(EMAIL);
					Thread.sleep(1000L);
					Assert.assertTrue(list.size() == 1);
					lock.countDown();
				} catch (InterruptedException e) {
					// DO NOTHING
					lock.countDown();
				} catch (Exception e) {
					// DO NOTHING
					e.printStackTrace();
				}
			}
			
		}; 
		new Thread(body).start();
		new Thread(body).start();
		
		try {
			if (!lock.await(3000, TimeUnit.MILLISECONDS)) {
				Assert.assertTrue("Error testing parallel registration", false);				
			}
		} catch (InterruptedException e) {
			// DO NOTHING
		}
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
