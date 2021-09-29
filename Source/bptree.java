import java.util.*;
import java.io.*;

public class bptree 
{
	public static void main(String[] args) throws Exception
	{
		BPlusTree bt = new BPlusTree();

		if (args.length < 3)
		{
			System.out.println("Wrong arguments! Program exit.");
			return;
		}

		if (args[0].equals("-c"))
		{
			if (args.length != 3)
			{
				System.out.println("Wrong arguments! Program exit.");
				return;
			}

			bt.create(args[1], Integer.parseInt(args[2]));
		}
		else if (args[0].equals("-i"))
		{
			if (args.length != 3)
			{
				System.out.println("Wrong arguments! Program exit.");
				return;
			}

			if (bt.init(args[1]) == false)
			{
				System.out.println("Wrong index file! Program exit.");
				return;
			}
			bt.insert(args[2]);
			bt.write(args[1]);
		}
		else if (args[0].equals("-s"))
		{
			if (args.length != 3)
			{
				System.out.println("Wrong arguments! Program exit.");
				return;
			}

			if (bt.init(args[1]) == false)
			{
				System.out.println("Wrong index file! Program exit.");
				return;
			}
			bt.search(Integer.parseInt(args[2]));
		}
		else if (args[0].equals("-r"))
		{
			if (args.length != 4)
			{
				System.out.println("Wrong arguments! Program exit.");
				return;
			}

			if (bt.init(args[1]) == false)
			{
				System.out.println("Wrong index file! Program exit.");
				return;
			}
			bt.search(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
		}
		else if (args[0].equals("-d"))
		{
			if (args.length != 3)
			{
				System.out.println("Wrong arguments! Program exit.");
				return;
			}
			if (bt.init(args[1]) == false)
			{
				System.out.println("Wrong index file! Program exit.");
				return;
			}
			bt.delete(args[2]);
			bt.write(args[1]);
		}
		else
		{
			System.out.println("Wrong arguments! Program exit.");
		}
	}
}
