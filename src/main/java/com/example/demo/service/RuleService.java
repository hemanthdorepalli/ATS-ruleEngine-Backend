// RuleService.java
package com.example.demo.service;

import com.example.demo.model.Node;
import com.example.demo.model.Rule;
import com.example.demo.repository.NodeRepository;
import com.example.demo.repository.RuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RuleService {

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private NodeRepository nodeRepository;

    public Rule createRule(String ruleString) {
        Node rootNode = parseRule(ruleString);
        Rule rule = new Rule();
        rule.setRuleString(ruleString);
        rule.setRootNode(rootNode);
        return ruleRepository.save(rule);
    }

    private Node parseRule(String ruleString) {
        // Remove outer parentheses if they exist
        ruleString = ruleString.trim();
        if (ruleString.startsWith("(") && ruleString.endsWith(")")) {
            ruleString = ruleString.substring(1, ruleString.length() - 1);
        }

        // Find the main logical operator (AND/OR)
        int level = 0;
        int mainOpIndex = -1;
        String mainOperator = null;

        for (int i = 0; i < ruleString.length(); i++) {
            char c = ruleString.charAt(i);
            if (c == '(') level++;
            else if (c == ')') level--;
            else if (level == 0) {
                if (ruleString.substring(i).startsWith(" AND ")) {
                    mainOpIndex = i;
                    mainOperator = "AND";
                    break;
                } else if (ruleString.substring(i).startsWith(" OR ")) {
                    mainOpIndex = i;
                    mainOperator = "OR";
                    break;
                }
            }
        }

        // If we found a main operator, split and recursively parse
        if (mainOperator != null) {
            Node node = new Node();
            node.setType("operator");
            node.setOperator(mainOperator);

            String leftPart = ruleString.substring(0, mainOpIndex).trim();
            String rightPart = ruleString.substring(mainOpIndex + mainOperator.length() + 2).trim();

            node.setLeft(parseRule(leftPart));
            node.setRight(parseRule(rightPart));

            return node;
        } else {
            // This is a leaf node (condition)
            Node node = new Node();
            node.setType("condition");
            node.setValue(ruleString);
            return node;
        }
    }

    public Node combineRules(List<String> ruleStrings) {
        List<Node> nodes = new ArrayList<>();
        for (String ruleString : ruleStrings) {
            nodes.add(parseRule(ruleString));
        }
        return combineNodes(nodes);
    }

    private Node combineNodes(List<Node> nodes) {
        if (nodes.isEmpty()) {
            return null;
        }
        if (nodes.size() == 1) {
            return nodes.get(0);
        }

        // Create a new AND node
        Node combinedNode = new Node();
        combinedNode.setType("operator");
        combinedNode.setOperator("OR");

        // Set left and right nodes
        combinedNode.setLeft(nodes.get(0));
        combinedNode.setRight(combineNodes(nodes.subList(1, nodes.size())));

        return combinedNode;
    }

    public boolean evaluateRule(Map<String, Object> data) {
        List<Rule> rules = getAllRules();
        boolean result = false;
        
        for (Rule rule : rules) {
            if (evaluateNode(rule.getRootNode(), data)) {
                result = true;
                break;
            }
        }
        
        return result;
    }

    private boolean evaluateNode(Node node, Map<String, Object> data) {
        if (node == null) {
            return false;
        }

        // Set data values
        if (data.containsKey("age")) {
            node.setAge((Integer) data.get("age"));
        }
        if (data.containsKey("salary")) {
            node.setSalary(((Number) data.get("salary")).doubleValue());
        }
        if (data.containsKey("department")) {
            node.setDepartment((String) data.get("department"));
        }
        if (data.containsKey("experience")) {
            node.setExperience((Integer) data.get("experience"));
        }

        if ("condition".equals(node.getType())) {
            return node.evaluateCondition();
        }

        boolean leftResult = evaluateNode(node.getLeft(), data);
        boolean rightResult = evaluateNode(node.getRight(), data);

        return "AND".equalsIgnoreCase(node.getOperator()) 
            ? leftResult && rightResult 
            : leftResult || rightResult;
    }

    public List<Rule> getAllRules() {
        return ruleRepository.findAll();
    }
}
