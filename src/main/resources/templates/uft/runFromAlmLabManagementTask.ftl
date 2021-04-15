[#--ftl attributes={"javascript":"/js/uft/runAlmLabManagement.js", "css":"../../css/uft/almLabManagement.css"} /--]
<!-- TODO - separate css and js from template -->

<div class="toolTip" style="display: block; float: none;">[@ww.text name='RunFromAlmLabManagementTask.taskDescription'/]</div>
[@ww.textfield labelKey="RunFromAlmLabManagementTask.almServer" name="almServer" required='true'/]
[@ui.bambooSection titleKey='ALM Connectivity' collapsible=true]
    [@ww.checkbox labelKey="RunFromAlmLabManagementTask.almSSOEnabledInputLbl" name="almSSO" toggle='true'/]

    [@ui.bambooSection dependsOn='almSSO' showOn='true']
        [@ww.textfield labelKey="RunFromAlmLabManagementTask.clientIDInputLbl" name="clientID" required='true' /]
        [@ww.password labelKey="RunFromAlmLabManagementTask.apiKeySecretInputLbl" name="apiKeySecret" showPassword='true' required='true'/]
    [/@ui.bambooSection]

    [@ui.bambooSection dependsOn='almSSO' showOn='false']
        [@ww.textfield labelKey="RunFromAlmLabManagementTask.userName" name="userName" required='true'/]
        [@ww.password labelKey="RunFromAlmLabManagementTask.password" name="password" showPassword='true'/]
    [/@ui.bambooSection]
[/@ui.bambooSection]

[@ww.textfield labelKey="RunFromAlmLabManagementTask.domain" name="domain" required='true'/]
[@ww.textfield labelKey="RunFromAlmLabManagementTask.projectName" name="projectName" required='true'/]
[@ww.select labelKey="RunFromAlmLabManagementTask.runType" name="runType" list="runTypeItems" emptyOption="false"/]
[@ww.textfield labelKey="RunFromAlmLabManagementTask.testId" name="testId" required='true'/]
[@ww.textfield labelKey="RunFromAlmLabManagementTask.description" name="description"/]
[@ww.textfield labelKey="RunFromAlmLabManagementTask.duration" name="duration" required='true'/]
<div class="control">
    [@ww.textfield labelKey="RunFromAlmLabManagementTask.environmentId" name="environmentId"/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('RunFromAlmLabManagementTask.toolTip.environmentId');">?</div>
<div id ="RunFromAlmLabManagementTask.toolTip.environmentId" class="toolTip">
    [@ww.text name='RunFromAlmLabManagementTask.toolTip.environmentId'/]
</div>

<div class="CDAcheckBox">
    [@ww.checkbox labelKey="RunFromAlmLabManagementTask.useSda" name="useSda" toggle='true'/]
</div>
[@ui.bambooSection dependsOn='useSda' showOn='true']
    [@ww.select labelKey="RunFromAlmLabManagementTask.deploymentAction" name="deploymentAction" list="deploymentActionItems"  emptyOption="false"/]
    [@ww.textfield labelKey="RunFromAlmLabManagementTask.deployedEnvironmentName" name="deployedEnvironmentName"/]
    [@ww.select labelKey="RunFromAlmLabManagementTask.deprovisioningAction" name="deprovisioningAction" list="deprovisioningActionItems" emptyOption="false"/]
[/@ui.bambooSection]


<script  type="text/javascript">
    var customWidth = "500px";
    document.getElementById('almServer').style.maxWidth=customWidth;
    document.getElementById('userName').style.maxWidth=customWidth;
    document.getElementById('password').style.maxWidth=customWidth;
    document.getElementById('domain').style.maxWidth=customWidth;
    document.getElementById('projectName').style.maxWidth=customWidth;
    document.getElementById('runType').style.maxWidth=customWidth;
    document.getElementById('testId').style.maxWidth=customWidth;
    document.getElementById('description').style.maxWidth=customWidth;
    document.getElementById('duration').style.maxWidth=customWidth;
    document.getElementById('environmentId').style.maxWidth=customWidth;
    document.getElementById('deployedEnvironmentName').style.maxWidth=customWidth;
    document.getElementById('deploymentAction').style.maxWidth=customWidth;
    document.getElementById('deprovisioningAction').style.maxWidth=customWidth;
    document.getElementsByClassName('collapsible-section')[0].style.maxWidth=customWidth;
    function toggle_visibility(id) {
        var e = document.getElementById(id);
        if(e.style.display == 'block')
            e.style.display = 'none';
        else
            e.style.display = 'block';
    }
</script>

<style type="text/css">
    .helpIcon{
        background-color: rgba(59, 115, 175, 1);
        color: white;
        width: 15px;
        border-radius:15px;
        font-weight: bold;
        padding-left:6px;
        cursor:pointer;
        margin:5px;
    }
    .control,.helpIcon, .toolTip, .CDAcheckBox{
        float:left;
    }
    .toolTip{
        display: none;
        border: solid #bbb 1px;
        background-color: #f0f0f0;
        padding: 1em;
        margin-bottom: 1em;
        width: 97%;
    }
    .CDAcheckBox{
        width:100%
    }
    .control{
        width:500px;
    }
</style>

