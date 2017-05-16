package mamene;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * @author Guillaume Andre
 */
public class Main {

    public static void main(String[] args) throws IOException, ParserConfigurationException, TransformerException {
        Converter c = new Converter(args[0]);
        c.convert();
        c.save();
        XMLAnalyzer anal = new XMLAnalyzer("output.xml");
        System.out.println("Nombre d'actions: " + anal.getNbAction());
        System.out.println("Ressources collectées : " + anal.getResourcesCollected());
        System.out.println("Actions costs : " + anal.getMeanCostofAction(""));
        System.out.println("Summary :" + anal.getSummary(""));
        System.out.println("Budget Total: " + anal.getTotalBudget());
    }

}
