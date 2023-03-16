import org.openiam.base.AttributeOperationEnum
import org.openiam.idm.srvc.auth.dto.Login
import org.openiam.idm.srvc.continfo.dto.EmailAddress
import org.openiam.idm.srvc.synch.dto.LineObject
import org.openiam.idm.srvc.user.dto.UserAttribute
import org.openiam.idm.srvc.user.dto.UserStatusEnum
import org.openiam.provision.dto.ProvisionUser
import org.openiam.provision.type.Attribute
import org.openiam.sync.service.TransformScript
import org.openiam.sync.service.impl.service.AbstractUserTransformScript

public class BHRUserSampleTransformation extends AbstractUserTransformScript {

    @Override
    int execute(LineObject rowObj, ProvisionUser pUser) {
        populateObject(rowObj, pUser)
        pUser.status = UserStatusEnum.ACTIVE
        pUser.mdTypeId = "DEFAULT_USER"

        pUser.setSkipPreprocessor(false)
        pUser.setSkipPostProcessor(false)
        return TransformScript.NO_DELETE
    }

    @Override
    void init() {}

    private void populateObject(LineObject rowObj, ProvisionUser pUser) {
        def attrVal
        Map<String, Attribute> columnMap = rowObj.columnMap

        attrVal = columnMap.get("firstName")
        if (attrVal) {
            pUser.firstName = attrVal.value
        }

        attrVal = columnMap.get("lastName")
        if (attrVal) {
            pUser.lastName = attrVal.value
        }

        attrVal = columnMap.get("workEmail")
        if (attrVal) {
            // Processing email address
            addAttribute(pUser, attrVal)
            def emailAddress = new EmailAddress()
            emailAddress.name = "PRIMARY_EMAIL"
            emailAddress.default = true
            emailAddress.active = true
            emailAddress.emailAddress = attrVal.value
            emailAddress.mdTypeId = "PRIMARY_EMAIL"
            addEmailAddress(pUser, emailAddress)
        }

        if (isNewUser) {
            if (attrVal) {
                // PRE-POPULATE THE USER LOGIN. IN SOME CASES THE COMPANY WANTS
                // TO KEEP THE LOGIN THAT THEY HAVE
                // THIS SHOWS HOW WE CAN DO THAT
                String login = attrVal.value;
                if (login.contains("@")) {
                    login = login.split("@")[0];
                }
                def lg = new Login()
                lg.operation = AttributeOperationEnum.ADD
                lg.login = login
                lg.managedSysId = "0"
                lg.setActive(true)
                pUser.principalList.add(lg)

                Login lg2 = new Login()
                lg2.operation = AttributeOperationEnum.ADD
                lg2.login = attrVal.value
                lg2.managedSysId = config.getManagedSysId()
                lg2.setActive(true)
                pUser.principalList.add(lg2)
            }
        }
    }

    def addEmailAddress(ProvisionUser pUser, EmailAddress emailAddress) {
        if (!isNewUser) {
            for (EmailAddress e : pUser.emailAddresses) {
                if (emailAddress.mdTypeId.equalsIgnoreCase(e.mdTypeId)) {
                    e.setEmailAddress(emailAddress.getEmailAddress())
                    e.setOperation(AttributeOperationEnum.REPLACE)
                    return
                }
            }
        }
        emailAddress.setOperation(AttributeOperationEnum.ADD)
        pUser.emailAddresses.add(emailAddress)
    }


    def addAttribute(ProvisionUser pUser, Attribute attr) {
        if (attr?.name) {
            def userAttr = new UserAttribute(attr.name, attr.value)
            userAttr.operation = AttributeOperationEnum.ADD
            if (!isNewUser) {
                for (String name : pUser.userAttributes.keySet()) {
                    if (name.equalsIgnoreCase(attr.name)) {
                        pUser.userAttributes.remove(name)
                        userAttr.operation = AttributeOperationEnum.REPLACE
                        break
                    }
                }
            }
            pUser.userAttributes.put(attr.name, userAttr)
        }
    }

}
