package xdb;

/**
 * 
 * �ڴ����������������.�������Ʊ�ɾ���ļ�¼ͨ������ӿڻص���
 * 
 * ���ݿ����Զ������ص�����ӿڡ�
 * 
 * ע��ӿ������ɳ����ı�ӿ����档
 * 
 * @author lichenghua
 *
 * @param <K>
 * @param <V>
 */
public interface CacheRemovedHandle<K, V> {
	public void recordRemoved(K key, V value);
}
