/* Authors: Bogdan Trigubov and Henry Mitchell
 * Date: June 4th, 2024
 * Program description: Program that builds huffmanTree coding  tree for file compression. */

package lab7;

import heap.Heap;
import avl.AVL;
import java.util.HashMap;
import java.util.Map;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.File;

public class Huffman {
  public Map<Character, Node> frequencyDict; // stores frequency of each char 
  public Heap<Node, Integer> frequencyHeap; // heap where frequency of each char is its priority
  public Node huffmanTree; // root node of huffmanTree
  public Map<Character, String> chartoBitMap; // stores map from char to its bitmap reprentation

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
      
    // build huffmantree
    Huffman huff = new Huffman();
    huff.countFrequencies(inputString);
    Map<Character, Node> frequencyDict = huff.frequencyDict;
    Heap<Node, Integer> frequencyHeap = huff.frequencyHeap;
    huff.BuildTree();

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
    System.out.println("Compression ratio: " + (encodedString.length() / inputString.length() / 8.0));
  }

  private static String getInputString(String filename) throws FileNotFoundException {
    try {
      String inputString = "";
      File file = new File(filename);
      Scanner sc = new Scanner(file);
      while (sc.hasNextLine()) {
        String line = sc.nextLine();
        inputString += line;
      }
      sc.close();
      return inputString;
    } catch (FileNotFoundException e) {
      throw e;
    }
  
  }

  public void countFrequencies(String string) {
    string.toLowerCase();
    frequencyHeap = new Heap<Node,Integer>();
    frequencyDict = new HashMap<Character,Node>(); // use frequency dict.put(char, int)
    char[] charArray = string.toCharArray();

    for (int i = 0; i < charArray.length; i++) { // go through all characters
      if (frequencyDict.containsKey(charArray[i])) { // if dictionary contains character increment its frequency
        char currentChar = charArray[i];
        Node currentNode = frequencyDict.get(charArray[i]);
        currentNode.priority = currentNode.priority + 1;
        frequencyHeap.changePriority(currentNode, currentNode.priority);
      } else { // if not add it to dict
        Node node = new Node(charArray[i], 1);
        frequencyDict.put(charArray[i], node);
        frequencyHeap.add(node, 1);
      }
    }
  }

  public void BuildTree() {
    this.chartoBitMap = new HashMap<Character,String>(); // build map from char to its corresponding bitcode
							 
    while (frequencyHeap.size() > 1) {
      Node rarest1 = frequencyHeap.poll(); // get nodes with lowest frequencies
      rarest1.addCode('0');
      Node rarest2 = frequencyHeap.poll();// get nodes with lowest frequencies
      rarest2.addCode('1');
      Node parent = new Node((rarest1.priority + rarest2.priority), rarest1, rarest2); // combine nodes with lowest frequencies
      frequencyHeap.add(parent, parent.priority); // add parent node to heap
    }
    frequencyHeap.peek().updateMap(chartoBitMap);
    huffmanTree = frequencyHeap.poll();
  }

  public String encode(String string) { // uses map for char to its corresponding bitcode to build bitcode for string
    char[] charArray = string.toCharArray();
    String bitcode = new String();
    for (char character : charArray) {
      bitcode += chartoBitMap.get(character);
    }
    return bitcode;
  }

  /* Alternative call for decode(string,Node) that assumes the passed node is huffmanTree
   * See decode(string,node) for spec) */
  public String decode(String string) {
    return decode(string, huffmanTree);
  }
  /* Decodes the passed bitcode into a string from the passed tree
   * Pre: bitcode is a valid code for huffmanTree at node tree */
  public String decode(String bitcode, Node tree) {
    /* Three cases:
     * 1: bitcode > 0 && character at node 'tree': add character and restart decoding with same bitcode from huffmanTree
     * 2: bitcode > 0 && no character: follow bitpath left or right (based on bitcode), return decode of the node left or right of 'tree'
     * 3: bitcode.length <= 0, return the character stored at tree */
    if (bitcode.length() > 0) {
      if (tree.character != null) {
        return tree.character + decode(bitcode, huffmanTree);
      } else {
        if (bitcode.charAt(0) == '0') {
	  return decode(bitcode.substring(1), tree.left);
	} else {
	  return decode(bitcode.substring(1), tree.right);
	}
      }
    } else {
      return tree.character + "";
    }
  }

  public class Node {
    public Character character;
    public int priority;
    public Node left;
    public Node right;
    public String bitcode; // given nodes bitcode representation of a string

    public Node(char character, int priority) { // constructor for a leaf node
      this.character = character;
      this.priority = priority;
      this.left = null;
      this.right = null;
      this.bitcode = "";
    }

    public Node(int priority, Node left, Node right) { // constructor for a parent node
      this.character = null;
      this.priority = priority;
      this.left = left;
      this.right = right;
    }

    public void addCode(char c) { // recursively construct bitcode
      if (left != null) {
        left.addCode(c);
        right.addCode(c);
      } else {
        bitcode = c + bitcode;
      }
    }

    public void updateMap(Map<Character, String> chartoBitMap) { // recursively update map
      if (left != null) {
        left.updateMap(chartoBitMap);
        right.updateMap(chartoBitMap);
      } else {
        chartoBitMap.put(character, bitcode);
      }
    }
  }
}
