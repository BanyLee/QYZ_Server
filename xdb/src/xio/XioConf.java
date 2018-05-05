package xio;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 * xio װ�����á�
 * 
 * @author lichenghua
 *
 */
public class XioConf {
	private String name;
	private Map<String, Manager> managers = new HashMap<String, Manager>();

	public String getName() {
		return name;
	}

	public Manager getManager(String name) {
		return managers.get(name);
	}

	void open() {
		for (Manager manager : managers.values()) {
			manager.open();
			// �� xio ֹͣʱ�����Զ� unregister ���е� MBean��
			Engine.mbeans().register(manager, "xio:type=" + manager.getMBeanName());
		}
	}

	void close() {
		for (Manager manager : managers.values()) {
			manager.close();
		}
	}

	private void add(Manager manager) {
		if (null != managers.put(manager.getName(), manager))
			throw new RuntimeException("Manager duplicate : " + manager);
	}

	public XioConf(Element self) throws Exception {
		if (false == self.getNodeName().equals("XioConf"))
			throw new IllegalArgumentException(self.getNodeName() + " is not a XioConf.");

		name = self.getAttribute("name");

		NodeList childnodes = self.getChildNodes();
		for (int i = 0; i < childnodes.getLength(); ++i) {
			Node node = childnodes.item(i);
			if (Node.ELEMENT_NODE != node.getNodeType())
				continue;

			Element e = (Element) node;
			String n = e.getNodeName();
			if (n.equals("Manager")) add(Manager.create(this, e));
			else
				throw new RuntimeException("Unkown! node=" + n + " parent=" + self.getNodeName());
		}
	}

	/**
	 * ��ָ���ڵ���ӽڵ��в��Ҳ�װ�� xio ���ã�Ȼ�󷵻ء�û���ҵ����׳��쳣��
	 * 
	 * @param parent ָ���ĸ��ڵ�
	 * @return xio���á�
	 * @throws Exception
	 */
	public static XioConf loadInChildNodes(Element parent) throws Exception {
		NodeList childnodes = parent.getChildNodes();
		for (int i = 0; i < childnodes.getLength(); ++i) {
			Node node = childnodes.item(i);
			if (Node.ELEMENT_NODE != node.getNodeType())
				continue;

			Element e = (Element) node;
			String nodename = e.getNodeName();
			if (nodename.equals("XioConf")) {
				return new XioConf(e);
			}
		}
		throw new java.lang.IllegalStateException();
	}

	/**
	 * ���ļ���װ�غ�ע�� xio ���á�
	 * @param conf �����ļ�����
	 * @throws Exception
	 */
	public static void loadAndRegister(String conf) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setXIncludeAware(true);
		dbf.setNamespaceAware(true);
		Document doc = dbf.newDocumentBuilder().parse(conf);
		Engine.getInstance().register(new XioConf(doc.getDocumentElement()));
	}

	/**
	 * @param self
	 * @throws Exception
	 */
	public static void loadAndRegister(Element self) throws Exception {
		Engine.getInstance().register(new XioConf(self));
	}

	/**
	 * �� XioConf �ڵ㴴����ע�����á�
	 * ֧�ִӽڵ㴴�����ã���Ϊ�˷������ÿ��԰���������� xml �����С�
	 * <p>��װ��ʱ��ֱ��ע�ᵽ�����У���ȥ����ʱ��ע����á������ڲ�����ͬ���� XioConf ���ڣ�
	 * �ظ�ע���ʧ�ܡ�����������ַ�ʽװ�ز�ע�����ã������һ�����⣬���а������õ��ļ����޷�װ�ض�Ρ�
	 * ���õķ����ǵ����ð���������������ʱ��ʹ�� loadInChildNodes �õ�����ʵ�����Լ�����ע��ʱ��
	 * ��������xio.Engine.start֮ǰ����
	 * @param parent
	 * @throws Exception
	 */
	public static void loadAndRegisterInChildNodes(Element parent) throws Exception {
		Engine.getInstance().register(loadInChildNodes(parent));
	}
}
