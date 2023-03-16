# openiam-bhr-connector

Managed System 
Connector: Groovy Script CONNECTOR
Managed System Name: BambooHR
Description: BambooHR Groovy Script Managed System
Manual: false
Active: true
Show on user change password Screen: true
All users provisioned with this managed system: false
Host URL: https://api.bamboohr.com
Port: - 
Password Policy: - 
Communication Protocol: CLEAR
Login Id: x
Password: API_KEY
Search Object Rule: /connector/groovy/bamboohr/BHR_UsersSearch.groovy
Test Connection Object Rule: /connector/groovy/template/TestScriptConnector.groovy
Category: -
Simulation Mode: -
Downstream managed system: -



Configure Synchronization: bamboohr
Records count in one batch: 1000
Is Active?: true
Detect orphans: false
Provision to target systems? (if unchecked, please take care about identities creation in transformation script): false
Synchronization Source: Connector(Deprecated)
Managed System: BambooHR
Synchronize Object: User
Synch Type: Complete
Synch Frequency: - (Manual)
Preprocessor Script: -
Postprocessor Script: -
Validation Rule: /sync/user/csv/CSVSampleValidationScript.groovy
Transformation Implementation: Transformation Scripts
Transformation Rule: /sync/user/bamboohr/BHR_UserTransformation.groovy
OpenIAM field name: PRIMARY EMAIL ADDRESS
Source Attribute Name: workEmail
Custom rule for matching: -
SQL Query / Directory Filter: *
Source attribute names: /sync/user/bamboohr/UserSyncAttributes.groovy
