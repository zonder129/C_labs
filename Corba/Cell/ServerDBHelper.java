package Cell;


/**
* Cell/ServerDBHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Cell.idl
* Wednesday, May 23, 2018 4:49:28 PM MSK
*/

abstract public class ServerDBHelper
{
  private static String  _id = "IDL:Cell/ServerDB:1.0";

  public static void insert (org.omg.CORBA.Any a, Cell.ServerDB that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static Cell.ServerDB extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (Cell.ServerDBHelper.id (), "ServerDB");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static Cell.ServerDB read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_ServerDBStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, Cell.ServerDB value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static Cell.ServerDB narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof Cell.ServerDB)
      return (Cell.ServerDB)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      Cell._ServerDBStub stub = new Cell._ServerDBStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static Cell.ServerDB unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof Cell.ServerDB)
      return (Cell.ServerDB)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      Cell._ServerDBStub stub = new Cell._ServerDBStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}
