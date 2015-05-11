package INST767.GirvanNewman;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.io.Writable;

public class ListOfAdjacencyNodes<E extends Writable> extends ArrayList<E> implements Writable {

  /**
   * 
   */
  private static final long serialVersionUID = -1426244279744529486L;

  public ListOfAdjacencyNodes() {
    super();
  }

  public ListOfAdjacencyNodes(ArrayList<E> arrayList) {
    super(arrayList);
  }


  @Override
  public void write(DataOutput out) throws IOException {
    out.writeInt(this.size());
    if (size() == 0)
      return;
    E obj = get(0);

    out.writeUTF(obj.getClass().getCanonicalName());

    for (int i = 0; i < size(); i++) {
      obj = get(i);
      if (obj == null) {
        throw new IOException("Cannot serialize null fields!");
      }
      obj.write(out);
    }
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    this.clear();

    int numFields = in.readInt();
    if (numFields == 0)
      return;
    String className = in.readUTF();
    E obj;
    try {
      @SuppressWarnings("unchecked")
      Class<E> c = (Class<E>) Class.forName(className);
      for (int i = 0; i < numFields; i++) {
        obj = c.newInstance();
        obj.readFields(in);
        this.add(obj);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append("[");
    for (int i = 0; i < this.size(); i++) {
      if (i != 0)
        sb.append(",");
      sb.append(this.get(i));
    }
    sb.append("]]");

    return sb.toString();
  }

}
