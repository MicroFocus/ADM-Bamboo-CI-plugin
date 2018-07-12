[#macro newSrfParam paramName='' paramValue=''
    tagNameName="srfParamName" tagNameValue="srfParamValue"]
    <div id = "ParamTemplate">
        [@ww.textfield labelKey="Parameter name" name=tagNameName value=paramName/]
        [@ww.textfield labelKey="Parameter value" name=tagNameValue value=paramValue/]
    </div>
[/#macro]

<div class="control">
    [@ww.textfield label="SRF Address" name="SRF Address" required='true'/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('srfAddressToolTip');">?</div>
<div id ="srfAddressToolTip" class="toolTip">
    [@ww.text name='URL of your SRF tenant server.'/]
</div>

<div class="control">
    [@ww.textfield label="SRF Client ID" name="Client Id" required='true'/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('clientIdToolTip');">?</div>
<div id ="clientIdToolTip" class="toolTip">
    [@ww.text name='Your client ID obtained from SRF.'/]
</div>

<div class="control">
    [@ww.password label="SRF Client Secret" name="Client Secret" required='true' showPassword='true'/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('clientSecretToolTip');">?</div>
<div id ="clientSecretToolTip" class="toolTip">
    [@ww.text name='Your client secret obtained from SRF.'/]
</div>

<div class="control">
    [@ww.textfield label="Proxy" name="Proxy" required='false'/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('proxyToolTip');">?</div>
<div id ="proxyToolTip" class="toolTip">
    [@ww.text name="The proxy address and port, using the following syntax: https://{proxy-host}:{proxy-port}"/]
</div>

<div class="control">
  [@ww.text name='Enter a Test ID or Tags'/]
</div>

<div class="control">
  [@ww.textfield label="SRF Test IDs" name="Test Ids" required='false'/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('testIdToolTip');">?</div>
<div id ="testIdToolTip" class="toolTip">
  [@ww.text name='One or more comma-separated SRF test ID.'/]
</div>

<div class="control">
  [@ww.textfield label="SRF Tags" name="Tags" required='false'/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('tagsToolTip');">?</div>
<div id ="tagsToolTip" class="toolTip">
  [@ww.text name='One or more SRF test tags, separated by commas. Bamboo runs all tests found with all tags listed.'/]
</div>

<div class="control">
  [@ww.textfield label="SRF Tunnel Name" name="Tunnel" required='false'/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('tunnelToolTip');">?</div>
<div id ="tunnelToolTip" class="toolTip">
  [@ww.text name='The name of the SRF tunnel you are using to connect.'/]
</div>

<div class="control">
  [@ww.checkbox label='Close tunnel when job completes' name='shouldCloseTunnel' toggle='false'/]
</div>

<div class="control">
    [@ww.textfield label="Build" name="Test build" required='false'/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('buildToolTip');">?</div>
<div id ="buildToolTip" class="toolTip">
  [@ww.text name='A build number to associate with your test and display in your test result.'/]
</div>

<div class="control">
  [@ww.textfield label="Release" name="Test release" required='false'/]
</div>
<div class="helpIcon" onclick="javascript: toggle_visibility('releaseToolTip');">?</div>
<div id ="releaseToolTip" class="toolTip">
  [@ww.text name='A release number to associate with your test and display in your test result.'/]
</div>

 <div class="control">
  [@ww.text name="SRF Test Parameters"/]
</div>

 <div class="buttons-container">
   <div class="buttons">
       <button class="aui-button aui-button-primary" type="button" onclick="javascript: addNewSrfParam()">
           [@ww.text name='Add Parameters'/]
       </button>
   </div>
</div>

<fieldset style="display: none;">
    [@newSrfParam /]
</fieldset>

<table id="paramTable">
    [#if srfParams??]
        [#list srfParams as prm]
            <tr>
                <td><input type="Button" class="Button" onclick="javascript: delRow(this)" value="[@ww.text name='Delete'/]"></td>
                <td>[@newSrfParam paramName=prm.srfParamName paramValue=prm.srfParamValue/]</td>
            </tr>
        [/#list]
    [/#if]
</table>

<script  type="text/javascript">
    function addNewSrfParam() {
        var divTemplate = document.getElementById('ParamTemplate');
        var table = document.getElementById('paramTable');

        var row = document.createElement("TR");
        var td1 = document.createElement("TD");
        var td2 = document.createElement("TD");

        var strHtml5 = "<INPUT TYPE=\"Button\" CLASS=\"aui-button aui-button-primary\" onClick=\"javascript: delRow(this)\" VALUE=\"[@ww.text name='Delete'/]\">";
        td1.innerHTML = strHtml5;
        td1.style.width = "100px";

        var divClone = divTemplate.cloneNode(true);
        td2.appendChild(divClone);

        row.appendChild(td1);
        row.appendChild(td2);

        table.appendChild(row);
    }

    function delRow(tableID) {
        var current = tableID;
        while ( (current = current.parentElement)  && current.tagName !="TR");
        current.parentElement.removeChild(current);
    }

    var customWidth = "500px";
    document.getElementById('SRF Address').style.maxWidth=customWidth;
    document.getElementById('Client Id').style.maxWidth=customWidth;
    document.getElementById('Client Secret').style.maxWidth=customWidth;
    document.getElementById('Proxy').style.maxWidth=customWidth;
    document.getElementById('Test Ids').style.maxWidth=customWidth;
    document.getElementById('Tags').style.maxWidth=customWidth;
    document.getElementById('Tunnel').style.maxWidth=customWidth;
    document.getElementById('Test build').style.maxWidth=customWidth;
    document.getElementById('Test release').style.maxWidth=customWidth;

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
    .control, .helpIcon, .toolTip, #paramTable {
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
    #paramTable{
        width:100%;
    }
    .control{
        width:500px;
        clear: both;
    }
</style>