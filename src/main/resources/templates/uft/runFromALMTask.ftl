[#--ftl attributes={"javascript":"/js/uft/runALM.js"} /--]
<!-- TODO - separate css and js from template -->

[@ww.textfield labelKey="RunFromAlmTask.almServerInputLbl" name="almServer" required='true'/]

[@ui.bambooSection titleKey='ALM Connectivity' collapsible=true]
    [@ww.checkbox labelKey="RunFromAlmTask.almSSOEnabledInputLbl" name="almSSO" toggle='true'/]

    [@ui.bambooSection dependsOn='almSSO' showOn='true']
        [@ww.textfield labelKey="RunFromAlmTask.clientIDInputLbl" name="clientID" required='true' /]
        [@ww.password labelKey="RunFromAlmTask.apiKeySecretInputLbl" name="apiKeySecret" showPassword='true' required='true'/]
    [/@ui.bambooSection]

    [@ui.bambooSection dependsOn='almSSO' showOn='false']
        [@ww.textfield labelKey="RunFromAlmTask.userNameInputLbl" name="userName" required='true'/]
        [@ww.password labelKey="RunFromAlmTask.passwordInputLbl" name="password" showPassword='true'/]
    [/@ui.bambooSection]
[/@ui.bambooSection]

[@ww.textfield labelKey="RunFromAlmTask.domainInputLbl" name="domain" required='true'/]
[@ww.textfield labelKey="RunFromAlmTask.projectInputLbl" name="projectName" required='true'/]
[@ww.textarea labelKey="RunFromAlmTask.testsPathInputLbl" name="testPathInput" required='true' rows="4"/]
[@ww.textfield labelKey="RunFromAlmTask.timelineInputLbl" name="timeoutInput"/]

[@ww.checkbox labelKey='RunFromAlmTask.advancedLbl' name='AdvancedOption' toggle='true' /]
[@ui.bambooSection dependsOn='AdvancedOption' showOn='true']
    [@ww.select labelKey="RunFromAlmTask.runModeInputLbl" name="runMode" list="runModeItems" emptyOption="false"/]
    [@ww.textfield labelKey="RunFromAlmTask.testingToolHostInputLbl" name="testingToolHost" required='false'/]
[/@ui.bambooSection]

<script  type="text/javascript">
    var customWidth = "500px";
    document.getElementById('almServer').style.maxWidth=customWidth;
    document.getElementById('userName').style.maxWidth=customWidth;
    document.getElementById('clientID').style.maxWidth=customWidth;
    document.getElementById('apiKeySecret').style.maxWidth=customWidth;
    document.getElementById('password').style.maxWidth=customWidth;
    document.getElementById('domain').style.maxWidth=customWidth;
    document.getElementById('projectName').style.maxWidth=customWidth;
    document.getElementById('testPathInput').style.maxWidth=customWidth;
    document.getElementById('timeoutInput').style.maxWidth=customWidth;
    document.getElementById('testingToolHost').style.maxWidth=customWidth;
    document.getElementById('runMode').style.maxWidth=customWidth;
    document.getElementsByClassName('collapsible-section')[0].style.maxWidth=customWidth;
</script>



