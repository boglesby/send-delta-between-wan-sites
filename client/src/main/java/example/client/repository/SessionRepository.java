package example.client.repository;

import example.domain.Session;
import org.springframework.data.gemfire.repository.GemfireRepository;

public interface SessionRepository extends GemfireRepository<Session, String> {
}