package net.sf.janos.web.structure;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Element implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2967551636846192228L;
	private static final Log LOG = LogFactory.getLog(Element.class);
	private static final int INDENT_SIZE = 1;

	private String key, value;
	private boolean isSibling = false;

	private LinkedList<Element> elementchildren = new LinkedList<Element>();

	private Map<String, String> attributes = new HashMap<String, String>(0);

	/**
	 * 
	 * Use this constructor if you want to create a simple element with no child
	 * elements
	 * 
	 * @param key
	 *            the name of the element
	 * 
	 * @param value
	 *            the content of this element
	 * 
	 * */
	public Element(String key, String value) {
		this.key = key;
		this.value = value;
		this.isSibling = false;
	}
	
	public Element(String key, String value, boolean isSibling) {
		this.key = key;
		this.value = value;
		this.isSibling = isSibling;

	}

	/**
	 * 
	 * Use this constructor if you want to create a element that must have child
	 * 
	 * elements. Use addChild to add child elements.
	 * 
	 * @param key
	 *            the name of the element
	 * 
	 * */
	public Element(String key) {
		this.key = key;
		this.value = null;
		this.isSibling = false;
	}

	public Element(String key, boolean isSibling) {
		this.key = key;
		this.value = null;
		this.isSibling = isSibling;
	}

	
	/**
	 * Add a child to this element
	 * 
	 * @param child
	 *            the child element to be added
	 */

	public void addChild(Element child) {
		elementchildren.add(child);
	}

	/**
	 * Add a child to this element, let this child be the first child among the
	 * current elements
	 * 
	 * @param child
	 *            the child element to be added
	 */

	public void addChildFirst(Element child) {
		elementchildren.addFirst(child);
	}

	/**
	 * Add an attribute to this element
	 * 
	 * @param name
	 *            the name of the attribute
	 * 
	 * @param value
	 *            the value of the attribute
	 */

	public void addAttribute(String name, String value) {
		attributes.put(name, value);
	}
	
	public String getKey() {
		return key;
	}
	
	public String getValue() {
		return value;
	}
	
	public Map<String ,String> getAttributes() {
		return attributes;
	}
	
	public List<Element> getChildren() {
		return elementchildren;
	}
	
	/**Returns if this element is expected to have siblings of the same type
	 * @return true if this element has (or could have) siblings with the same key.*/
	public boolean isSibling() {
		return isSibling;
	}
	

	/**
	 * Write a string representation to an output stream
	 * 
	 * @param os
	 *            the outputstream
	 * 
	 * @param indent
	 *            the indentation of this element (for pretty printing)
	 */
	public void writeElementOld(Writer writer, int indent) {

		// BEGIN initialization
		char[] indentchars = new char[indent];
		for (int i = 0; i < indentchars.length; i++)
			indentchars[i] = ' ';
		// END initialization

		try {
			// BEGIN start element
			writer.write(indentchars);
			writer.write("<");
			writer.write(key);

			// BEGIN element attributes
			for (Entry<String, String> attrib : attributes.entrySet()) {
				writer.write(" ");
				writer.write(attrib.getKey());
				writer.write("=\"");
				writer.write(attrib.getValue());
				writer.write("\"");
			}
			// END element attributes

			writer.write(">");
			// END start element

			// BEGIN content
			if (value != null)
				writer.write(value);
			else {
				// BEGIN sub elements
				writer.write("\n");
				writer.flush();
				for (Element child : elementchildren) {
					child.writeElementOld(writer, indent + INDENT_SIZE);
				}
				writer.write(indentchars);
				// END sub elements

			}
			// END content

			// BEGIN end element
			writer.write("</");
			writer.write(key);
			writer.write(">\n");
			writer.flush();
			// END end element
		}

		catch (IOException ioe) {
			LOG.debug("IOException\n" + ioe.toString());
		}
	}
}