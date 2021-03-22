package example.domain;

import org.apache.geode.DataSerializable;

public interface SessionAttributeEvent extends DataSerializable {

  void apply(Session session);
}
