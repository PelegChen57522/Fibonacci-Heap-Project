/**
 * FibonacciHeap
 * 
 * User: pelegchen
 * ID: 314953159

 * An implementation of a Fibonacci Heap over integers.
 */
public class FibonacciHeap
{

		private HeapNode min;
		private HeapNode first;
		
		private int numTrees;
		
		private int size;
		private int countMarkNodes;

		
		private static int countLinks=0;
		private static int countCuts=0;

        private static double phi = (1 + Math.sqrt(5))/2; //The golden ratio

        
       /** 
        * Constructor of Fibonacci Heap!
        * 
        * public FibonacciHeap()
        * 
        * Initializing Fibonacci heap
        * 
        * Complexity: O(1)
        * 
        * **/
	public FibonacciHeap() {
			this.min=null;
			this.first=null;
			this.numTrees=0;
			this.size=0;
			this.countMarkNodes=0;
			
		}
	
	/**
     * public HeapNode getFirst()
     *
     * Return the First (most left and newest) node in the heap.
     *
     * Complexity: O(1)
     */
	
	public HeapNode getFirst() {
		return this.first;
	}
	
	/**
     * public int getSize()
     *
     * Return the number of nodes in the heap
     *
     * Complexity: O(1)
     */
		
	public int getSize() {
		return this.size;
	}
	
	/**
     * public int getNumTrees()
     *
     * Return the number of trees in the heap.
     *
     * Complexity: O(1)
     */

    public int getNumTrees() {
		return this.numTrees;
	}
    
    
    /**
     * public int getCountMarkNodes()
     *
     * Return the number of mark nodes.
     *
     * Complexity: O(1)
     */

    public int getCountMarkNodes() {
		return this.countMarkNodes;
	}
   /**
    * public boolean isEmpty()
    *
    * Returns true if and only if the heap is empty.
    *   
    * Complexity: O(1)  
    */
    public boolean isEmpty()
    {
    	if (this.size==0) {
    		return true;
    	}
    	return false;
    }
    
	
   /**
    * private replaceMin(HeapNode node)
    *
    * If argument node has minimal key, update min attr in Heap.
    * 
    * @pre: node is in heap, node.getParent() == null (node is root node).
    *
    * Returns true if min was replaced, false otherwise.
    */
    private boolean replaceMin(HeapNode node) {
    	if (this.min == null) {
    		this.min=node;
    		return true;
    	}
        if ((node.getKey() < this.min.getKey())) {
            this.min = node;
            return true;
        }
        return false;
    }

   /**
    * public HeapNode insert(int key)
    *
    * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap.
    * The added key is assumed not to already belong to the heap.  
    * 
    * Help functions: replaceMin
    * Complexity: O(1)
    *
    * Returns the newly created node.
    */
    public HeapNode insert(int key)
    {    
        HeapNode temp = new HeapNode(key);
        if (this.first != null) {
            this.first.insertBefore(temp);
        }
        this.first = temp;
        this.size++;
        this.numTrees++;
        this.replaceMin(temp);
    	return temp;
    }

    /**
    * private void removeMinNode()
    *
    * Only used for deleteMin. Remove the node with minimal key, add and connect it's children as heap roots.
    *
    * @pre: there is more than 1 node in Heap.
    * Help functions: HeapNode.resetMarkedInChain, HeapNode.nulifyParentInChain, HeapNode.insertBefore
    * Complexity: O(m), m - number of subtrees of root with minimum key (m = O(logn))
    */
    public void removeMinNode() {
        int children_amount;
        int reset_mark_amount;
        HeapNode minNode = this.min;
        if (this.numTrees == 1) { // If there is only one tree and heap has 2+ nodes therefore min has child.
            this.first = minNode.child;
            this.min = minNode.child;
            reset_mark_amount = this.min.ResetMarkedInChain();
            children_amount = this.min.nulifyParentInChain();
            this.numTrees = this.numTrees - 1 + children_amount;
            this.countMarkNodes = this.countMarkNodes - reset_mark_amount;
		    this.size--;
        }
        else { // If there 2+ trees, min can be a lone root.
           if (minNode.child == null) {
                if (minNode == this.first) {
                    this.first = this.min.next;
                }
           }
           else {
                reset_mark_amount = this.min.child.ResetMarkedInChain();
                children_amount = this.min.child.nulifyParentInChain();
                minNode.insertBefore(minNode.child, minNode.child.prev);
                if (minNode == this.first) {
                    this.first = minNode.child;
                }
                this.numTrees = this.numTrees + children_amount;
                this.countMarkNodes = this.countMarkNodes - reset_mark_amount;
           }
           HeapNode rightBrother = this.min.next;
           HeapNode leftBrother = this.min.prev;
           rightBrother.prev = leftBrother;
           leftBrother.next = rightBrother;
           this.min = this.first; //it doesn't matter who is considered min before we start consolidate, only that it is a root node
           this.numTrees--;
           this.size--;
        }
    }

    /**
    * private HeapNode consolidateConnect(HeapNode node1, HeapNode node2)
    *
    * Only used for consolidate. Add the node with the bigger key as left-most child of node with the smaller key.
    *
    * Complexity: O(1)
    *
    * Returns the HeapNode with the smaller key
    */
    private HeapNode consolidateConnect(HeapNode node1, HeapNode node2) {
        var minHeapNode = node1.key < node2.key ? node1 : node2;
		var maxHeapNode = node1.key > node2.key ? node1 : node2;
		if (minHeapNode.mark) {
	        minHeapNode.mark = false;
	        this.countMarkNodes--;
		}
        var temp = minHeapNode.getChild();
        minHeapNode.setChild(maxHeapNode);
        maxHeapNode.setParent(minHeapNode);
        // connect maxHeapNode to children
        if (temp == null) { //if there are no children
            maxHeapNode.setNext(maxHeapNode);
            maxHeapNode.setPrev(maxHeapNode);
        }
        else {
        	temp.insertBefore(maxHeapNode);
        }
        minHeapNode.setRank(minHeapNode.getRank()+1); //update rank because child was added
        countLinks++;
        return minHeapNode;
    }

    /**
    * public void consolidate()
    *
    * Consolidate trees by linking them with consolidateConnect so that we will have O(log(n)) trees, each with a different rank, using the "buckets" method we saw in lecture.
    *
    * Help functions: HeapNode.resetMarkedInChain, HeapNode.nulifyParentInChain, HeapNode.insertBefore
    * Complexity: O(k-1+m), k - number of trees in heap (before deletion of min used prior) / m - number of subtrees of root with minimum key (m = O(logn))
    *
    */
    public void consolidate() {
        HeapNode[] heapArr = new HeapNode[(int)(Math.log(this.size)/Math.log(FibonacciHeap.phi)) + 1]; // the "buckets" array
        HeapNode currNode = this.first;
        HeapNode tempNode;
        int currRank;
        int tempCount = this.numTrees;
        for (int i = 0; i < tempCount; i++) {
            tempNode = currNode;
            currNode = currNode.next;
            tempNode.next = tempNode;
            tempNode.prev = tempNode;
            currRank = tempNode.getRank();
            while (heapArr[currRank] != null) { // Incase there is tree in bucket connect them appropriately
                tempNode = consolidateConnect(heapArr[currRank], tempNode);
                this.numTrees--;
                heapArr[currRank] = null;
                currRank++;
            }
            heapArr[currRank] = tempNode;
            tempNode.setRank(currRank);
        }
        this.min=null;
        this.first=null;
        for (HeapNode heapNode : heapArr) {
            if (heapNode != null) { // go over the buckets and start inserting all trees back to the heap
                if (this.first==null) {
                    this.first = heapNode;
                }
                else {
                    this.first.insertBefore(heapNode);
                }
                replaceMin(heapNode);
            }
        }
    }

   /**
    * public void deleteMin()
    *
    * Deletes the node containing the minimum key. First we remove the minimum key with removeMinNode;
    * Then we consolidate and connect all trees using the method we saw in lecture with consolidate function.
    *
    * Help functions: removeMinNode, consolidate
    * Complexity: O(k-1+m), k - number of trees in heap / m - number of subtrees of root with minimum key (m = O(logn))
    *
    */
    public void deleteMin()
    {
        if (this.isEmpty()) {return;}
        if (this.size == 1) { //If empty no need to do anything
            this.min = null;
            this.first = null;
            this.size--;
            this.numTrees--;
            return;
        }
        this.removeMinNode();
        this.consolidate();

     	return;
    }

   /**
    * public HeapNode findMin()
    *
    * Returns the node of the heap whose key is minimal, or null if the heap is empty.
    * 
    * Complexity: O(1)
    *
    */
    public HeapNode findMin()
    {
    	return this.min;
    } 
    
   /**
    * public void meld (FibonacciHeap heap2)
    *
    * Melds heap2 with the current heap.
    *
    * Help functions: FibonacciHeap.insertBefore, replaceMin
    * Complexity: O(1)
    *
    */
    public void meld (FibonacciHeap heap2)
    {
        if (this.first != null && heap2.first != null) {
            HeapNode node1 = this.first;
            HeapNode node2 = this.first.getPrev();
            heap2.first.insertBefore(node1, node2);
            this.size = this.size + heap2.size;
            this.numTrees = this.numTrees + heap2.numTrees;
            this.countMarkNodes = this.countMarkNodes + heap2.countMarkNodes;
            this.replaceMin(heap2.min);
        }
        else if (this.first == null && heap2.first != null) {
            this.first = heap2.first;
            this.size = heap2.size;
            this.min = heap2.min;
            this.numTrees = heap2.numTrees;
            this.countMarkNodes = heap2.countMarkNodes;
        }
        		
    }

   /**
    * public int size()
    *
    * Returns the number of elements in the heap.
    *  
    * Complexity: O(1)
    */
    public int size()
    {
    	return this.size;
    }
    	
    /**
    * public int[] countersRep()
    *
    * Return an array of counters. The i-th entry contains the number of trees of order i in the heap.
    * (Note: The size of of the array depends on the maximum order of a tree.)  
    * 
    * 
    * Help functions:findMaxRank()
    * Complexity: O(n)
    * 
    */
    public int[] countersRep()
    {
        if (this.isEmpty()==true) {
        	int[] res = new int[0];
        	return res;
    	}
        else {
         // Create the array, loop over the root level and count the ranks.
            int[] res = new int[findMaxRank() + 1];
            HeapNode firstNode = this.first;
            res[firstNode.getRank()]++;
            HeapNode iterNode = firstNode.getNext();
            int currNodeRank;
            while(iterNode != firstNode) {
            	currNodeRank=iterNode.getRank();
                res[currNodeRank]++;
                iterNode = iterNode.getNext();
            }
            return res;

        }
    }
    
    
    /**
     *  public int findMaxRank()
     *
     * Return the max rank of the all trees in the heap
     * 
     * Complexity: O(n)
     * 
     */
    public int findMaxRank() {
    	  int maxRank = this.first.getRank();
 
          HeapNode iterNode = this.first.getNext();
          while(iterNode!=this.first) {
        	  maxRank=Math.max(maxRank, iterNode.getRank());
        	  iterNode=iterNode.getNext();
        	  }
          return maxRank;
    }
	
   /**
    * public void delete(HeapNode x)
    *
    * Deletes the node x from the heap.
	* It is assumed that x indeed belongs to the heap.
    *
    * Complexity: O(n)
    * 
    */
    public void delete(HeapNode x) 
  //implemented using Decrease-key and delete-min
    {    
    	//this is the min
    	if (this.size==1) {
    		this.min=null;
    		this.first=null;
    		this.numTrees--;
    		this.size--;
 
    	}
    	else {
    		this.decreaseKey(x,x.getKey() + 1 - this.min.getKey());
    		deleteMin();
    		
    	}

    }

   /**
    * public void decreaseKey(HeapNode x, int delta)
    *
    * Decreases the key of the node x by a non-negative value delta. The structure of the heap should be updated
    * to reflect this change (for example, the cascading cuts procedure should be applied if needed).
    * 
    * Help functions:cascadingCut()
    * 
    * Complexity:O(log(n))
    */
    public void decreaseKey(HeapNode x, int delta)
    {    
    //if x is root
    if (x.getParent()==null) {
    	x.setKey(x.getKey()-delta);
    }
    else {
    	x.setKey(x.getKey()-delta);
	    //check if there is violation
	    if (x.getKey()<x.getParent().getKey()) {
	    	cascadingCut(x,x.getParent()); //Complexity O(log(n)) //doesn't implement yet!
	    }
	   
	}
	    //check if after the decrease we have to change the current min
	if (x.getKey()<this.min.getKey()) {
		this.min=x;
	}
    }
   
    

   /**
    * public int nonMarked() 
    *
    * This function returns the current number of non-marked items in the heap
    * 
    * Complexity:O(1)
    * 
    */
    public int nonMarked() 
    {    
        return this.size-this.countMarkNodes;
    }

   /**
    * public int potential() 
    *
    * This function returns the current potential of the heap, which is:
    * Potential = #trees + 2*#marked
    * 
    * In words: The potential equals to the number of trees in the heap
    * plus twice the number of marked nodes in the heap. 
    * 
    * Complexity:O(1)
    */
    public int potential() 
    {    
        return this.numTrees+2*this.countMarkNodes;
    }

   /**
    * public static int totalLinks() 
    *
    * This static function returns the total number of link operations made during the
    * run-time of the program. A link operation is the operation which gets as input two
    * trees of the same rank, and generates a tree of rank bigger by one, by hanging the
    * tree which has larger value in its root under the other tree.
    * 
    * Complexity:O(log(n))
    */
    public static int totalLinks()
    {    
    	return countLinks;
    }

   /**
    * public static int totalCuts() 
    *
    * This static function returns the total number of cut operations made during the
    * run-time of the program. A cut operation is the operation which disconnects a subtree
    * from its parent (during decreaseKey/delete methods). 
    * 
    * Complexity:O(1)
    */
    public static int totalCuts()
    {    
    	return countCuts;
    }
    
    
    
    
    /**
     *  public void cascadingCut(HeapNode x,HeapNode xParent) 
     *
     * This is recursive function!
     * 
     * This function continues to make cascading cuts as long as the parent tree is marked. 
     * 
     * Help functions:cut()
     */
    
    
    
    public void cascadingCut(HeapNode x,HeapNode xParent) {
        cut(x,xParent);
        if(xParent.getParent()!=null) {
	        if(xParent.getMarked()==false) {
		        xParent.setMarked(true);
		        this.countMarkNodes++;
		    }
	        else {
	        	cascadingCut(xParent,xParent.getParent());
	        }
	        }
	    FibonacciHeap.countCuts++;
    }
    
    
    /**
     * public void cut(HeapNode x,HeapNode xParent) 
     *
     * Cuts node x from xParent , xParent its x's parent and adds it as a new tree.
     * 
     * Help functions:replaceMin()
     * 
     * Complexity:O(1)
     */

        public void cut(HeapNode x,HeapNode xParent) {
        if (xParent!=null) {
	        xParent.setRank(xParent.getRank()-1);
	        //x is single child
	        if (x.getNext()==x) {
	        	xParent.setChild(null);
	        }
	        else {
		        x.getPrev().setNext(x.getNext());
		        x.getNext().setPrev(x.getPrev());
	        }
	        // If x is parent's child
	        if (x == xParent.getChild()) {  
	        	xParent.setChild(x.getNext());
	        }
	        if (x.getMarked()==true) {
	        	x.setMarked(false);
	        	this.countMarkNodes--;
	        }
	        x.setParent(null);
	       
	       
	        //add to the trees
            if (this.first != null) {
                this.first.insertBefore(x);
            }
            this.first = x;
            this.numTrees++;
            this.replaceMin(x);
	
	        }
        }
    

     /**
    * public static int[] kMin(FibonacciHeap H, int k) 
    *
    * This static function returns the k smallest elements in a Fibonacci heap that contains a single tree.
    * The function should run in O(k*deg(H)). (deg(H) is the degree of the only tree in H.)
    *  
    * ###CRITICAL### : you are NOT allowed to change H. 
    * 
    * Help functions:isEmpty(),deleteMin(),findMin(),insert(),
    * 
    * Complexity:O(k*deg(H))
    */
    public static int[] kMin(FibonacciHeap H, int k)
    {  
    	int [] res= new int[k];
    	
    	//if H is empty Heap
    	if(H.isEmpty()==true) {
    		
    		return res;
    	}
    	
    	FibonacciHeap helperFib=new FibonacciHeap();

    	HeapNode minFibH=H.findMin();
    	HeapNode addedNode=helperFib.insert(minFibH.getKey());
    	addedNode.setKMinPointer(minFibH);
    	for (int i=0;i<k;i++) {
    		minFibH=helperFib.findMin();
    		res[i]=minFibH.getKey();
    		helperFib.deleteMin();
    		
    		HeapNode currMinHelpKMinPTR=minFibH.getKMinPointer();
    		if(currMinHelpKMinPTR.getChild()!=null) {
    			HeapNode currMinHelpKMinPTRSon=currMinHelpKMinPTR.getChild();
    			do {
    				addedNode=helperFib.insert(currMinHelpKMinPTRSon.getKey());
    				addedNode.setKMinPointer(currMinHelpKMinPTRSon);
    				currMinHelpKMinPTRSon=currMinHelpKMinPTRSon.getNext();
    			}
    			while (currMinHelpKMinPTRSon!=currMinHelpKMinPTR.getChild());
    				
    		}
    	}
    	return res;

     }

    
   /**
    * public class HeapNode
    * 
    * If you wish to implement classes other than FibonacciHeap
    * (for example HeapNode), do it in this file, not in another file. 
    *  
    */
    public static class HeapNode{

    	
    	private int key;
    	private int rank;
    	private boolean mark;
    	private HeapNode child;
    	private HeapNode parent;
    	private HeapNode next;
    	private HeapNode prev;
        private HeapNode KMinPointer;

    	
        /** 
         * Constructor of HeapNode Heap!
         * 
         * public HeapNode(int key)
         * 
         * Initializing heap node
         * 
         * Complexity: O(1)
         * 
         * **/
        
    	public HeapNode(int key) { 
    		this.key = key;
    		this.rank=0;
    		this.mark=false;
    		this.child=null;
    		this.parent=null;
    		this.next=this;
    		this.prev=this;
            this.KMinPointer = null;
    		
    	}
    	
    	/**
         * public int getKey()
         *
         * Return key of the node
         *
         * Complexity: O(1)
         */
    	
    	public int getKey() {
    		return this.key;
    	}
    	
    	/**
         * public void setKey(int k)
         *
         * Set k as the node's key
         *
         * Complexity: O(1)
         */
    	
    	public void setKey(int k) {
    		this.key=k;
    	}
    	
    	/**
         * public int getRank()
         *
         * Return the rank of the node
         *
         * Complexity: O(1)
         */
    	
     	public int getRank() {
    		return this.rank;
    	}
     	
     	/**
         * public void setRank(int k)
         *
         * Set k as the node's rank
         *
         * Complexity: O(1)
         */
    	
    	public void setRank(int k) {
    		this.rank=k;
    	} 
    	
    	/**
         * public boolean getMarked()
         *
         * Return true if the current node is marked else return false.
         *
         * Complexity: O(1)
         */
    	
    	public boolean getMarked() {
    		return this.mark;
    	}
    	
    	/**
         * public void setMarked(boolean TF)
         *
         * Set true if the node is marked else set false.
         *
         * Complexity: O(1)
         */
    	
    	public void setMarked(boolean TF) {
    		this.mark=TF;
    	}
    	
    	/**
         * public HeapNode getChild()
         *
         * Return the child of the node.
         *
         * Complexity: O(1)
         */
    	
    	public HeapNode getChild() {
    		return this.child;
    	}
    	
    	/**
         * public void setChild(HeapNode node)
         *
         * Set node as the child of the current node.
         *
         * Complexity: O(1)
         */
    	
    	public void setChild(HeapNode node) {
    		this.child=node;
    	}
    	
    	/**
         * public HeapNode getParent()
         *
         * Return the parent of the node.
         *
         * Complexity: O(1)
         */
    	
    	public HeapNode getParent() {
    		return this.parent;
    	}
    	
    	/**
         * public void setParent(HeapNode node)
         *
         * Set node as the parent of the current node.
         *
         * Complexity: O(1)
         */
    	
    	public void setParent(HeapNode node) {
    		this.parent=node;
    	}
    	
    	/**
         * public HeapNode getNext()
         *
         * Return the next node of the current.
         *
         * Complexity: O(1)
         */
    	
    	public HeapNode getNext() {
    		return this.next;
    	}
    	
    	/**
         * public HeapNode getFirst()
         *
         * Set node as the next node of the current.
         *
         * Complexity: O(1)
         */
    	
    	public void setNext(HeapNode node) {
    		this.next=node;
    	}
    	
    	/**
         * public HeapNode getPrev()
         *
         * Return the previous node of the current.
         *
         * Complexity: O(1)
         */
    	
    	public HeapNode getPrev() {
    		return this.prev;
    	}
    	
    	/**
         * public void setPrev(HeapNode node)
         *
         * Set node as the next node of the current.
         *
         * Complexity: O(1)
         */
    	
    	public void setPrev(HeapNode node) {
    		this.prev=node;
    	}
    	
    	/**
         * public HeapNode getKMinPointer()
         *
         * Return the KMinPointer. We use this in the function kMin(FibonacciHeap H, int k).
         *
         * Complexity: O(1)
         */

        public HeapNode getKMinPointer() {
    		return this.KMinPointer;
    	}
        
        /**
         * public void setKMinPointer(HeapNode node)
         *
         * Set node as the KMinPointer of the current.
         *
         * Complexity: O(1)
         */
        
        public void setKMinPointer(HeapNode node) {
    		this.KMinPointer=node;
    	}
        
        /**
         * private void insertBefore(HeapNode node)
         *
         * Add node x as a left sibling to instance node.
         * 
         * Complexity: O(1)
         */
        private void insertBefore(HeapNode node) {
            HeapNode temp = this.prev;
            node.next = this;
            this.prev = node;
            temp.next = node;
            node.prev = temp;
        }

        /**
         * private void insertBefore(HeapNode node1, HeapNode node2)
         *
         * Add a chain of nodes and connect them to instance node. The last node in the chain (node2) will be the left sibling of instance node.
         * 
         * Complexity: O(1)
         */
        private void insertBefore(HeapNode node1, HeapNode node2) {
            HeapNode temp = this.prev;
            node2.next = this;
            this.prev = node2;
            temp.next = node1;
            node1.prev = temp;
        }

        /**
         * private int nulifyParentInChain()
         *
         * Go over instance node and all it's sibling and change attribute 'parent' to null.
         * 
         * Returns the number of nodes in chain (k+1).
         * 
         * Complexity: O(k+1), k - number of siblings instance node has.
         */
        private int nulifyParentInChain() {
            HeapNode target = this;
            int count = 0;
            do {
                count++;
                target.parent = null;
                target = target.next;
            } while (target != this);
            return count;
        }

        /**
         * private int ResetMarkedInChain()
         *
         * Go over instance node and all it's sibling and change attribute 'mark' to 'false'.
         * 
         * Returns the number of nodes in chain in which 'mark' was set to 'true'.
         * 
         * Complexity: O(k+1), k - number of siblings instance node has.
         */
        private int ResetMarkedInChain() {
            HeapNode target = this;
            int count = 0;
            do {
                if (target.mark) {
                    count++;
                    target.mark = false;
                }
                target = target.next;
            } while (target != this);
            return count;
        }
        
    }

}
