import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class Test extends StatefulWidget {
  const Test({super.key});

  @override
  State<Test> createState() => _TestState();
}

class _TestState extends State<Test> {
  static const methodChannel = const MethodChannel("conversion");


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        color: Colors.white,
        child: ListView(
          children: [
            Container(
              margin: EdgeInsets.only(top: 300),
              child: Column(
                children: [
                  GestureDetector(
                    onTap: (){
                       methodChannel.invokeMethod("test");
                    },
                    child: Container(
                      width: 200,
                      height: 70,
                      color: Colors.black,
                      child: Text(
                        "Connexion spotify",
                        style: TextStyle(fontSize: 25, fontWeight: FontWeight.bold, color: Colors.white),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ),
                  SizedBox(height: 20),
                  Container(
                    width: 200,
                    height: 70,
                    color: Colors.black,
                    child: Text(
                      "Connexion Deezer",
                      style: TextStyle(fontSize: 25, fontWeight: FontWeight.bold, color: Colors.white),
                      textAlign: TextAlign.center,
                    ),
                  )
                ],
              ),
            )
          ],
        ),
      ),
    );
  }
}



