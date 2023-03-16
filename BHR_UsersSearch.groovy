import com.ibm.db2.cmx.internal.json4j.JSONObject
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.commons.lang3.StringUtils
import org.apache.http.HttpHeaders
import org.apache.http.protocol.HTTP
import org.openiam.api.connector.helper.ConnectorObjectUtils
import org.openiam.api.connector.model.ConnectorObjectMetaData
import org.openiam.api.connector.model.StringConnectorAttribute
import org.openiam.api.connector.model.StringOperationalConnectorValue
import org.openiam.api.connector.model.UserConnectorObject
import org.openiam.api.connector.user.response.SearchUserProvisioningConnectorResponse
import org.openiam.base.AttributeOperationEnum
import org.openiam.base.ws.ResponseStatus
import org.openiam.common.beans.jackson.CustomJacksonMapper
import org.openiam.connector.core.base.commands.AbstractCommandExecutor
import org.openiam.connector.core.base.exception.ConnectorException
import org.openiam.constants.ProvisionConnectorConstant
import org.openiam.http.client.OpenIAMHttpClient
import org.openiam.http.model.HttpClientResponseWrapper
import org.springframework.context.ApplicationContext
import org.apache.commons.collections4.CollectionUtils


class SearchScriptConnector extends AbstractCommandExecutor<UserConnectorObject, SearchUserProvisioningConnectorResponse> {
    private ApplicationContext context

    static String GET_USERS_URL = "/api/gateway.php/inspectorio/v1/employees/directory"

    OpenIAMHttpClient client

    @Override
    SearchUserProvisioningConnectorResponse perform(UserConnectorObject request) throws ConnectorException {
        println("Calling user search operation...");
        SearchUserProvisioningConnectorResponse response = new SearchUserProvisioningConnectorResponse();
        response.setStatus(ResponseStatus.SUCCESS);
        List<UserConnectorObject> result = new ArrayList<>();
        Set<String> attributeNames = this.getReturnAttributeNames(request);
        CustomJacksonMapper mapper = context.getBean(CustomJacksonMapper.class)
        ConnectorObjectMetaData meta = request.getMetaData();
        //client = new OpenIAMHttpClient();
        String hostUrl = meta.getUrl();
        GET_USERS_URL = hostUrl + GET_USERS_URL;
        String token = getBasicAuth(meta)
        String fetchAPI = GET_USERS_URL;

        URL url = new URL(GET_USERS_URL)
        HttpURLConnection connection = (HttpURLConnection)url.openConnection()
        connection.setRequestMethod("GET")


        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("Authorization", token)

        int resp = connection.getResponseCode()
        StringBuffer content = new StringBuffer()
        if (resp.equals(200)) {
            BufferedReader inp = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))
            String inputLine
            while ((inputLine = inp.readLine()) != null) {
                content.append(inputLine)
            }
            inp.close()
        }
        List<LinkedHashMap> employeeList = new JsonSlurper().parseText(content.toString())["employees"] as List<LinkedHashMap>

        if (CollectionUtils.isNotEmpty(employeeList)) {
            for (LinkedHashMap employeeData : employeeList) {
                result.add(jsonToUserConnector(employeeData, attributeNames, request.getIdentityName()))
            }
            println("end of user Search script..." + result.subList(0, 10));
            response.setUserList(result)
            return response;
        }
    }

    private static UserConnectorObject jsonToUserConnector(LinkedHashMap jsonObj, Set<String> attributeNames, String identityName) {
        if (attributeNames == null)
            return null;
        UserConnectorObject userConnectorObject = new UserConnectorObject();
        List<StringConnectorAttribute> attributes = new ArrayList<>();
        for (String attributeName : attributeNames) {
            attributes.add(fillAttribute(attributeName, (String) jsonObj.get(attributeName)))
        }
        userConnectorObject.setAttributes(attributes);
        userConnectorObject.setIdentityName(identityName);
        return userConnectorObject;
    }

    private static StringConnectorAttribute fillAttribute(String name, String value) {
        StringConnectorAttribute attribute = new StringConnectorAttribute(name);

        attribute.addValue(new StringOperationalConnectorValue(value, AttributeOperationEnum.NO_CHANGE));
        return attribute;
    }

    private static void printResponse(HttpClientResponseWrapper wrapper) {
        println(String.format("Status: %s. Response: %s", wrapper.getStatus(), wrapper.getResponse()))
    }

    private static String getBasicAuth(ConnectorObjectMetaData meta) {
        String userpass = "${meta.getPassword()}:${meta.getLogin()}";
        String token = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
        println("Auth token: " + token);
        return token;
    }
}
