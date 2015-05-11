package INST767.GirvanNewman;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

import tl.lin.data.array.ArrayListOfIntsWritable;
import tl.lin.data.pair.PairOfInts;

/***
 *
 * Custom message used for sending messages accross super steps.
 *
 * @author Namesh Kher, Sarika Hegde, Pramod Chavan
 *
 */

public class MyMessage implements WritableComparable<MyMessage> {
  private int parentId;
  private ArrayListOfIntsWritable sourceIds;

  /**
   * Creates a pair.
   */
  public MyMessage() {
    sourceIds = new ArrayListOfIntsWritable();
  }

  /**
   * Creates a pair.
   *
   * @param left the left element
   * @param right the right element
   */
  public MyMessage(int parentId, ArrayListOfIntsWritable sourceIds) {
    set(parentId, sourceIds);
  }

  /**
   * Deserializes this pair.
   *
   * @param in source for raw byte representation
   */
  @Override
  public void readFields(DataInput in) throws IOException {
    parentId = in.readInt();
    this.sourceIds = new ArrayListOfIntsWritable();
    int size = in.readInt();
    for (int i = 0; i < size; i++) {
      sourceIds.add(i, in.readInt());
    }
  }

  /**
   * Serializes this pair.
   *
   * @param out where to write the raw byte representation
   */
  @Override
  public void write(DataOutput out) throws IOException {
    out.writeInt(parentId);
    int size = sourceIds.size();
    out.writeInt(size);
    for (int i = 0; i < size; i++) {
      out.writeInt(sourceIds.get(i));
    }
  }

  /**
   * Returns the left element.
   *
   * @return the left element
   */
  public int getParent() {
    return parentId;
  }

  /**
   * Returns the right element.
   *
   * @return the right element
   */
  public ArrayListOfIntsWritable getSourceList() {
    return sourceIds;
  }


  /**
   * Sets the right and left elements of this pair.
   *
   * @param left the left element
   * @param right the right element
   */
  public void set(int parentId, ArrayListOfIntsWritable sourceIds) {
    this.parentId = parentId;
    this.sourceIds = sourceIds;
  }

  /**
   * Returns a hash code value for the pair.
   *
   * @return hash code for the pair
   */
  @Override
  public int hashCode() {
    return parentId;
  }

  /**
   * Generates human-readable String representation of this pair.
   *
   * @return human-readable String representation of this pair
   */
  @Override
  public String toString() {
    return "(" + parentId + ")" + sourceIds.toString();
  }

  /**
   * Clones this object.
   *
   * @return clone of this object
   */
  @Override
  public MyMessage clone() {
    return new MyMessage(this.parentId, this.sourceIds);
  }

  /** Comparator optimized for <code>PairOfInts</code>. */
  public static class Comparator extends WritableComparator {

    /**
     * Creates a new Comparator optimized for <code>PairOfInts</code>.
     */
    public Comparator() {
      super(MyMessage.class);
    }

    /**
     * Optimization hook.
     */
    @Override
    public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
      int thisLeftValue = readInt(b1, s1);
      int thatLeftValue = readInt(b2, s2);

      if (thisLeftValue == thatLeftValue) {
        int thisRightValue = readInt(b1, s1 + 4);
        int thatRightValue = readInt(b2, s2 + 4);

        return (thisRightValue < thatRightValue ? -1
            : (thisRightValue == thatRightValue ? 0 : 1));
      }

      return (thisLeftValue < thatLeftValue ? -1
          : (thisLeftValue == thatLeftValue ? 0 : 1));
    }
  }

  static { // register this comparator
    WritableComparator.define(PairOfInts.class, new Comparator());
  }

  @Override
  public int compareTo(MyMessage o) {
    // TODO Auto-generated method stub
    return 0;
  }
}

