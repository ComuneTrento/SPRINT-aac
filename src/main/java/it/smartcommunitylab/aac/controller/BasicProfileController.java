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

package it.smartcommunitylab.aac.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import it.smartcommunitylab.aac.manager.BasicProfileManager;
import it.smartcommunitylab.aac.profile.model.BasicProfile;
import it.smartcommunitylab.aac.profile.model.BasicProfiles;

/**
 * @author raman
 *
 */
@Controller
@RequestMapping("/basicprofile")
public class BasicProfileController extends AbstractController {

	private Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private BasicProfileManager profileManager;

	@RequestMapping(method = RequestMethod.GET, value = "/all/{userId}")
	public @ResponseBody
	BasicProfile getUser(@PathVariable("userId") String userId) throws IOException {
		return profileManager.getBasicProfileById(userId);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/all")
	public @ResponseBody
	BasicProfiles searchUsers(@RequestParam(value = "filter", required = false) String fullNameFilter) {

		List<BasicProfile> list;
		if (fullNameFilter != null && !fullNameFilter.isEmpty()) {
			list = profileManager.getUsers(fullNameFilter);

		} else {
			list = profileManager.getUsers();
		}

		BasicProfiles profiles = new BasicProfiles();
		profiles.setProfiles(list);
		return profiles;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/me")
	public @ResponseBody
	BasicProfile findProfile(HttpServletResponse response) {
		Long user = getUserId();
		if (user == null) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		return profileManager.getBasicProfileById(user.toString());
	}

	@RequestMapping(method = RequestMethod.GET, value = "/profiles")
	public @ResponseBody
	BasicProfiles findProfiles(@RequestParam List<String> userIds) {
		BasicProfiles profiles = new BasicProfiles();
		profiles.setProfiles(profileManager.getUsers(userIds));
		return profiles;
	}


	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	@ResponseBody
	public void handleBadRequest(HttpServletRequest req, Exception ex) {
		logger.error(ex.getMessage(), ex);
	}
	
}
