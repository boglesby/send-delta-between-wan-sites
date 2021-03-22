package example.domain;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.geode.DataSerializer;

public class UpdateSessionAttributeEvent implements SessionAttributeEvent {

  private String attributeName;

  private Object attributeValue;

  public UpdateSessionAttributeEvent() {}

  public UpdateSessionAttributeEvent(String attributeName, Object attributeValue) {
    this.attributeName = attributeName;
    this.attributeValue = attributeValue;
  }

  public String getAttributeName() {
    return this.attributeName;
  }

  public Object getAttributeValue() {
    return this.attributeValue;
  }

  @Override
  public void apply(Session session) {
    session.localSetAttribute(this.attributeName, this.attributeValue);
  }

  @Override
  public void fromData(DataInput in) throws IOException, ClassNotFoundException {
    this.attributeName = DataSerializer.readString(in);
    this.attributeValue = DataSerializer.readObject(in);
  }

  @Override
  public void toData(DataOutput out) throws IOException {
    DataSerializer.writeString(this.attributeName, out);
    DataSerializer.writeObject(this.attributeValue, out);
  }

  public String toString() {
    return new StringBuilder()
    	.append(getClass().getSimpleName())
    	.append("[")
    	.append("attributeName=")
      .append(this.attributeName)
    	.append("; attributeValue=")
      .append(this.attributeValue)
      .append("]")
      .toString();
  }
}
