# HPE ALM Octane Bamboo CI plugin

  
* [Prerequisites](#Before you configure the connection)  
* [Configure the conneciton](#Configure the connection)  
  * [Required fields information](#Required fields information)

##Before you configure the connection

###  Configure ALM Octane shared space admin for an API access Client ID/Secret 
Ask the ALM Octane shared space admin for an API access Client ID and Client secret. The plugin uses these for authentication when
communicating with ALM Octane. The access keys must be assigned the CI/CD Integration role in all relevant workspaces. For details, see Set up API access for integration.
### Enable the CI server to communicate with ALM Octane
To enable the CI server to communicate with ALM Octane, make sure the server can access the Internet. If your network requires a proxy to
connect to the Internet, setup the required proxy configuration in Bamboo.
### Decide which Bamboo user ALM Octane will use to execute jobs on the server.
Caution: We strongly recommend setting this user’s permissions to the minimum required for this integration: Job Build permissions.

##Configure the connection

1. Click the Administration cogwheel button and select Add-ons from the menu.
2. In the left pane, click "HPE ALM Octane CI Plugin" under COMMUNICATION.
3. Configure the required fields
4. Press "Test Connection"
5. Save

###Required fields information:

####Location	
The URL of the ALM Octane server, using its fully qualified domain name (FQDN).

Use the following format (port number is optional):

    http://<ALM Octane hostname / IP address> {:<port number>}/ui/?p=<shared space ID>

Example:  
In this URL, the shared space ID is 1002:
 
    http://myServer.myCompany.com:8081/ui/?p=1002
    
Tip: You can copy the URL from the address bar of the browser in which you opened ALM Octane.
####Client ID	
The API access Client ID that the plugin should use to connect to ALM Octane. For details, see prerequisites.
####Client secret	
The Client secret that the plugin should use to connect to ALM Octane. For details, see prerequisites.
####Bamboo user	
The Bamboo CI server user account that will run jobs at ALM Octane's request.
Caution:  
Make sure the user exists in the CI server.
In Bamboo, you must specify a user.