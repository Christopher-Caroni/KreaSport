# Kreasport<sup>1</sup>

Android app for orienteering races with Spring server to host and manage the data. Created as part of my studies at IUT A, Lille 1 Univeristy. I continued development on this project for my Erasmus project work at Glasgow Caledonian Univeristy. Read the [project report](doc/Kreasport%20Report.pdf) to learn more.

## Screenshots
![image](screens/preview.png)

## Built With

* [Spring](https://spring.io/) - The web framework used for security, data and authentication.
* [Maven](https://maven.apache.org/) - Dependency Management.
* [Auth0](https://auth0.com/) - Used to authenticate users.
* [MongoDB](https://www.mongodb.com/) - Document based database structure.
* [Heroku](https://www.heroku.com/) - Used to deploy the servers.
* [Realm](https://realm.io/) - Used to store data with the Android app.
* [Osmdroid](https://github.com/osmdroid/osmdroid) - Mapping solution for the Android app.

## Authors

* **Christopher Caroni** - *Idea and project maintainer* - [Github](https://github.com/Christopher-Caroni)

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details

## Project structure

 - **KreaSport**: This folder contains the Android app
 - **servers**: git submodule to [the separate project](https://github.com/Christopher-Caroni/kreasport-server) for the backend and frontend servers.

The two servers are because of a Maven conflict with Auth0 dependencies.

<sup>1</sup> **Copyright disclaimer**: I do not own "Kreasport" nor the logo associated with it. All use is from the original concept developement for [www.kreasport.com](www.kreasport.com) as part of my studies at Lille 1 University.
