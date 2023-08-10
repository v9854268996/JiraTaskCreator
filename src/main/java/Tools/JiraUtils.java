package Tools;

import net.rcarz.jiraclient.*;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


public class JiraUtils {
    static JiraClient jiraRestClient;
    private static final Logger logger = LoggerFactory.getLogger(JiraUtils.class);

    public static Issue getIssue(String issueKey) throws Exception {
        return getJiraRestClient().getIssue(issueKey);
    }

    public static JiraClient getJiraRestClient() throws JiraException {
        System.setProperty("java.net.useSystemProxies", "true");

        if (jiraRestClient!=null)
            return jiraRestClient;

        BasicCredentials creds = new BasicCredentials(CheckerProperties.getParameterValue("login"), CheckerProperties.getParameterValue("password"));
        try {
            logger.info("Подключаемся к jira по адресу: " + CheckerProperties.getParameterValue("jiraurl"));
            System.out.println("Подключаемся к jira по адресу: " + CheckerProperties.getParameterValue("jiraurl"));
            jiraRestClient = new JiraClient(
                    new InsecureHttpClientFactory().buildHttpClient() ,
                    CheckerProperties.getParameterValue("jiraurl"),
                    creds);
        } catch (Exception e) {
            logger.error("Не удалось создать подключение к JIRA" ,e);
            System.out.println("Не удалось создать подключение к JIRA");
            throw new JiraException("ConnectException");
        }
        return  jiraRestClient;

    }

//    public static boolean isValidProject(String projectKey) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, URISyntaxException, JiraException {
//        return getValidProjectKeys().contains(projectKey.toUpperCase());
//    }
//
//    public static ArrayList<String> getValidProjectKeys() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, URISyntaxException, JiraException {
//        RestClient jiraRestClient = getJiraRestClient().getRestClient();
//        ArrayList<String> projectsKeys = Project.getAll(jiraRestClient).stream()
//                .map(p -> p.getKey())
//                .collect(Collectors.toCollection(ArrayList::new));
//        return projectsKeys;
//    }
//
//    public static boolean isValidIssueType(String projectKey,String issueType) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, URISyntaxException, JiraException {
//        return getValidIssueTypes(projectKey,issueType).contains(issueType);
//    }
//
//    public static ArrayList<String> getValidIssueTypes (String projectKey,String issueTypeKey) throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, URISyntaxException, JiraException {
//        RestClient jiraRestClient = getJiraRestClient().getRestClient();
//        Project project = Project.get(jiraRestClient,projectKey);
//        ArrayList<String> issueTypes = project
//                .getIssueTypes().stream()
//                .map(i -> i.getName()).collect(Collectors.toCollection(ArrayList::new));
//        return issueTypes;
//    }

    public static net.sf.json.JSONObject getCreateMeta(String projectKey, String issueType) throws JiraException {
        RestClient restClient = getJiraRestClient().getRestClient();
        net.sf.json.JSONObject createMetadata = null;
        try {
            createMetadata = Issue.getCreateMetadata(restClient, projectKey.toUpperCase(), issueType);
            //createMetadata = customCreateMetadata(restClient, projectKey.toUpperCase(), issueType);
        } catch (JiraException e) {
            logger.error("Не удалось получить метаданные для задач: " + projectKey.toUpperCase() + " " + issueType + ". Проверьте логин/пароль" , e);
            System.out.println("Не удалось получить метаданные для задач: " + projectKey.toUpperCase() + " " + issueType + ". Проверьте логин/пароль");
            throw new JiraException("Не удалось получить метаданные для задач");
        }
        return createMetadata;
    }

    public static HashMap<String,String> getFieldsNameKeyMapping(String projectKey, String issueType) throws JiraException {
        //JSONObject jsonObject = Issue.getCreateMetadata(getJiraRestClient(),projectKey, issueType)
        net.sf.json.JSONObject createMetadata = getCreateMeta(projectKey,issueType);
        HashMap<String,String> collect = (HashMap<String, String>) createMetadata.keySet().stream().collect(
                Collectors.toMap(e -> ((net.sf.json.JSONObject)createMetadata.get(e)).get("name").toString(),
                e -> e.toString()));
        return collect;
    }



    public static net.sf.json.JSONObject customCreateMetadata(RestClient restclient, String project, String issueType) throws JiraException {
        String pval = project;
        String itval = issueType;
        JSON result = null;

        try {
            //Map<String, String> params = new HashMap();
            //params.put("expand", "projects.issuetypes.fields");
            //params.put("projectKeys", pval);
            //params.put("issuetypeNames", itval);
            //URI createuri = restclient.buildURI(Issue.getBaseUri() + "issue/createmeta", params);
            URI createuri = restclient.buildURI(Issue.getBaseUri() + "field");
            result = restclient.get(createuri);
        } catch (Exception var8) {
            throw new JiraException("Failed to retrieve issue metadata", var8);
        }

        if (!(result instanceof net.sf.json.JSONArray)) {
            throw new JiraException("JSON payload is malformed");
        } else {
            net.sf.json.JSONArray jo = (JSONArray)result;
            if (jo instanceof net.sf.json.JSONArray) {
                List<Project> projects = Field.getResourceArray(Project.class, jo, restclient);
                if (!projects.isEmpty() && !((Project)projects.get(0)).getIssueTypes().isEmpty()) {
                    return ((IssueType)((Project)projects.get(0)).getIssueTypes().get(0)).getFields();
                } else {
                    throw new JiraException("Project '" + project + "'  or issue type '" + issueType + "' missing from create metadata. Do you have enough permissions?");
                }
            } else {
                throw new JiraException("Create metadata is malformed");
            }
        }
    }
}
