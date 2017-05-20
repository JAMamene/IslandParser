package conversion;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws ParserConfigurationException, TransformerException, IOException {
        if (args.length == 0 || !new File(args[0]).exists()) {
            System.out.println("Could not find json log input");
        }
        Converter c = new Converter(args[0]);
        c.convert();
        c.save();
        try {
            XMLAnalyzer analyzer = new XMLAnalyzer("output.xml");
            System.out.println("Nombre d'actions total : " + analyzer.getNbAction());
            System.out.println("Ressources obtenues : " + analyzer.getResourcesCollected());
            System.out.println("Statistiques d'action :" + analyzer.getSummary(""));
            System.out.println("Budget total des actions : " + analyzer.getTotalBudget());
        } catch (FileNotFoundException e) {
            System.out.println("Could not analyze XML file: islands.dtd or output.xml file missing.\n");
            e.printStackTrace();
        }
    }

}
