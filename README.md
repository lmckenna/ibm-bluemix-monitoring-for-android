# IBM Bluemix Monitoring for Android™: A Sample App

This is a basic Android app meant as a starting project to build on. It currently utilizes private APIs to retrieve data used in the "Monitoring" tab of IBM Bluemix applications.

For now, to get the monitoring test results you will need to know the IP, username and password for the back end HA Proxy API interface, and specify them in the api_endpoints_required.xml value file. This will likely only be available to IBMers.

This source code and resulting app is not supported or endorsed by IBM, it is intended as sample code only. See LICENSE.txt.

## Screenshots

![Login Page Screenshot](screenshots/LoginPage.png?raw=true "Login")
![App List Screenshot](screenshots/AppList.png?raw=true "Application List")
![Test List Screenshot](screenshots/TestList.png?raw=true "Test List")

## Getting Started

Clone this repo using Git, open it in Android Studio and accept the defaults.

### Prerequisities

To use build this app you will need

```
The URL and basic auth credentials of the API env to use
Android Studio 2.1+ (which includes Gradle)
And either:
 A GitHub account to import the code straight from GitHub
 or Git installed so you can clone this repo (https://git-scm.com/downloads)
```

### Steps to run the app

```
1. Install Android Studio 2.1+
2. Either import this repo using "File -> New -> Project from Version Control -> GitHub"
or clone it using command line Git and click "File -> Open" and find the clone
3. Edit the app->src->res->values->api_endpoints_required.xml file to add the API endpoint details
4. Hit the "play" button in the toolbar (create a virtual device if needed - stick to phones for
best results)
```

## Built With

* Android Studio 2.1

## Authors

* **Luke McKenna** - *Initial work* - [lmckenna](https://github.com/lmckenna)

## License

This project is licensed under the Apache v2 License - see the [LICENSE.txt](LICENSE.txt) file for details

## Attribution

Android is a trademark of Google Inc.