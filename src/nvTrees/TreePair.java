package nvTrees;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;



/**
 * Holds a Tree Pair.
 * Tree Pair represents a map in the group nV,
 * and corresponds to a pattern pair. 
 * The map takes the labeled partitions defined
 * by the left tree and maps them linearly
 * to corresponding partitions in the right tree.
 * <br>
 * It is assumed that the leaves in the left tree
 * are labeled 1,2,..,n (order in which the leaves
 * are discovered in depth-first search)
 * 
 * @author Romwell
 *
 */
public class TreePair {

	/**
	 * The left tree in the pair
	 */
	public NvTree left_tree;
	
	/**
	 * The right tree in the pair
	 */
	public NvTree right_tree;
	
	/**
	 * Stores the permutation of the tree leaves
	 */
	private  TreePermutation permutation;
	
	/**
	 * Constructs a tree pair from a pair of trees and a permutation
	 * <ul>
	 * <li>leftTree is a string encoding of the left tree
	 * <li>rightTree is a string encoding of the right tree
	 * <li>permutation is the permutation of the leaves, written
	 * as number 1, 2, ...,n in some order separated by spaces
	 * </ul> 
	 * <br>Example: 12000,20100,2 3 1
	 * @param left_tree string representation of the left tree in the pair
	 * @param right_tree string representation of the right tree in the pair
	 * @param perm_string permutation string
	 */
	public TreePair(String left_tree, String right_tree, String perm_string) throws TreeNodeException
	{
		this.left_tree = new NvTree(left_tree);
		this.right_tree = new NvTree(right_tree);		   
		this.permutation = new TreePermutation(perm_string, this);		   
	}
	
	/**
	 * Constructs a tree pair from a string format
	 * <br>the string must have format S=leftTree,rightTree,permutation   where
	 * <ul>
	 * <li>leftTree is a string encoding of the left tree
	 * <li>rightTree is a string encoding of the right tree
	 * <li>permutation is the permutation of the leaves, written
	 * as number 1, 2, ...,n in some order separated by spaces
	 * </ul> 
	 * <br>Example: 12000,20100,2 3 1
	 * @param treePairString the string encoding of the treepair in the format
	 * <br><b>S=leftTree,rightTree,permutation string</b>;
	 * <br>see documentation for NvTree class for the tree string format reference. 
	 */
	public TreePair(String treePairString) throws TreeNodeException
	{
		if (treePairString.matches("random \\d+ \\d+"))
		{
			String param = treePairString.replaceAll("random ", "");			
			makeRandom(param.trim());
		}
		else
		try
		{
				StringTokenizer T = new StringTokenizer(treePairString,",");
				ArrayList<String> tokens = new ArrayList<String>();
				while (T.hasMoreTokens()) { tokens.add(T.nextToken());}
				if (tokens.size()< 2)
				{
					throw new TreeNodeException("Invalid format " 
							+"for a tree pair in String \n"+treePairString+"\n"
							+"The string format must be one of the following forms:\n "
							+"*   {left_tree,right_tree,permutation} \n"
							+"*   {left_tree,right_tree} \n"
							+"*   {random N C} \n");
				}
				this.left_tree = new NvTree(tokens.get(0));
				String rtreeStr = tokens.get(1);
				if ((rtreeStr.trim()).equals("same"))
				{
					this.right_tree = this.left_tree.duplicate();
				}
			else
			{
				this.right_tree = new NvTree(rtreeStr);
			}
			
			if (tokens.size()>2) //if user entered a permutation... 
			{
				this.permutation = new TreePermutation(tokens.get(2), this);
			}
			else
			{
				this.permutation = new TreePermutation("id", this);
			}
		}
		catch(NumberFormatException e)
		{
			throw new TreeNodeException("Invalid number format in tree pair string "+treePairString+ " : "+e.getMessage());
		}
	}	
	
	/**
	 * Makes this TreePair a random treepair, with N leaves an colors 1...C
	 * @param paramString a string containing N and C, in this order, separated by spaces 
	 */
	public void makeRandom(String paramString) throws TreeNodeException
	{
		if (paramString.matches("\\d+ \\d+"))
		{
		StringTokenizer T = new StringTokenizer(paramString);
		int N= Integer.valueOf(T.nextToken());
		int C = Integer.valueOf(T.nextToken());
		this.left_tree = new NvTree("random "+N+" "+C);
		this.right_tree = new NvTree("random "+N+" "+C);
		this.permutation = new TreePermutation("random", this);
		}
		else
		{
			throw new TreeNodeException("Invalid format string for random pair: " +
					"the string \""+paramString+"\" must have format \"N C\"");
		}
	}
	
	
	/**
	 * Constructs a tree pair from a TreePermutation (it already includes all information)
	 * @param perm_map the permutation map 
	 **/
	public TreePair(TreePermutation perm_map) throws TreeNodeException 
	{		
		this.left_tree = perm_map.left_tree;
		this.right_tree = perm_map.right_tree;		
		this.setPermutation(perm_map);
	}
	

	
		
	/**
	 * Adds a caret at a leaf node on the left tree. An excpetion is thrown if the node is not a leaf
	 * @param nodepath the path to the node to append a caret to 
	 * @param color the color to paint the node into (since it's not a leaf anymore)
	 */
	public void addCaretOnLeftTreeAt(String nodepath, int color) throws TreeNodeException
	{
		try
		{
			TreeNode node = left_tree.nodeByPath(nodepath);				
		if (!node.isLeaf())
		{
			throw new TreeNodeException("The node you are trying to append the caret to is not a leaf node");
		}
		else
		{
			SuperPath left_node, right_node; //Super Paths for the nodes that you're appending a caret to
			//add caret on the left tree
			addCaretAt(node, color);
			
			left_node =  node.getSuperpath();
			if (!permutation.containsKey(left_node))
			{
				throw new TreeNodeException("Permutation does not contain this node. Maybe you're tryingto append to right tree.");
			}
			right_node = new SuperPath(permutation.get(left_node));
			
//			fix the right tree	
			TreeNode rnode = right_tree.nodeBySuperPath(right_node);
			addCaretAt(rnode, color);

			SuperPath LL = new SuperPath(left_node); LL.appendDown(color, true);
			SuperPath LR = new SuperPath(left_node); LR.appendDown(color, false);
			SuperPath RL = new SuperPath(right_node); RL.appendDown(color, true);
			SuperPath RR = new SuperPath(right_node); RR.appendDown(color, false);
			
			permutation.remove(left_node);				
			permutation.put(LL, RL);
			permutation.put(LR, RR);
 				
			}		
		}
		catch(TreeNodeException e)
		{
			throw new TreeNodeException(e.errorString);
		}
	}
	
	/**
	 * Adds a caret at a node. Used internally and should not be called from outside,
	 * since it does not modify the node permutation.
	 * @param node node to add a caret to
	 * @param nodelabel new label assigned to the modified node
	 */
	private void addCaretAt(TreeNode node, int nodelabel) throws TreeNodeException
	{
		node.color = nodelabel;
		TreeNode l = new TreeNode(node,0,true);
		TreeNode r = new TreeNode(node,0,false);
		node.left = l;
		node.right = r;
	}

	/**
	 * Removes a caret at a node. Used internally and should not be called from outside,
	 * since it does not modify the node permutation.
	 * @param node node to remove a caret from
	 */
	private void removeCaretAt(TreeNode node) throws TreeNodeException
	{
		node.color = 0;
		node.left = null;
		node.right = null;
	}
	
	
	
	/**
	 * Adds a caret at a leaf node on the left tree. An exception is thrown if the node is not a leaf
	 * @param path_and_label String containing the path and the label, separated by space.
	 * <br>
	 * Call the invert() method to perform this operation on the right tree 
	 * Exception is thrown if the formatting is incorrect. Path must be a sequence of 0's and 1's;
	 * label must be an integer in 1..9 An exception is thrown if the tree does not contain the path.
	 */
	public void addCaretOnLeftTreeAt(String path_and_label) throws TreeNodeException, TreeNodeException
	{
	   	StringTokenizer t = new StringTokenizer(path_and_label); 
	   	if (t.countTokens()==2)
	   	{
	   		try {
	   		String path = t.nextToken();
	   		String label = t.nextToken();
	   		this.addCaretOnLeftTreeAt(path, Integer.valueOf(label));	   		
	   		}
	   		catch (NumberFormatException err)
	   		{
	   			throw new TreeNodeException("You must input an integer as node label: "+err.getMessage());	
	   		}
	   	}
	   	else
	   	{
	   		throw new TreeNodeException("Not Enough tokens: enter both path and label to append a caret");
	   	}		
	}

	
	/**
	 * Inverts this tree pair by switching the left and the right tree
	 * and inverting the permutation
	 */
	private void invert()
	{
		/* IMPORTANT!
		 * You MUST invert the permutation whenever you swap the left and right trees
		 */		
		NvTree temp = this.left_tree;
		this.left_tree = this.right_tree;
		this.right_tree = temp;
		this.permutation.invert();
	}

	
	
	
	
	
	/**
	 * Extends the left tree in the pair to a full "square" tree, i.e.
	 * a complete binary tree in which each color occurs the same 
	 * number of times on a root-leaf path.
	 * @param mindim minimal dimension of the tree, the highest label that will be used in the exapnsion.
	 * For instance, if the root-leaf path is 1211 and ndim=3, the expanded path
	 * will be 121122333  
	 * @param mindepth smallest number of times a color should appear on a root-leaf 
	 * path in the expansion
	 */
	public void extendLeftTree(int mindim, int mindepth) throws TreeNodeException
	{
		int [] colcount = this.left_tree.getColorDepths();
		int L=colcount.length-1;
		while ((L>0)&&(colcount[L]==0)) {L--;}
		L = Math.max(L,mindim);
		int D = Math.max(left_tree.maxColorDepth(), mindepth);
		int[] directive = new int[10];
		for (int i=1;i<=L;i++) {directive[i]=D;}		
		extendLeftTreeAt(left_tree.rootNode, directive);
	}

	
	/**
	 * Extends the left treeso that each color appears at least
	 * a certain amount of times
	 * @param colors array containing the minimum number of times each color should occur.
	 * Value in colors[i] = #times color i should occur, for i>0.
	 */
	public void extendLeftTree(int[] colors) throws TreeNodeException
	{
		int [] colcount = this.left_tree.getColorDepths();
		int [] M = max(colcount, colors);		
		extendLeftTreeAt(left_tree.rootNode, M);
	}
	
	
	/**
	 * Extends the subtree at a given node so that each color appears at least
	 * a certain amount of times
	 * @param node the root of the subtree to expand at
	 * @param colors array containing the minimum number of times each color should occur.
	 * Value in colors[i] = #times color i should occur, for i>0.
	 */
	private void extendLeftTreeAt(TreeNode node, int[]colors) throws TreeNodeException
	{
		colors[0]=0; //ignore color 0
		int C = firstPositive(colors);
		if (C>0)  //if we still have to add nodes of a certain color  
		{
			if (node.isLeaf())
			{	
				try{addCaretOnLeftTreeAt(node.path, C);}
				catch(TreeNodeException e)
				{
					throw new TreeNodeException(e.errorString);
				}
			}
			
			int c = node.color;
			int[] newcol = arrCopy(colors);
			newcol[c]=newcol[c]-1;
			extendLeftTreeAt(node.left, newcol);
			extendLeftTreeAt(node.right, newcol);
		}
	}
	
	
	/**
	 * Returns the largest integer in the array
	 * @param A the array to search
	 * @return the largest integer in A, or 0 if the array is empty
	 */
	public static int max(int[] A)
	{
		if (A.length == 0)
		{
			return 0;
		}
		else
		{
			int M = A[0];
			for (int i:A)
			{
				if (i>M) {M=i;}
			}
			return M;
		}
	}
	

	/**
	 * Returns the array with the larger element from the two arrays for each index
	 * @param A the first array
	 * @param B the other array
	 * @return array where ans[i]=max(a[i],b[i])
	 */
	public static int[] max(int[] A, int[]B) throws TreeNodeException
	{
		if (A.length != B.length)
		{
			throw new TreeNodeException("Arrays lengths do not match !");
		}
		else
		{
			int[] M = new int[A.length];
			for (int i=0;i<A.length;i++)
			{
				M[i]=Math.max(A[i], B[i]);
			}
			return M;
		}
	}
	
	/**
	 * Returns a copy of an integer array A
	 * @param A the array to copy
	 * @return a copy of A
	 */
	public static int[] arrCopy(int[] A)
	{
		int [] B = new int[A.length];
		for (int i=0;i<A.length;i++)
		{
			B[i]=A[i];
		}
		return B;
	}
	
	/**
	 * Returns the index of the first positive (nonzero) element in A
	 * @param A the array to search
	 * @return the index of the first nonzero element, or -1 if A is empty or none exists
	 */
	public static int firstPositive(int[] A)
	{
		if (A.length==0)
		{
			return -1;
		}
		else
		{
			int i=0;
			while ((i<A.length)&&(A[i]==0)){i++;}
			if (i<A.length)
			{
				return i;
			}
			else return -1;				
		}
	}
	
	/**
	 * Resets the permutation to identity permutation
	 *
	 */
	public void resetPermutation() throws TreeNodeException
	{
		this.permutation.reset();
	}

	/**
	 * Returns the permutation of the tree leaves associated with this tree pair
	 * @return the permutation
	 */
	public TreePermutation getPermutation() 
	{
		return permutation;
	}

	/**
	 * Sets the permutation for this pair. The permutation must be 
	 * @param permutation the permutation to set
	 */
	public void setPermutation(TreePermutation permutation) throws TreeNodeException 
	{
		/*
		 * FIXME
		 * check that the permutation's trees correspond to the pair's trees
		 * (the TreePermutation class, for various reasons, actually stores
		 *  references to the trees in the TreePair that has the 
		 *  TreePermutation)
		 */
		this.permutation = permutation;
	}
	
	
	/**
	 * Reduces the tree pair by removing exposed carets
	 *
	 */
	public void removeExposedCarets()
	{
		reduceLeftTreeAt(left_tree.rootNode);
	}
	
	
	
	/**
	 * Returns a new TreePair that is the reduced canonical representative
	 * of the map represented by this pair of trees.
	 * <br>
	 * In a canonical representative, the leaf-root paths are of smallest
	 * length and are lexicographically smallest (among representative).
	 * @return a new canonical treepair
	 */
	public TreePair reduce() throws TreeNodeException 
	{
		return reduce(true);
	}

	/**
	 * Returns a new TreePair that is the canonical representative
	 * of the map represented by this pair of trees.
	 * @param mergeBlocks if set to false, the returned canonical treepair
	 * is the pair in which the left pattern is the smallest grid.
	 * <p>
	 * If set to true, the unique smallest grid on the left is greedily reduced. 
	 * @return a new canonical treepair
	 */
	public TreePair reduce(boolean mergeBlocks) throws TreeNodeException 
	{
		extendToLeftFlat();
		HashMap<SuperPath, SuperPath> map = this.getPermutation().permutationMap;
		reduceGrid(map);
		if (mergeBlocks){ mergeBlocks(map);}
		NvTree Ltree = NvTree.fromPattern(map.keySet());
		NvTree Rtree = NvTree.fromPattern(map.values());
		TreePermutation perm = new TreePermutation(Ltree,Rtree,map);
		TreePair p = new TreePair(perm);
		return p;			
	}		
	
	/**
	 * Extends the left tree to a flat tree
	 *
	 */
	public void extendToLeftFlat() throws TreeNodeException
	{
		int[] Lcols = this.left_tree.getColorDepths();
		extendLeftTree(Lcols);				
	}
	
	/**
	 * This operation reduces a grid of blocks to a supergrid (a less finer grid).
	 * Since grid reductions commute, if you start from a grid representative,
	 * you get a unque reduced grid.
	 * @param map
	 * @throws TreeNodeException
	 */
	@SuppressWarnings("unchecked")
	public static void reduceGrid(Map<SuperPath, SuperPath>map) throws TreeNodeException
	{
		
		int curcol = 1;		
		while (curcol < NvTree.MAXCOL)
		{
			if (map.keySet().size() == 1) {return;}
			ArrayList<SuperPath> blocks= new ArrayList<SuperPath>(map.keySet());
			Collections.sort(blocks, new SuperPathComparator(curcol));
			ArrayList<SuperPath> mergeables = new ArrayList<SuperPath>();		
			boolean canmerge = true;
			while (canmerge&&(blocks.size()>0))
			{
				SuperPath LL = blocks.get(0);			
				SuperPath LR = blocks.get(1);
				if ((LL.isAdjacentTo(LR)==curcol)&&(areMergeable(LL, LR, map))) //if L is adjacent along curcol
				{				
					blocks.remove(0);
					blocks.remove(0);			//after the loop is done, if canmerge is true					
					mergeables.add(LL);			//then mergables has all the blocks
					mergeables.add(LR);			//and thus all the blocks can be merged along curcol
				}
				else {canmerge = false;}
			}
			if (canmerge)						
			{
				while (mergeables.size()>0)
				{
					SuperPath LL = mergeables.get(0);
					SuperPath LR = mergeables.get(1);
					SuperPath LM = new SuperPath(LL);
					SuperPath RL = map.get(LL);		
					SuperPath RM = new SuperPath(RL);
					LM.goUp(curcol);	//this is the merge of LL and LR
					RM.goUp(curcol);	//this is the merge of RM and RR			
					map.remove(LL);
					map.remove(LR);
					map.put(LM, RM);
					mergeables.remove(0);
					mergeables.remove(0);
				}
			}
			else
			{
				curcol++;
			}
		}
	}
	
	/**
	 * This code merges the blocks in a pattern pair color-greedily:
	 * it merges the blocks along the least dimension that's available. 
	 * I.e., it merges blocks along color 1 whenever possible; if not,
	 * tries color 2, then 3, etc. After each merge, the blocks are re-examined.
	 * NOTE that this may or may not yield a unque pattern, even when going down
	 * from a rectangular or square grid. We didn't prove yet that the algorithm will yield
	 * the same result when going down from finer / different grids. Also, the proof
	 * (and the algorithm) depends on how the SuperPathComparator is implemented
	 * (i.e., what order the blocks have when sorted); it REQUIRES that blocks
	 * adjacent along color i be adjacent in the list sort with SuperPathComparator(i).
	 * TODO
	 * -Optimize
	 * -Merge code with NvTree.fromPattern()
	 * @param  map the Map between two patterns that defines a pattern pair
	 * @throws TreeNodeException
	 */
	
	@SuppressWarnings("unchecked")
	public static void mergeBlocks(Map<SuperPath, SuperPath>map) throws TreeNodeException	
	{
		ArrayList<SuperPath> blocks= new ArrayList<SuperPath>(map.keySet());
		int i=0;
		int curcol = 1;		
		boolean found = false;
		boolean searchmore=true;
		if (blocks.size() < 2) {searchmore = false;}
		Collections.sort(blocks, new SuperPathComparator(curcol));
		while (searchmore)	
		{	 
			SuperPath LL = blocks.get(i);			
			SuperPath LR = blocks.get(i+1);
			if ((LL.isAdjacentTo(LR)==curcol)&&(areMergeable(LL, LR, map))) //if L is adjacent along curcol
			{				
				found = true;				  		//..then we have found a mergeable pair
				SuperPath LM = new SuperPath(LL);
				SuperPath RL = map.get(LL);		
				SuperPath RM = new SuperPath(RL);
				LM.goUp(curcol);	//this is the merge of LL and LR
				RM.goUp(curcol);				
				map.remove(LL);
				map.remove(LR);
				map.put(LM, RM);
				blocks.remove(LL);
				blocks.remove(LR);
				blocks.add(LM);
				Collections.sort(blocks, new SuperPathComparator(curcol));				
			}
			if (i<blocks.size()-2){i++;} else 
			{				
				if (!found)
				{
					curcol++;
					if (curcol<NvTree.MAXCOL)
					{
						Collections.sort(blocks, new SuperPathComparator(curcol));	
					} 
					else {searchmore=false;}//if not found anything in all colors, we're done 
					
				}
				else {curcol = 1; i=0;} //if a merge was made, we start all over.
				found = false;
				i=0;
			}
			if (blocks.size()==1) {searchmore = false;} //maybe we just merged everyting and got the identity
		}
	}
	
	
	/**
	 * Given two blocks A and B, tells whethe they can be merged into one
	 * (i.e. if they are adjacent and map to adjacent blocks in the same order
	 *  and dimension)
	 * @param A first block
	 * @param B second block
	 * @param map the map 
	 * @return true if you can merge A and B
	 */
	public static boolean areMergeable(SuperPath A, SuperPath B, Map<SuperPath, SuperPath> map) throws TreeNodeException
	{
		int curcol = A.isAdjacentTo(B);
		if ((curcol>0)&&(curcol < NvTree.MAXCOL)) //if A is adjacent to B
		{
			SuperPath RA = map.get(A);			
			SuperPath RB = map.get(B);
			if (RA.isAdjacentTo(RB)==curcol) //if the blocks that A and B map to are adjacent...					
			{
				SuperPathComparator comp = new SuperPathComparator(curcol);
				if (comp.compare(A, B)==comp.compare(RA, RB))   //...and the order (left/right top/bottom etc.) is preserved..
				{
					return true;
				}
			}
		}
		return false;
	}
	
	
	
	
	
	
	/**
	 * Reduces a subtree of a left tree
	 * @param node the root of the subtree of the left tree
	 */
	private void reduceLeftTreeAt(TreeNode node)
	{
		 if (!node.isLeaf())
		 {
			 reduceLeftTreeAt(node.left);
			 reduceLeftTreeAt(node.right);
			 removeCaretOnLeftTreeAt(node);
		 }
	}
	
	
	/**
	 * Attempts to reduce an exposed caret. 
	 * @param path path to the exposed caret
	 * @return true if the node was an exposed caret and was reduced; false otherwise 
	 */
	public boolean removeCaretOnLeftTreeAt(String path) throws TreeNodeException
	{
		TreeNode node = left_tree.nodeByPath(path);
		return removeCaretOnLeftTreeAt(node);
	}
	
	
	/**
	 * Attempts to reduce an exposed caret. 
	 * @param node supposed exposed caret
	 * @return true if the node was an exposed caret and was reduced; false otherwise 
	 */
	private boolean removeCaretOnLeftTreeAt(TreeNode node)
	{
		if (!isExposedLeftCaret(node))
		{
			return false;
		}
		else
		try 
		{
			SuperPath LLpath = node.left.getSuperpath();
			SuperPath LRpath = node.right.getSuperpath();
			SuperPath RLpath = permutation.get(LLpath);
			TreeNode RLnode = right_tree.nodeBySuperPath(RLpath);
			TreeNode R = RLnode.parent; //supposed common parent of RL and RR
			
			
			//fix permuation
			permutation.remove(LLpath);
			permutation.remove(LRpath);
			permutation.put(node.getSuperpath(), R.getSuperpath());
			
			//fix tree
			removeCaretAt(node);
			removeCaretAt(R);
			
			return true;
		}
		catch(TreeNodeException e)
		{
			JOptionPane.showMessageDialog(null, "Unknown error during tree reduction: \n"+e.errorString);
			return false;
		}

	}
	
	/**
	 * Tells whether the node is an exposed left caret
	 * @param path path to the node in the left tree
	 * @return true if the caret maps linearly into other caret, i.e.
	 * <br> 1 2 goes to  1 2
	 */
	public boolean isExposedLeftCaret(String path) throws TreeNodeException
	{
		TreeNode node = left_tree.nodeByPath(path);
		return isExposedLeftCaret(node);
	}
	
	
	/**
	 * Tells whether the node is an exposed left caret
	 * @param node node on the left tree
	 * @return true if the caret maps linearly into other caret, i.e.
	 * <br> 1 2 goes to  1 2
	 */
	private boolean isExposedLeftCaret(TreeNode node)
	{
		if (node.isLeaf())
		{
			return false;
		}
		else if (node.left.isLeaf()&&node.right.isLeaf())
		try
		{
			SuperPath LLpath = node.left.getSuperpath();     //Paths of LL and LR, the left and 
			SuperPath LRpath = node.right.getSuperpath();	 //right children of supposed caret
			SuperPath RLpath = permutation.get(LLpath);
			SuperPath RRpath = permutation.get(LRpath);
			TreeNode RLnode = right_tree.nodeBySuperPath(RLpath); //RL and RR are the images of LL and LR
			TreeNode RRnode = right_tree.nodeBySuperPath(RRpath);
			TreeNode R = RLnode.parent; //supposed common parent of RL and RR
			
			if ((R.left!=RLnode)||(R.right!=RRnode)) //check if they get mapped to the same node, i.e form a caret
			{
				return false;
			}
			else
			{
				return (R.color == node.color); //return true if the carets are of the same color 
			}
		}
		catch(TreeNodeException e)
		{
			JOptionPane.showMessageDialog(null, "Unknown error during tree reduction: \n"+e.errorString);
		}
		return false;
	}

	
	
	/**
	 * Makes the left trees in two pairs correspond to the same pattern
	 * <br>
	 * Current implementation: refine down to a flat rectangular tree
	 * (each path contains the same number of cuts in a given dimension)
	 * @param A a tree pair
	 * @param B another tree pair 
	 */
	public static void makeLeftTreesSame(TreePair A, TreePair B) throws TreeNodeException
	{
		A.removeExposedCarets();
		B.removeExposedCarets();
		
		int[] Adepth = A.left_tree.getColorDepths();
		int[] Bdepth = B.left_tree.getColorDepths();
		int[] Mdepth = TreePair.max(Adepth, Bdepth);
		
		A.extendLeftTree(Mdepth);
		B.extendLeftTree(Mdepth);
	}


	/**
	 * Returns the product of A and B
	 * (in the function composition sense)
	 * @param A a tree pair
	 * @param B another tree pair to multiply by
	 * @return A*B [right action] 
	 */
	public static TreePair multiply(TreePair A, TreePair B) throws TreeNodeException
	{
		A = A.duplicate();
		B = B.duplicate(); //necessary to avoid modification of original trees
		A.invert();
		TreePair.makeLeftTreesSame(A, B);
		A.invert();
		TreePermutation perm = TreePermutation.multiply(A.getPermutation(), B.getPermutation());		
		A.removeExposedCarets();
		B.removeExposedCarets();
		TreePair result = new TreePair(perm);
		return result.reduce();
	}
	
	
	/**
	 * Returns a duplicate of this tree pair
	 * @return a deep copy of the tree pair
	 */
	public TreePair duplicate() throws TreeNodeException
	{
		return new TreePair(left_tree.toString()+","+right_tree.toString()+","+permutation.toString());
	}
	
	/**
	 * Raises a tree pair to a power (repeatedly multiplies by itself n times)
	 * @param A a tree pair to raise to a power
	 * @param n power to raise A to; must be a positive integer 
	 * @return A*A*A*...*A (n times)
	 * @throws an exception is thrown if $n$ is not a positive integer.
	 */
	public static TreePair power(TreePair A, int n) throws TreeNodeException
	{
		TreePair a = A.duplicate();		//these lines implement negaive powers
		if (n<0)						//A^-n = (a=inverse of A)^n
		{
			a.invert();
			n = -n;
		}
		TreePair ans = new TreePair("0,0,1");
		for (int i=0;i<n;i++)
		{
			ans = multiply(ans, a);
		}
		return ans;
	}
	
	/**
	 * Tells whether the order of an element does not exceed a certain number.
	 * <br>
	 * The method calculates A^1, A^2, A^3, ... A^maxorder and stops when an element is 
	 * equal to the identity.  
	 * @param A a treepair to get the order for
	 * @param maxorder maximum order to go to
	 * @return n such that A^n =1 or -1 if such n is greater than maxorder. 
	 */
	public static int order(TreePair A, int maxorder) throws TreeNodeException
	{
		if (maxorder>0)
		{
			TreePair ans = new TreePair("0,0,1");		
			for (int i=0;i<maxorder;i++)
			{				
				ans = multiply(ans, A);
				if (ans.numBlocks()==1)
				{
					return (i+1);
				}
			}
			return -1;
		}
		else
		{
			throw new TreeNodeException("You specified an invalid Maximum Order: "+maxorder);
		}		
	}
	
	/**
	 * Returns the number of leaves in a tree of this tree pair 
	 * @return number of leaves = number of blocks in pattern 
	 */
	public int numBlocks()
	{
		return this.permutation.size();
	}

	
	/**
	 * Returns the string representation of this treepair
	 */
	public String toString()
	{
		return left_tree.toString()+","+right_tree.toString()+","+permutation.toString();
	}
	
	
	/**
	 * Returns A conjugated by B [right action]
	 * @param A
	 * @param B
	 * @return B^-1 * A * B 
	 */
	public static TreePair conjugate(TreePair A, TreePair B) throws TreeNodeException
	{
		   if ((A!=null)&&(B!=null))
		   {
			   TreePair B1 = B.duplicate();
			   B1.invert();
			   return TreePair.multiply(B1, TreePair.multiply(A, B));
		   }
		   else
		   {
			   throw new TreeNodeException("Conjugation failed: some arguments are null");   
		   }		
	}
	
	/**
	 * Returns the commutator of A and B [right action]
	 * @param A a tree pair
	 * @param B another tree pair 
	 * @return B^-1 * A^-1 * B * A = B^-1 * B^A [right action] 
	 */
	public static TreePair commutator(TreePair A, TreePair B) throws TreeNodeException
	{
		   if ((A!=null)&&(B!=null))
		   {
			   TreePair B1 = inverseOf(B);
			   return TreePair.multiply(B1, TreePair.conjugate(B, A));
		   }
		   else
		   {
			   throw new TreeNodeException("Commutator failed: some arguments are null");   
		   }		
	}

	/**
	 * Returns a treepair corresponding to the inverse of a map defined by a given treepair
	 * @param A a tree pair 
	 * @return A^-1
	 */
	public static TreePair inverseOf(TreePair A) throws TreeNodeException 
	{
			TreePair B = A.duplicate();
			B.invert();
			return B;
	}
	
}