import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class SessionValidationFilter implements javax.servlet.Filter {

  @Autowired
  private SessionRegistry sessionRegistry;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    String sessionId = httpRequest.getRequestedSessionId();
    if (sessionId != null) {
      SessionInformation sessionInformation = sessionRegistry.getSessionInformation(sessionId);
      if (sessionInformation == null || sessionInformation.isExpired()) {
        new SecurityContextLogoutHandler().logout(httpRequest, httpResponse, null);
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.getWriter().write("Session expired. Please log in again.");
        return;
      }
    }

    chain.doFilter(request, response);
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Initialization logic if needed
  }

  @Override
  public void destroy() {
    // Cleanup logic if needed
  }
}
