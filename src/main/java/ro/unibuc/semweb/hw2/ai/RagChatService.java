package ro.unibuc.semweb.hw2.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RAG pipeline:
 *   1. embed the user question
 *   2. retrieve top-k most similar documents from the {@link VectorStore}
 *   3. build a librarian prompt that *forbids* outside knowledge
 *   4. call Gemini for the answer
 *
 * Step 3 is what makes the bot satisfy acceptance criterion 3 in the rubric:
 * if our XML claims "Harry Potter was written by Gigel", the bot will
 * answer "Gigel" — because Gemini is only allowed to use what we
 * retrieved, not what it remembers from pre-training.
 */
@Service
public class RagChatService {

    private static final Logger log = LoggerFactory.getLogger(RagChatService.class);
    private static final int TOP_K = 6;

    private final GeminiClient gemini;
    private final VectorStore store;

    public RagChatService(GeminiClient gemini, VectorStore store) {
        this.gemini = gemini;
        this.store = store;
    }

    public String answer(String question, String pageContext) {
        if (!gemini.isConfigured()) {
            return "The chatbot is not configured. Add `app.gemini.api-key=<your key>` to application-local.properties (or set the GEMINI_API_KEY environment variable) and restart the app.";
        }
        if (store.size() == 0) {
            return "My book database has not been indexed yet — startup may still be in progress, or indexing failed. Check the server logs.";
        }

        float[] queryEmb;
        try {
            queryEmb = gemini.embed(question, "RETRIEVAL_QUERY");
        } catch (Exception ex) {
            log.warn("Embedding the user question failed: {}", ex.getMessage());
            return "I couldn't reach the embedding service. Please try again in a moment.";
        }

        List<BookDocument> top = store.search(queryEmb, TOP_K);
        String prompt = buildPrompt(question, pageContext, top);

        try {
            String reply = gemini.chat(prompt);
            return reply.isBlank() ? "I don't have that information in my book database." : reply.trim();
        } catch (Exception ex) {
            log.warn("Gemini chat call failed: {}", ex.getMessage());
            return "I couldn't reach the chat model. Please try again in a moment.";
        }
    }

    private static String buildPrompt(String question, String pageContext, List<BookDocument> top) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a helpful librarian assistant for a small book-recommendation app.\n")
          .append("Answer the user's QUESTION using ONLY the information in CONTEXT below.\n")
          .append("If the CONTEXT does not contain the answer, reply exactly: \"I don't have that information in my book database.\"\n")
          .append("Never use outside knowledge — even if you remember a different author or theme for a well-known book.\n")
          .append("Keep your answer concise (1–3 sentences).\n\n")
          .append("CONTEXT:\n");
        for (BookDocument d : top) {
            sb.append("- ").append(d.text()).append('\n');
        }
        if (pageContext != null && !pageContext.isBlank() && !"unknown".equals(pageContext)) {
            sb.append("\nThe user is currently viewing page context: ").append(pageContext).append('\n');
        }
        sb.append("\nQUESTION: ").append(question).append("\n\nANSWER:");
        return sb.toString();
    }
}
