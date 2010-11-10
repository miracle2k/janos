package net.sf.janos.web.structure;

import java.io.IOException;
import java.io.Writer;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XMLFormatter extends Formatter {
	private static final Log LOG = LogFactory.getLog(Element.class);
	private final String indent = "  "; 
	private boolean condense = false;
	
	/**Constructs a new XMLFormatter for Elements
	 * @param condense Condense the output leaving out spaces and newlines*/
	public XMLFormatter(boolean condense) {
		this.condense = condense;
	}
	
	
	/** {@inheritDoc}
	 * */
	public void write(Writer writer, Element e) {
		try {
			writer.write("<?xml version=\"1.0\"?>\n");
			write(writer, e, 0);
		} catch (IOException ioe) {
			LOG.debug("IOException\n" + ioe.toString());
		}
	}
	
	
	/**writes the element, attributes and sub-elements or content in the format promised by the formatter
	 * @param writer the Writer to write to
	 * @param e the element
	 * @param level the level of the elements (in some formats the level from the root element is important)*/
	private void write(Writer writer, Element e, int level) {
		try {
			for (int i=0; i<level && !condense; i++) {
				writer.write(indent);
			}
			String key = e.getKey();
			// BEGIN start element
			writer.write("<");
			writer.write(key);

			// BEGIN element attributes
			for (Entry<String, String> attrib : e.getAttributes().entrySet()) {
				writer.write(" ");
				writer.write(attrib.getKey());
				writer.write("=\"");
				writer.write(createSafeXMLContentString(attrib.getValue()));
				writer.write("\"");
			}
			// END element attributes

			writer.write(">");
			// END start element	
			
			// BEGIN content
			if (e.getValue() != null)
				writer.write(createSafeXMLContentString(e.getValue()));
			else {
				// BEGIN sub elements
				if (!condense) {
					writer.write("\n");
				}
				writer.flush();
				for (Element child : e.getChildren()) {
					write(writer, child, level + 1);
				}
				// END sub elements

				//Indent for the end element
				for (int i=0; i<level && !condense; i++) {
					writer.write(indent);
				}
			}
			// END content

			// BEGIN end element
			writer.write("</");
			writer.write(key);
			writer.write(">");
			if (!condense) {
				writer.write("\n");
			}
			writer.flush();
			// END end element
		} catch (IOException ioe) {
			LOG.debug("IOException\n" + ioe.toString());
		}
	}
	/**Creates a string that is safe to display as content in XML
	 * @param input the input string
	 * @param the safe version of the input string
	 * */
	private String createSafeXMLContentString(String input) {
		StringBuilder output = new StringBuilder();
		int numchars = input.length();
		char[] characters = new char[numchars];
		input.getChars(0, numchars, characters, 0);
		for (char character : characters) {
			if (character == '&') {
				output.append("&amp;");
			} else if (character == '<') {
				output.append("&lt;");
			} else if (character == '>') {
				output.append("&gt;");
			} else if (character == '\"') {
				output.append("&quot;");
			} else if (character == '\'') {
				output.append("&#039;");
			} else {
				output.append(character);
			}
		}
		return output.toString();

	}


	/**{@inheritDoc}*/
	public void modifyResponseHeader(HttpServletResponse response) {
		response.setContentType("text/xml");
	}
	


}