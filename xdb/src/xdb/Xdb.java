package xdb;

import java.util.Map;
import java.util.Random;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Timer;


/**
 * xdb��һ���򵥵��ļ����ݿ⣬֧�ֱ���������񣬲��߱���֮��Ĺ�����ϵ��
 *
 */
public final class Xdb implements XdbMBean {
	private volatile XdbConf    conf;
	private volatile Tables     tables;
	private volatile Angel      angel;
	private volatile Checkpoint checkpoint;

	private volatile boolean isOpen = false;

	private static Timer timer = new Timer("xdb.Timer", true);

	private Random random = new Random(System.currentTimeMillis());
	private static Xdb xdbinstance = new Xdb();

	private xdb.util.MBeans.Manager xdbmbeans = new xdb.util.MBeans.Manager();

	public static xdb.util.MBeans.Manager mbeans() {
		return xdbinstance.xdbmbeans;
	}

	public static Xdb getInstance() {
		return xdbinstance;
	}

	// package private
	final Angel getAngel() {
		return angel;
	}

	// package private
//	final Checkpoint getCheckpoint() {
//		return checkpoint;
//	}

	public final Tables getTables() {
		return tables;
	}

	public static Timer timer() {
		return timer;
	}

	public static Random random() {
		return xdbinstance.random;
	}

	public final Executor getExecutor() {
		return Executor.getInstance();
	}

	public static Executor executor() {
		return Executor.getInstance();
	}

	public static boolean isOpen() {
		return xdbinstance.isOpen;
	}

	private Xdb() {
		Runtime.getRuntime().addShutdownHook(new Thread("xdb.ShutdownHook") {
			@Override
			public void run() {
				Trace.fatal("xdb stop from ShutdownHook.");
				Xdb.this.stop();
			}
		});
	}


	public final void setConf(XdbConf conf) {
		if (null == conf)
			throw new NullPointerException();
		Trace.set(conf.getTrace());
		this.conf = conf;
		final Executor e = Executor.getInstance(); // volatile
		if (null != e)
			e.setCorePoolSize(conf.getCorePoolSize(), conf.getProcPoolSize(), conf.getSchedPoolSize());
		XBean._set_xdb_verify_(conf.isXdbVerify());
	}

	public final XdbConf getConf() {
		return conf;
	}

	/**
	 * ������������
	 */
	public final static int NETWORK      = 1;

	/**
	 * ����Ψһ��������Ψһ�����������������档û��Ψһ��֧�֣�����ʹ��������(autoIncrement)
	 */
	public final static int UNIQNAME     = 2;

	////////////////////////////////////////////////////////////////////
	// ������ֹ��ͬһ�����ݿ�Ŀ¼����������ʵ����ͬʱ������鲻�����˳���
	private File inuseFile;
	public static final String XDBINUSEFILENAME = "xdb.inuse";
	private void deleteInuseFile() {
		this.inuseFile.delete();
	}
	private final void createInuseFile() {
		try {
			inuseFile = new File(conf.getDbHome(), XDBINUSEFILENAME);
			if (false == inuseFile.createNewFile())
				throw new XError("xdb is still in active use(never use simultaneously) OR"
						+ "\n\t\tnot stop normally: 1 verify and repair the db(commendatory), "
						+ "2 delete the file '" + inuseFile.getPath() + "', "
						+ "3 start again)");
		}
		catch (java.io.IOException ex) {
			throw new XError(ex);
		}
	}
	////////////////////////////////////////////////////////////////////

	public final synchronized boolean start() {
		if (xdb.util.Dbx.getManager().isOpen())
			throw new IllegalAccessError("i hate dbx.");

		if (isOpen)
			return false;

		conf.getTableHome().mkdirs();
		conf.getLogHome().mkdirs();

		//������ݿ��ṹ�Ƿ�ƥ�䡣added by scm
		//if(!xdb.util.DatabaseMetaData.getInstance().isSame(conf.getDbHome()))
		//	throw new RuntimeException("Compare metadata fail,should run xtransform?");
		xdb.util.DatabaseMetaData.getInstance().createXML(conf.getDbHome());
		
		Trace.open(conf);
		this.createInuseFile();
		Trace.fatal("xdb start begin");
		{
			// ȷ�������þ�̬ʵ�����������Ժ���߳�ͬʱװ��ʱ���߳����⡣
			// ��Ȼ�����ClassLoaderȷ��װ��һ�����ǲ������ģ���ô�Ͳ���Ҫ���������д�ȷ�ϡ�
			Lockeys.getInstance();
			new XBean(null, null);
		}


		try {
			Class<?> cls = Class.forName("xtable._Tables_");
			Object instance = cls.newInstance();
			if (!(instance instanceof Tables))
				throw new XError("invalid xtable.Tables");

			Executor.start(conf.getProcedureConf().getMaxExecutionTime(),
					conf.getCorePoolSize(), conf.getProcPoolSize(), conf.getSchedPoolSize(),
					conf.getTimeoutPeriod());

			// MORE CHECK ?
			tables = (Tables)instance;
			tables.open(conf);

			checkpoint = new Checkpoint(tables);
			angel = new Angel();

			isOpen = true; // ������,Xdb�Ѿ�������ɡ�����������������ݿ⣬�����ñ�־��

			mbeans().register(this, "xdb:type=Xdb");

			checkpoint.start();
			angel.start();

			Trace.fatal("xdb start end");
			return true;

		} catch (XError e) {
			close();
			throw e;
		} catch (Throwable e) {
			close();
			throw new XError(e);
		}
	}

	/**
	 * ��������Ψһ�������硣
	 */
	public void startNetwork() {
		xio.Engine.getInstance().open();
	}

	private final synchronized void close() {
		isOpen = false;
		Trace.fatal("xdb stop begin");
		mbeans().unregisterAll();
		xio.Engine.getInstance().close();

		Executor.stop();

		if (null != angel) {
			angel.shutdown();
			angel = null;
		}

		if (null != checkpoint) {
			checkpoint.shutdown();
			checkpoint = null;
		}

		if (null != tables) {
			tables.close();
			tables = null;
		}
		this.deleteInuseFile();
		Trace.fatal("xdb stop end");
	}

	public final void stop() {
		// try { Thread.sleep(60000); } catch (Exception ex) { } // delay stop. debug
		synchronized (this)	{
			if (false == isOpen)
				return;
			isOpen = false; // �������ñ�־
		}
		final StopHandle handle = this.stopHandle;
		if (null != handle)
			handle.beforeStop();
		this.close();
		if (null != handle)
			handle.afterStop();
	}

	private volatile StopHandle stopHandle = null;
	public void setStopHandle(StopHandle stopHandle) {
		this.stopHandle = stopHandle;
	}

	public static interface StopHandle {
		/**
		 * XDB ֹͣǰ���á�
		 * ��ʱXDB���ݵȹ��ܶ���ʹ�á�������isOpen�Ѿ�Ϊfalse�������������־�Ĺ��ܻ���Ӱ�졣
		 */
		public void beforeStop();
		/**
		 * XDB ֹͣ����á�
		 */
		public void afterStop();
	}

	///////////////////////////////////////////////////////////////////////////////
	// mbean implement
	@Override
	public void shutdown(String iamsure) {
		if (iamsure.equals("iamsure")) {
			this.stop();
			Trace.fatal("halt program from jmx");
			Runtime.getRuntime().halt(0);
		}
	}

	@Override
	public int getAngleInterruptCount() {
		return this.angel.getInterruptCount();
	}

	@Override
	public long getTransactionCount() {
		return Transaction.getTotalCount();
	}

	@Override
	public long getTransactionFalse() {
		return Transaction.getTotalFalse();
	}

	@Override
	public long getTransactionException() {
		return Transaction.getTotalException();
	}

	@Override
	public void testAlive(long timeout)
		throws InterruptedException, ExecutionException, TimeoutException {
		this.getExecutor().testAlive(timeout);
		// more test ...
	}

	public CheckpointMBean getCheckpointMBean() {
		return checkpoint;
	}

	@Override
	public Map<String, AtomicInteger> top(String nsClass, String nsLock) {
		return new xdb.util.StackTrace(nsClass, nsLock).top();
	}
}
