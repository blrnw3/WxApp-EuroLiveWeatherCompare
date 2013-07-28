/*
Swipeable wxvar-changer bar
*/
function valcolr(value, num) {
	var tempvals = new Array(-10, -5, 0, 5, 10, 15, 20, 25, 30, 35);
	var humivals = new Array(30, 40, 50, 60, 70, 80, 90, 97);
	var presvals = new Array(970, 980, 990, 1000, 1010, 1015, 1020, 1030, 1040);
	var windvals = new Array(1, 2, 4, 7, 10, 15, 20, 30, 40);
	var rainvals = new Array(0, 0.2, 0.6, 1, 2, 5, 10, 15, 20, 25, 50);
	
	var valcol = new Array(tempvals, rainvals, windvals, humivals, presvals);
	var col_descrip = new Array('temp','rain','wind','humi','pres');
	
	var values = valcol[num];
	var level_type = col_descrip[num];
	
	for(var i = 0; i < values.length; i++) {
		if(value <= values[i]) {
			return 'level' + level_type + '_' + i;
		}
	}
	
	return 'level' + level_type + '_' + i;
}

function debug() {
	document.getElementById("header").innerHTML="Debugging...";
}

//function byDirection(a, b, isDescending) {
//	 return isDescending ? a-b : b-a;
//}

var sortDown = true;
var wxvar = 0; //NB! JS REQUIRES INITIALISATION OF INTS!!!!!!!
var cities;
var valsSorted = new Array();
var valOrders = new Array();
var valsOriginal = new Array();
var sortbyCol = 0;

var varNames = ["Temperature", "Rain", "Wind", "Humidity", "Pressure", "Condition"];

function sorter(index) {
	//make data unique so multi-column sorting works
	valsOriginal[index] = new Array();
	for(var i = 0; i < cities.length; i++) {
		valsOriginal[index][i] = parseInt(window.data[(4 + index)][i]) - i/10000;
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

function updateGUI(sort){
	console.log("GUI updating...");
	if(sort){
		for(var i = 0; i < varNames.length; i++) {
			valsSorted[i].reverse();
			valOrders[i].reverse();
		}
		sortDown = !sortDown;
		sortbyCol = 1;
	}
	
	var cond = sortbyCol > 0;
		
	$('td[class*="level"]').each(function( index ) {
	var value = cond ? valsSorted[wxvar][index] : valsOriginal[wxvar][index];
		$(this).text(Math.round(value));
		$(this).attr( "class", valcolr(value, wxvar) );
	});
		
	$('td[class*="cell"]').each(function( index ) {
	var city = cond ? cities[valOrders[wxvar][index]] : cities[index];
		$(this).text(city);
	});
}

function restore() {
	sortbyCol = 0;
	cities.reverse();
	for(var i = 0; i < varNames.length; i++) {
		valsOriginal[i].reverse();
	}
	updateGUI(false);
}
 
function changeVar() {
	wxvar = (wxvar + 1) % 5;
	document.getElementById("wxvarName").innerHTML=varNames[wxvar];
	updateGUI(false);
}

$(document).ready(function(){
	//alert("FFS");
	cities = window.data[0]; //simplify name
	for(var i = 0; i < varNames.length; i++) {
		sorter(i);
	}
	updateGUI(false);
});