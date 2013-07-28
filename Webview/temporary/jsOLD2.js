/*
Swipeable wxvar-changer bar
*/
function valcolr(value, num) {
	var tempvals = new Array(-10, -5, 0, 5, 10, 15, 20, 25, 30, 35);
	var humivals = new Array(30, 40, 50, 60, 70, 80, 90, 97);
	var presvals = new Array(970, 980, 990, 1000, 1010, 1015, 1020, 1030, 1040);
	var windvals = new Array(1, 2, 4, 7, 10, 15, 20, 30, 40);
	var rainvals = new Array(0, 0.2, 0.6, 1, 2, 5, 10, 15, 20, 25, 50);
	var condvals = new Array(0,1,2,3,4,5,6,7,8);
	
	var valcol = new Array(tempvals, rainvals, windvals, humivals, presvals, condvals);
	var col_descrip = new Array('temp','rain','wind','humi','pres','cond');
	
	var values = valcol[num];
	var level_type = col_descrip[num];
	
	for(var i = 0; i < values.length; i++) {
		if(value <= values[i]) {
			return 'level' + level_type + '_' + i;
		}
	}
	
	return 'level' + level_type + '_' + i;
}

// function debug() {
	// document.getElementById("header").innerHTML="Debugging...";
// }

//function byDirection(a, b, isDescending) {
//	 return isDescending ? a-b : b-a;
//}

var sortDown = new Array();
sortDown[0] = false;
sortDown[1] = true;
//var citiesReversed = false;
var wxvar = 0; //NB! JS REQUIRES INITIALISATION OF INTS!!!!!!!
var cities;
var citiesReversed;
var cityDirAsc = true;
var countries;
var userCities;
var valsSorted = new Array();
var valOrders = new Array();
var valsOriginal = new Array();
// var ranks = new Array();
var sortbyCol = 0;

var varNames = ["Temperature", "Rain", "Wind", "Humidity", "Pressure", "Condition"];
var ICON_LABELS = [ "Sunny", "Clear", "Partly Cloudy", "Partly Cloudy", "Cloudy", "Rainy",
		"Snowy", "Foggy", "Thundery" ];

function sorter(index) {
	//make data unique so multi-column sorting works on non-unique data
	valsOriginal[index] = new Array();
	for(var i = 0; i < cities.length; i++) {
		valsOriginal[index][i] = parseInt(window.data[(3 + index)][i]) - i/10000;
	}
		
	valsSorted[index] = valsOriginal[index].slice(); //make copy of array
	valOrders[index] = new Array();

	//sort array, ascending
	valsSorted[index].sort(function(a,b){
		return a-b
	});

	//Get the mapping between sorted and unsorted
	for(var i = 0; i < cities.length; i++) {
		valOrders[index][i] = valsOriginal[index].indexOf(valsSorted[index][i]);
	}
	console.log(valsSorted[index]);
}

function arrow(type) {
	blank = "&nbsp;";
	var line1 = (type == "up") ? blank : "&#9650;";
	var line2 = (type == "down") ? blank : "&#9660;";
	return line1 + "<br />" + line2;
}

function updateGUI(sort){
	console.log("GUI updating... " + sort);
	document.getElementById("wxvarTitle").innerHTML=varNames[wxvar];
	
	if(sort){
		for(var i = 0; i < varNames.length; i++) {
			valsSorted[i].reverse();
			valOrders[i].reverse();
		}
		
		sortDown[1] = !sortDown[1];
		sortbyCol = 1;
		

		sortArrow = sortDown[1] ? arrow("down") : arrow("up");
		document.getElementsByClassName('tableHeadSortArrows')[1].innerHTML=sortArrow;
		document.getElementsByClassName('tableHeadSortArrows')[0].innerHTML=arrow("both");
	}
	
	var cond = sortbyCol > 0;
	console.log(cond);
	$('td[class*="level"]').each(function( index ) {
		var value = cond ? valsSorted[wxvar][index] : valsOriginal[wxvar][index];	
		var finalValue = Math.round(value);
		if(wxvar == varNames.length - 1) {
			finalValue = ICON_LABELS[finalValue];
		}		
		$(this).text(finalValue);
		$(this).attr( "class", valcolr(value, wxvar) );
	});
	// $('td[class*="rank"]').each(function( index ) {
		// var rank = !cond ? valsSorted[wxvar].indexOf(valsOriginal[wxvar][index]) + 1 : (index + 1);
		// $(this).text(rank);
	// });
	var rankNodes = document.getElementsByClassName('rank');
	for(var i = 0; i < cities.length; i++) {
		var rank = !cond ? valsSorted[wxvar].indexOf(valsOriginal[wxvar][i]) + 1 : (i + 1);
		rankNodes[i].innerHTML = rank;
	}

	console.log(valOrders[wxvar]);
	//var lol = " [";
	$('td[class*="city"]').each(function( index ) {
		//lol += "\"" + cities[valOrders[wxvar][index]] + "\", ";
		var accessIndex = cond ? valOrders[wxvar][index] : index;
		var city = (cityDirAsc || sort) ? cities[accessIndex] : citiesReversed[accessIndex];
		$(this).text(city);
		// $(this).attr( "style", highlight);
	});
	//console.log(lol + "]");
	// var cityNodes = document.getElementsByClassName('city');
	// var rowNodes = document.getElementsByClassName('r');
	// for(var i = 0; i < cities.length; i++) {
		// var accessIndex = cond ? valOrders[wxvar][i] : i;
		// var city = cities[accessIndex];
		// var highlight = (userCities[accessIndex] == 1) ? "bold" : "normal";
		// cityNodes[i].innerHTML = city;
		// rowNodes[i].style.fontWeight = highlight;
	// }
	// console.log(cityNodes.item(0));
		
	$('td[class*="country"]').each(function( index ) {
		var accessIndex = cond ? valOrders[wxvar][index] : index;
		var cntry = countries[accessIndex];
		$(this).text(cntry);
	});
}

function restore() {
	sortArrow = sortDown[0] ? arrow("down") : arrow("up");
	document.getElementsByClassName('tableHeadSortArrows')[0].innerHTML=sortArrow;
	document.getElementsByClassName('tableHeadSortArrows')[1].innerHTML=arrow("both");

	sortDown[0] = !sortDown[0];
	sortbyCol = 0;
	
	// citiesReversed = !citiesReversed;
	// console.log("citiesReversed = " + citiesReversed);
	countries.reverse();
	userCities.reverse();
	for(var i = 0; i < varNames.length; i++) {
		valsOriginal[i].reverse();
	}
	cityDirAsc = !cityDirAsc;
	updateGUI(false);
}

function countrysort() {
	
}
 
function changeVar(increase) {
	var amount = increase ? 1 : varNames.length - 1;
	wxvar = (wxvar + amount) % varNames.length;
	console.log("Wvar: " + wxvar);
	updateGUI(false);
}

$(document).ready(function(){
	//asign names to some of the arrays
	cities = window.data[0];
	citiesReversed = cities.slice();
	citiesReversed.reverse();
	countries = window.data[1];
	userCities = window.data[2];
	//sort the weather data
	for(var i = 0; i < varNames.length; i++) {
		sorter(i);
	}
	updateGUI(true);
});