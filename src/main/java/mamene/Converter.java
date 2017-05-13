
package mamene;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
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
        men.setNodeValue(Integer.toString(initData.getInt("men")));
        data.appendChild(men);

        Element contracts = document.createElement("contracts");
        data.appendChild(contracts);

        for (Object o : initData.getJSONArray("contracts")) {
            JSONObject contractJson = (JSONObject) o;
            Element contract = document.createElement("contract");
            contracts.appendChild(contract);
            Element amount = document.createElement("amount");
            amount.setNodeValue(Integer.toString(contractJson.getInt("amount")));
            contract.appendChild(amount);
            Element resource = document.createElement("resource");
            resource.setAttribute("name", contractJson.getString("resource"));
            contract.appendChild(resource);
        }

        Element budget = document.createElement("budget");
        budget.setNodeValue(Integer.toString(initial.getInt("budget")));
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
        answer.setAttribute("status", answerJson.getString("status"));
        turn.appendChild(answer);

        Element cost = document.createElement("cost");
        cost.setNodeValue(Integer.toString(dataAnswer.getInt("cost")));
        answer.appendChild(cost);

        Element extras = document.createElement("extras");
        answer.appendChild(extras);
        addExtras(action, extras, actionType, dataAction, dataAnswer.getJSONObject("extras"));
    }

    private void addExtras(Element action, Element extras, String actionType, JSONObject dataAction, JSONObject extrasJson) {
        if (actionType.equals("echo") || actionType.equals("heading") || actionType.equals("move_to")
                || actionType.equals("scout") || actionType.equals("glimpse")) {
            Element direction = document.createElement("direction");
            direction.setAttribute("dir", dataAction.getJSONObject("parameters").getString("direction"));
            action.appendChild(direction);

            if (actionType.equals("echo")) {
                Element found = document.createElement("found");
                found.setNodeValue(extrasJson.getString("found"));
                extras.appendChild(found);

                Element range = document.createElement("found");
                range.setNodeValue(Integer.toString(extrasJson.getInt("range")));
                extras.appendChild(range);
            }
        } else if (actionType.equals("transform")) {
            Map<String, Object> map = dataAction.getJSONObject("parameters").toMap();
            for (String key : map.keySet()) {
                Element resource = document.createElement("resource");
                resource.setAttribute("name", key);
                action.appendChild(resource);

                Element amount = document.createElement("amount");
                amount.setNodeValue(Integer.toString((Integer) map.get(key)));
                resource.appendChild(amount);
            }
            Element resource = document.createElement("resource");
            resource.setAttribute("name", extrasJson.getString("kind"));
            extras.appendChild(resource);

            Element amount = document.createElement("amount");
            amount.setNodeValue(Integer.toString(extrasJson.getInt("production")));
            resource.appendChild(amount);
        } else if (actionType.equals("exploit")) {
            Element resource = document.createElement("resource");
            resource.setAttribute("name", dataAction.getJSONObject("parameters").getString("resource"));
            action.appendChild(resource);

            Element amount = document.createElement("amount");
            amount.setNodeValue(Integer.toString(extrasJson.getInt("amount")));
            extras.appendChild(amount);
        } else if (actionType.equals("explore")) {
            for (Object o : extrasJson.getJSONArray("resources")) {
                JSONObject resourceJson = (JSONObject) o;
                Element resource = document.createElement("resource");
                resource.setAttribute("name", resourceJson.getString("resource"));
                extras.appendChild(resource);

                Element quantity = document.createElement("quantity");
                quantity.setNodeValue(resourceJson.getString("amount"));
                resource.appendChild(quantity);

                Element difficulty = document.createElement("difficulty");
                difficulty.setNodeValue(resourceJson.getString("cond"));
                resource.appendChild(difficulty);
            }
        }
    }

}

