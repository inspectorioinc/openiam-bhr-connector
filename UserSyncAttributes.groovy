import org.openiam.sync.service.AttributesScript 


class BambooUserSyncAttributes implements AttributesScript {
    @Override
    String[] getAttributes() {
        return ["displayName", "firstName", "lastName", "preferredName",
        "jobTitle","workEmail" , "department", "location", "division", "pronouns","supervisor","photoUploaded","photoUrl"] as String[]
    }

}
