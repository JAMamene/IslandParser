package conversion;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.DoubleStream;

public class XMLAnalyzer {

    public static final String ALL = "";
    private Document doc;

    /**
     * Builds a Document with the xml file provided
     *
     * @param fileName the file
     */
    public XMLAnalyzer(String fileName) throws FileNotFoundException {
        try {
            File inputFile = new File(fileName);
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
        } catch (FileNotFoundException e) {
            throw e;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////// PUBLIC METHODS //////////////////////////////////////////////////

    /**
     * get the number of actions done by the bot
     * @return the number of actions
     */
    public int getNbAction() {
        return doc.getElementsByTagName("turn").getLength();
    }
    /**
     * returns some statistics on the cost of an action or all actions
     *
     * @param name action to compute statistics on ("" for all actions)
     * @return a few statistics on the resource at hand
     */
    public DoubleSummaryStatistics getSummary(String name) {
        return getActionCostAsStream(name).summaryStatistics();
    }

    /**
     * Build a stream with the cost of actions
     *
     * @param name action to stream ("" for all actions)
     * @return a stream of actioncost (double)
     */
    private DoubleStream getActionCostAsStream(String name) {
        return getAllTurns()
                .stream()
                .filter(node -> name.equals(ALL) || (((Element) ((Element) node).getElementsByTagName("action").item(0)).getAttribute("type").equals(name)))
                .mapToDouble(this::getCostForTurn);
    }

    /**
     * returns all turns as a list
     *
     * @return a list of nodes from the turns
     */
    private List<Node> getAllTurns() {
        List<Node> values = new ArrayList<>();
        NodeList nodeList = doc.getElementsByTagName("turn");
        for (int i = 0; i < nodeList.getLength(); i++) {
            values.add(nodeList.item(i));
        }
        return values;
    }

    /////////////////////////////////////////////////// PRIVATE METHODS ////////////////////////////////////////////////

    /**
     * return the total cost of all actions
     *
     * @return double total cost
     */
    public double getTotalBudget() {
        return getActionCostAsStream(ALL).sum();
    }

    /**
     * get all resources collected as a map
     *
     * @return a map of resources and the number we got
     */
    public Map<String, Integer> getResourcesCollected() {
        List<ResourceQuant> resourceQuants = new ArrayList<>();
        NodeList nodeList = doc.getElementsByTagName("action");
        // for each action node
        for (int i = 0; i < nodeList.getLength(); i++) {
            // syntax for exploit action
            if (nodeList.item(i).getAttributes().getNamedItem("type").getTextContent().equals("exploit")) {
                //get the resource name from the action
                String resName = ((Element) ((Element) nodeList.item(i)).getElementsByTagName("resource").item(0)).getAttribute("name");
                //get the answer
                Node answer = nodeList.item(i).getNextSibling();
                while (!(answer instanceof Element) && answer != null) {
                    answer = answer.getNextSibling();
                }
                if (answer != null) {
                    //get the resource quantity from the answer
                    int qty = Integer.parseInt(((Element) ((Element) answer).getElementsByTagName("extras").item(0)).getElementsByTagName("amount").item(0).getTextContent());
                    resourceQuants.add(new ResourceQuant(resName, qty));
                }
            }
            //transform syntax
            else if (nodeList.item(i).getAttributes().getNamedItem("type").getTextContent().equals("transform")) {
                //get the used resources names from the action
                NodeList nodeList2 = ((Element) nodeList.item(i)).getElementsByTagName("resource");
                for (int j = 0; j < nodeList2.getLength(); ++j) {
                    //get name of resource from the action
                    String usedResName = ((Element) nodeList2.item(j)).getAttribute("name");
                    //get quantity
                    int qtytoDeduce = Integer.parseInt(((Element)
                            (nodeList2.item(j)))
                            .getElementsByTagName("amount").item(0).getTextContent());
                    //add to the map the quantity as negative (used for crafting)
                    resourceQuants.add(new ResourceQuant(usedResName, -qtytoDeduce));
                }
                //get the answer
                Node answer = nodeList.item(i).getNextSibling();
                while (!(answer instanceof Element) && answer != null) {
                    answer = answer.getNextSibling();
                }
                //get name of resource crafted and quantity from the answer
                if (answer != null) {
                    Element resource = (Element) ((Element) ((Element) answer).getElementsByTagName("extras").item(0)).getElementsByTagName("resource").item(0);
                    String name = resource.getAttribute("name");
                    int value = Integer.parseInt(resource.getElementsByTagName("amount").item(0).getTextContent());
                    resourceQuants.add(new ResourceQuant(name, value));
                }
            }
        }
        // compile all the list of resource and quantity into a map
        return aggregate(resourceQuants);
    }

    /**
     * private method to aggregate resources by their quantity
     *
     * @param resourceQuants pairs of resource and quantity to aggregate
     * @return a map of all resources collected and their quantity
     */
    private Map<String, Integer> aggregate(List<ResourceQuant> resourceQuants) {
        Map<String, Integer> hm = new HashMap<>();
        for (ResourceQuant rq : resourceQuants) {
            String name = rq.getResource();
            int value = hm.getOrDefault(name, 0);
            value += rq.getQuantity();
            hm.put(name, value);
        }
        return hm;
    }

    /**
     * private method to get the cost of a turn
     *
     * @param node the turn node
     * @return the cost of the action
     */
    private Double getCostForTurn(Node node) {
        return Double.parseDouble(
                ((Element) ((Element) node).getElementsByTagName("answer").item(0)).getElementsByTagName("cost").item(0).getTextContent()
        );
    }

    /**
     * Private class to store resources and quantity for computations
     */
    private class ResourceQuant {

        private String resource;
        private int quantity;

        public ResourceQuant(String resource, int quantity) {
            this.resource = resource;
            this.quantity = quantity;
        }

        public String getResource() {
            return resource;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}