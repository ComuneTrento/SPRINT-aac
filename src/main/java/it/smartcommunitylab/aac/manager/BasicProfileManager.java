/**
 *    Copyright 2012-2013 Trento RISE
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
 */

package it.smartcommunitylab.aac.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import it.smartcommunitylab.aac.model.User;
import it.smartcommunitylab.aac.profile.model.AccountProfile;
import it.smartcommunitylab.aac.profile.model.BasicProfile;
import it.smartcommunitylab.aac.repository.UserRepository;

/**
 * @author raman
 *
 */
@Component
@Transactional
public class BasicProfileManager {

	@Autowired
	private UserRepository userRepository;
	/**
	 * @param userId
	 * @return
	 */
	public BasicProfile getBasicProfileById(String userId) {
		return BasicProfileConverter.toBasicProfile(userRepository.findOne(Long.parseLong(userId)));
	}
	/**
	 * returns all users in the
	 * system
	 * 
	 * @return the list of all minimal profiles of users
	 * @throws CommunityManagerException
	 */
	public List<BasicProfile> getUsers() {
		return BasicProfileConverter.toBasicProfile(userRepository.findAll());
	}
	/**
	 * returns all minimal set of
	 * users who match part of name
	 * 
	 * @param name
	 *            the string to match with name of user
	 * @return the list of minimal
	 *        set of users which
	 *         name contains parameter or an empty list
	 * @throws CommunityManagerException
	 */
	public List<BasicProfile> getUsers(String fullNameFilter) {
		return BasicProfileConverter.toBasicProfile(userRepository.findByFullNameIgnoreCaseLike("%"+fullNameFilter+"%"));
	}
	/**
	 * @param userIds
	 * @return
	 */
	public List<BasicProfile> getUsers(List<String> userIds) {
		if (userIds != null && !userIds.isEmpty()) {
			List<BasicProfile> list = new ArrayList<BasicProfile>();
			for (String userId : userIds) {
				BasicProfile bp = getBasicProfileById(userId);
				if (bp != null) {
					list.add(bp);
				}
			}
			return list;
		}
		return Collections.emptyList();
	}

	/**
	 * @param userId
	 * @return
	 */
	public AccountProfile getAccountProfileById(String userId) {
		User user = userRepository.findOne(Long.parseLong(userId));
		return AccountProfileConverter.toAccountProfile(user);
	}
	/**
	 * @param userIds
	 * @return
	 */
	public List<AccountProfile> getAccountProfilesById(List<String> userIds) {
		List<AccountProfile> list = new ArrayList<AccountProfile>();
		if (userIds != null && !userIds.isEmpty()) {
			for (String userId : userIds) {
				AccountProfile ap = getAccountProfileById(userId);
				if (ap != null) {
					list.add(ap);
				}
			}
		}
		return list;
	}

}
