package it.smartcommunitylab.aac.auth.internal;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.OncePerRequestFilter;

public class InternalRegFilter extends OncePerRequestFilter {

	@Value("${application.url}")
	private String applicationURL;
	@Value("${mode.reauth}")
	private boolean reauth;	

	public static final String SESSION_INTERNAL_CHECK = "internal-login"; 
	
	@Override
	public void destroy() {
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String loggedWithInternal = (String) request.getSession().getAttribute(
				InternalRegFilter.SESSION_INTERNAL_CHECK);
		if (loggedWithInternal == null || reauth && (reauth && StringUtils.isEmpty( request.getParameter("email")))) {
			response.sendRedirect(applicationURL + "/internal/login");
		} else {
			filterChain.doFilter(request, response);
		}

	}

}
