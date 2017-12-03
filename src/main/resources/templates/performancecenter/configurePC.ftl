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
            <span>PC Server*</span>
        </td>
        <td>
            <span>[@ww.textfield name="PC Server" required='true'/]</span>
        </td>
    </tr>
    <tr>
        <td>

        </td>
        <td>
        [@ww.checkbox label='Use HTTPS Protocol' name='https' toggle='false'/]
        </td>
    </tr>
    <tr>
        <td>
            <span>User name*</span>
        </td>
        <td>
        [@ww.textfield name="User name" required='true'/]
        </td>
    </tr>
    <tr>
        <td>
            <span>Password</span>
        </td>
        <td>
        [@ww.password name="Password" required='false' showPassword='true'/]
        </td>
    </tr>
    <tr>
        <td>
            <span>Domain*</span>
        </td>
        <td>
        [@ww.textfield name="Domain" required='true'/]
        </td>
    </tr>
    <tr>
        <td>
            <span>PC Project*</span>
        </td>
        <td>
        [@ww.textfield name="PC Project" required='true'/]
        </td>
    </tr>
    <tr>
        <td>
            <span>Test ID*</span>
        </td>
        <td>
        [@ww.textfield name="Test ID" required='true'/]
        </td>
    </tr>
    <tr>
        <td style="vertical-align: top;">
            <span>Test Instance ID</span>
        </td>
        <td >
        [@ww.radio  listKey="key" listValue="value" name="TestInstanceIDRadio" list="testInstanceList" toggle="true" /]
        </td>
    </tr>
    <tr>
        <td>

        </td>
        <td>
        [@ui.bambooSection dependsOn='TestInstanceIDRadio' showOn='MANUAL']
            [@ww.textfield labelKey="Test Instance ID" name="Test Instance ID" required='false'/]
        [/@ui.bambooSection]
        </td>
    </tr>
</table>
<table  class="t1">
    <tr>
        <td>
            <span>Local Proxy</span>
        </td>
        <td>
        [@ww.textfield  name="Local Proxy" required='false'/]
        </td>
        <td style="min-width: 3em;padding-left: 3em;">
            <span>User:</span>
        </td>
        <td>
        [@ww.textfield  name="ProxyUser" required='false'/]
        </td>
        <td style="min-width: 3em;">
            <span>Password:</span>
        </td>
        <td>
        [@ww.password  name="ProxyPassword" required='false' showPassword='true'/]
        </td>
    </tr>
    <tr>
</table>
<table  class="t1">
    <td>
        <span>Post Run Action</span>
    </td>
    <td>
    [@ww.select listKey="key" listValue="value" name="postRunAction" list="postRunActionList" toggle="true" /]
    </td>
    </tr>
</table>


[@ui.bambooSection dependsOn='postRunAction' showOn='Collate_and_Analyze']

<table class="t1">
    <tr>
        <td>

            <span>Trending</span>
        </td>
        <td>
                [@ww.radio listKey="key" listValue="value" name="trendingRadio" list="trendReporList" toggle="true"/]
        </td>
    </tr>
    <tr>
        <td>

        </td>
        <td>
            [@ui.bambooSection dependsOn='trendingRadio' showOn='USE_ID']
            [@ww.textfield labelKey="Trend Report ID" name="Trend Report ID" required='false' /]
        [/@ui.bambooSection]
        </td>
    </tr>
    <tr>
        <td>

        </td>
        <td>

        </td>
    </tr>
    <tr>
        <td>

        </td>
        <td>

        </td>
    </tr>
</table>

[/@ui.bambooSection]



<table  cellpadding="0" cellspacing="0" border="0">
    <tr>
        <td rowspan="2">
            <span>Timeslot Duration</span>
        </td>
        <td rowspan="2" style="vertical-align:middle">
            <span>Hours&#160;</span>
        </td>
        <td rowspan="2" style="vertical-align:middle">
        [@ww.textfield  name="Hours" required='true' style="width:40px;height:24px;text-align:center" onchange="var hours = parseInt(this.value);
        value = (isNaN(hours) || (hours < 0)) ? 0 : ((hours > 480) ? 480 : hours);
        var minutes = document.getElementById('Minutes');
        if (value == 0 && minutes.value < 30) minutes.value = 30;
        else if (value == 480) minutes.value = 0;" /]
        </td>
        <td style="padding: 0">
            <input type="button" value=" /\ "
                   style="font-size:7px;margin:0;padding:0;width:20px;height:15px;vertical-align:top"
                   onclick="var hours = document.getElementById('Hours');
                           var v = parseInt(hours.value);
                           v = (isNaN(v) || v &lt; 0) ? 0 : v + 1;
                           hours.value = (v &gt; 480) ? 480 : v;" />
        </td>
        <td rowspan="2" style="vertical-align:middle">
            <span>&#160;&#160;Minutes&#160;</span>
        </td>
        <td rowspan="2" style="vertical-align:middle">
        [@ww.textfield name="Minutes" required='true' style="width:40px;height:24px;text-align:center" onchange="var v = parseInt(this.value);
										v = isNaN(v) || (v < 0) || (v > 59) ? 0 : v ;
										value = (v < 30 && document.getElementById('Hours').value == 0) ? 30 : v;" /]

        <td style="padding: 0">
            <input type="button" value=" /\ "
                   style="font-size:7px;margin:0;padding:0;width:20px;height:15px;vertical-align:top"
                   onclick="var minutes = document.getElementById('Minutes');
                           var v = parseInt(minutes.value);
                           v = (v + 15) % 60; minutes.value = v - v % 15;
                           var hours = document.getElementById('Hours');
                           if (hours.value == 0 &amp;&amp; minutes.value &lt; 30)
                           minutes.value = 30;" />
        </td>
        <td rowspan="2" style="vertical-align:middle;padding-left:2em">
            <span>(Minimum: 30 minutes)</span>
        </td>
    </tr>
    <tr>
        <td style="padding: 0">
            <input type="button" value=" \/ "
                   style="font-size:7px;margin:0;padding:0;width:20px;height:14px;vertical-align:middle"
                   onclick="var hours = document.getElementById('Hours');
                           var v = parseInt(hours.value);
                           v = isNaN(v) || v &lt; 1 ? 1 : hours.value = v - 1;
                           var minutes = document.getElementById('Minutes');
                           if (hours.value == 0 &amp;&amp; minutes.value &lt; 30)
                           minutes.value = 30;" />
        </td>
        <td style="padding: 0">
            <input type="button" value=" \/ "
                   style="font-size:7px;margin:0;padding:0;width:20px;height:14px;vertical-align:middle"
                   onclick="var minutes = document.getElementById('Minutes');
                           var v = parseInt(minutes.value);
                           v = (v + 45) % 60;
                           if (v % 15 != 0)
                           v = v + 15 - v % 15;
                           if (document.getElementById('Hours').value == 0 &amp;&amp; v &lt; 30)
                           v = 45;
                           minutes.value= v" />
        </td>
    </tr>
</table>
[@ww.checkbox label='Use VUDs' name='vuds' toggle='true'/]
[@ww.checkbox label='Set step status according to SLA' name='sla' toggle='false'/]

<script>
</script>

</body></html>