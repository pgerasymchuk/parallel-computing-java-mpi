import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SequentialWordLengthCounter {

    private static final Pattern wordPattern = Pattern.compile("\\b[\\w'-]+\\b");

    public static List<Integer> compute(List<String> lines){
        List<Integer> counts = new ArrayList<>();

        for (String line : lines) {
            Matcher matcher = wordPattern.matcher(line);
            while (matcher.find()) {
                counts.add(matcher.group().length());
            }
        }
        return counts;
    }
}
