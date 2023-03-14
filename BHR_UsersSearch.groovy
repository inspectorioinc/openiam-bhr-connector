import com.ibm.db2.cmx.internal.json4j.JSONObject
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
        client = new OpenIAMHttpClient();
        String hostUrl = meta.getUrl();
        GET_USERS_URL = hostUrl + GET_USERS_URL;
        String token = getBasicAuth(meta)
        String fetchAPI = GET_USERS_URL;

        final Map<String, String> loginHeaders = new HashMap<>();
        loginHeaders.put(HTTP.CONTENT_TYPE, "application/json; utf-8");
        loginHeaders.put(HttpHeaders.ACCEPT, "application/json");
        loginHeaders.put(HttpHeaders.AUTHORIZATION, token);
        HttpClientResponseWrapper httpClientResponseWrapper = client.doGet(new URL(fetchAPI), loginHeaders, null, null);
        List<LinkedHashMap> searchUserResponseArr = mapper.readValue(httpClientResponseWrapper.getResponse(), ArrayList.class);
        println("result of API GET USERS:" + httpClientResponseWrapper.getResponse())
        for (LinkedHashMap data : searchUserResponseArr) {
            result.add(jsonToUserConnector(data, attributeNames, request.getIdentityName()))
        }
        println("end of user Search script..." + result.subList(0, 10));
        response.setUserList(result)
        return response;
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
