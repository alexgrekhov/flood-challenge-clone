package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Controller
public class ChallengeController {

    private final String globalToken = UUID.randomUUID().toString();

    // ================= HOME =================
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String home(HttpSession session) {
        session.setAttribute("token", globalToken);
        session.setAttribute("stepId", UUID.randomUUID().toString());
        return htmlPage("Flood IO Script Challenge",
                """
                <form action="/step/2" method="post">
                    <input type="hidden" name="authenticity_token" value="%s"/>
                    <input type="hidden" name="challenger[step_id]" value="%s"/>
                    <button class="btn" type="submit">Start</button>
                </form>
                """,
                session);
    }

    // ================= STEP PAGES =================
    @PostMapping(value = "/step/{id}", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String step(@PathVariable int id, @RequestParam Map<String,String> params, HttpSession session) {
        String token = Objects.toString(session.getAttribute("token"), globalToken);
        session.setAttribute("stepId", UUID.randomUUID().toString());

        switch (id) {
            case 2:
                List<String> ages = Arrays.asList("18","20","22","25","30","35","40","45");
                StringBuilder options = new StringBuilder();
                for(String a : ages) options.append(String.format("<option value='%s'>%s</option>\n", a,a));
                return htmlPage("How old are you?",
                        """
                        <form action="/step/3" method="post">
                            <input type="hidden" name="authenticity_token" value="%s"/>
                            <input type="hidden" name="challenger[step_id]" value="%s"/>
                            <select name="challenger[age]">%s</select>
                            <div style="margin-top:12px"><button class="btn" type="submit">Next</button></div>
                        </form>
                        """.formatted(token, session.getAttribute("stepId"), options.toString()),
                        session);

            case 3:
                List<Integer> orders = Arrays.asList(55, 120, 250, 310, 99, 400, 220);
                int maxOrder = Collections.max(orders);
                session.setAttribute("maxOrder", maxOrder);

                StringBuilder radios = new StringBuilder();
                for(Integer o : orders) {
                    radios.append(String.format("<label class='order'><input type='radio' name='challenger[largest_order]' value='%d'/> %d</label><br/>\n", o,o));
                }
                return htmlPage("Choose your largest order",
                        """
                        <form action="/step/4" method="post">
                            <input type="hidden" name="authenticity_token" value="%s"/>
                            <input type="hidden" name="challenger[step_id]" value="%s"/>
                            %s
                            <div style="margin-top:12px"><button class="btn" type="submit">Next</button></div>
                        </form>
                        """.formatted(token, session.getAttribute("stepId"), radios.toString()),
                        session);

            case 4:
                String chosenOrderStr = params.get("challenger[largest_order]");
                int chosenOrder = chosenOrderStr == null ? 0 : Integer.parseInt(chosenOrderStr);
                int correctMax = (int) session.getAttribute("maxOrder");
                if(chosenOrder != correctMax) {
                    return htmlPage("Error", "<p style='color:red'>Wrong order selected! Choose the largest number.</p><a href='/step/3'>Back</a>", session);
                }

                StringBuilder hiddenOrders = new StringBuilder();
                for(int i=1;i<=10;i++) hiddenOrders.append(String.format("<input type='hidden' name='challenger_order_%d' value='%d'/>\n", i, 100+i));

                return htmlPage("Continue",
                        """
                        <form action="/step/5" method="post">
                            <input type="hidden" name="authenticity_token" value="%s"/>
                            <input type="hidden" name="challenger[step_id]" value="%s"/>
                            %s
                            <div style="margin-top:12px"><button class="btn" type="submit">Next</button></div>
                        </form>
                        """.formatted(token, session.getAttribute("stepId"), hiddenOrders.toString()), session);

            case 5:
                long otp = ThreadLocalRandom.current().nextLong(1_000_000_000L, 9_999_999_999L);
                session.setAttribute("oneTimeToken", otp);
                return htmlPage("Enter one-time token",
                        """
                        <form action="/done" method="post">
                            <input type="hidden" name="authenticity_token" value="%s"/>
                            <input type="hidden" name="challenger[step_id]" value="%s"/>
                            <input type="text" name="challenger[one_time_token]" placeholder="Enter token here"/>
                            <p class="muted">Token (demo): <strong>%d</strong></p>
                            <div style="margin-top:12px"><button class="btn" type="submit">Next</button></div>
                        </form>
                        """.formatted(token, session.getAttribute("stepId"), otp), session);

            default:
                return htmlPage("Unknown Step","<p>Unknown step</p>", session);
        }
    }

    // ================= DONE =================
    @PostMapping(value = "/done", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String done(@RequestParam Map<String,String> params, HttpSession session) {
        String provided = params.get("challenger[one_time_token]");
        String expected = Objects.toString(session.getAttribute("oneTimeToken"), "");
        if(!expected.equals(provided)) {
            return htmlPage("Invalid Token",
                    "<p style='color:red'>Wrong token!</p><a href='/step/5'>Try again</a>", session);
        }
        return htmlPage("Done", "<p>Challenge completed!</p>", session);
    }

    // ================= HELPERS =================
    private String htmlPage(String title, String bodyContent, HttpSession session){
        return """
            <!doctype html>
            <html>
            <head>
                <meta charset="utf-8"/>
                <title>%s</title>
                <link rel="stylesheet" href="/styles.css"/>
            </head>
            <body>
                <div class="card">
                    <h1>%s</h1>
                    %s
                </div>
            </body>
            </html>
            """.formatted(title, title, bodyContent);
    }
}
