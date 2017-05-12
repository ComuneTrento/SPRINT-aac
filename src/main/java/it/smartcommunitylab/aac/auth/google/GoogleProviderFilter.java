package it.smartcommunitylab.aac.auth.google;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.OncePerRequestFilter;

public class GoogleProviderFilter extends OncePerRequestFilter {

	@Value("${application.url}")
	private String applicationURL;
	@Value("${mode.reauth}")
	private boolean reauth;	

	@Override
	public void destroy() {
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String loggedWithGoogle = (String) request.getSession().getAttribute(
				GoogleAuthHelper.SESSION_GOOGLE_CHECK);
		if (loggedWithGoogle == null || (reauth && StringUtils.isEmpty( request.getParameter("OIDC_CLAIM_email")))) {
			response.sendRedirect(applicationURL + "/auth/google-oauth");
		} else {
			filterChain.doFilter(request, response);
		}

	}

}
