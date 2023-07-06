import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'dart:typed_data';
import 'package:http/http.dart' as http;

import 'TestPage.dart';

class Convert extends StatefulWidget {
  const Convert({super.key});

  @override
  State<Convert> createState() => _ConvertState();
}

class _ConvertState extends State<Convert> {
  static const methodChannel = const MethodChannel("conversion");

  List<Widget> listPlaylistDepart = [];
  List<Widget> listPlaylistArrive = [];

  List<List<String>> listMusiques = [];

  @override
  void initState() {
    super.initState();
  }

  Future<void> loadSpotifyPlaylist() async {
    List<List<String>> playlist = await getSpotifyPlaylist();

    if (playlist != null) {
      List<Widget> containers = playlist.map((playlist) {

        String name = playlist[0];
        String playlistId = playlist[1];
        String nbMusiques = playlist[2];
        String url = playlist[3];

        return Container(
          margin: EdgeInsets.only(left: 30, right: 30, top: 30),
          width: MediaQuery.of(context).size.width,
          height: 80,
          child: Row(
            children: [
              Container(
                width: 80,
                height: 80,
                child: url != null && url.isNotEmpty
                    ? Image.network(
                  url,
                  fit: BoxFit.cover,
                )
                    : Container(),
              ),
              Container(
                margin: EdgeInsets.only(left: 20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Container(
                      width : 180,
                      height: 35,
                      child: Text(
                        name,
                        style: TextStyle(fontSize: 30, fontWeight: FontWeight.bold, color: Colors.white),
                      ),
                    ),
                    Container(
                      margin: EdgeInsets.only(top: 5),
                      child: Text(
                        nbMusiques + " titres",
                        style: TextStyle(fontSize: 25, fontWeight: FontWeight.w500, color: Color(0xFF696969)),
                      ),
                    ),
                  ],
                ),
              ),
              GestureDetector(
                onTap: (){
                  getSpotifyMusics(playlistId);
                  setState(() {
                    depart = false;
                  });
                },
                child: Container(
                  width: 60,
                  height: 40,
                  margin: EdgeInsets.only(left: 11),
                  decoration: BoxDecoration(
                      image: DecorationImage(
                          image: AssetImage("assets/arrow_right.png"),
                          fit: BoxFit.fitHeight
                      )
                  ),
                ),
              )
            ],
          ),
        );
      }).toList();

      setState(() {
        listPlaylistDepart = containers;
      });

    }
  }

  Future<List<List<String>>> getSpotifyPlaylist() async {

    try {
      final List<dynamic> result = await methodChannel.invokeMethod('spotifyPlaylist');
      List<List<String>> playlist = result.map<List<String>>((row) => List<String>.from(row.map<String>((item) => item.toString()))).toList();
      print('Taille playlist : ' + playlist.length.toString());
      return playlist;
    } on PlatformException catch (e) {
      print("Error calling SpotifyPlaylist method: ${e.message}");
      return []; // Or any other default value as needed
    }
  }

  Future<List<List<String>>> getSpotifyMusics(String playlistId) async {

    try {
      final List<dynamic> result = await methodChannel.invokeMethod('spotifyMusics', {"playlistId" : playlistId});
      List<List<String>> musiques = result.map<List<String>>((row) => List<String>.from(row.map<String>((item) => item.toString()))).toList();
      listMusiques = musiques;
      return musiques;
    } on PlatformException catch (e) {
      print("Error calling SpotifyPlaylist method: ${e.message}");
      return []; // Or any other default value as needed
    }
  }

  // Future<List<List<String>>> afficherMusiques() async {
  //
  //   try {
  //     final List<dynamic> result = await methodChannel.invokeMethod('spotifyMusics', {"playlistId" : playlistId});
  //     List<List<String>> musiques = result.map<List<String>>((row) => List<String>.from(row.map<String>((item) => item.toString()))).toList();
  //     listMusiques = musiques;
  //     return musiques;
  //   } on PlatformException catch (e) {
  //     print("Error calling SpotifyPlaylist method: ${e.message}");
  //     return []; // Or any other default value as needed
  //   }
  // }


  bool depart = true;
  PageController _pageControllerDepart = PageController(initialPage: 0);
  PageController _pageControllerArrivee = PageController(initialPage: 0);
  int _currentPageIndexDepart = 0;
  int _currentPageIndexArrivee = 0;
  int _pageCount = 2;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        color: Color(0xFF14161B),
        width: MediaQuery.of(context).size.width,
        height: MediaQuery.of(context).size.height,
        child: ListView(
          children: [
            Container(
              width: MediaQuery.of(context).size.width,
              margin: EdgeInsets.only(top: 30),
              child: Text(
                "Conversion",
                style: TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 40,
                    color: Colors.white),
                textAlign: TextAlign.center,
              ),
            ),
            Container(
              margin: EdgeInsets.only(top: 40),
              child: Column(
                children: [
                  Container(
                    width: MediaQuery.of(context).size.width - 60,
                    child: Row(
                      children: [
                        GestureDetector(
                          onTap: () {
                            setState(() {
                              depart = true;
                              _currentPageIndexDepart = 0;
                            });
                          },
                          child: Container(
                            width: (MediaQuery.of(context).size.width - 60) / 2,
                            child: Column(
                              children: [
                                Container(
                                  margin: EdgeInsets.only(bottom: 10),
                                  child: Text(
                                    "Départ",
                                    style: TextStyle(
                                        fontSize: 25,
                                        color: Colors.white,
                                        fontWeight: FontWeight.w500),
                                  ),
                                ),
                                Visibility(
                                  visible: depart,
                                  child: Container(
                                    width: 100,
                                    height: 5,
                                    decoration: BoxDecoration(
                                        color: Color(0xFF864BFE),
                                        borderRadius: BorderRadius.only(
                                            topLeft: Radius.circular(4),
                                            topRight: Radius.circular(4),
                                            bottomLeft: Radius.circular(1),
                                            bottomRight: Radius.circular(1))),
                                  ),
                                )
                              ],
                            ),
                          ),
                        ),
                        GestureDetector(
                          onTap: () {
                            setState(() {
                              depart = false;
                              _currentPageIndexArrivee = 0;
                            });
                          },
                          child: Container(
                            width: (MediaQuery.of(context).size.width - 60) / 2,
                            child: Column(
                              children: [
                                Container(
                                  margin: EdgeInsets.only(bottom: 10),
                                  child: Text(
                                    "Arrivée",
                                    style: TextStyle(
                                        fontSize: 25,
                                        color: Colors.white,
                                        fontWeight: FontWeight.w500),
                                  ),
                                ),
                                Visibility(
                                  visible: !depart,
                                  child: Container(
                                    width: 100,
                                    height: 5,
                                    decoration: BoxDecoration(
                                        color: Color(0xFF864BFE),
                                        borderRadius: BorderRadius.only(
                                            topLeft: Radius.circular(4),
                                            topRight: Radius.circular(4),
                                            bottomLeft: Radius.circular(1),
                                            bottomRight: Radius.circular(1))),
                                  ),
                                )
                              ],
                            ),
                          ),
                        )
                      ],
                    ),
                  ),
                  Container(
                    width: MediaQuery.of(context).size.width - 60,
                    height: 2,
                    color: Color(0xFF444444),
                  )
                ],
              ),
            ),
            Visibility(
              visible: depart,
              child: Column(
                children: [
                  Visibility(
                    child: Container(
                      margin: EdgeInsets.only(top: 30),
                      height: 360,
                      child: Column(
                        children: [
                          Container(
                            width: 340,
                            height: 300,
                            child: PageView(
                              controller: _pageControllerDepart,
                              onPageChanged: (int index) {
                                setState(() {
                                  _currentPageIndexDepart = index;
                                });
                              },
                              children: [
                                Container(
                                  margin: EdgeInsets.only(left: 20, right: 20),
                                  child: Container(
                                    decoration: BoxDecoration(
                                      borderRadius: BorderRadius.circular(15),
                                      gradient: LinearGradient(
                                        begin: Alignment.topLeft,
                                        end: Alignment.bottomRight,
                                        colors: [
                                          Color.fromRGBO(255, 255, 255, 0.3),
                                          Color.fromRGBO(124, 124, 124, 0.05),
                                        ],
                                      ),
                                      /*boxShadow: [
                                BoxShadow(
                                  color: Colors.black,
                                  blurRadius: 100,
                                  offset: Offset(4, 8)
                                )
                              ]*/
                                    ),
                                    child: Column(
                                      children: [
                                        Container(
                                          margin: EdgeInsets.only(top: 20),
                                          child: Image(
                                            image: AssetImage('assets/spotify.png'),
                                          ),
                                        ),
                                        Container(
                                          margin: EdgeInsets.only(top: 20, left: 15, right: 15),
                                          child: Text(
                                            "Convertir une playlist depuis Spotify",
                                            style: TextStyle(fontWeight: FontWeight.bold, fontSize: 25, color: Colors.white),
                                            textAlign: TextAlign.center,
                                          ),
                                        ),
                                        GestureDetector(
                                          onTap: () async {
                                            loadSpotifyPlaylist();
                                          },
                                          child: Container(
                                            margin: EdgeInsets.only(top: 40),
                                            width: 180,
                                            height: 60,
                                            decoration: BoxDecoration(
                                              color: Color(0xFF864BFE),
                                              borderRadius: BorderRadius.circular(30),
                                            ),
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Text(
                                                "Connexion",
                                                style: TextStyle(fontSize: 25, fontWeight: FontWeight.bold, color: Colors.white),
                                              ),
                                            ),
                                          )
                                          ,
                                        )
                                      ],
                                    ),
                                  ),
                                ),
                                Container(
                                  margin: EdgeInsets.only(left: 20, right: 20),
                                  child: Container(
                                    decoration: BoxDecoration(
                                      borderRadius: BorderRadius.circular(15),
                                      gradient: LinearGradient(
                                        begin: Alignment.topLeft,
                                        end: Alignment.bottomRight,
                                        colors: [
                                          Color.fromRGBO(255, 255, 255, 0.3),
                                          Color.fromRGBO(124, 124, 124, 0.05),
                                        ],
                                      ),
                                      /*boxShadow: [
                                BoxShadow(
                                  color: Colors.black,
                                  blurRadius: 100,
                                  offset: Offset(4, 8)
                                )
                              ]*/
                                    ),
                                    child: Column(
                                      children: [
                                        Container(
                                          margin: EdgeInsets.only(top: 20),
                                          child: Image(
                                            image: AssetImage('assets/deezer.png'),
                                          ),
                                        ),
                                        Container(
                                          margin: EdgeInsets.only(top: 20, left: 15, right: 15),
                                          child: Text(
                                            "Convertir une playlist depuis Deezer",
                                            style: TextStyle(fontWeight: FontWeight.bold, fontSize: 25, color: Colors.white),
                                            textAlign: TextAlign.center,
                                          ),
                                        ),
                                        GestureDetector(
                                          onTap: (){
                                            methodChannel.invokeMethod("deezerPlaylist");
                                          },
                                          child: Container(
                                            margin: EdgeInsets.only(top: 40),
                                            width: 180,
                                            height: 60,
                                            decoration: BoxDecoration(
                                              color: Color(0xFF864BFE),
                                              borderRadius: BorderRadius.circular(30),
                                            ),
                                            child: Align(
                                              alignment: Alignment.center,
                                              child: Text(
                                                 "Connexion",
                                                style: TextStyle(fontSize: 25, fontWeight: FontWeight.bold, color: Colors.white),
                                              ),
                                            ),
                                          ),
                                        )
                                      ],
                                    ),
                                  ),

                                ),
                              ],
                            ),
                          ),// Espacement entre le PageView et les bulles
                          SizedBox(height: 20), // Espacement entre le PageView et les bulles
                          Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: List.generate(
                              _pageCount,
                                  (index) => AnimatedContainer(
                                duration: Duration(milliseconds: 300), // Durée de l'animation
                                curve: Curves.easeInOut, // Courbe d'animation
                                margin: EdgeInsets.symmetric(horizontal: 4),
                                width: _currentPageIndexDepart == index ? 50 : 10,
                                height: 10,
                                decoration: BoxDecoration(
                                  shape: BoxShape.rectangle,
                                  borderRadius: BorderRadius.circular(5),
                                  color: _currentPageIndexDepart == index ? Color(0xFFED1515) : Colors.white,
                                ),
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                  Container(
                    height: 300,
                    child: ListView.builder(
                      itemCount: listPlaylistDepart.length,
                      itemBuilder: (BuildContext context, int index) {
                        return listPlaylistDepart[index];
                      },
                    ),
                  ),
                ],
              ),
            ),
            Visibility(
              visible: !depart,
              child: Column(
                children: [
                  Visibility(
                    child: Container(
                      margin: EdgeInsets.only(top: 30),
                      height: 400,
                      child: Column(
                        children: [
                          Container(
                            width: 340,
                            height: 300,
                            child: PageView(
                              controller: _pageControllerArrivee,
                              onPageChanged: (int index) {
                                setState(() {
                                  _currentPageIndexArrivee = index;
                                });
                              },
                              children: [
                                Container(
                                  margin: EdgeInsets.only(left: 20, right: 20),
                                  child: Container(
                                    decoration: BoxDecoration(
                                      borderRadius: BorderRadius.circular(15),
                                      gradient: LinearGradient(
                                        begin: Alignment.topLeft,
                                        end: Alignment.bottomRight,
                                        colors: [
                                          Color.fromRGBO(255, 255, 255, 0.3),
                                          Color.fromRGBO(124, 124, 124, 0.05),
                                        ],
                                      ),
                                      /*boxShadow: [
                                BoxShadow(
                                  color: Colors.black,
                                  blurRadius: 100,
                                  offset: Offset(4, 8)
                                )
                              ]*/
                                    ),
                                    child: Column(
                                      children: [
                                        Container(
                                          margin: EdgeInsets.only(top: 20),
                                          child: Image(
                                            image: AssetImage('assets/spotify.png'),
                                          ),
                                        ),
                                        Container(
                                          margin: EdgeInsets.only(top: 20, left: 15, right: 15),
                                          child: Text(
                                            "Convertir une playlist vers Spotify",
                                            style: TextStyle(fontWeight: FontWeight.bold, fontSize: 25, color: Colors.white),
                                            textAlign: TextAlign.center,
                                          ),
                                        ),
                                        Container(
                                          margin: EdgeInsets.only(top: 40),
                                          width: 180,
                                          height: 60,
                                          decoration: BoxDecoration(
                                            color: Color(0xFF864BFE),
                                            borderRadius: BorderRadius.circular(30),
                                          ),
                                          child: Align(
                                            alignment: Alignment.center,
                                            child: Text(
                                              "Connexion",
                                              style: TextStyle(fontSize: 25, fontWeight: FontWeight.bold, color: Colors.white),
                                            ),
                                          ),
                                        )
                                      ],
                                    ),
                                  ),
                                ),
                                Container(
                                  margin: EdgeInsets.only(left: 20, right: 20),
                                  child: Container(
                                    decoration: BoxDecoration(
                                      borderRadius: BorderRadius.circular(15),
                                      gradient: LinearGradient(
                                        begin: Alignment.topLeft,
                                        end: Alignment.bottomRight,
                                        colors: [
                                          Color.fromRGBO(255, 255, 255, 0.3),
                                          Color.fromRGBO(124, 124, 124, 0.05),
                                        ],
                                      ),
                                    ),
                                    child: Column(
                                      children: [
                                        Container(
                                          margin: EdgeInsets.only(top: 20),
                                          child: Image(
                                            image: AssetImage('assets/deezer.png'),
                                          ),
                                        ),
                                        Container(
                                          margin: EdgeInsets.only(top: 20, left: 15, right: 15),
                                          child: Text(
                                            "Convertir une playlist vers Deezer",
                                            style: TextStyle(fontWeight: FontWeight.bold, fontSize: 25, color: Colors.white),
                                            textAlign: TextAlign.center,
                                          ),
                                        ),
                                        Container(
                                          margin: EdgeInsets.only(top: 40),
                                          width: 180,
                                          height: 60,
                                          decoration: BoxDecoration(
                                            color: Color(0xFF864BFE),
                                            borderRadius: BorderRadius.circular(30),
                                          ),
                                          child: Align(
                                            alignment: Alignment.center,
                                            child: Text(
                                              "Connexion",
                                              style: TextStyle(fontSize: 25, fontWeight: FontWeight.bold, color: Colors.white),
                                            ),
                                          ),
                                        )
                                      ],
                                    ),
                                  ),

                                ),
                              ],
                            ),
                          ),
                          SizedBox(height: 20), // Espacement entre le PageView et les bulles
                          Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: List.generate(
                              _pageCount,
                                  (index) => AnimatedContainer(
                                duration: Duration(milliseconds: 300), // Durée de l'animation
                                curve: Curves.easeInOut, // Courbe d'animation
                                margin: EdgeInsets.symmetric(horizontal: 4),
                                width: _currentPageIndexArrivee == index ? 50 : 10,
                                height: 10,
                                decoration: BoxDecoration(
                                  shape: BoxShape.rectangle,
                                  borderRadius: BorderRadius.circular(5),
                                  color: _currentPageIndexArrivee == index ? Color(0xFFED1515) : Colors.white,
                                ),
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                  Container(
                    height: 300,
                    child: ListView.builder(
                      itemCount: listPlaylistArrive.length,
                      itemBuilder: (BuildContext context, int index) {
                        return listPlaylistArrive[index];
                      },
                    ),
                  ),
                ],
              ),
            )

          ],
        ),

      ),
    );
  }



  Future<Uint8List?> fetchImage(String url) async {
    print('test');
    try {
      final response = await http.get(Uri.parse(url));
      if (response.statusCode == 200) {
        return response.bodyBytes;
      }
    } catch (e) {
      print('Error fetching image: $e');
    }
    return null;
  }


}
