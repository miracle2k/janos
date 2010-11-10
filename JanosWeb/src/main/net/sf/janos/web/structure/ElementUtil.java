package net.sf.janos.web.structure;


public class ElementUtil {
	private static final Element succes = new Element("succes", "true");
	private static final Element failure = new Element("succes", "false");

	/**Get a simple Element representation of a success-element with content "true"
	 * @return the Element representation of a success-element with content "true"
	 * */
	public static Element getStatusSuccesElement() {
		return succes;
	}

	/**Get a simple Element representation of a success-element with content "false"
	 * @return the Element representation of a success-element with content "false"
	 * */
	public static Element getStatusFailureElement() {
		return failure;
	}
	
	
	/**Create a response tag, made for containing sub-element (children)
	 * @return the Element representation of a response element*/
	public static Element createResponse() {
		return new Element("response");
	}
	

	/**Create a failure tag, containing 3 tags with textual content:
	 * @param message The message that will be contained in the <message>-element
	 * @param detail The detail-text that will be contained in the <detail>-element
	 * @param classname The classname that will be contained in the <classname>-element (for debugging purposes)
	 */
	public static Element createFailureElement(String message, String detail, String classname) {
		Element failure = new Element("failure");
		Element msgtag = new Element("message", message);
		Element dettag = new Element("detail", detail);
		Element clstag = new Element("classname", classname);
		failure.addChild(msgtag);
		failure.addChild(dettag);
		failure.addChild(clstag);
		return failure;
	}
}
