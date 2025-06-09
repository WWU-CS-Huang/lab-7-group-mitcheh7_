/* Authors: Bogdan Trigubov and Henry Mitchell
 * Date: June 4th, 2024
 * Program description: Program that builds huffmanTree coding tree for file compression. */

package lab7;

import heap.Heap;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.io.File;
import java.lang.StringBuilder;

public class Huffman {
  private Map<Character, String> chartoBitMap; // stores map from char to its bitmap reprentation
  private Map<Character, Node> frequencyDict; // stores frequency of each char 
  private Heap<Node, Integer> frequencyHeap; // heap where frequency of each char is its priority
  private Node huffmanTree; // root node of huffmanTree

  public static void main(String[] args) throws FileNotFoundException {
    // read file
    String filename = args[0];
    String inputString;
    try {
      inputString = getInputString(filename);
    } catch (FileNotFoundException e) {
      System.out.println("Please specify valid filename via command line: " + e.getMessage());
      throw e;
    }
      
    // build huffmanTree
    Huffman huff = new Huffman();
    huff.buildTree(inputString);

    // encode and decode strings
    String encodedString = huff.encode(inputString);
    String decodedString = huff.decode(encodedString);

    // print output (encoded/decoded/input strings only printed if input length is < 100)
    if (inputString.length() < 100) {
      System.out.println("Input string: " + inputString);
      System.out.println("Encoded string: " + encodedString);
      System.out.println("Decoded string: " + decodedString);
    }
    System.out.println("Decoded equals input: " + inputString.equals(decodedString));
    System.out.println("Compression ratio: " + ((float)encodedString.length() / (float)inputString.length() / 8.0));
  }


  /* returns a string representing the file at filename
   * Pre: filename is a valid file at app/filename */
  private static String getInputString(String filename) throws FileNotFoundException {
    try {
      StringBuilder strb = new StringBuilder();
      File file = new File(filename);
      Scanner sc = new Scanner(file);

      if (sc.hasNextLine()) {
        strb.append(sc.nextLine());
      }
      while (sc.hasNextLine()) {
	strb.append("\n"); //adding "\n" is necessary since hasNextLine() means there was a "\n" before the next line
        strb.append(sc.nextLine());
      }
      
      sc.close();
      return strb.toString();
    } catch (FileNotFoundException e) {
      throw e;
    }
  }

  /* initializes Huffman variables except for huffmanTree Node */
  public Huffman () {
    chartoBitMap  = new HashMap<Character,String>();// map from character to its corresponding bitcode
    frequencyDict = new HashMap<Character,Node>();  // map from character to its corresponding node
    frequencyHeap = new Heap<Node,Integer>();       // heap storing node/frequency pairs
  }


  /* Builds a huffman coding tree from inputString
   * Pre: inputString != null
   * Post: huffmanTree is the root node of a huffman coding tree for inputString */
  public void buildTree(String inputString) {
    countFrequencies(inputString); //populates frequencyHeap
							 
    while (frequencyHeap.size() > 1) {
      Node rarest1 = frequencyHeap.poll(); // get nodes with lowest frequencies
      Node rarest2 = frequencyHeap.poll(); // get nodes with lowest frequencies
      Node parent = new Node(rarest1, rarest2); // combine nodes with lowest frequencies
      frequencyHeap.add(parent, parent.priority); // add parent node to heap
    }
    huffmanTree = frequencyHeap.poll(); // last node in heap is the root of a huffman coding tree
    huffmanTree.updateMap(chartoBitMap, ""); // add bitcodes to all leaf nodes
  }

  /* counts the frequency of each character in string, storing it in frequencyHeap
   * Pre: string != null */
  private void countFrequencies(String string) {
    char[] charArray = string.toCharArray();

    for (int i = 0; i < charArray.length; i++) { // go through all characters
      char c = charArray[i];
      if (frequencyDict.containsKey(c)) { // if dictionary contains character increment its frequency
        Node currentNode = frequencyDict.get(c);
        currentNode.priority += 1;
        frequencyHeap.changePriority(currentNode, currentNode.priority);
      } else { //else add it to dict
        Node node = new Node(c, 1);
        frequencyDict.put(c, node);
        frequencyHeap.add(node, 1);
      }
    }
  }
  
  /* uses map for char to its corresponding bitcode to build bitcode for string 
   * Pre: string != null, tree is built */
  public String encode(String string) {
    StringBuilder strb = new StringBuilder();
    char[] charArray = string.toCharArray();
    int strLeng = string.length();

    for (int i = 0; i < strLeng; i++) {
      strb.append(chartoBitMap.get(charArray[i]));
    }
    return strb.toString();
  }

  /* Decodes the passed bitcode into a string from the passed tree
   * Pre: bitcode is a valid code for huffmanTree at node tree */
  private String decode(String bitcode) {
    StringBuilder decoded = new StringBuilder();
    Node tree = huffmanTree;
    char[] bitArr = bitcode.toCharArray();
    int length = bitArr.length;
    int i = 0;

    while (i < length) { //while there is bitcode after index i
      if (tree.character != null) { //if this is a leaf, add character and restart from root
	decoded.append(tree.character);
        tree = huffmanTree;
      } else {
        if (bitArr[i] == '0') { //else advance i and decode left or right based on current bit
	  i++;
	  tree = tree.left;
	} else {
	  i++;
	  tree = tree.right;;
	}
      }
    }
    decoded.append(tree.character);
    return decoded.toString();
  }

  public class Node {
    public Character character;
    public int priority;
    public Node left;
    public Node right;

    /* constructor for a leaf node
     * Pre: priority != null */
    public Node(char character, int priority) {
      this.character = character;
      this.priority = priority;
      this.left = null;
      this.right = null;
    }

    /* constructor for a parent node
     * Pre: left != null, right != null, childrens' priorities are set and correct */
    public Node(Node left, Node right) {
      this.character = null;
      this.priority = left.priority + right.priority;
      this.left = left;
      this.right = right;
    }

    /* Recursively updates chartoBitMap for this node and any children it has 
     * Pre: chartoBitMap != null, bitcode is the bitcode for this node */
    public void updateMap(Map<Character, String> chartoBitMap, String bitcode) {
      if (left != null) {
        left.updateMap(chartoBitMap, bitcode + '0');
        right.updateMap(chartoBitMap, bitcode + '1');
      } else {
        chartoBitMap.put(character, bitcode);
      }
    }
  }
}
