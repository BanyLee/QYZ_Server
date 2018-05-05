package xio;

import com.goldhuman.Common.Marshal.Marshal;
import com.goldhuman.Common.Marshal.MarshalException;
import com.goldhuman.Common.Marshal.OctetsStream;
import com.goldhuman.Common.Octets;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public abstract class Protocol implements Marshal, Runnable {
	private Xio    connection;
	private Object context;

	//add by cjc 2011.2.15
	private Object sender = null;
	private Object receiver = null;
	
	public final void setSender(Object s)
	{
		sender = s;
	}
	
	public final Object getSender()
	{
		return sender;
	}
	
	public final void setReceiver(Object r)
	{
		receiver = r;
	}
	
	public final Object getReceiver()
	{
		return receiver;
	}
	
	public final Object getContext() {
		return context;
	}

	public final void setContext(Object context) {
		this.context = context;
	}

	public final void setConnection(Xio connection) {
		this.connection = connection;
	}

	public final Xio getConnection() {
		return connection;
	}

	public final Manager getManager() {
		return connection.getCreator().getManager();
	}

	public abstract int getType();

	public String str() 
	{
		StringBuilder ss = new StringBuilder();
		if (sender != null)
		{
			ss.append("sender=" + sender.toString() + " ");
		}
		if (receiver != null)
		{
			ss.append("receiver=" + receiver.toString() + " ");
		}
		ss.append("type=" + getType() + " class=" + getClass().getName() + " this=" + this);
		return ss.toString();
	}

	@Override
	public final void run() {
		//long begin = System.currentTimeMillis();
		try {
			process();
		} catch (Throwable e) {
			// ����ر����ӵı�����Ϊ���ô�����챩¶���������������Զ���������ʹ�����������֡�
			// �Ͳ��ٹر������ˡ� getConnection().close();
			xdb.Trace.error("Protocol.run exception class=" + getClass().getName(),  e);
		}
		//counter.increment(this.getClass().getName() + ".Time", (int)(System.currentTimeMillis() - begin));
	}

	public void dispatch(Stub stub) throws Exception {
		if (xdb.Trace.isDebugEnabled())
			xdb.Trace.debug("xio.Protocol execute " + str());
		getManager().execute(this);
	}

	protected void process() {
		throw new UnsupportedOperationException("process of " + getClass().getName());
	}

	/////////////////////////////////////////////////////////////
	// ���緢��
	public boolean send(Xio to) {
		if (null != to) {
			to.getCreator().getManager().beforeSend(this, to);
			return checkSend(to, new OctetsStream().marshal(this));
		}else {
			return false;
		}
	}

	public final boolean send(Manager manager) {
		return send(manager.get());
	}

	private static xdb.util.Counter counterSend
		= new xdb.util.Counter(Engine.mbeans(), "xdb", "Protocols.send");

	/**
	 * ���Э�������С������
	 * �������Э����룬���͡�
	 * @param to
	 * @param parameter
	 * @return һ��������󷵻� false�����������쳣��
	 * 
	 * ���ǰѲ���parameter�ĳ�Э�����ã����ڲ�ϵ�л������� marshal ���Ρ�
	 * ����Ҫ�޸�OctetsStream��buffer����ʽ����ʱ������
	 * ���ʱ��������Э������ȼ�ȥ compact_uint����󳤶ȡ�
	 */
	protected final boolean checkSend(Xio to, Octets parameter) {
		Protocol.Coder coder = (Protocol.Coder)to.getCreator().getManager().getCoder();
		coder.getStub(this.getType()).checkSend(this, parameter.size());
		if (to.add(new OctetsStream().compact_uint32(this.getType()).marshal(parameter).getByteBuffer())) {
			counterSend.increment(this.getClass().getName());
			return true;
		}
		return false;
	}

	public static class Stub {
		private int type;
		private Class<Protocol> cls;
		private int maxSize;

		public int getType() {
			return type;
		}

		public Class<Protocol> getCls() {
			return cls;
		}

		public Protocol newInstance() throws Exception {
			return cls.newInstance();
		}

		@SuppressWarnings("unchecked")
		public Stub(Element self) throws Exception {
			// ���������ʵ���������ͼ�顣��ʱ�������ý��������У�һ����˵Xdb��û��������
			// �����ӳټ����ã��磺��ʱֻ��¼������֡���Manager�򿪵�ʱ����������顣
			this.cls = (Class<Protocol>)Class.forName(self.getAttribute("class"));
			Protocol p = cls.newInstance();
			this.type = p.getType();

			String tmp = self.getAttribute("maxSize");
			this.maxSize = tmp.isEmpty() ? 0 : Integer.parseInt(tmp);
		}

		public void dispatch(OctetsStream os, Xio from, Object context) throws Exception {
			Protocol p = newInstance();
			p.unmarshal(os);
			p.setConnection(from);
			p.setContext(context);
			p.dispatch(this);
		}

		public void checkSend(Protocol p, int size) {
			if (p.getClass() != cls)
				throw new xio.MarshalError("checkSend of " + this + " class mismatch!");
			if (xdb.Trace.isDebugEnabled())
				xdb.Trace.debug("xio.send " + p.str());
			checkSize(size);
		}

		public void checkSize(int size) {
			if (size < 0 || (maxSize > 0 && size > maxSize))
				throw new xio.MarshalError("checkSize of " + this + " size=" + size);
		}

		@Override
		public String toString() {
			return "Stub(" + type + ", " + cls.getName() + ")";
		}
	}

	// Э�����������ʵ�֡�
	public static final class Coder extends Manager.Coder {
		private Map<Integer, Stub> stubs = new HashMap<Integer, Stub>();

		public Stub getStub(int type) {
			Stub stub = stubs.get(type);
			if (null == stub)
				throw new xio.MarshalError("Protocol.Stub NOT found type=" + type);
			return stub;
		}

		@Override
		public void checkSend(Protocol p, int psize) {
			getStub(p.getType()).checkSend(p, psize);
		}

		@Override
		public void dispatch(int type, OctetsStream os, Xio from, Object context) throws Exception {
			getStub(type).dispatch(os, from, context);
		}

		@Override
		public void initFilterList(Filter.List input, Filter.List output) {
			input.addLast(new Protocol.Decoder(this));
		}

		@Override
		public void parse(Manager manager, Element self) throws Exception {
			NodeList childnodes = self.getChildNodes();
			for (int i = 0; i < childnodes.getLength(); ++i) {
				Node node = childnodes.item(i);
				if (Node.ELEMENT_NODE != node.getNodeType())
					continue;
				Element e = (Element) node;
				String n = e.getNodeName();
				if (n.equals("Protocol")) add(new Protocol.Stub(e));
				if (n.equals("Rpc"))      add(new Rpc.Stub(e));
//				else
//					throw new RuntimeException("Unkown! node=" + n +
//							" parent=" + self.getNodeName() + "@" + manager);
			}
		}

		void add(Stub stub) {
			if (null != stubs.put(stub.getType(), stub))
				throw new RuntimeException("duplicate type of " + stub);
		}
	}
	private static xdb.util.Counter counterRecv
		= new xdb.util.Counter(Engine.mbeans(), "xdb", "Protocols.recv");

	// ���������ʵ�֡�
	public static class Decoder extends Filter {
		private ByteBuffer buffer;
		private Protocol.Coder coder;

		public Decoder(Protocol.Coder coder) {
			super("xio.Protocol.Decoder");
			this.coder = coder;
		}

		@Override
		public void doFilter(Filter.Iterator it, ByteBuffer b, Xio x) {
			buffer = Helper.realloc(buffer, b.remaining());
			buffer.put(b).flip();
			buffer.position(decode(buffer.array(), buffer.limit(), x));
			buffer.compact();
			if (0 == buffer.position()) // isEmpty
				buffer = null;
		}

		private int decode(byte[] bytes, int length, Xio x) {
			OctetsStream os = OctetsStream.wrap(Octets.wrap(bytes, length));
			int mark = 0; // os.position(); MUSTBE ZERO
			try
			{
				while (os.size() > mark)
				{
					int type = os.uncompact_uint32();
					int size = os.uncompact_uint32();
					Protocol.Stub stub = coder.getStub(type);
					stub.checkSize(size);
					if (size > os.remain())
						break; // not enough
					mark = os.position() + size;
					try {
						counterRecv.increment(stub.getCls().getName());
						
//						static class X implements Runnable {
//							private Protocol.Stub stub;
//							private OctetsStream os;
//							private Xio x;
//							private int type;
//							private int size;
//							public X(Protocol.Stub stub, OctetsStream os, Xio x, int type, int size) {
//								this.stub = stub;
//								this.os = os;
//								this.x = x;
//								this.type = type;
//								this.size = size;
//							}
//							public void run() {
//								stub.dispatch(os, x, null);
//								if (os.position() != os.size()) {
//									xdb.Trace.warn("xio.MarshalError(" + type + ", " + size + ")=" + (os.size() - os.position()));
//								}
//							}
//						}
//						new X(stub, OctetsStream.wrap(Octets.wrap(Arrays.copyOfRange(bytes, os.position(), mark))), x, type, size);

						stub.dispatch(os, x, null);
					} catch (Throwable e) {
						throw new xio.MarshalError("(" + type + ", " + size + ")", e);
					}
					// p ����պðѵ�ǰ���������������ѵ�������Ҳ���١�
					if (os.position() != mark) {
						throw new xio.MarshalError("(" + type + ", " + size + ")=" + (mark-os.position()));
					}
				}
			} catch (MarshalException e) {
				// SKIP! ֻ�к�С�İ���Э��ͷ��������ʱ��Żᷢ������쳣�����������ܷ�����
			}
			return mark;
		}
	}
}
