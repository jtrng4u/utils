import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomSessionRegistry {

  @Autowired
  private RedisTemplate<String, Object> redisTemplate;

  private final SessionRegistry sessionRegistry = new SessionRegistryImpl();

  public void registerSession(String sessionId, Object principal) {
    sessionRegistry.registerNewSession(sessionId, principal);
    redisTemplate.opsForSet().add("user:sessions:" + ((UserDetails) principal).getUsername(), sessionId);
  }

  public void removeSession(String sessionId, String username) {
    sessionRegistry.removeSessionInformation(sessionId);
    redisTemplate.opsForSet().remove("user:sessions:" + username, sessionId);
  }

  public void expireUserSessions(String username) {
    Set<Object> sessionIds = redisTemplate.opsForSet().members("user:sessions:" + username);
    if (sessionIds != null) {
      for (Object sessionId : sessionIds) {
        SessionInformation session = sessionRegistry.getSessionInformation(sessionId.toString());
        if (session != null) {
          session.expireNow();
        }
        redisTemplate.delete("spring:session:sessions:" + sessionId.toString());
        removeSession(sessionId.toString(), username);
      }
    }
  }
}



import javax.servlet.http.HttpSessionEvent;
    import javax.servlet.http.HttpSessionListener;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.userdetails.UserDetails;
    import javax.servlet.http.HttpSession;

public class CustomHttpSessionListener implements HttpSessionListener {

  @Autowired
  private CustomSessionRegistry sessionRegistry;

  @Override
  public void sessionCreated(HttpSessionEvent event) {
    HttpSession session = event.getSession();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof UserDetails) {
      sessionRegistry.registerSession(session.getId(), auth.getPrincipal());
    }
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent event) {
    HttpSession session = event.getSession();
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof UserDetails) {
      sessionRegistry.removeSession(session.getId(), ((UserDetails) auth.getPrincipal()).getUsername());
    }
  }
}



@Service
public class UserService {

  @Autowired
  private CustomSessionRegistry sessionRegistry;

  public void updatePassword(String username, String newPassword) {
    // Update the password in the database
    // ...

    // Invalidate all sessions for the user
    sessionRegistry.expireUserSessions(username);
  }
}


