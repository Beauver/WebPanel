# WebPanel
A Minecraft 1.21.4 PaperMC/Spigot Plugin that creates a WebPanel. 
<br>This plugin allows you to modify your server via the web, including adding, removing, and downloading files, as well as managing the console.

## Early Access
This plugin is still in early access and may contain bugs or insecurities.
<br> Features may also be subject to change.

## Features
These are the current features of this WebPanel plugin.

### WebPanel
- Manage the server console
- Stop the server
- Upload files to the server
- Download files from the server
- Delete files or directories
- Create new folders

### Commands
There are no commands yet.

### Events
There are no events yet.

### Planned
To see what features are currently planned please check out the [WebPanel GitHub project board](https://github.com/users/Beauver/projects/3)

## Installation
1. Download the plugin jar file.
2. Place the jar file in the `plugins` folder of your PaperMC/Spigot server.
3. Restart the server to load the plugin.

## Usage
1. Add your IP address to the `allowedIPs` list in the plugins `config.yml` file.
2. Restart the server to apply the changes. (Later there will be a command)
3. Access the panel via `http://<server-ip>:<server-port>/` in your browser.

## Endpoints
These endpoints are currently used by the panel. They may not yet be final and may be subject to change.

| Endpoint           | URL                 | Method | Headers                                                                              | Parameters           | Response                                                                                                                                                                                                                                                                       |
|--------------------|---------------------|--------|--------------------------------------------------------------------------------------|----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Get Files          | `/getFiles`         | GET    | `Content-Type: application/x-www-form-urlencoded`                                    | `path`               | `200 OK`: File list retrieved successfully<br>`400 Bad Request`: Missing path<br>`401 Unauthorized`: Missing or invalid API Key<br>`403 Forbidden`: Forbidden<br>`500 Internal Server Error`: Failed to retrieve file list                                                     |
| Upload File        | `/uploadFile`       | POST   | `Authorization: Bearer <API_KEY>`, `Content-Type: multipart/form-data`               | `path`, `folderName` | `200 OK`: File uploaded successfully<br>`400 Bad Request`: File not found in request body<br>`401 Unauthorized`: Missing or invalid API Key<br>`403 Forbidden`: Forbidden<br>`500 Internal Server Error`: Failed to save file                                                  |
| Delete File        | `/deleteFile`       | POST   | `Authorization: Bearer <API_KEY>`, `Content-Type: application/x-www-form-urlencoded` | `path`               | `200 OK`: File or directory deleted successfully<br>`400 Bad Request`: Missing file path<br>`401 Unauthorized`: Missing or invalid API Key<br>`403 Forbidden`: Forbidden<br>`404 Not Found`: File not found<br>`500 Internal Server Error`: Failed to delete file or directory |
| Create Folder      | `/createFolder`     | POST   | `Authorization: Bearer <API_KEY>`, `Content-Type: application/x-www-form-urlencoded` | `path`, `folderName` | `200 OK`: Folder created successfully<br>`400 Bad Request`: Missing path or folder name<br>`401 Unauthorized`: Missing or invalid API Key<br>`403 Forbidden`: Forbidden<br>`500 Internal Server Error`: Failed to create folder                                                |
| Download File      | `/downloadFile`     | GET    | `"Authorization": Bearer <API_KEY>`                                                  | `path`               | `200 OK`: File downloaded successfully<br>`400 Bad Request`: Missing file path<br>`401 Unauthorized`: Missing or invalid API Key<br>`403 Forbidden`: Forbidden<br>`404 Not Found`: File not found<br>`500 Internal Server Error`: Failed to download file                      |
| Send to Console    | `/sendToConsole`    | POST   | `Authorization: Bearer <API_KEY>`, `Content-Type: application/x-www-form-urlencoded` | `command`            | `200 OK`: Command sent successfully<br>`400 Bad Request`: Missing command<br>`401 Unauthorized`: Missing or invalid API Key<br>`403 Forbidden`: Forbidden<br>`500 Internal Server Error`: Failed to send command                                                               |
| Get Console Output | `/getConsoleOutput` | GET    |                                                                                      |                      | `200 OK`: Console output retrieved successfully<br>`500 Internal Server Error`: Failed to retrieve console output                                                                                                                                                              |
| Get Players        | `/getPlayers`       | GET    |                                                                                      |                      | `200 OK`: Player list retrieved successfully<br>`500 Internal Server Error`: Failed to retrieve player list                                                                                                                                                                    |
| Stop Server        | `/stopServer`       | POST   | `Authorization: Bearer <API_KEY>`                                                    |                      | `200 OK`: Server stopped successfully<br>`401 Unauthorized`: Missing or invalid API Key<br>`403 Forbidden`: Forbidden<br>`500 Internal Server Error`: Failed to stop server                                                                                                    |
| Get API Key        | `/getApiKey`        | GET    | `Authorization: IP Based (Config)`                                                   |                      | `200 OK`: API Key retrieved successfully<br>`401 Unauthorized`: Missing or invalid API Key<br>`403 Forbidden`: Forbidden<br>`500 Internal Server Error`: Failed to retrieve API Key                                                                                            |

## Security
**Security may be subject to change. This is due the user inconvenience of IP based security.**
- API Key authentication is used to secure the endpoints. For now only permitted IPs in the plugins config.yml can access the `/getApiKey` endpoint.
- IP address validation is performed to ensure requests are from authorized sources.

## License
This project currently has no license yet.
For now all rights are reserved.
You may however suggest changes via PRs
