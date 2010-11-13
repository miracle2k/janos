package net.sf.janos.web.structure;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.Stack;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JSONFormatter extends Formatter {
	private static final Log LOG = LogFactory.getLog(Element.class);
	private final String indent = "  "; 
	private boolean condense = false;
	
	/**Constructs a new JSONFormatter for Elements
	 * @param condense Condense the output leaving out spaces and newlines*/
	public JSONFormatter(boolean condense) {
		this.condense = condense;
	}
	
	
	/** {@inheritDoc}
	 * */
	public void write(Writer writer, Element e) {
		try {
			writer.write("{\n" + indent + "\"" + e.getKey() + "\" : ");
			write(writer, e, 2);
			writer.write("\n}");
			writer.flush();
		} catch (IOException ioe) {
			LOG.debug("IOException\n" + ioe.toString());
		}
	}
	
	private void handleStack(Writer writer, LinkedList<Element> unwrchildren, int level) throws IOException {
		Element curchild = unwrchildren.remove();
		if (unwrchildren.size() == 0 && !curchild.isSibling()) {
			//single element
			for (int i=0; i<level && !condense; i++) {
				writer.write(indent);
			}
			writer.write("\"" + curchild.getKey() + "\" : ");
			write(writer, curchild, level + 1);
		} else if (unwrchildren.size() > 0 || curchild.isSibling()) {
			//array
			//writer.write("{\n");
			for (int i=0; i<level && !condense; i++) {
				writer.write(indent);
			}
			writer.write("\"" + curchild.getKey() + "\" : [\n");
			for (int i=0; i<level+1 && !condense; i++) {
				writer.write(indent);
			}
			write(writer, curchild, level +2);
			while (unwrchildren.size() > 0) {
				writer.write(",\n");
				curchild = unwrchildren.remove();
				for (int i=0; i<level+1 && !condense; i++) {
					writer.write(indent);
				}
				write(writer, curchild, level +2);
			}
			writer.write("\n");
			for (int i=0; i<level && !condense; i++) {
				writer.write(indent);
			}
			writer.write("]");
			//writer.write("}");
		}
	}
	
	/**writes the element, attributes and sub-elements or content in the format promised by the formatter
	 * @param writer the Writer to write to
	 * @param e the element
	 * @param level the level of the elements (in some formats the level from the root element is important)*/
	private void write(Writer writer, Element e, int level) {
		try {
			String value = e.getValue();
			// BEGIN content
			// Simple element is written as "key" : value or "key" : "value"
			if (value != null) {
				if (value.equals("true") || value.equals("false") ) {
					//boolean
					writer.write(value);
				} else if (value.matches("(-?[0-9]+(\\.[0-9]+((e|E)(-|\\+)[0-9]+)?)?)+")) { 
					//number (see http://json.org/)
					writer.write(value);
				} else {
					//string
					writer.write("\"" + createSafeJSONContentString(value) + "\"");
				}
			} else {
				//element with sub elements
				// BEGIN sub elements
				writer.write("{\n");
				LinkedList<Element> unwrchildren = new LinkedList<Element>();
				
				String lastkey = "";
				String thiskey = "";
				for (Element child : e.getChildren()) {
					thiskey = child.getKey();
					if (lastkey.equals(thiskey)) {
						//part of array that is being built
						unwrchildren.add(child);
					} else {
						//Array or single element is on the stack (unless it's the very first child)
						if (unwrchildren.size() > 0) {
							handleStack(writer, unwrchildren, level);
							writer.write(",\n");
						}
						unwrchildren.add(child);
					}
					lastkey = thiskey;
				}
				//When all children are traversed the last will be on the stack
				//handle that.
				if (unwrchildren.size() > 0) {
					handleStack(writer, unwrchildren, level);
					writer.write("\n");
				}
				// END sub elements

				//write end } for this element
				for (int i=0; i<level-1 && !condense; i++) {
					writer.write(indent);
				}
				writer.write("}");
			}
			// END content
			writer.flush();
			
			
			//TODO Add attribute handling to the JSON-formatter
			/*
			// BEGIN element attributes (property name @}
			code here
			// END element attributes
	*/

		
		} catch (IOException ioe) {
			LOG.debug("IOException\n" + ioe.toString());
		}
	}
	
	/**Creates a string that is safe to display as content in XML
	 * @param input the input string
	 * @param the safe version of the input string
	 * */
	private String createSafeJSONContentString(String input) {
		StringBuilder output = new StringBuilder();
		int numchars = input.length();
		char[] characters = new char[numchars];
		input.getChars(0, numchars, characters, 0);
		for (char character : characters) {
			if (character == '"') {
				output.append("\\\"");
			} else if (character == '\\') {
				output.append("\\\\");
			} else if (character == '/') {
				output.append("\\/");
			} else if (character == '\b') {
				output.append("\\b");
			} else if (character == '\f') {
				output.append("\\f");
			} else if (character == '\n') {
				output.append("\\n");
			} else if (character == '\r') {
				output.append("\\r");
			} else if (character == '\t') {
				output.append("\\t");
			} else {
				output.append(character);
			}
		}
		return output.toString();

	}

	/**{@inheritDoc}*/
	public void modifyResponseHeader(HttpServletResponse response) {
		response.setContentType("application/json");
	}
	

	
}