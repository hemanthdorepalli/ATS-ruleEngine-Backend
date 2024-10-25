package com.example.demo.controller;

import com.example.demo.model.Node;
import com.example.demo.model.Rule;
import com.example.demo.repository.RuleRepository;
import com.example.demo.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    @Autowired
    private RuleService ruleService;

    @Autowired
    private RuleRepository ruleRepository;
    
    @PostMapping
    public ResponseEntity<Rule> createRule(@RequestBody Map<String, String> payload) {
        String ruleString = payload.get("ruleString");
        Rule createdRule = ruleService.createRule(ruleString);
        return ResponseEntity.ok(createdRule);
    }

    @GetMapping
    public ResponseEntity<List<Rule>> getAllRules() {
        List<Rule> rules = ruleService.getAllRules();
        return ResponseEntity.ok(rules);
    }

    @PostMapping("/combine")
    public ResponseEntity<Rule> combineRules(@RequestBody List<String> ruleStrings) {
        Node combinedNode = ruleService.combineRules(ruleStrings);
        Rule combinedRule = new Rule();
        combinedRule.setRootNode(combinedNode);
        combinedRule.setRuleString(String.join(" OR ", ruleStrings));
        Rule savedCombinedRule = ruleRepository.save(combinedRule);
        return ResponseEntity.ok(savedCombinedRule);
    }
    
    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        if (!ruleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ruleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/evaluate")
    public ResponseEntity<Map<String, Boolean>> evaluateRules(@RequestBody Map<String, Object> data) {
        boolean result = ruleService.evaluateRule(data);
        return ResponseEntity.ok(Map.of("matches", result));
    }
}
