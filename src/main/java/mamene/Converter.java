
package mamene;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class Converter {

    private JSONArray mainArray;
    private Document document;
    private Element root;

    public Converter(String filename) throws IOException {
        mainArray = new JSONArray(new String(Files.readAllBytes(Paths.get(filename))));
    }

    public void convert() throws ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        document = builder.newDocument();
        root = document.createElement("log");
        document.appendChild(root);
        convertInitial();
        Element actions = document.createElement("actions");
        root.appendChild(actions);
        for (int i = 1; i < mainArray.length(); i += 2) {
            JSONObject actionJson = mainArray.getJSONObject(i);
            JSONObject answer = mainArray.getJSONObject(i + 1);
            convertAction(actionJson, answer, actions);
        }
    }

    public void save() throws IOException, TransformerException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("output.xml"));
        DOMSource domSource = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(domSource, result);
        bw.write(writer.toString());
        bw.close();
    }

    private void convertInitial() {
        Element context = document.createElement("context");
        root.appendChild(context);

        Element data = document.createElement("data");
        context.appendChild(data);

        JSONObject initial = mainArray.getJSONObject(0);
        JSONObject initData = initial.getJSONObject("data");
        Element direction = document.createElement("direction");
        direction.setAttribute("dir", initData.getString("heading"));
        data.appendChild(direction);

        Element men = document.createElement("men");
        men.setTextContent(Integer.toString(initData.getInt("men")));
        data.appendChild(men);

        Element contracts = document.createElement("contracts");
        data.appendChild(contracts);

        for (Object o : initData.getJSONArray("contracts")) {
            JSONObject contractJson = (JSONObject) o;
            Element contract = document.createElement("contract");
            contracts.appendChild(contract);
            Element amount = document.createElement("amount");
            amount.setTextContent(Integer.toString(contractJson.getInt("amount")));
            contract.appendChild(amount);
            Element resource = document.createElement("resource");
            resource.setAttribute("name", contractJson.getString("resource"));
            contract.appendChild(resource);
        }

        Element budget = document.createElement("budget");
        budget.setTextContent(Integer.toString(initData.getInt("budget")));
        data.appendChild(budget);
    }

    private void convertAction(JSONObject actionJson, JSONObject answerJson, Element actions) {
        JSONObject dataAction = actionJson.getJSONObject("data");
        JSONObject dataAnswer = answerJson.getJSONObject("data");
        String actionType = dataAction.getString("action");

        Element turn = document.createElement("turn");
        actions.appendChild(turn);

        Element action = document.createElement("action");
        action.setAttribute("type", actionType);
        turn.appendChild(action);

        Element answer = document.createElement("answer");
        answer.setAttribute("status", dataAnswer.getString("status"));
        turn.appendChild(answer);

        Element cost = document.createElement("cost");
        cost.setTextContent(Integer.toString(dataAnswer.getInt("cost")));
        answer.appendChild(cost);

        Element extras = document.createElement("extras");
        answer.appendChild(extras);
        addExtras(action, extras, actionType, dataAction, dataAnswer.getJSONObject("extras"));
    }

    private void addExtras(Element action, Element extras, String actionType, JSONObject dataAction, JSONObject extrasJson) {
        switch (actionType) {
            case "echo":
            case "heading":
            case "move_to":
            case "scout":
            case "glimpse":
                Element direction = document.createElement("direction");
                direction.setAttribute("dir", dataAction.getJSONObject("parameters").getString("direction"));
                action.appendChild(direction);

                if (actionType.equals("echo")) {
                    Element found = document.createElement("found");
                    found.setTextContent(extrasJson.getString("found"));
                    extras.appendChild(found);

                    Element range = document.createElement("range");
                    range.setTextContent(Integer.toString(extrasJson.getInt("range")));
                    extras.appendChild(range);
                }

                if (actionType.equals("scout")) {
                    Element altitude = document.createElement("altitude");
                    altitude.setTextContent(Integer.toString(extrasJson.getInt("altitude")));
                    extras.appendChild(altitude);
                    for (Object o : extrasJson.getJSONArray("resources")) {
                        Element resource = document.createElement("resource");
                        resource.setAttribute("name", o.toString());
                        extras.appendChild(resource);
                    }
                }

                if (actionType.equals("glimpse")) {
                    int tileIndex = 0;
                    for (Object o : extrasJson.getJSONArray("report")) {
                        tileIndex++;
                        Element tile = document.createElement("tile");
                        tile.setAttribute("range", Integer.toString(tileIndex));
                        extras.appendChild(tile);
                        if (o instanceof JSONArray) {
                            for (int i = 0; i < ((JSONArray) o).length(); i += 2) {
                                Element resource = document.createElement("resource");
                                resource.setAttribute("name", ((JSONArray) o).getString(i));
                                tile.appendChild(resource);
                                Element percent = document.createElement("percent");
                                percent.setTextContent(Integer.toString(((JSONArray) o).getInt(i + 1)));
                                resource.appendChild(percent);
                            }
                        } else {
                            Element resource = document.createElement("resource");
                            resource.setAttribute("name", o.toString());
                            tile.appendChild(resource);
                        }
                    }
                }
                break;
            case "transform": {
                Map<String, Object> map = dataAction.getJSONObject("parameters").toMap();
                for (String key : map.keySet()) {
                    Element resource = document.createElement("resource");
                    resource.setAttribute("name", key);
                    action.appendChild(resource);

                    Element amount = document.createElement("amount");
                    amount.setTextContent(Integer.toString((Integer) map.get(key)));
                    resource.appendChild(amount);
                }
                Element resource = document.createElement("resource");
                resource.setAttribute("name", extrasJson.getString("kind"));
                extras.appendChild(resource);

                Element amount = document.createElement("amount");
                amount.setTextContent(Integer.toString(extrasJson.getInt("production")));
                resource.appendChild(amount);
                break;
            }
            case "exploit": {
                Element resource = document.createElement("resource");
                resource.setAttribute("name", dataAction.getJSONObject("parameters").getString("resource"));
                action.appendChild(resource);

                Element amount = document.createElement("amount");
                amount.setTextContent(Integer.toString(extrasJson.getInt("amount")));
                extras.appendChild(amount);
                break;
            }
            case "explore":
                for (Object o : extrasJson.getJSONArray("resources")) {
                    JSONObject resourceJson = (JSONObject) o;
                    Element resource = document.createElement("resource");
                    resource.setAttribute("name", resourceJson.getString("resource"));
                    extras.appendChild(resource);

                    Element quantity = document.createElement("quantity");
                    quantity.setTextContent(resourceJson.getString("amount"));
                    resource.appendChild(quantity);

                    Element difficulty = document.createElement("difficulty");
                    difficulty.setTextContent(resourceJson.getString("cond"));
                    resource.appendChild(difficulty);
                }
                break;
            case "land":
                Element creek = document.createElement("creek");
                creek.setTextContent(dataAction.getJSONObject("parameters").getString("creek"));
                action.appendChild(creek);

                Element people = document.createElement("people");
                people.setTextContent(Integer.toString(dataAction.getJSONObject("parameters").getInt("people")));
                action.appendChild(people);
                break;
            case "scan":
                Element biomes = document.createElement("biomes");
                extras.appendChild(biomes);
                for (Object o : extrasJson.getJSONArray("biomes")) {
                    Element biome = document.createElement("biome");
                    biome.setTextContent(o.toString());
                    biomes.appendChild(biome);
                }
                Element sites = document.createElement("sites");
                extras.appendChild(sites);
                for (Object o : extrasJson.getJSONArray("sites")) {
                    Element emergency = document.createElement("emergency");
                    emergency.setTextContent(o.toString());
                    sites.appendChild(emergency);
                }
                for (Object o : extrasJson.getJSONArray("creeks")) {
                    Element landing = document.createElement("creek");
                    landing.setTextContent(o.toString());
                    sites.appendChild(landing);
                }
                break;
        }
    }

}

