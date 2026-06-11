import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ServerClient {
    private static final String BASE_URL = "http://127.0.0.1:8000";

    private static String request(String method, String path, String body) throws Exception {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");

        if (body != null) {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }

        InputStream is = conn.getResponseCode() >= 400
                ? conn.getErrorStream()
                : conn.getInputStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString();
    }

    public static String getMyStatus() throws Exception {
        return request("GET", "/me", null);
    }

    public static String getTodos() throws Exception {
        return request("GET", "/todos", null);
    }

    public static String createTodo(String title, String subject) throws Exception {
        String body = String.format(
                "{\"title\":\"%s\",\"subject\":\"%s\"}",
                escape(title),
                escape(subject)
        );

        return request("POST", "/todos", body);
    }

    public static String completeTodo(String todoId, String learnedNote) throws Exception {
        String body = String.format(
                "{\"learned_note\":\"%s\"}",
                escape(learnedNote)
        );

        return request("POST", "/todos/" + todoId + "/complete", body);
    }

    public static String startFocus() throws Exception {
        return request("POST", "/focus/start", "{}");
    }

    public static String endFocus() throws Exception {
        return request("POST", "/focus/end", "{}");
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}