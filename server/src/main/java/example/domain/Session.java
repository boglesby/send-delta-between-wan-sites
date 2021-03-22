package example.domain;

import org.apache.geode.DataSerializable;
import org.apache.geode.DataSerializer;
import org.apache.geode.Delta;
import org.apache.geode.InvalidDeltaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Session implements Delta, DataSerializable {

  private List<SessionAttributeEvent> events;

  private Map<String,Object> attributes;

	private String id;

  private static final Logger logger = LoggerFactory.getLogger(Session.class);

	public Session() {}

	public String getId() {
	  return this.id;
	}

	protected void localSetAttribute(String name, Object value) {
		this.attributes.put(name, value);
	}

	protected void localRemoveAttribute(String name) {
		this.attributes.remove(name);
	}

	public boolean hasDelta() {
		return !this.events.isEmpty();
	}

  @Override
  public void toDelta(DataOutput out) throws IOException {
    DataSerializer.writeArrayList((ArrayList<SessionAttributeEvent>) this.events, out);
  }

  @Override
  public void fromDelta(DataInput in) throws IOException, InvalidDeltaException {
    // Read the events
    List<SessionAttributeEvent> events;
    try {
      events = DataSerializer.readArrayList(in);
    } catch (ClassNotFoundException e) {
      throw new InvalidDeltaException(e);
    }

    // Iterate and apply the events
    for (SessionAttributeEvent event : events) {
      event.apply(this);
    }
  }

  @Override
  public void toData(DataOutput out) throws IOException {
    DataSerializer.writeString(this.id, out);
    DataSerializer.writeHashMap(this.attributes, out);
  }

  @Override
  public void fromData(DataInput in) throws IOException, ClassNotFoundException {
    this.id = DataSerializer.readString(in);
    this.attributes = DataSerializer.readHashMap(in);
    this.events = new ArrayList<>();
  }
  
  public String toString() {
    return new StringBuilder()
      .append(getClass().getSimpleName())
      .append("[")
      .append("id=")
      .append(this.id)
      .append("; attributes=")
      .append(this.attributes)
      .append("]")
      .toString();
  }
}