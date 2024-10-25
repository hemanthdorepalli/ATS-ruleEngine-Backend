package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "rules")
public class Rule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    @NotNull
    private String ruleString;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "root_node_id", referencedColumnName = "id")
    private Node rootNode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleString() {
        return ruleString;
    }

    public void setRuleString(String ruleString) {
        this.ruleString = ruleString;
    }

    public Node getRootNode() {
        return rootNode;
    }

    public void setRootNode(Node rootNode) {
        this.rootNode = rootNode;
    }

    public boolean evaluate() {
        return rootNode != null && evaluateNode(rootNode);
    }

    private boolean evaluateNode(Node node) {
        if (node.isLeaf()) {
            return node.evaluateCondition();
        }

        boolean leftResult = node.getLeft() != null && evaluateNode(node.getLeft());
        boolean rightResult = node.getRight() != null && evaluateNode(node.getRight());

        if ("AND".equalsIgnoreCase(node.getOperator())) {
            return leftResult && rightResult;
        } else if ("OR".equalsIgnoreCase(node.getOperator())) {
            return leftResult || rightResult;
        }

        return false;
    }

    public static Node combineRules(List<Rule> rules) {
        if (rules == null || rules.isEmpty()) {
            return null;
        }

        Node combinedRoot = new Node();
        combinedRoot.setType("operator");
        combinedRoot.setOperator("AND");

        for (Rule rule : rules) {
            Node ruleRootNode = rule.getRootNode();
            if (ruleRootNode != null) {
                combinedRoot.addChild(ruleRootNode);
            }
        }

        return combinedRoot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rule rule = (Rule) o;
        return Objects.equals(id, rule.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Rule{" +
                "id=" + id +
                ", ruleString='" + ruleString + '\'' +
                ", rootNode=" + rootNode +
                '}';
    }
}
