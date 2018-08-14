[#--ftl attributes={"javascript":"/js/uft/runFileSystem.js", "css":"../../css/uft/fileSystem.css"} /--]
[#--import "../../css/uft/fileSystem.css" as ss/--]
<!-- TODO - separate css and js from template -->

<div class="control">
[@ww.textfield name="CommonTask.taskId" disabled="true"/]
</div>
<hr>
<div class="control">
[@ww.textarea labelKey="FileSystemTaskConfigurator.testsPathInputLbl" id="testPathInput" name="testPathInput" required='true' rows="4"/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('FileSystemTaskConfigurator.toolTip.tests');">?</div>
<div id ="FileSystemTaskConfigurator.toolTip.tests" class="toolTip">
[@ww.text name='FileSystemTaskConfigurator.toolTip.tests'/]
</div>
<hr>
<div class="control">
[@ww.textfield labelKey="FileSystemTaskConfigurator.timelineInputLbl" name="timeoutInput"/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('FileSystemTaskConfigurator.toolTip.timeOut');">?</div>
<div id ="FileSystemTaskConfigurator.toolTip.timeOut" class="toolTip">
[@ww.text name='FileSystemTaskConfigurator.toolTip.timeOut'/]
</div>
<hr>
<div class="control">
[@ww.select labelKey="RunFromFileSystemTask.publishMode" name="publishMode" list="publishModeItems" emptyOption="false"/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('FileSystemTaskConfigurator.toolTip.viewResults');">?</div>
<div id ="FileSystemTaskConfigurator.toolTip.viewResults" class="toolTip">
[@ww.text name='FileSystemTaskConfigurator.toolTip.viewResults'/]
</div>
<hr>
<div class="MCcheckBox">
[@ww.checkbox labelKey="FileSystemTaskConfigurator.toolTip.useMC" name="useMC" toggle='true'/]
</div>
[@ui.bambooSection dependsOn='useMC' showOn='true']
<div class="btn-container">
    <button class="action-button" id="openMCBtn" onclick="javascript: openMCWizardHandler(event);">Open Wizard</button>
</div>
<div class="control">
    [@ww.textfield labelKey="FileSystemTaskConfigurator.mcServerURLInputLbl" name="mcServerURLInput"/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('FileSystemTaskConfigurator.toolTip.mcServerURL');">?</div>
<div id ="FileSystemTaskConfigurator.toolTip.mcServerURL" class="toolTip">
    [@ww.text name='FileSystemTaskConfigurator.toolTip.mcServerURL'/]
</div>
<hr>
<div class="control">
    [@ww.textfield labelKey="FileSystemTaskConfigurator.mcUserNameInputLbl" name="mcUserNameInput"/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('FileSystemTaskConfigurator.toolTip.mcUserName');">?</div>
<div id ="FileSystemTaskConfigurator.toolTip.mcUserName" class="toolTip">
    [@ww.text name='FileSystemTaskConfigurator.toolTip.mcUserName'/]
</div>
<hr>
<div class="control">
    [@ww.password labelKey="FileSystemTaskConfigurator.mcPasswordInputLbl" name="mcPasswordInput"/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('FileSystemTaskConfigurator.toolTip.mcPassword');">?</div>
<div id ="FileSystemTaskConfigurator.toolTip.mcPassword" class="toolTip">
    [@ww.text name='FileSystemTaskConfigurator.toolTip.mcPassword'/]
</div>
<hr>
<div class="MCcheckBox">
    [@ww.checkbox labelKey="FileSystemTaskConfigurator.useSSL" name="useSSL" toggle='false'/]
</div>
<hr>

<div class="MCcheckBox">
    [@ww.checkbox labelKey="FileSystemTaskConfigurator.useProxy" name="useProxy" toggle='true'/]
</div>
<hr>
    [@ui.bambooSection dependsOn='useProxy' showOn='true']
    <div class="control">
        [@ww.textfield labelKey="FileSystemTaskConfigurator.proxyAddress" name="proxyAddress"/]
    </div>
    <hr>
    <div class="MCcheckBox">
        [@ww.checkbox labelKey="FileSystemTaskConfigurator.specifyAuthentication" name="specifyAuthentication" toggle='true'/]
    </div>
    <hr>
    <div class="control">
        [@ww.textfield labelKey="FileSystemTaskConfigurator.proxyUsername" name="proxyUserName" disabled="true" /]
    </div>
    <hr>
    <div class="control">
        [@ww.password labelKey="FileSystemTaskConfigurator.proxyPassword" name="proxyPassword" disabled="true"/]
    </div>
    <hr>
    [/@ui.bambooSection]
<h3 class="title" id="deviceCapability">Device Information</h3>
<!-- <input type="hidden" id="jobUUID" name="jobUUID"/> -->
    [@ww.hidden id="jobUUID" name="jobUUID"/]
<div class="control">
    [@ww.textfield labelKey="FileSystemTaskConfigurator.deviceId" name="deviceId" readonly="true" /]
</div>
<hr>

<div class="control">
    [@ww.textfield labelKey="OS" name="OS" readonly="true" /]
</div>
<hr>

<div class="control">
    [@ww.textfield labelKey="FileSystemTaskConfigurator.manufacturerAndModel" name="manufacturerAndModel" readonly="true" /]
</div>
<hr>
<div class="control">
    [@ww.textfield labelKey="FileSystemTaskConfigurator.targetLab" name="targetLab" readonly="true" /]
</div>
<hr>
<!-- <h3 class="title">Application Under Test</h3> -->
<div class="control">
    [@ww.textarea labelKey="FileSystemTaskConfigurator.applicationUnderTest" id="extraApps" name="extraApps" rows="4" readonly="true"/]
</div>
<hr>

<h3 class="title">Test Definitions</h3>
<div class="control">
    [@ww.textfield labelKey="FileSystemTaskConfigurator.launchOnStart" name="launchApplicationName" readonly="true" /]
</div>
<hr>

<div class="control">
    [@ww.textfield labelKey="FileSystemTaskConfigurator.AUTActions" name="autActions" readonly="true" /]
</div>
<hr>

<div class="control">
    [@ww.textfield labelKey="FileSystemTaskConfigurator.AUTPackaging" name="instrumented" readonly="true" /]
</div>
<hr>
<div class="control">
    [@ww.textfield labelKey="FileSystemTaskConfigurator.deviceMetrics" name="deviceMetrics" readonly="true" /]
</div>
<hr>
[/@ui.bambooSection]

<script  type="text/javascript">
    var jobId,
            wizard,
            loginInfo,
            mcServerURLInput,
            mcUserNameInput,
            mcPasswordInput,
            useProxy,
            proxyAddress,
            proxyUserName,
            proxyPassword,
            useAuthentication;
    var customWidth = "500px";
    document.getElementById('timeoutInput').style.maxWidth=customWidth;
    document.getElementById('testPathInput').style.maxWidth=customWidth;
    document.getElementById('publishMode').style.maxWidth=customWidth;
    document.getElementById('mcServerURLInput').style.maxWidth=customWidth;
    document.getElementById('mcUserNameInput').style.maxWidth=customWidth;
    document.getElementById('mcPasswordInput').style.maxWidth=customWidth;
    document.getElementById('extraApps').style.maxWidth=customWidth;
    var openMCBtn = document.getElementById('openMCBtn');
    var specifyAuthenticationBox = document.getElementById('specifyAuthentication');
    specifyAuthenticationBox.addEventListener('change', function (e) {
        var proxyUserNameInput = document.getElementById('proxyUserName'),
                proxyPasswordInput = document.getElementById('proxyPassword');

        if (specifyAuthenticationBox.checked == true) {
            proxyUserNameInput.disabled = false;
            proxyPasswordInput.disabled = false;
        } else {
            proxyUserNameInput.disabled = true;
            proxyPasswordInput.disabled = true;
        }
    });

    function toggle_visibility(id) {
        var e = document.getElementById(id);
        if (e.style.display == 'block')
            e.style.display = 'none';
        else
            e.style.display = 'block';
    }

    function openMCWizardHandler(e) {
        //disable open wizard button
        openMCBtn.disabled = true;

        //get login info, url, username, password
        mcServerURLInput = document.getElementById('mcServerURLInput').value.replace(/\/$/, "");//remove tailing slash
        mcUserNameInput = document.getElementById('mcUserNameInput').value;
        mcPasswordInput = document.getElementById('mcPasswordInput').value;
        proxyAddress = document.getElementById('proxyAddress').value;
        proxyUserName = document.getElementById('proxyUserName').value;
        proxyPassword = document.getElementById('proxyPassword').value;
        useProxy = document.getElementById('useProxy').checked;
        useAuthentication = specifyAuthenticationBox.checked;

        loginInfo = {
            mcServerURLInput: mcServerURLInput,
                    mcUserNameInput: mcUserNameInput,
                mcPasswordInput: mcPasswordInput,
                proxyAddress: proxyAddress,
                proxyUserName: proxyUserName,
                proxyPassword: proxyPassword,
                useProxy: useProxy,
                useAuthentication: useAuthentication
        };
        //no need do login, get job id directly
        getJobIdHelper(loginInfo);
    }

    function getJobIdHelper(loginInfo) {
        var jobIdInput = document.getElementById("jobUUID");
        AJS.$.ajax({
            url: "${req.contextPath}/plugins/servlet/httpOperationServlet?method=createTempJob",
            method: "POST",
            data: loginInfo,
            success: function(data) {
                var dataJSON = JSON.parse(data);
                if(dataJSON != null){
                    var errorCode = dataJSON.myErrorCode;
                    if (errorCode != null) {
                        openMCBtn.disabled = false;
                        if (errorCode == 0) {
                            alert("The URL, User name, and Password fields cannot be empty.");
                            return;
                        } else if (errorCode == 2) {
                            alert("Use Proxy is enabled, but no proxy address was provided.");
                            return;
                        } else if (errorCode == 4) {
                            alert("Specific Authentication is selected, but the Proxy User name or password is empty.");
                            return;
                        }
                    }
                    jobId =  data.data && data.data.id;
                    if (!jobId){
                        alert('The login to Mobile Center failed. Check that the Mobile Center login information is correct.');
                        openMCBtn.disabled = false;
                        return;
                    }

                    //set jobId to hidden input
                    jobIdInput.value = jobId;
                    //open MC wizard
                    wizard = window.open(
                            mcServerURLInput+ "/integration/#/login?jobId=" + jobId + "&displayUFTMode=true&appType=native",
                            "MCWizardWindow",
                            "width=1024,height=768");
                    wizard.focus();
                    window.addEventListener('message', messageEventHandler, false);
                }else{
                    alert('The login to Mobile Center failed. Check that the Mobile Center login information is correct.');
                    openMCBtn.disabled = false;
                    return;
                }


            },
            error: function(error) {
                var errorCode = error.myErrorCode;
                if (errorCode == 0) {
                    alert("The URL, User name, and Password fields cannot be empty.");
                } else if (errorCode == 2) {
                    alert("Use Proxy is enabled, but no proxy address was provided.");
                } else if (errorCode == 4) {
                    alert("Specific Authentication is selected, but the Proxy User name or password is empty.");
                }

                openMCBtn.disabled = false;
            }
        });
    }

    function messageEventHandler(event) {
        var me = this;
        //stop event bubble
        event.stopPropagation();
        console.log("===message event listener called from bamboo=====", event.data);

        if (event && event.data == "mcJobUpdated") {
            console.log("=====get device and application from mc success====", loginInfo);
            //get device and application
            AJS.$.ajax({
                url: "${req.contextPath}/plugins/servlet/httpOperationServlet?method=getJobJSONData&jobUUID=" + jobId,
                type: "POST",
                data: loginInfo,
                dataType: "json",
                success: function(data) {
                    //data = JSON.parse(data);
                    console.log("=====get device and application from mc success====", data);
                    //set device and application information to test
                    me._parseTestInfoHelper(data);
                    //enable action button after the wizard closed
                    openMCBtn.disabled = false;

                    wizard.close();
                },
                error: function(error) {
                    console.log("=====get job detail from mc fail====");
                    alert('Get job detail information from Mobile Center failed, please try again.');
                    //enable action button after the wizard closed
                    openMCBtn.disabled = false;
                }
            });
        }

        if (event && event.data === 'mcCloseWizard') {
            wizard.close();
            //enable action button after the wizard closed
            openMCBtn.disabled = false;
        }
    }

    function checkWizardStatus() {
        if (wizard && wizard.closed) {
            clearInterval(timer);
            //enable action button after the wizard closed
            openMCBtn.disabled = false;
        }
    }
    var timer = setInterval(checkWizardStatus, 500);

    function _parseTestInfoHelper(testData) {
        delete testData.jobUUID;
        //render extra apps first
        _extraAppsReader(testData.extraApps || []);

        //delete testData.extraApps;

        for (var key in testData) {
            if (key == 'extraApps') continue;

            for (var infoKey in testData[key]) {
                if (document.getElementById(infoKey) != null) {
                    document.getElementById(infoKey).value = testData[key][infoKey];
                }
            }
        }

        //deviceCapability and specificDevice cannot exist at the same time
        if (testData.specificDevice.deviceId) {
            document.getElementById('targetLab').value = '';
        } else {
            document.getElementById('deviceId').value = '';
        }

        return false;
    }

    function _extraAppsReader(extraApps) {
        console.log('=====extraApps=====', extraApps);
        var extraAppsContainer = document.getElementById("extraApps");
        var extraAppsInfo = '';

        //remove all children before add new
        //extraAppsContainer.innerHTML = '';
        extraApps.forEach(function(app, index, array) {
            // extraAppsContainer.appendChild(appContainer);
            extraAppsInfo += app.extraAppName + ': ' + app.instrumented + '\n';
        });

        extraAppsContainer.value = extraAppsInfo;

        return false;
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

    .control,.helpIcon, .toolTip, .MCcheckBox, .parameterWrapper, #paramTable {
        float:left;
    }

    #paramTable{
        width:100%;
    }
    .MCcheckBox{
        width:100%
    }

    .control,.helpIcon, .toolTip{
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

    hr{
        clear:both;
        border:none;
    }

    .control{
        width:500px;
    }

    form.aui .field-group input.text {
        max-width: 500px;
    }

    h3.title {
        margin: 0px;
    }

    #extraApps {
        min-height: 30px;
        border: 1px solid #ccc;
        border-radius: 1px;
    }

    .extra-app-info {
        padding: 8px 0px;
    }

    .extra-app-info .app-name {
        margin-right: 10px;
    }
</style>