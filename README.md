# Spotify Discover Weekly Saver

This project is used to save the "Discover Weekly" playlist created by Spotify. 
The jar is executed by a cron job every week and stores the songs in a different playlist.

## Config

The app.config is the configuration file and must be located in the same folder as the jar.  
If there is no app.config, the program will request all parameters.  

In order to use the Spotify API, a client ID and a client secret are required.  
These can be created here:
[Spotify Developer Dashboard](https://developer.spotify.com/dashboard/applications)

`ClientId` From Developer Dashboard  
`ClientSecret` From Developer Dashboard  
`PlaylistIdWeekly`  The Playlist ID from the Discover Weekly Playlist  
`PlaylistId` The Playlist ID of the playlist in which the songs should be saved  
`RefreshToken` Used to generate an access token, more below  
`RemoveDuplications` True or False depending on whether duplicates should be removed  

Since the application runs in the background, no login with spotify is possible to get an access token.     
Therefore, a refresh token is used here, which can be used to generate an access token.   
You can find out more here: [Authorization Code Flow](https://developer.spotify.com/documentation/general/guides/authorization/code-flow/)

A refresh token can be generated with the [Spotify Auth examples](https://github.com/spotify/web-api-auth-examples)

## Build

Since this is a Gradle project, Gradle must be installed to build the project. [Gradle](https://docs.gradle.org/current/userguide/installation.html)  
After that you can simply type `gradlew jar` and the jar file will be built. The jar file is then located in `/build/libs`.