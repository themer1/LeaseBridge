package com.lb.docusign.qa;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QaController {
    private final QaService qaService;

    public QaController(QaService qaService) {
        this.qaService = qaService;
    }
    @GetMapping("/qa/answer")
    public QaService.QaResponse answer(
            @RequestParam("question") String question
    ) {
        // Pass null for envelopeIdFilter to search all envelopes
        return qaService.answer(question, 100, null);
    }
}
