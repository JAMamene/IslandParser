package mamene;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class XMLAnalyzer {

    private Document doc;

    public XMLAnalyzer(String fileName) {
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            File inputFile = new File(classloader.getResource(fileName).getPath());
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public int getNbAction() {
        return doc.getElementsByTagName("turn").getLength();
    }

    public static void main(String[] args) {
        XMLAnalyzer xml = new XMLAnalyzer("islands.xml");
        System.out.println(xml.getNbAction());
    }
}