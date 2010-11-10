package net.sf.janos.web.structure;

import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

public abstract class Formatter {
	/**writes the element, attributes and sub-elements or content in the format promised by the formatter
	 * The element is assumed to be the root element.
	 * @param writer the Writer to write to
	 * @param e the element*/
	public abstract void write(Writer writer, Element e); // {
		//write(writer, e, 0);
	//}

	/**Gives the formatter a chance to change options in the response header, like content type
	* @param response the HttpServletResponse*/
	public abstract void modifyResponseHeader(HttpServletResponse response);
}