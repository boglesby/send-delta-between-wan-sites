package example.domain;

import org.springframework.data.annotation.Id;

import org.apache.geode.DataSerializable;
import org.apache.geode.DataSerializer;
import org.apache.geode.Delta;
import org.apache.geode.InvalidDeltaException;
import org.springframework.data.gemfire.mapping.annotation.Region;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Region("session")
public class Session implements Delta, DataSerializable {

  private List<SessionAttributeEvent> events;

  private Map<String,Object> attributes;

  @Id
	private String id;

	public Session() {}
	
	public Session(String id) {
	  this.id = id;
	  this.attributes = new HashMap<>();
	  this.events = new ArrayList<>(); 
	}
	
	public String getId() {
	  return this.id;
	}
	
	public void setAttribute(String name, Object value) {
	  localSetAttribute(name, value);
		this.events.add(new UpdateSessionAttributeEvent(name, value));
	}
	
	protected void localSetAttribute(String name, Object value) {
		this.attributes.put(name, value);
	}

	public void removeAttribute(String name) {
		localRemoveAttribute(name);
		this.events.add(new DestroySessionAttributeEvent(name));
	}
	
	protected void localRemoveAttribute(String name) {
		this.attributes.remove(name);
	}

	public void clearEvents() {
		this.events.clear();
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