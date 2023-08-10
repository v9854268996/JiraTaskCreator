package Tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created by vpetrov on 20.11.2017.
 */
public class CheckerProperties {

    private static final Logger logger = LoggerFactory.getLogger(CheckerProperties.class);
    static private Properties properties = null;
    //static private Path propertiesPath = Paths.get("C:\\Users\\V.D.Petrov\\IdeaProjects\\JiraConnector\\src\\main\\java\\Properties.properties");
    static private Path propertiesPath = Paths.get(System.getProperty("user.dir") +"\\Properties.properties");
    static {
        logger.info("Используется файл с настройками нарезки: " + propertiesPath);
        properties = getProperties();
    }

    public static Properties getProperties() {
        if (properties == null) {
            try {
                properties = new Properties();
                //properties.load(new FileInputStream(propertiesPath.toFile()));
                properties.load(new InputStreamReader(new FileInputStream(propertiesPath.toFile()), "UTF-8")  );
                //properties.entrySet().stream()
                //        .forEach(e -> e.setValue(e.getValue().toString().toLowerCase()));
            } catch (IOException e) {
                logger.error("Properties file not found. Stopping programm...",e);
                System.exit(1);
            }
        }
        return properties;
    }

    public static String getParameterValue(String parameterName) {
        String parameterValue;
        if ((parameterValue = getProperties().getProperty(parameterName)) == null) {
            logger.error("В настройках не задан параметр " + parameterName);
        }
        return parameterValue;
    }

    public static void setProperty(String parameterName,String parameterValue){
        getProperties().put(parameterName, parameterValue);
    }
}
