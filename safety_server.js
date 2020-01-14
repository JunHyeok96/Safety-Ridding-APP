function Mobility(){
    this.id = 'default';
    this.type = 'default';
    this.latitude = '0'; 
    this.longitude='0';
}
function Car(){
    this.id = 'default';
    this.type = 'default';
    this.latitude = '0'; 
    this.longitude='0';
}
function deg2rad(degrees)
{
  var pi = Math.PI;
  return degrees * (pi/180);
}

function rad2deg(radians)
{
    var pi = Math.PI;
    return radians * (180/pi);
}

function distance(lat1, lon1, lat2, lon2) { // 두 개의 point 거리 계산
    lat1 = parseFloat(lat1)
    lat2 = parseFloat(lat2)
    lon1 = parseFloat(lon1)
    lon2 = parseFloat(lon2)
5
    let theta = lon1 - lon2;
    let dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
    dist = Math.acos(dist);
    dist = rad2deg(dist);
    dist = dist * 60 * 1.1515;
    return dist = dist * 1609.344; // 미터로 변환
}
rush_car = new Car();
rush1 = new Mobility();
rush2 = new Mobility();
rush3 = new Mobility();

var moment = require('moment');
require('moment-timezone');
moment.tz.setDefault("Asia/Seoul");

var arrayMobility = [rush1, rush2, rush3];
//IP주소가 변화하면 안드로이드 앱 내에 있는 url 주소도 바꿔주어야 정상 동작하기시작함!
const express = require('express');
const http = require('http');
const bodyParser = require('body-parser');
const app = express();

app.set('port', process.env.PORT || 5000);
app.use(bodyParser.urlencoded({
    extended: false
}));
app.use(bodyParser.json()); 
//첫 번째 미들웨어
app.use((req, res, next) => {
    const id = req.body.id;
    const type = req.body.type;
    const latitude = req.body.latitude;
    const longitude = req.body.longitude;
    if(type == "car"){
        rush_car.id = id;
        rush_car.type = type;
        rush_car.latitude = latitude;
        rush_car.longitude = longitude;
        console.log(moment().format('YYYY-MM-DD HH:mm:ss') + "  " +id+"차량 값 갱신. ");
        var resultArray =  [];
        for(var i=0; i<3 ; i++){
            dist = distance(latitude, longitude, arrayMobility[i].latitude, arrayMobility[i].longitude);
            if(parseFloat(dist)<parseInt(100)){
            var result = new Object();
            result.id = arrayMobility[i].id;
            result.type = arrayMobility[i].type;
            result.latitude = arrayMobility[i].latitude;
            result.longitude = arrayMobility[i].longitude;
            resultArray.push(result)
            console.log("                     "+ "반경내 " + result.id +"_"+result.type+"정보가 표시됩니다." + " 거리 : " + parseInt(dist)+"m");
            }
        }
    res.contentType('application/json');
    res.send(JSON.stringify(resultArray));
    }
    else{
        if(id=="RUSH"){
            rush1.id = id;
            rush1.type = type;
            rush1.longitude = longitude;
            rush1.latitude = latitude;
            console.log(moment().format('YYYY-MM-DD HH:mm:ss') + "  " +rush1.id+"_"+rush1.type+"값 갱신." +latitude+", " +longitude );
        }
        else if(id == "RUSH2"){
            rush2.id = id;
            rush2.type = type;
            rush2.longitude = longitude;
            rush2.latitude = latitude;
            console.log(moment().format('YYYY-MM-DD HH:mm:ss') + "  "  +rush2.id+"_"+rush2.type+"값 갱신." +latitude+", " +longitude );
        }
        else if(id =="RUSH3"){
            rush3.id = id;
            rush3.type = type;
            rush3.longitude = longitude;
            rush3.latitude = latitude;
            console.log(moment().format('YYYY-MM-DD HH:mm:ss') + "  "  +rush3.id+"_"+rush3.type+"값 갱신. " +latitude+", " +longitude );
        }
        var resultArray =  [];
        dist = distance(latitude, longitude, rush_car.latitude, rush_car.longitude);
        if(parseFloat(dist)<parseInt(50)){
        var result = new Object();
        result.id = rush_car.id;
        result.type = rush_car.type;
        result.latitude = rush_car.latitude;
        result.longitude = rush_car.longitude;
        resultArray.push(result)
        console.log(moment().format('YYYY-MM-DD HH:mm:ss') + "  "  +"반경내 " + result.id +"차량 정보가 표시됩니다." + " 거리 : " + parseInt(dist)+"m");
    }
    res.contentType('application/json');
    res.send(JSON.stringify(resultArray));
    res.end()
}
});

var server = http.createServer(app).listen(app.get('port'), () => {
    console.log(moment().format('YYYY-MM-DD HH:mm:ss') + "  "  +"익스프레스로 웹 서버를 실행함 : " + app.get('port'));
});