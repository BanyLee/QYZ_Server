package xdb;

/**
 * Cache �����ص��ӿڡ�
 * 
 * ��ֻ�ڻص�ʱ���֣�Ȼ���ڻص�֮���ͷŵ���
 * 
 * �����޸ġ�
 * ���ܱ���ǳ����������á�
 * 
 * @author lichenghua
 *
 */
public interface CacheQuery<K, V> {
	public void onQuery(K key, V value);
}
