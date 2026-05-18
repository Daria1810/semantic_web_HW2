package ro.unibuc.semweb.hw2.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * Thin wrapper around the Google Gemini REST API.
 * Two endpoints are used:
 *   - models/{embedding-model}:embedContent  → vector embedding
 *   - models/{chat-model}:generateContent    → text generation
 *
 * The api key is read from {@code app.gemini.api-key}; if empty, {@link #isConfigured()}
 * returns false and callers should fall back to a stub response.
 */
@Service
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta";

    private final String apiKey;
    private final String chatModel;
    private final String embeddingModel;
    private final RestClient http;

    public GeminiClient(@Value("${app.gemini.api-key:}") String apiKey,
                        @Value("${app.gemini.chat-model:gemini-2.0-flash}") String chatModel,
                        @Value("${app.gemini.embedding-model:text-embedding-004}") String embeddingModel) {
        this.apiKey = apiKey;
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
        this.http = RestClient.builder().baseUrl(BASE_URL).build();
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Embed a single text. {@code taskType} should be
     * {@code "RETRIEVAL_DOCUMENT"} for indexed corpus chunks and
     * {@code "RETRIEVAL_QUERY"} for user queries.
     */
    @SuppressWarnings("unchecked")
    public float[] embed(String text, String taskType) {
        Map<String, Object> body = Map.of(
                "model", "models/" + embeddingModel,
                "content", Map.of("parts", List.of(Map.of("text", text))),
                "taskType", taskType
        );
        Map<String, Object> resp = http.post()
                .uri(uri -> uri.path("/models/{model}:embedContent")
                        .queryParam("key", apiKey)
                        .build(embeddingModel))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        Map<String, Object> emb = (Map<String, Object>) resp.get("embedding");
        List<Number> values = (List<Number>) emb.get("values");
        float[] out = new float[values.size()];
        for (int i = 0; i < values.size(); i++) out[i] = values.get(i).floatValue();
        return out;
    }

    /**
     * Generate an answer given a fully built prompt.
     * @return the model's text response, or an empty string if the API returned no candidate.
     */
    @SuppressWarnings("unchecked")
    public String chat(String prompt) {
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", prompt))
                )),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "maxOutputTokens", 512
                )
        );
        try {
            Map<String, Object> resp = http.post()
                    .uri(uri -> uri.path("/models/{model}:generateContent")
                            .queryParam("key", apiKey)
                            .build(chatModel))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) resp.get("candidates");
            if (candidates == null || candidates.isEmpty()) return "";
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            if (content == null) return "";
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) return "";
            return String.valueOf(parts.get(0).get("text"));
        } catch (RestClientException ex) {
            log.error("Gemini chat call failed: {}", ex.getMessage());
            throw ex;
        }
    }
}
