package batchprogs;

@SuppressWarnings("serial")
public class HtmlFinder extends URLsearch {
    public static final String HTML_EXTENSION = ".*[.]html";

    public HtmlFinder() {
        super(HTML_EXTENSION);
    }
}
