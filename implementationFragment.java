public class GitHubServiceImpl implements GitService {

    private final WebClient webClient;
    private String token;
    private boolean authenticated = false;

    public GitHubServiceImpl(@Value("${github.api.url:https://api.github.com}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github.v3+json")
                .defaultHeader(HttpHeaders.USER_AGENT, "Git-Integration-API")
                .build();
    }

    @Override
    public String getProviderName() {
        return "github";
    }

    @Override
    public boolean authenticate(String token) {
        this.token = token;
        try {
            // Test authentication by getting user info
            webClient.get()
                    .uri("/user")
                    .headers(headers -> {
                        if (token != null && !token.isEmpty()) {
                            headers.setBearerAuth(token);
                        }
                    })
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            this.authenticated = true;
            return true;
        } catch (Exception e) {
            log.error("GitHub authentication failed: {}", e.getMessage());
            this.authenticated = false;
            return false;
        }
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthToken(String token) {
        this.token = token;
        authenticate(token);
    }
