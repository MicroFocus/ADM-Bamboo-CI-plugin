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
        [@ww.textfield labelKey="SRF Tunnel Client Path" name="SRF Tunnel Client Path" required='true'/]
        </td>
    </tr>
    <tr>
        <td>
        [@ww.textfield labelKey="SRF Tunnel Config File" name="SRF Tunnel Config File" required='true'/]
        </td>
    </tr>
</table>
</body></html>