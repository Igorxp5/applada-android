# applada-android

## Description

The applada-android is an Android app that connects you to your next soccer, volleyball or basketball match. If you're
interested to met new people or just practice some sport, download the app and see what are the nearest
matches from you.

## How to build

1. Clone this repository and go to the folder:
```
git clone https://github.com/Igorxp5/applada-android
cd applada-android
```

2. Create a file for the environment variables, call it **env.properties**. You can use ***env.sample.properties*** as reference.

```
GOOGLE_MAPS_API_KEY=
API_BASE_URL=
```

3. Fill the environment variables with the Google Maps API Key. Follow this [tutorial ](https://developers.google.com/maps/documentation/android-sdk/get-api-key) if you doubts how generate it.

4. Browse to https://crudcrud.com/ and copy the URL provided by the service, it'll be your API endpoint. **Make sure your API_BASE_URL ends with slash (/).**

5. Compile the app using Android Studio and generate the APK

**Note: crudcrud.com has usage limit of 100 requests. It also expires in 24h. If that happens you need to get another API endpoint, hence re-build the app.**

## How to use the app

- To create a match, just tap "New match" button. Note: Make sure you've allowed the app access your location, otherwise the app won't be able.

## Screenshots

<p>
    <img src="https://github.com/Igorxp5/applada-android/assets/8163093/91e2cb82-a017-46b5-b9fd-b4e69dcd4c4f" width="30%" alt="Close matches">
    <img src="https://github.com/Igorxp5/applada-android/assets/8163093/d7ce8a05-1712-4601-99d0-25c8a03f921b" width="30%" hspace="20" alt="View match">
</p>

## Features

- [X] Visualize the nearest matches
- [X] Offline usage (the app caches close matches)
- [X] Create Match with random fields
- [X] Subscribe to a match 
- [ ] Create Match by manually inserting the fields
- [ ] Notify about match subscription
- [ ] Show error messages in a Snackbar
- [ ] Alert user when their device doesn't have internet connection


## LICENSE

This project is under MIT license.
