package mikkelmattern.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import mikkelmattern.exceptions.ApiException;
import mikkelmattern.records.RawgGameDTO;
import mikkelmattern.records.RawgSearchResultDTO;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class RawgClient {
    private static final String BASE_URL = "https://api.rawg.io/api";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final String apiKey;

    public RawgClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public List<RawgGameDTO> search(String query) {
        String url = BASE_URL + "/games?page_size=10&search="
                + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&key=" + apiKey;
        return get(url, RawgSearchResultDTO.class).results();
    }

    public RawgGameDTO getGame(long rawgId) {
        return get(BASE_URL + "/games/" + rawgId + "?key=" + apiKey, RawgGameDTO.class);
    }

    private <T> T get(String url, Class<T> type) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                throw new ApiException(404, "Game not found on RAWG");
            }
            if (response.statusCode() != 200) {
                throw new ApiException(502, "RAWG returned " + response.statusCode());
            }
            return mapper.readValue(response.body(), type);
        } catch (IOException e) {
            throw new ApiException(502, "Could not reach RAWG");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(502, "Request to RAWG was interrupted");
        }
    }
}
