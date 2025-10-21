package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import jakarta.servlet.http.HttpSession;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Controller
public class ChallengeController {

    private final String globalToken = UUID.randomUUID().toString();

    // Главная страница
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String home(HttpSession session) {
        String stepId = UUID.randomUUID().toString();
        session.setAttribute("token", globalToken);
        session.setAttribute("stepId", stepId);

        return "<!doctype html>" +
                "<html><head><meta charset=\"utf-8\"/><title>Flood IO Script Challenge</title>" +
                "<link rel=\"stylesheet\" href=\"/styles.css\"/></head><body>" +
                "<div class=\"card\">" +
                "<h1>Flood IO Script Challenge</h1>" +
                "<form action=\"/start\" method=\"post\">" +
                "<input type=\"hidden\" name=\"authenticity_token\" value=\"" + escapeHtml(globalToken) + "\"/>" +
                "<input type=\"hidden\" name=\"challenger[step_id]\" value=\"" + escapeHtml(stepId) + "\"/>" +
                "<input type=\"hidden\" name=\"challenger[step_number]\" value=\"1\"/>" +
                "<button class=\"btn\" type=\"submit\">Start</button>" +
                "</form></div></body></html>";
    }

    @GetMapping(value = "/step/{id}", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String step(@PathVariable int id, HttpSession session) {
        String token = Objects.toString(session.getAttribute("token"), globalToken);
        session.setAttribute("stepId", UUID.randomUUID().toString());

        switch (id) {
            case 2: // Age
                List<String> ages = Arrays.asList("18","20","22","25","30","35","40","45");
                StringBuilder options = new StringBuilder();
                for (String a : ages) {
                    options.append("<option value=\"" + a + "\">" + a + "</option>\n");
                }
                return "<!doctype html><html><head><meta charset=\"utf-8\"/><title>Age</title>" +
                        "<link rel=\"stylesheet\" href=\"/styles.css\"/></head><body>" +
                        "<div class=\"card\"><h2>How old are you?</h2>" +
                        "<form action=\"/start\" method=\"post\">" +
                        "<input type=\"hidden\" name=\"authenticity_token\" value=\"" + escapeHtml(token) + "\"/>" +
                        "<input type=\"hidden\" name=\"challenger[step_id]\" value=\"" + escapeHtml((String)session.getAttribute("stepId")) + "\"/>" +
                        "<input type=\"hidden\" name=\"challenger[step_number]\" value=\"2\"/>" +
                        "<select name=\"challenger[age]\">" + options.toString() + "</select>" +
                        "<div style=\"margin-top:12px\"><button class=\"btn\" type=\"submit\">Next</button></div>" +
                        "</form></div></body></html>";
            case 3: // Orders
                List<Integer> orders = Arrays.asList(55,120,250,310,99,400,220);
                int maxOrder = orders.stream().max(Integer::compareTo).orElse(0);

                StringBuilder radios = new StringBuilder();
                for (Integer o : orders) {
                    radios.append("<label class=\"order\"><input type=\"radio\" name=\"challenger[largest_order]\" value=\"" + o + "\"/> " + o + "</label><br/>\n");
                }
                return "<!doctype html><html><head><meta charset=\"utf-8\"/><title>Orders</title>" +
                        "<link rel=\"stylesheet\" href=\"/styles.css\"/></head><body>" +
                        "<div class=\"card\"><h2>Choose your largest order</h2>" +
                        "<form action=\"/start\" method=\"post\">" +
                        "<input type=\"hidden\" name=\"authenticity_token\" value=\"" + escapeHtml(token) + "\"/>" +
                        "<input type=\"hidden\" name=\"challenger[step_id]\" value=\"" + escapeHtml((String)session.getAttribute("stepId")) + "\"/>" +
                        "<input type=\"hidden\" name=\"challenger[step_number]\" value=\"3\"/>" +
                        radios.toString() +
                        "<div style=\"margin-top:12px\"><button class=\"btn\" type=\"submit\">Next</button></div>" +
                        "</form></div></body></html>";
            case 4: // Continue, readonly orders
                StringBuilder hiddenOrders = new StringBuilder();
                for (int i = 1; i <= 10; i++) {
                    hiddenOrders.append("<input type=\"hidden\" name=\"challenger_order_" + i + "\" value=\"" + (100+i) + "\"/>\n");
                }
                return "<!doctype html><html><head><meta charset=\"utf-8\"/><title>Continue</title>" +
                        "<link rel=\"stylesheet\" href=\"/styles.css\"/></head><body>" +
                        "<div class=\"card\"><h2>Continue</h2>" +
                        "<form action=\"/start\" method=\"post\">" +
                        "<input type=\"hidden\" name=\"authenticity_token\" value=\"" + escapeHtml(token) + "\"/>" +
                        "<input type=\"hidden\" name=\"challenger[step_id]\" value=\"" + escapeHtml((String)session.getAttribute("stepId")) + "\"/>" +
                        "<input type=\"hidden\" name=\"challenger[step_number]\" value=\"4\"/>" +
                        hiddenOrders.toString() +
                        "<div style=\"margin-top:12px\"><button class=\"btn\" type=\"submit\">Next</button></div>" +
                        "</form></div></body></html>";
            case 5: // One-time token
                long otp = ThreadLocalRandom.current().nextLong(1_000_000_000L, 9_999_999_999L);
                session.setAttribute("oneTimeToken", String.valueOf(otp));
                return "<!doctype html><html><head><meta charset=\"utf-8\"/><title>Token</title>" +
                        "<link rel=\"stylesheet\" href=\"/styles.css\"/></head><body>" +
                        "<div class=\"card\"><h2>Enter one-time token</h2>" +
                        "<p class=\"muted\">Token (demo): <strong>" + otp + "</strong></p>" +
                        "<form action=\"/start\" method=\"post\">" +
                        "<input type=\"hidden\" name=\"authenticity_token\" value=\"" + escapeHtml(token) + "\"/>" +
                        "<input type=\"hidden\" name=\"challenger[step_id]\" value=\"" + escapeHtml((String)session.getAttribute("stepId")) + "\"/>" +
                        "<input type=\"hidden\" name=\"challenger[step_number]\" value=\"5\"/>" +
                        "<input type=\"text\" name=\"challenger[one_time_token]\" placeholder=\"Enter token here\"/>" +
                        "<div style=\"margin-top:12px\"><button class=\"btn\" type=\"submit\">Next</button></div>" +
                        "</form></div></body></html>";
            default:
                return "<html><body><h3>Unknown step</h3></body></html>";
        }
    }

    @PostMapping(value = "/start", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String start(@RequestParam Map<String,String> params, HttpSession session) {
        String stepNum = params.getOrDefault("challenger[step_number]","1");
        int step = Integer.parseInt(stepNum);

        String auth = params.get("authenticity_token");
        if (auth == null) return "<html><body><h3>Missing token</h3></body></html>";

        switch(step) {
            case 1: return redirectHtml("/step/2");
            case 2: return redirectHtml("/step/3");
            case 3:
                int chosen = 0;
                try {
                    chosen = Integer.parseInt(params.getOrDefault("challenger[largest_order]","0"));
                } catch(NumberFormatException e) {
                    return htmlPage("Error","Invalid number chosen");
                }
                // проверка на максимальное число
                if (chosen != 400) {
                    return htmlPage("Error","You did not choose the largest order!");
                }
                return redirectHtml("/step/4");
            case 4: return redirectHtml("/step/5");
            case 5:
                String provided = params.get("challenger[one_time_token]");
                String expected = Objects.toString(session.getAttribute("oneTimeToken"), "");
                if (expected.equals(provided)) return redirectHtml("/done");
                else return htmlPage("Invalid token","Provided: "+escapeHtml(provided)+"<br/>Expected: "+escapeHtml(expected));
            default: return redirectHtml("/");
        }
    }

    @GetMapping(value="/done", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String done() {
        return "<!doctype html><html><head><meta charset=\"utf-8\"/><title>Done</title>" +
                "<link rel=\"stylesheet\" href=\"/styles.css\"/></head><body>" +
                "<div class=\"card\"><h1>Done!</h1><p>Thanks for playing the challenge clone.</p></div></body></html>";
    }

    private String redirectHtml(String path) {
        return "<html><head><meta http-equiv=\"refresh\" content=\"0;url=" + path + "\"/></head><body></body></html>";
    }

    private String htmlPage(String title, String msg) {
        return "<!doctype html><html><head><meta charset=\"utf-8\"/><title>" + escapeHtml(title) + "</title>" +
                "<link rel=\"stylesheet\" href=\"/styles.css\"/></head><body>" +
                "<div class=\"card\"><h2>" + escapeHtml(title) + "</h2><p>" + msg + "</p>" +
                "<p><a href=\"/\">Back to start</a></p></div></body></html>";
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }
}
