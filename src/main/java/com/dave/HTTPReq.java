package com.dave;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record HTTPReq(String[] reqLine, Map<String, String> headers, String body) {

    public static HTTPReq parse(String reqString) throws HttpParseException {
        String splitRegex = "\\r\\n";
        List<String> lines = new ArrayList<>(List.of(reqString.split(splitRegex)));

        String[] reqLine = lines.removeFirst().split(" ");
        if (reqLine.length != 3) {
            throw new HttpParseException("Could not parse request line of HTTP message!");
        }
        String body = "";
        Map<String, String> headers = new HashMap<>();

        while (!lines.isEmpty()) {
            String s = lines.removeFirst();
            if (s.isBlank()) {
                body = String.join(splitRegex, lines);
                break;
            }
            String[] split = s.split(":", 2);
            if (split.length == 2) {
                headers.put(split[0].trim(), split[1].trim());
            } else {
                headers.put(s.trim(), ""); // TODO not clean; could loose last empty line
            }
        }

        return new HTTPReq(reqLine, headers, body);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.join(" ", reqLine)).append("\r\n");

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }

        sb.append("\r\n");

        if (body != null && !body.isEmpty()) {
            sb.append(body);
        }

        return sb.toString();
    }

}
