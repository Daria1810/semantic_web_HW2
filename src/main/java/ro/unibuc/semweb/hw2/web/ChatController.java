package ro.unibuc.semweb.hw2.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.unibuc.semweb.hw2.ai.RagChatService;
import ro.unibuc.semweb.hw2.ai.StarterService;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final RagChatService rag;
    private final StarterService starters;

    public ChatController(RagChatService rag, StarterService starters) {
        this.rag = rag;
        this.starters = starters;
    }

    public record ChatRequest(String question, String context) {}
    public record ChatResponse(String answer) {}
    public record StartersResponse(List<String> starters) {}

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest req) {
        String q = (req.question() == null) ? "" : req.question().trim();
        if (q.isEmpty()) {
            return new ChatResponse("Please type a question first.");
        }
        return new ChatResponse(rag.answer(q, req.context()));
    }

    @GetMapping("/starters")
    public StartersResponse starters(@RequestParam(value = "context", required = false) String context) {
        return new StartersResponse(starters.startersFor(context));
    }
}
