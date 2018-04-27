package spring.spel;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.HashMap;
import java.util.Map;

public class SpelTest {
    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("s", "3");
        map.put("a",7);
        StandardEvaluationContext sec = new StandardEvaluationContext();
//        context.setRootObject(new Object());
        sec.setVariables(map);

        ExpressionParser parser = new SpelExpressionParser();

        String randomPhrase = (String)parser.parseExpression(
                "random number is #{T(java.lang.Math).random()} #{#s},#{#a} ",ParserContext.TEMPLATE_EXPRESSION).getValue(sec);
        System.out.println(randomPhrase);
    }
}
