import java.util.ArrayList;

class Node
{
	private int degree;
	private int maxKeyCnt;
	private int minKeyCnt;

	private ArrayList<Node> ptrList;
	private ArrayList<Integer> keyList;
	private ArrayList<Integer> recordPtrList;
	
	boolean isLeaf;

	Node(int degree)
	{
		int size = degree / 2;

		this.degree = degree;
		this.ptrList = new ArrayList<Node>(size);
		this.keyList = new ArrayList<Integer>(size);
		this.recordPtrList = new ArrayList<Integer>(size);
		this.isLeaf = false;
		this.maxKeyCnt = degree - 1;
		this.minKeyCnt = (degree - 1) / 2;
	}

	int getKeyCnt()
	{
		return keyList.size();
	}

	int getMaxKeyCnt()
	{
		return maxKeyCnt;
	}

	int getMinKeyCnt()
	{
		return minKeyCnt;
	}

	int getKey(int idx)
	{
		return keyList.get(idx);
	}

	int getLeftMostKey()
	{
		return keyList.get(0);
	}

	int getRightMostKey()
	{
		return keyList.get(keyList.size() - 1);
	}

	int getPtrCnt()
	{
		return ptrList.size();
	}

	Node getPtr(int idx)
	{
		return ptrList.get(idx);
	}

	int getRecordPtr(int idx)
	{
		return recordPtrList.get(idx);
	}

	Node getRightMostPtr()
	{
		if (ptrList.size() == 0)
			return null;
		else
			return ptrList.get(ptrList.size() - 1);
	}

	void addKey(int key)
	{
		keyList.add(key);
	}

	void addKey(int idx, int key)
	{
		keyList.add(idx, key);
	}

	void addPtr(Node ptr)
	{
		ptrList.add(ptr);
	}

	void addPtr(int idx, Node ptr)
	{
		ptrList.add(idx, ptr);
	}

	void addRecordPtr(int rPtr)
	{
		recordPtrList.add(rPtr);
	}

	void addRecordPtr(int idx, int rPtr)
	{
		recordPtrList.add(idx, rPtr);
	}

	int removeKey(int idx)
	{
		return keyList.remove(idx);
	}

	int removeRightMostKey()
	{
		return keyList.remove(keyList.size() - 1);
	}

	int removeLeftMostKey()
	{
		return keyList.remove(0);
	}
	
	Node removePtr(int idx)
	{
		return ptrList.remove(idx);
	}

	int removeRecordPtr(int idx)
	{
		return recordPtrList.remove(idx);
	}

	int removeRightMostRecordPtr()
	{
		return recordPtrList.remove(recordPtrList.size() - 1);
	}

	int removeLeftMostRecordPtr()
	{
		return recordPtrList.remove(0);
	}

	Node removeRightMostPtr()
	{
		if (ptrList.size() == 0)
			return null;
		else
			return ptrList.remove(ptrList.size() - 1);
	}

	Node removeLeftMostPtr()
	{
		if (ptrList.size() == 0)
			return null;
		else
			return ptrList.remove(0);
	}

	int setKey(int idx, int key)
	{
		return keyList.set(idx, key);
	}

	Node setPtr(int idx, Node ptr)
	{
		return ptrList.set(idx, ptr);
	}

	boolean isEmpty()
	{
		return keyList.isEmpty();
	}

	boolean isOverflowed()
	{
		return keyList.size() > maxKeyCnt;
	}

	boolean isUnderflowed()
	{
		return keyList.size() < minKeyCnt;
	}

	boolean hasMoreThanMinKey()
	{
		return keyList.size() > minKeyCnt;
	}

	void printKeys()
	{
		int i ;
		int keyListSize = keyList.size();

		if (keyListSize == 0)
		{
			System.out.printf("Empty ");
			return;
		}

		for (i = 0; i < keyListSize - 1; i++)
			System.out.printf("%d,", keyList.get(i));
		System.out.print(keyList.get(i));
	}

	void printlnKeys()
	{
		int i ;
		int keyListSize = keyList.size();

		if (keyListSize == 0)
		{
			System.out.printf("Empty ");
			return;
		}

		for (i = 0; i < keyListSize - 1; i++)
			System.out.printf("%d,", keyList.get(i));
		System.out.println(keyList.get(i));
	}

	String keysToString()
	{
		StringBuilder sb = new StringBuilder();

		int i ;
		int keyListSize = keyList.size();

		for (i = 0; i < keyListSize - 1; i++)
			sb.append(Integer.toString(keyList.get(i)) + ",");
		sb.append(Integer.toString(keyList.get(i)));

		return sb.toString();
	}

	String recordPtrsToString()
	{
		StringBuilder sb = new StringBuilder();

		int i ;
		int recPtrListSize = recordPtrList.size();

		for (i = 0; i < recPtrListSize - 1; i++)
			sb.append(Integer.toString(recordPtrList.get(i)) + ",");
		sb.append(Integer.toString(recordPtrList.get(i)));

		return sb.toString();
	}
}
