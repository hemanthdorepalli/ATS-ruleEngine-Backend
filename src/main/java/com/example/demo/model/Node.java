package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "nodes")
public class Node {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String type;  // "condition", "action", "operator"

    private String value; // Condition value or action value

    private String operator;  // "AND", "OR"

    // Fields for condition evaluation
    private String field;     // The field to evaluate (age, salary, etc.)
    private String comparison; // The comparison operator (>, <, =, etc.)
    private String targetValue; // The value to compare against

    @Min(18) @Max(100)
    private Integer age;

    @DecimalMin(value = "0.0", inclusive = false)
    private Double salary;

    private String department;

    @Min(0)
    private Integer experience;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "left_id")
    private Node left;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "right_id")
    private Node right;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Node parent;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent")
    private List<Node> children = new ArrayList<>();

    // Standard getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getValue() { return value; }
    public void setValue(String value) { 
        this.value = value;
        parseValue();  // Parse the value when it's set
    }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Integer getExperience() { return experience; }
    public void setExperience(Integer experience) { this.experience = experience; }

    public Node getLeft() { return left; }
    public void setLeft(Node left) { this.left = left; }

    public Node getRight() { return right; }
    public void setRight(Node right) { this.right = right; }

    public Node getParent() { return parent; }
    public void setParent(Node parent) { this.parent = parent; }

    public List<Node> getChildren() { return children; }
    
    public void addChild(Node child) {
        this.children.add(child);
        child.setParent(this);
    }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public String getComparison() { return comparison; }
    public void setComparison(String comparison) { this.comparison = comparison; }

    public String getTargetValue() { return targetValue; }
    public void setTargetValue(String targetValue) { this.targetValue = targetValue; }

    public boolean isLeaf() {
        return this.left == null && this.right == null;
    }

    public boolean hasChildren() {
        return !this.children.isEmpty();
    }

    private void parseValue() {
        if (value != null && type != null && type.equals("condition")) {
            String[] parts = value.split(" ");
            if (parts.length == 3) {
                this.field = parts[0];
                this.comparison = parts[1];
                this.targetValue = parts[2];
            }
        }
    }

    public boolean evaluateCondition() {
        if (!"condition".equals(type)) {
            return false;
        }

        Object fieldValue = getFieldValue();
        if (fieldValue == null || targetValue == null) {
            return false;
        }

        return compareValues(fieldValue, targetValue, comparison);
    }

    private Object getFieldValue() {
        if (field == null) return null;
        
        switch (field.toLowerCase()) {
            case "age": return age;
            case "salary": return salary;
            case "department": return department;
            case "experience": return experience;
            default: return null;
        }
    }

    private boolean compareValues(Object actual, String target, String operator) {
        try {
            if (actual instanceof Integer) {
                int actualValue = (Integer) actual;
                int targetValue = Integer.parseInt(target);
                return compareNumbers(actualValue, targetValue, operator);
            } else if (actual instanceof Double) {
                double actualValue = (Double) actual;
                double targetValue = Double.parseDouble(target);
                return compareNumbers(actualValue, targetValue, operator);
            } else if (actual instanceof String) {
                return compareStrings((String) actual, target, operator);
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return false;
    }

    private boolean compareNumbers(double actual, double target, String operator) {
        switch (operator) {
            case ">": return actual > target;
            case ">=": return actual >= target;
            case "<": return actual < target;
            case "<=": return actual <= target;
            case "=": return actual == target;
            case "!=": return actual != target;
            default: return false;
        }
    }

    private boolean compareStrings(String actual, String target, String operator) {
        target = target.replace("'", "").replace("\"", "");
        switch (operator) {
            case "=": return actual.equalsIgnoreCase(target);
            case "!=": return !actual.equalsIgnoreCase(target);
            default: return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(id, node.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", operator='" + operator + '\'' +
                ", field='" + field + '\'' +
                ", comparison='" + comparison + '\'' +
                ", targetValue='" + targetValue + '\'' +
                '}';
    }
}