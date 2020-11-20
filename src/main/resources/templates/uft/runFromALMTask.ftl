[#--ftl attributes={"javascript":"/js/uft/runALM.js"} /--]
<!-- TODO - separate css and js from template -->

[@ww.textfield labelKey="RunFromAlmTask.almServerInputLbl" name="almServer" required='true'/]
[@ww.textfield labelKey="RunFromAlmTask.userNameInputLbl" name="userName" required='true'/]
[@ww.password labelKey="RunFromAlmTask.passwordInputLbl" name="password" showPassword='true'/]
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
    document.getElementById('password').style.maxWidth=customWidth;
    document.getElementById('domain').style.maxWidth=customWidth;
    document.getElementById('projectName').style.maxWidth=customWidth;
    document.getElementById('testPathInput').style.maxWidth=customWidth;
    document.getElementById('timeoutInput').style.maxWidth=customWidth;
    document.getElementById('testingToolHost').style.maxWidth=customWidth;
    document.getElementById('runMode').style.maxWidth=customWidth;

</script>



