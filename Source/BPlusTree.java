import java.util.*;
import java.io.*;

class BPlusTree
{
	private Node root;
	private int degree;

	private ArrayList<Integer> values;
	private Queue<Integer> deletedValIdx;

	private Serializer srz;
	
	//Constructor
	BPlusTree()
	{
		this.root = null;
		this.degree = 0;
		this.values = null;
		this.deletedValIdx = null;
		this.srz = null;
	}

	//Create BPlusTree
	void create(String indexpath, int degree) throws Exception
	{
		this.degree = degree;

		this.root = new Node(degree);
		this.root.isLeaf = true;

		this.values = new ArrayList<Integer>();
		this.deletedValIdx = new LinkedList<Integer>();

		srz = new Serializer();

		this.write(indexpath);
	}

	//Search single key
	boolean search(int key) throws Exception
	{
		return searchRec(root, key);
	}

	private boolean searchRec(Node node, int key) throws Exception
	{
		int idx = 0;
		int keyCnt = node.getKeyCnt();

		idx = findKeyIdx(node, key);

		if(!node.isLeaf)
		{
			node.printlnKeys();
			if (idx < keyCnt && key == node.getKey(idx))
				idx++;
			return searchRec(node.getPtr(idx), key);
		}
		else if (idx < keyCnt && key == node.getKey(idx))
		{
			System.out.println(values.get(node.getRecordPtr(idx)));
			return true;
		}
		else
		{
			System.out.println("NOT FOUND");
			return false;
		}
	}

	private int findKeyIdx(Node node, int key)
	{
		int left, right, mid;
		int leftKey, rightKey, midKey;

		left = 0;
		right = node.getKeyCnt() - 1;

		if (right < 0)
			return 0;

		while (left < right)
		{
			mid = (left + right) / 2;
			midKey = node.getKey(mid);

			if (key < midKey)
				right = mid;
			else if (key == midKey)
				return mid;
			else
				left = mid + 1;

		}

		if (key > node.getKey(left))
			left++;

		return left;
	}

	//Search keys in range
	boolean search(int startKey, int endKey) throws Exception
	{
		return searchRec(root, startKey, endKey);
	}

	private boolean searchRec(Node node, int startKey, int endKey) throws Exception
	{
		int idx = 0;
		int keyCnt = node.getKeyCnt();
		boolean foundKeys = false;

		idx = findKeyIdx(node, startKey);

		if(!node.isLeaf)
		{
			if (idx < keyCnt && startKey == node.getKey(idx))
				idx++;
			return searchRec(node.getPtr(idx), startKey, endKey);
		}
		else
		{
			while (node != null)
			{
				keyCnt = node.getKeyCnt();

				while (idx < keyCnt && node.getKey(idx) <= endKey)
				{
					foundKeys = true;
					System.out.printf("%d,%d\n", node.getKey(idx), values.get(node.getRecordPtr(idx)));
					idx++;
				}

				if (idx < keyCnt)
					break;

				node = node.getRightMostPtr();
				idx = 0;
			}

			if (!foundKeys)
				System.out.println("NOT FOUND");

			return foundKeys;
		}
	}

	//Insert key & value
	boolean insert(String inputfile) throws Exception
	{
		if (!checkFileExists(inputfile))
            return false;

		FileReader fr = new FileReader(inputfile);
		BufferedReader br = new BufferedReader(fr);

		ArrayList<KeyValPair> keyValList = new ArrayList<KeyValPair>();

		String line;
		String[] keyVal;
		int key, val;
		while ((line = br.readLine()) != null)
		{
			keyVal = line.split(",");
			key = Integer.parseInt(keyVal[0]);
			val = Integer.parseInt(keyVal[1]);
			keyValList.add(new KeyValPair(key, val));
		}

		fr.close();
		br.close();

		boolean result = true;

		for (KeyValPair pair: keyValList)
		{
			if(!insert(pair.key, pair.val))
				result = false;
		}

		return result;
	}

	private boolean insert(int key, int val) throws Exception
	{
		boolean ret;
		ret = insertRec(root, key, val);

		if (ret && root.isOverflowed())
		{
			Node newRoot = new Node(degree);
			newRoot.addPtr(root);
			splitChild(newRoot, 0);
			root = newRoot;
		}

		return ret;
	}

	private boolean insertRec(Node node, int key, int val) throws Exception
	{
		int idx = 0;
		boolean ret;
		int keyCnt = node.getKeyCnt();

		idx = findKeyIdx(node, key);

		if (!node.isLeaf)
		{
			if (idx < keyCnt && key == node.getKey(idx))
				idx++;

			ret = insertRec(node.getPtr(idx), key, val);

			if (ret && node.getPtr(idx).isOverflowed())
				splitChild(node, idx);

			return ret;
		}
		else
		{
			if (idx < keyCnt && key == node.getKey(idx))
			{
				System.out.printf("Duplicated key insertion error! - Key: %d\n", key);
				return false;
			}

			node.addKey(idx, key);

			if (deletedValIdx.isEmpty())
			{
				values.add(val);
				node.addRecordPtr(idx, values.size() - 1);
			}
			else
			{
				int delValIdx = deletedValIdx.poll();

				values.set(delValIdx, val);
				node.addRecordPtr(idx, delValIdx);
			}

			return true;
		}
	}

	private void splitChild(Node parent, int idx)
	{
		Node left = parent.getPtr(idx);
		Node right = new Node(degree);

		right.isLeaf = left.isLeaf;

		int leftKeyCnt = left.getKeyCnt();
		int midKeyIdx = leftKeyCnt / 2;

		parent.addPtr(idx + 1, right);
		parent.addKey(idx, left.getKey(midKeyIdx));
		
		if (left.isLeaf)
		{
			for (int j = midKeyIdx; j < leftKeyCnt; j++)
			{
				right.addKey(left.getKey(j));
				right.addRecordPtr(left.getRecordPtr(j));
			}

			for (int j = leftKeyCnt - 1; j >= midKeyIdx; j--)
			{
				left.removeKey(j);
				left.removeRecordPtr(j);
			}

			Node nextLeaf = left.removeRightMostPtr();

			if (nextLeaf != null)
				right.addPtr(nextLeaf);

			left.addPtr(right);
		}
		else
		{
			for (int j = midKeyIdx + 1; j < leftKeyCnt; j++)
				right.addKey(left.getKey(j));

			for (int j = leftKeyCnt - 1; j >= midKeyIdx; j--)
				left.removeKey(j);

			for (int j = midKeyIdx + 1; j <= leftKeyCnt; j++)
				right.addPtr(left.getPtr(j));

			for (int j = leftKeyCnt; j >= midKeyIdx + 1; j--)
				left.removePtr(j);
		}
	}

	boolean delete(String deletefile) throws Exception
	{
		if (!checkFileExists(deletefile))
			return false;
		
		FileReader fr = new FileReader(deletefile);
		BufferedReader br = new BufferedReader(fr);

		ArrayList<Integer> keyList = new ArrayList<Integer>();

		String line;
		while ((line = br.readLine()) != null)
			keyList.add(Integer.parseInt(line));

		fr.close();
		br.close();

		boolean result = true;

		for (int key : keyList)
		{
			if (!delete(key))
				result = false;
		}

		return result;
	}

	private boolean delete(int key)
	{
		DelReturn ret = deleteRec(null, root, -1, key);

		if (!ret.ret)
			return false;

		//delete??? ?????? tree height??? ???????????? ??????
		if (root.isEmpty() && root.getPtrCnt() > 0)
			root = root.getPtr(0);

		return true;
	}

	private DelReturn deleteRec(Node parent, Node node, int nodeIdx, int key)
	{
		DelReturn ret;
		int idx = 0;
		int keyCnt = node.getKeyCnt();

		idx = findKeyIdx(node, key);

		if (!node.isLeaf)
		{
			boolean hasSameKey = false;

			if (idx < keyCnt && key == node.getKey(idx))
			{
				idx++;
				hasSameKey = true;
			}

			Node delNode = node.getPtr(idx);

			ret = deleteRec(node, delNode, idx, key);

			//delete??? key??? ?????? ????????? ?????? ?????? return
			if (ret.ret == false)
				return ret;

			if (hasSameKey)
				node.setKey(idx - 1, ret.replaceKey);

			//delete ??? node??? underflow??? ???????????? ???????????? key replace??? ????????? return
			if (!delNode.isUnderflowed())
				return ret;

			//underflow??? ???????????? ?????? ?????? borrow??? ?????????
			if (canBorrowFromLeft(node, idx))
			{
				borrowLeftAndBalance(node, idx);
				return ret;
			}
			else if (canBorrowFromRight(node, idx))
			{
				borrowRightAndBalance(node, idx);
				return ret;
			}
			//borrow??? ??????????????? merge??? ??????
			//borrow??? ????????? merge??? sibling??? ????????? ????????? ?????????
			else if (idx > 0)
			{
				mergeWithLeft(node, idx);
				return ret;
			}
			else
			{
				mergeWithRight(node, idx);
				return ret;
			}
		}
		//leaf node?????? ?????? ?????????, parent node?????? underflow ????????? ?????? ???????????? ?????????
		else
		{
			//delete??? key??? ???????????? ??????
			if (idx >= keyCnt || key != node.getKey(idx))
			{
				System.out.printf("Key missing error! - Key: %d\n", key);
				return new DelReturn(false);
			}

			deletedValIdx.add(node.getRecordPtr(idx));

			node.removeRecordPtr(idx);
			node.removeKey(idx);

			//leaf node??? left-most key??? ?????? ??????
			//???????????? internal node?????? ?????? key??? ???????????? ???????????? ??????
			//??? key??? ????????? replace??? ??? key??? delete key ?????? ????????? key??? ?????????
			//root node??? ???????????? empty node??? ??? ???????????? ???????????? ?????? ?????? ????????? ???
			//root node??? ????????? left-most key??? ???????????????, ?????? ????????? key??? ???????????? ?????? ?????????
			//empty node??? ??? ????????? ????????? => ??? ?????? merge??? ????????? ?????? key??? replace ?????? ??????
			//?????? ???????????? ????????? ?????? replaceKey = 0??? ?????? ?????? ???????????? ?????? ??????
			int replaceKey = 0;

			if (parent != null && idx == 0)
			{
				if (!node.isEmpty())
					replaceKey = node.getLeftMostKey();
				else if (node.getPtrCnt() > 0)
					replaceKey = node.getRightMostPtr().getLeftMostKey();
			}

			return new DelReturn(true, replaceKey);
		}
	}

	private boolean canBorrowFromLeft(Node parent, int idx)
	{
		if (idx == 0)
			return false;
		return parent.getPtr(idx - 1).hasMoreThanMinKey();
	}

	private boolean canBorrowFromRight(Node parent, int idx)
	{
		if (idx >= parent.getPtrCnt() - 1)
			return false;
		return parent.getPtr(idx + 1).hasMoreThanMinKey();
	}

	private void borrowLeftAndBalance(Node parent, int delNodeIdx)
	{
		Node delNode, leftNode;

		delNode = parent.getPtr(delNodeIdx);
		leftNode = parent.getPtr(delNodeIdx - 1);

		int borrowedKey = leftNode.removeRightMostKey();

		if (delNode.isLeaf)
		{
			int borrowedRecordPtr = leftNode.removeRightMostRecordPtr();

			delNode.addKey(0, borrowedKey);
			delNode.addRecordPtr(0, borrowedRecordPtr);

			parent.setKey(delNodeIdx - 1, borrowedKey);
		}
		else
		{
			delNode.addKey(0, parent.getKey(delNodeIdx - 1));
			delNode.addPtr(0, leftNode.removeRightMostPtr());

			parent.setKey(delNodeIdx - 1, borrowedKey);
		}
	}

	private void borrowRightAndBalance(Node parent, int delNodeIdx)
	{
		Node delNode, rightNode;

		delNode = parent.getPtr(delNodeIdx);
		rightNode = parent.getPtr(delNodeIdx + 1);

		int borrowedKey = rightNode.removeLeftMostKey();

		if (delNode.isLeaf)
		{
			int borrowedRecordPtr = rightNode.removeLeftMostRecordPtr();

			delNode.addKey(borrowedKey);
			delNode.addRecordPtr(borrowedRecordPtr);

			parent.setKey(delNodeIdx, rightNode.getLeftMostKey());
		}
		else
		{
			delNode.addKey(parent.getKey(delNodeIdx));
			delNode.addPtr(rightNode.removeLeftMostPtr());

			parent.setKey(delNodeIdx, borrowedKey);
		}

	}

	//delNode??? left sibling?????? merge
	private void mergeWithLeft(Node parent, int delNodeIdx)
	{
		Node delNode, leftNode;

		delNode = parent.getPtr(delNodeIdx);
		leftNode = parent.getPtr(delNodeIdx - 1);

		if (delNode.isLeaf)
		{
			int keyCnt = delNode.getKeyCnt();

			for (int i = 0; i < keyCnt; i++)
			{
				leftNode.addKey(delNode.getKey(i));
				leftNode.addRecordPtr(delNode.getRecordPtr(i));
			}

			leftNode.removeRightMostPtr();
			
			if (delNode.getPtrCnt() > 0)
				leftNode.addPtr(delNode.removeRightMostPtr());
		}
		else
		{
			int keyCnt = delNode.getKeyCnt();

			leftNode.addKey(parent.getKey(delNodeIdx - 1));

			for (int i = 0; i < keyCnt; i++)
			{
				leftNode.addKey(delNode.getKey(i));
				leftNode.addPtr(delNode.getPtr(i));
			}
			leftNode.addPtr(delNode.getRightMostPtr());
		}

		parent.removeKey(delNodeIdx - 1);
		parent.removePtr(delNodeIdx);
	}

	//right sibling??? delNode??? merge
	private void mergeWithRight(Node parent, int delNodeIdx)
	{
		Node delNode, rightNode;

		delNode = parent.getPtr(delNodeIdx);
		rightNode = parent.getPtr(delNodeIdx + 1);

		if (delNode.isLeaf)
		{
			int keyCnt = rightNode.getKeyCnt();

			for (int i = 0; i < keyCnt; i++)
			{
				delNode.addKey(rightNode.getKey(i));
				delNode.addRecordPtr(rightNode.getRecordPtr(i));
			}

			delNode.removeRightMostPtr();
			
			if (rightNode.getPtrCnt() > 0)
				delNode.addPtr(rightNode.removeRightMostPtr());
		}
		else
		{
			int keyCnt = rightNode.getKeyCnt();

			delNode.addKey(parent.getKey(delNodeIdx));

			for (int i = 0; i < keyCnt; i++)
			{
				delNode.addKey(rightNode.getKey(i));
				delNode.addPtr(rightNode.getPtr(i));
			}
			delNode.addPtr(rightNode.getRightMostPtr());
		}

		parent.removeKey(delNodeIdx);
		parent.setPtr(delNodeIdx + 1, delNode);
		parent.removePtr(delNodeIdx);
	}

	private boolean checkFileExists(String path)
	{
		File f = new File(path);

		if (!f.exists())
		{
			System.out.println("File missing error! - " + path);
			return false;
		}
		else
			return true;
	}

	void print()
	{
		if (root.isEmpty())
		{
			System.out.println("B+ tree is empty.");
			return;
		}

		int newlineCnt = 1;
		int sum = 0;

		Queue<Node> q = new LinkedList<Node>();
		q.add(root);

		Node node;
		while (!q.isEmpty())
		{
			node = q.poll();

			node.printKeys();
			System.out.printf(" | ");

			newlineCnt--;

			if (!node.isLeaf)
			{
				int cnt = node.getPtrCnt();
				sum += cnt;

				for (int i = 0; i < cnt; i++)
					q.add(node.getPtr(i));
			}

			if (newlineCnt == 0)
			{
				System.out.println();
				newlineCnt = sum;
				sum = 0;
			}
		}
	}

	boolean init(String indexpath) throws Exception
	{
		if (!checkFileExists(indexpath))
			return false;

		this.srz = new Serializer();

		srz.init(indexpath);

		return true;
	}

	void write(String indexpath) throws Exception
	{
		srz.write(indexpath);
	}

	private class DelReturn
	{
		boolean ret;
		int replaceKey;

		DelReturn(boolean ret, int replaceKey)
		{
			this.ret = ret;
			this.replaceKey = replaceKey;
		}

		DelReturn(boolean ret)
		{
			this.ret = ret;
			this.replaceKey = 0;
		}
	}

	private class KeyValPair
	{
		int key;
		int val;

		KeyValPair(int key, int val)
		{
			this.key = key;
			this.val = val;
		}
	}

	private class Serializer
	{
		void init(String indexpath) throws Exception
		{
			FileReader fr = new FileReader(indexpath);
			BufferedReader br = new BufferedReader(fr);

			degree = Integer.parseInt(br.readLine());
			
			String valueLine = br.readLine();

			if (valueLine.length() == 0)
			{
				values = new ArrayList<Integer>();
				deletedValIdx = new LinkedList<Integer>();
				root = new Node(degree);
				root.isLeaf = true;
				return;
			}

			String[] valueSplit = valueLine.split(",");
			
			values = new ArrayList<Integer>(valueSplit.length);

			for (String s : valueSplit)
				values.add(Integer.parseInt(s));

			String deletedValIdxLine = br.readLine();

			deletedValIdx = new LinkedList<Integer>();

			if (deletedValIdxLine.length() > 0)
			{
				String[] delValSplit = deletedValIdxLine.split(",");

				for (String s : delValSplit)
					deletedValIdx.add(Integer.parseInt(s));
			}

			Queue<Node> q = new LinkedList<Node>();

			root = new Node(degree);
			q.add(root);

			String isLeafStr;
			String keyStr;
			String recPtrStr;
			String[] keySplit;
			String[] recPtrSplit;
			Node node;

			ArrayList<Node> leaves = new ArrayList<Node>();

			while (!q.isEmpty())
			{
				node = q.poll();

				isLeafStr = br.readLine();

				if (isLeafStr.equals("1"))
					node.isLeaf = true;
				else
					node.isLeaf = false;

				keyStr = br.readLine();

				if (keyStr.length() > 0)
				{
					keySplit = keyStr.split(",");

					for (String s : keySplit)
						node.addKey(Integer.parseInt(s));
				}

				if (node.isLeaf)
				{
					recPtrStr = br.readLine();

					if (recPtrStr.length() > 0)
					{
						recPtrSplit = recPtrStr.split(",");
						
						for (String s : recPtrSplit)
							node.addRecordPtr(Integer.parseInt(s));
					}

					leaves.add(node);
				}
				else
				{
					int ptrCnt = node.getKeyCnt() + 1;

					Node child;
					for (int i = 0; i < ptrCnt; i++)
					{
						child = new Node(degree);
						node.addPtr(child);
						q.add(child);
					}
				}
			}

			int leafCnt = leaves.size();
			Node leaf;

			for (int i = 0; i < leafCnt - 1; i++)
			{
				leaf = leaves.get(i);
				leaf.addPtr(leaves.get(i + 1));
			}

			br.close();
		}

		void write(String indexpath) throws Exception
		{
			FileWriter fw = new FileWriter(indexpath);
			BufferedWriter bw = new BufferedWriter(fw);

			bw.write(Integer.toString(degree));
			bw.newLine();

			int valCnt = values.size();
			if (valCnt > 0)
			{
				int i;
				for (i = 0; i < valCnt - 1; i++)
					bw.write(Integer.toString(values.get(i)) + ",");
				bw.write(Integer.toString(values.get(i)));
			}
			bw.newLine();

			int delValCnt = deletedValIdx.size();
			if (delValCnt > 0)
			{
				int i;
				for (i = 0; i < delValCnt - 1; i++)
					bw.write(Integer.toString(deletedValIdx.poll()) + ",");
				bw.write(Integer.toString(deletedValIdx.poll()));
			}
			bw.newLine();

			if (root.isEmpty())
			{
				bw.write("1"); //leaf node
				bw.newLine();

				bw.newLine(); //empty keyList

				bw.newLine(); //empty recordPtrList

				bw.flush();
				bw.close();

				return;
			}

			Queue<Node> q = new LinkedList<Node>();
			q.add(root);

			Node node;
			while (!q.isEmpty())
			{
				node = q.poll();

				if (node.isLeaf)
					bw.write("1");
				else
					bw.write("0");
				bw.newLine();

				bw.write(node.keysToString());
				bw.newLine();

				if (node.isLeaf)
				{
					bw.write(node.recordPtrsToString());
					bw.newLine();
				}
				else
				{
					int cnt = node.getPtrCnt();

					for (int i = 0; i < cnt; i++)
						q.add(node.getPtr(i));
				}
			}

			bw.flush();
			bw.close();
		}
	}
}
