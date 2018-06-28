<html>
<head>
    <title>Create Tunnel</title>
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
            <span>SRF Tunnel Client Path</span>
        </td>
        <td>
        [@ww.textfield name="Tunnel client path" required='false'/]

        </td>
    </tr>
    <tr>
        <td>
            <span>SRF Tunnel Config File</span>
        </td>
        <td>
        [@ww.textfield name="Config file path" required='false'/]
        </td>
    </tr>
</table>
</body></html>