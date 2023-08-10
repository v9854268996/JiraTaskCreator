import Tools.CheckerProperties;
import Tools.JiraUtils;
import Tools.Utils;
import net.rcarz.jiraclient.Field;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.TimeTracking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TaskList implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TaskList.class);
    ArrayList<Issue.FluentCreate> jiraSubTasks = new ArrayList<Issue.FluentCreate>();
    ArrayList<Issue> issues = new ArrayList<Issue>();
    ArrayList<String> dependencies = new ArrayList<>();
    Path templatePath;



    public TaskList(Path templatePath) {
        this.templatePath = templatePath;
    }

    @Override
    public synchronized void run() {
        List<String> lines = new ArrayList<>();
        logger.info("Подзадачи будут нарезаны в бизнес-задаче " + CheckerProperties.getParameterValue("parentBusinessTask"));
        logger.info("-----------------------------------------------");
        logger.info("Считываем подзадачи из файла шаблона..");

        System.out.println("Подзадачи будут нарезаны в бизнес-задаче " + CheckerProperties.getParameterValue("parentBusinessTask"));
        System.out.println("-----------------------------------------------");
        System.out.println("Считываем подзадачи из файла шаблона..");
        try {
            lines = Files
                    .readAllLines(templatePath)
                    .stream()
                    .map(s -> Utils.replaceParameters(s))
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            logger.error("Не удалось прочитать шаблон нарезки задач:" + templatePath);
            System.out.println("Не удалось прочитать шаблон нарезки задач:" + templatePath);
            return;
        }

        for (String line:lines){
            //////////////////////////////////////

            String[] split = line.split("\\s\\/\\s");
            if (split.length!= 3){
                logger.error("Неверный формат задачи:" + line + ". \nНеобходимый формат: - %Наименование задачи% / %Зависимости по строкам% / %Атрибуты%");
                System.out.println("Неверный формат задачи:" + line + ". \nНеобходимый формат: - %Наименование задачи% / %Зависимости по строкам% / %Атрибуты%");
                return;
            }
            ArrayList<StringArrayPair> attributes = getAttributes(split[2]);

            String issueType = "";
            logger.debug("Добавляем атрибуты для задачи " + split[0]);
            for(StringArrayPair stringArrayPair :attributes){
                if (stringArrayPair.getKey().equals("issueType"))
                    issueType = stringArrayPair.getValue().get(0);
            }

            if (issueType.equals(""))
                logger.error("Не удалось найти тип подзадачи issueType в строке " + line);

            String projectType = CheckerProperties.getParameterValue("parentBusinessTask").split("-")[0];
            net.sf.json.JSONObject createMeta = null;

            try {
                createMeta = JiraUtils.getCreateMeta(projectType,issueType);
            } catch (JiraException e) {
                return;
            }


            String prefixName = split[0];
            if (prefixName.startsWith("-"))
                prefixName = prefixName.substring(1).trim();
            String dependsOn = split[1];

            dependencies.add(parseAttributesToMap(dependsOn).get(0).getValue());
            //////////////////////////////////////
            try {
                jiraSubTasks.add(getCreateByLine(issueType, prefixName,attributes, createMeta));
            } catch (Exception e) {
                logger.error("Не удалось подготовить подзадачу " + issueType + " " + prefixName,e);
                System.out.println("Не удалось подготовить подзадачу " + issueType + " " + prefixName);

            }
        }

        logger.info("-----------------------------------------------");
        logger.info("Создаем в JIRA задачи по собранному шаблону..");
        System.out.println("-----------------------------------------------");
        System.out.println("Создаем в JIRA задачи по собранному шаблону..");
        try {
            Desktop.getDesktop().browse(new URI(CheckerProperties.getParameterValue("jiraurl") + "/browse/" + CheckerProperties.getParameterValue("parentBusinessTask")));
        } catch (Exception e) {
            logger.warn("Не получилось открыть страницу браузера с созданной задачей " + CheckerProperties.getParameterValue("parentBusinessTask"), e);
        }
        for (Issue.FluentCreate fluentCreate : jiraSubTasks){
            Issue issue = null;
            try {
                issue = fluentCreate.execute();
            } catch (JiraException e) {
                logger.error("Не удалось создать подзадачу в строке " + (jiraSubTasks.indexOf(fluentCreate) + 1), e);
                System.out.println("Не удалось создать подзадачу в строке " + (jiraSubTasks.indexOf(fluentCreate) + 1));
                return;
            }
            issues.add(issue);

            logger.info("Создана задача " + issue.getKey() + ". (" + issue.getSummary() + ")");
            System.out.println("Создана задача " + issue.getKey() + ". (" + issue.getSummary() + ")");

        }
        //set dependencies

        logger.info("-----------------------------------------------");
        logger.info("Устанавливаем зависимости для созданных задач..");
        System.out.println("-----------------------------------------------");
        System.out.println("Устанавливаем зависимости для созданных задач..");

        for(int i=0;i<lines.size();i++){
            for(String dependsOnLineString : dependencies.get(i).split(",")){
                if (!dependsOnLineString.equals("0")){
                    Integer dependsOnLine = Integer.parseInt(dependsOnLineString);
                    try {
                        //issues.get(i).link(issues.get(dependsOnLine-1).getKey(),"Зависимые задачи");
                        issues.get(dependsOnLine-1).link(issues.get(i).getKey(),"Зависимые задачи");
                    } catch (JiraException e) {
                        logger.warn("Не удалось проставить связь задач " + issues.get(i).getKey() + " и " + issues.get(dependsOnLine-1).getKey());
                        System.out.println("Не удалось проставить связь задач " + issues.get(i).getKey() + " и " + issues.get(dependsOnLine-1).getKey());
                    }
                }
            }
        }
        logger.info("Нарезка задач завершена");
        System.out.println("Нарезка задач завершена");
    }

    public Issue.FluentCreate getCreateByLine (String issueType, String prefixName, ArrayList<StringArrayPair> attributes,net.sf.json.JSONObject createMeta ) throws JiraException {


        String projectType = CheckerProperties.getParameterValue("parentBusinessTask").split("-")[0];
        HashMap<String,String> fieldsNameKeyMapping = JiraUtils.getFieldsNameKeyMapping(projectType,issueType);


        Issue.FluentCreate issue = null;
        try {
            issue = JiraUtils.getJiraRestClient()
                    .createIssue(projectType,issueType)
                    .field(Field.SUMMARY, prefixName + " " + CheckerProperties.getParameterValue("taskPostfixName"))
                    .field(Field.PARENT, CheckerProperties.getParameterValue("parentBusinessTask"));
        } catch (JiraException e) {
            System.out.println("Ошибка. Смотрите логи");
            logger.error("Не удалось создать пустой шаблон для подзадачи " + prefixName);
            logger.error("Использованные атрибуты:");
            logger.error("   projectType: " + projectType);
            logger.error("   issueType: " + issueType);
            logger.error("   SUMMARY: " + prefixName + " " + CheckerProperties.getParameterValue("taskPostfixName"));
            logger.error("   PARENT: " + CheckerProperties.getParameterValue("parentBusinessTask"), e);
        }

        logger.debug("Добавляем атрибуты подзадаче " + prefixName);
        //String realValue;

        for (StringArrayPair entry : attributes){
            ArrayList<String> realValue = new ArrayList<>();
            logger.debug("Обрабатываем атрибут " + entry.getKey() + " " + entry.getValue());
            String realKey;
            if ("cfield".equals(entry.getKey())){
                String[] pair = entry.getValue().get(0).split(":");
                if (pair.length!=2)
                    logger.error("Неверно заполнено поле " + entry.getValue());
                String foundKey = Utils.stripQuotes(pair[0].trim());
                realValue.add(Utils.stripQuotes(pair[1].trim()));
                realKey = fieldsNameKeyMapping.get(foundKey);
                if(realKey == null) {
                    logger.warn("Не найден параметр Jira, соответствующий описанию \"" + foundKey + "\" в задаче " + prefixName);//throw  new IllegalArgumentException("Не найден параметр Jira, соответствующий описанию "+ entry.getKey());
                    realValue.clear();
                    continue;
                } else if (realKey.equals("customfield_15275")) {
                    realValue.set(0,"{\"value\": \"" + realValue.get(0) + "\"}");
                }
            }
            else {
                realKey = entry.getKey();
                realValue = entry.getValue();
            }
            logger.debug("Итоговая пара атрибутов: ");
            logger.debug("   Ключ: " + realKey );
            logger.debug("   Значение: " + realValue + "");

            //realValue = realValue.replace("{n}","\n");
            realValue = realValue.stream()
                    .map(n -> n.replace("{n}","\n"))
                    .map(String::trim)
                    .collect(Collectors.toCollection(ArrayList::new));

            try {
                if (!realKey.equals("issueType")) {
                    if (Field.getFieldMetadata(realKey, createMeta).type.equals("array")) {
                        //issue.field(realKey.toLowerCase(),Double.parseDouble(realValue));
                        ArrayList<String> arrayList = realValue;
                        ArrayList<String> arrayList1 = new ArrayList<>(Arrays.asList("ЦХД"));
                        issue.field(realKey.toLowerCase(),arrayList);
                    } else if(Field.getFieldMetadata(realKey, createMeta).type.equals("timetracking")){
                        TimeTracking timeTracking = new TimeTracking();
                        timeTracking.setOriginalEstimate(realValue.get(0));
                        timeTracking.setOrignalEstimateSeconds(-1);
                        timeTracking.setRemainingEstimateSeconds(-1);
                        issue.field(realKey.toLowerCase(),timeTracking);
                    } else if(Field.getFieldMetadata(realKey, createMeta).type.equals("number")){
                        issue.field(realKey.toLowerCase(),Double.parseDouble(realValue.get(0)));
                    } else {
                        issue.field(realKey.toLowerCase(),realValue.stream().collect(Collectors.joining(",")));
                    }
                }
            } catch (JiraException e){
                logger.error("Не удалось получить метаданные для поля " + realKey);
                System.out.println("Не удалось получить метаданные для поля " + realKey);
                throw new JiraException("Не удалось получить метаданные для поля ");
            } finally {
                //realValue.clear();
            }

        }
        return issue;
//            //(\b\w*?\:".*?")
    }



    public static ArrayList<StringArrayPair> getAttributes(String attributes){
        ArrayList<StringArrayPair> stringArrayListHashMap = new ArrayList<>();
        ArrayList<StringPair> attributesMap = parseAttributesToMap(attributes);

        for (StringPair entry : attributesMap){
            ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(entry.getValue().split("\\,")));
            stringArrayListHashMap.add(new StringArrayPair(entry.getKey(), arrayList));
        }
        return stringArrayListHashMap;
    }

    public static ArrayList<StringPair> parseAttributesToMap (String attributes){
        //HashMap<String, String> attributesMap = new HashMap<>();
        ArrayList<StringPair> attributesMap = new ArrayList<>();
        //Pattern pattern = Pattern.compile("\\b(\\w*?)\\:\"(.*?)\"");
        Pattern pattern = Pattern.compile("\\b(\\w*?)\\:\"(\\s?.*?\\s?)\"");
        Matcher matcher = pattern.matcher(attributes);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            attributesMap.add(new StringPair(key,value));
        }
        return attributesMap;
    }

    public ArrayList<Issue.FluentCreate> getJiraSubTasks() {
        return jiraSubTasks;
    }



    public static class StringPair {
        String key;
        String value;

        public StringPair(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class StringArrayPair {
        String key;
        ArrayList<String> value;

        public StringArrayPair(String key, ArrayList<String> value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public ArrayList<String> getValue() {
            return value;
        }

        public void setValue(ArrayList<String> value) {
            this.value = value;
        }
    }




//    public Issue.FluentCreate getCreateByLine (String issueType, String prefixName, ArrayList<StringArrayPair> attributes,net.sf.json.JSONObject createMeta ){
//
//
//        String projectType = Tools.CheckerProperties.getParameterValue("defaultProject");
//        HashMap<String,String> getFieldsNameKeyMapping = Tools.JiraUtils.getFieldsNameKeyMapping(projectType,issueType);
//
//
//        Issue.FluentCreate issue = null;
//        try {
//            issue = Tools.JiraUtils.getJiraRestClient()
//                    .createIssue(projectType,issueType)
//                    .field(Field.SUMMARY, prefixName + " " + Tools.CheckerProperties.getParameterValue("taskPostfixName"))
//                    .field(Field.PARENT, Tools.CheckerProperties.getParameterValue("parentBusinessTask"));
//        } catch (JiraException e) {
//            System.out.println("Ошибка. Смотрите логи");
//            logger.error("Не удалось создать пустой шаблон для подзадачи " + prefixName);
//            logger.error("Использованные атрибуты:");
//            logger.error("   projectType: " + projectType);
//            logger.error("   issueType: " + issueType);
//            logger.error("   SUMMARY: " + prefixName + " " + Tools.CheckerProperties.getParameterValue("taskPostfixName"));
//            logger.error("   PARENT: " + Tools.CheckerProperties.getParameterValue("parentBusinessTask"), e);
//        }
//
//        logger.debug("Добавляем атрибуты подзадаче " + prefixName);
//        String realValue;
//        for (StringArrayPair entry : attributes){
//            logger.debug("Обрабатываем атрибут " + entry.getKey() + " " + entry.getValue());
//            String realKey;
//            if ("cfield".equals(entry.getKey())){
//                String[] pair = entry.getValue().get(0).split(":");
//                if (pair.length!=2)
//                    logger.error("Неверно заполнено поле " + entry.getValue());
//                String foundKey = Tools.Utils.stripQuotes(pair[0].trim());
//                realValue = Tools.Utils.stripQuotes(pair[1].trim());
//                realKey = getFieldsNameKeyMapping.get(foundKey);
//                if(realKey == null) {
//                    logger.warn("Не найден параметр Jira, соответствующий описанию \"" + foundKey + "\" в задаче " + prefixName);//throw  new IllegalArgumentException("Не найден параметр Jira, соответствующий описанию "+ entry.getKey());
//                    continue;
//                } else if (realKey.equals("customfield_15275"))
//                    realValue = "{\"value\": \"" + realValue + "\"}";
//            }
//            else {
//                realKey = entry.getKey();
//                realValue = entry.getValue().get(0);
//            }
//            logger.debug("Итоговая пара атрибутов: ");
//            logger.debug("   Ключ: " + realKey );
//            logger.debug("   Значение: " + realValue + "");
//
//            realValue = realValue.replace("{n}","\n");
//
//            try {
//                if (!realKey.equals("issueType")) {
//                    if (Field.getFieldMetadata(realKey, createMeta).type.equals("array")) {
//                        //issue.field(realKey.toLowerCase(),Double.parseDouble(realValue));
//                        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(realValue.split(",")));
//                        issue.field(realKey.toLowerCase(),arrayList);
//                    } else if(Field.getFieldMetadata(realKey, createMeta).type.equals("number")){
//                        issue.field(realKey.toLowerCase(),Double.parseDouble(realValue));
//                    } else {
//                        issue.field(realKey.toLowerCase(),realValue);
//                    }
//                }
//            } catch (JiraException e){
//                logger.error("Ну удалось получить метаданные для поля " + realKey);
//                System.out.println("Ну удалось получить метаданные для поля " + realKey);
//                System.exit(1);
//            }
//        }
//        return issue;
////            //(\b\w*?\:".*?")
//    }


}
