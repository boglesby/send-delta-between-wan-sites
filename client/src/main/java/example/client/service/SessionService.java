package example.client.service;

import example.domain.Session;
import example.client.repository.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SessionService {

  @Autowired
  private SessionRepository repository;

  private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

  public void create(int numEntries) {
    logger.info("Creating {} sessions", numEntries);
    for (int i=0; i<numEntries; i++) {
      Session session = new Session(String.valueOf(i));
      session = this.repository.save(session);
      logger.info("Saved {}", session);
    }
  }

  public void addAttributes(int numEntries) {
    logger.info("Adding attributes to {} sessions", numEntries);
    for (int i=0; i<numEntries; i++) {
      Optional<Session> optionalSession = this.repository.findById(String.valueOf(i));
      if (optionalSession.isPresent()) {
        Session session = optionalSession.get();
        logger.info("Retrieved {}", session);
        session.setAttribute("attr1", "attr1_value");
        session.setAttribute("attr2", "attr2_value");
        session.setAttribute("attr3", "attr3_value");
        session = this.repository.save(session);
        session.clearEvents();
        logger.info("Added attributes to {}", session);
      }
    }
  }

  public void removeAttributes(int numEntries) {
    logger.info("Removing attributes from {} sessions", numEntries);
    for (int i=0; i<numEntries; i++) {
      Optional<Session> optionalSession = this.repository.findById(String.valueOf(i));
      if (optionalSession.isPresent()) {
        Session session = optionalSession.get();
        logger.info("Retrieved {}", session);
        session.removeAttribute("attr1");
        session.removeAttribute("attr2");
       session = this.repository.save(session);
        session.clearEvents();
        logger.info("Removed attributes from {}", session);
      }
    }
  }

  public void destroy(int numEntries) {
    logger.info("Destroying {} sessions", numEntries);
    for (int i=0; i<numEntries; i++) {
      String key = String.valueOf(i);
      this.repository.deleteById(key);
      logger.info("Destroyed session key={}", key);
    }
  }
}
