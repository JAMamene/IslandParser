package mamene;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;
import java.util.stream.DoubleStream;

public class XMLAnalyzer {

    public static final String ALL = "";
    private Document doc;

    public XMLAnalyzer(String fileName) {
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            File inputFile = new File(fileName);
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        XMLAnalyzer xml = new XMLAnalyzer("islands.xml");
        System.out.println(xml.getNbAction());
        System.out.println(xml.getMeanCostofAction("fly"));
        System.out.println(xml.getMeanCostofAction(ALL));
        System.out.println(xml.getSummary("fly"));
        System.out.println(xml.getSummary(ALL));
        System.out.println(xml.getTotalBudget());
        System.out.println(xml.getResourcesCollected());
    }

    public int getNbAction() {
        return doc.getElementsByTagName("turn").getLength();
    }

    public double getMeanCostofAction(String name) {
        return getActionCostAsStream(name).average().orElse(0);
    }

    public DoubleSummaryStatistics getSummary(String name) {
        return getActionCostAsStream(name).summaryStatistics();
    }

    public double getTotalBudget() {
        return getActionCostAsStream(ALL).sum();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Map<String, Integer> getResourcesCollected() {
        List<ResourceQuant> resourceQuants = new ArrayList<>();
        NodeList nodeList = doc.getElementsByTagName("action");
        for (int i = 0; i < nodeList.getLength(); i++) {
            //exploit syntax
            if (nodeList
                    .item(i)
                    .getAttributes()
                    .getNamedItem("type")
                    .getTextContent()
                    .equals("exploit")) {
                String resName = ((Element)
                        ((Element) nodeList.item(i))
                                .getElementsByTagName("resource")
                                .item(0))
                        .getAttribute("name");
                Node answer = nodeList.item(i).getNextSibling();
                while (!(answer instanceof Element) && answer != null) {
                    answer = answer.getNextSibling();
                }
                if (answer != null) {
                    int qty = Integer.parseInt(
                            ((Element) ((Element) answer)
                                    .getElementsByTagName("extras").item(0))
                                    .getElementsByTagName("amount")
                                    .item(0)
                                    .getTextContent()
                    );
                    resourceQuants.add(new ResourceQuant(resName, qty));
                }
            }
            //transform syntax
            else if (nodeList.item(i)
                    .getAttributes()
                    .getNamedItem("type")
                    .getTextContent()
                    .equals("transform")) {
                NodeList nodeList2 = ((Element) nodeList.item(i)).getElementsByTagName("resource");
                for (int j = 0; j < nodeList2.getLength(); ++j) {
                    String usedResName = ((Element) nodeList2.item(j)).getAttribute("name");
                    int qtytoDeduce = Integer.parseInt(((Element)
                            (nodeList2.item(j)))
                            .getElementsByTagName("amount").item(0).getTextContent());
                    resourceQuants.add(new ResourceQuant(usedResName, -qtytoDeduce));
                }
                Node answer = nodeList.item(i).getNextSibling();
                while (!(answer instanceof Element) && answer != null) {
                    answer = answer.getNextSibling();
                }
                if (answer != null) {
                    String name = ((Element)
                            ((Element) ((Element) answer).getElementsByTagName("extras").item(0))
                                    .getElementsByTagName("resource")
                                    .item(0))
                            .getAttribute("name");
                    int value = Integer.parseInt(
                            ((Element)
                                    ((Element) ((Element) answer).getElementsByTagName("extras").item(0))
                                            .getElementsByTagName("resource")
                                            .item(0))
                                    .getElementsByTagName("amount")
                                    .item(0)
                                    .getTextContent()
                    );
                    resourceQuants.add(new ResourceQuant(name, value));
                }
            }
        }
        return aggregate(resourceQuants);
    }

    private List<Node> getAllTurns() {
        List<Node> values = new ArrayList<>();
        NodeList nodeList = doc.getElementsByTagName("turn");
        for (int i = 0; i < nodeList.getLength(); i++) {
            values.add(nodeList.item(i));
        }
        return values;
    }

    private Double getCostForTurn(Node node) {
        return Double.parseDouble(
                ((Element) ((Element) node).getElementsByTagName("answer").item(0))
                        .getElementsByTagName("cost")
                        .item(0)
                        .getTextContent()
        );
    }

    private DoubleStream getActionCostAsStream(String name) {
        return getAllTurns()
                .stream()
                .filter(
                        node -> name.equals(ALL)
                                || (
                                ((Element) ((Element) node).getElementsByTagName("action").item(0))
                                        .getAttribute("type")
                                        .equals(name)
                        )

                )
                .mapToDouble(this::getCostForTurn);
    }

    private Map<String, Integer> aggregate(List<ResourceQuant> resourceQuants) {
        Map<String, Integer> hm = new HashMap<>();
        for (ResourceQuant rq : resourceQuants) {
            String name = rq.getResource();
            int value = hm.containsKey(name) ? hm.get(name) : 0;
            value += rq.getQuantity();
            hm.put(name, value);
        }
        return hm;
    }

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