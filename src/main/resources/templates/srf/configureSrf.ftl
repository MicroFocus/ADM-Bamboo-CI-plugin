[#macro newSrfParam paramName='' paramValue=''
    tagNameName="srfParamName" tagNameValue="srfParamValue"]
    <div id = "ParamTemplate">
        [@ww.textfield labelKey="Parameter name" name=tagNameName value=paramName/]
        [@ww.textfield labelKey="Parameter value" name=tagNameValue value=paramValue/]
    </div>
[/#macro]


<html>
<head>
    <title>Update Environments</title>
    <style>
        table td {
            padding:0 1rem 0 0;
            vertical-align:middle;}
        .t1 td{min-width:7rem;}
    </style>
</head>
<body>
[#--<h1>Welcome ${user.name}!</h1>--]

<table  class="t1" cellpadding="0" cellspacing="0" border="0">
    <tr>
        <td>
            <span>SRF Test ID</span>
        </td>
        <td>
        [@ww.text name='Please provide Test ID or Tags!'/]
        [@ww.textfield name="Test Id" required='false'/]
        </td>
    </tr>
    <tr>
        <td>
            <span>Tags</span>
        </td>
        <td>
        [@ww.textfield name="Tags" required='false'/]
        </td>
    </tr>

    <tr>
        <td>
            <span>SRF Tunnel Name</span>
        </td>
        <td>
        [@ww.textfield name="Tunnel" required='false'/]
        </td>
    </tr>
    <tr>
        <td>

        </td>
        <td>
        [@ww.checkbox label='Close Tunnel When Job Completed' name='shouldCloseTunnel' toggle='false'/]
        </td>
    </tr>
    <tr>
       <td>
           <span>Build</span>
       </td>
       <td>
       [@ww.textfield name="Test build" required='false'/]
       </td>
    </tr>
    <tr>
        <td>
            <span>Release</span>
        </td>
        <td>
        [@ww.textfield name="Test release" required='false'/]
        </td>
    </tr>
    <tr>
        <td>
            [@ww.text name="Parameters"/]
        </td>
    </tr>
    <tr>
        <td>
            <div class="buttons-container">
               <div class="buttons">
                   <button class="aui-button aui-button-primary" type="button" onclick="javascript: addNewSrfParam()">
                       [@ww.text name='Add Parameters'/]
                   </button>
               </div>
           </div>
        </td>
    </tr>

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
</script>

</body></html>