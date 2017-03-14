# HPE ALM Octane Bamboo CI plugin
This plugin integrates ALM Octane with Bamboo, enabling ALM Octane to display Bamboo build pipelines and track build and test run results.

##Installation instructions

1. Click the Administration cogwheel button and select Add-ons from the menu. 
2. Click Pause server to pause the server while you install a new add on, to avoid adverse effects on currently running builds.
3. Click Upload add-on, browse to the plugin file that you downloaded and click Upload. 
4. Click Resume server at the top of the page.
5. Refresh the add-on management page.

**Note**: If you enable or disable the plugin at any time after installation, you must restart your CI server.
We need to add this line or the title below to provide context before saying : “Before you configure…”
After you install the HPE ALM Octane CI plugin, configure the plugin to connect to ALM Octane.


### Configuring the Bamboo plugin to connect to ALM Octane 
#### Before you configure the connection: 
1. Ask the ALM Octane shared space admin for an API access Client ID and Client secret. The plugin uses these for authentication when
communicating with ALM Octane. The access keys must be assigned the CI/CD Integration role in all relevant workspaces. For details, see Set up API access for integration.
2. To enable the Bamboo server to communicate with ALM Octane, make sure the server can access the Internet. If your network requires a proxy to connect to the Internet, setup the required proxy configuration. 
3. Decide which Bamboo user ALM Octane will use to execute jobs on the server.

**Caution: We strongly recommend setting this user’s permissions to the minimum required for this integration:  Build plan permissions.**

####Configure the connection
To instruct the plugin to connect to ALM Octane, navigate to the HPE ALM Octane CI Plugin entry in the COMMUNICATION section in bamboo system administration.

Enter the following information:
 
**Location**: http:// Octane fully qualified domain name / IP address> {:}/ui/?p= 
For example, in this URL, the shared space ID is 1002:  http://myServer.myCompany.com:8081/ui/?p=1002
 
**Client ID/Secret**: Ask the ALM Octane shared space admin for an API access Client ID and Client secret. The plugin uses these for authentication when communicating with ALM Octane
 
**Bamboo user**: admin - account that will run jobs for ALM Octane (must have build plan permissions)

**After the connection is set up**, open ALM Octane, define a CI server and create pipelines. 
For details, see Integrate with your CI server in the ALM Octane Help.


###Required fields information:

####Location	
The URL of the ALM Octane server, using its fully qualified domain name (FQDN).

Use the following format (port number is optional):

    http://<ALM Octane hostname / IP address> {:<port number>}/ui/?p=<shared space ID>

Example:  
In this URL, the shared space ID is 1002:
 
    http://myServer.myCompany.com:8081/ui/?p=1002
    
**Tip: You can copy the URL from the address bar of the browser in which you opened ALM Octane.**

#####Client ID	
The API access Client ID that the plugin should use to connect to ALM Octane.
#####Client secret	
The Client secret that the plugin should use to connect to ALM Octane.
#####Bamboo user	
The Bamboo CI server user account that will run jobs at ALM Octane's request.

**Caution:**  
Make sure the user exists in the CI server.
In Bamboo, you must specify a user.