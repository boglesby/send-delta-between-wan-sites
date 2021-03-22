package example.domain;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.geode.DataSerializer;

public class DestroySessionAttributeEvent implements SessionAttributeEvent {

  private String attributeName;

  public DestroySessionAttributeEvent() {}

  public DestroySessionAttributeEvent(String attributeName) {
    this.attributeName = attributeName;
  }

  public String getAttributeName() {
    return this.attributeName;
  }

  @Override
  public void apply(Session session) {
    session.localRemoveAttribute(this.attributeName);
  }

  @Override
  public void fromData(DataInput in) throws IOException {
    this.attributeName = DataSerializer.readString(in);
  }

  @Override
  public void toData(DataOutput out) throws IOException {
    DataSerializer.writeString(this.attributeName, out);
  }

  public String toString() {
    return new StringBuilder()
    	.append(getClass().getSimpleName())
    	.append("[")
    	.append("attributeName=")
      .append(this.attributeName)
      .append("]")
      .toString();
  }
}
