package Tools;

import Tools.CheckerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.Properties;

public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static String stripQuotes(String s) {
        String noQuotes = s;
        if (noQuotes.startsWith("\""))
            noQuotes = noQuotes.substring(1);

        if (noQuotes.endsWith("\"")) {
            noQuotes = noQuotes.substring(0, noQuotes.length() - 1);
        }
        return noQuotes;

    }

    public static String replaceParameters(String s) {
        Properties properties = CheckerProperties.getProperties();
        Enumeration<Object> keyEnumeration = properties.keys();
        while (keyEnumeration.hasMoreElements()) {
            String element = (String) keyEnumeration.nextElement();
            if (element.startsWith("replacement")){
                s = s.replace("%%" + element + "%%", properties.getProperty(element).trim());
                logger.debug("ReplacedParameter: " + element + " -> " + properties.getProperty(element).trim());
            }

        }

        return s;

    }
}
