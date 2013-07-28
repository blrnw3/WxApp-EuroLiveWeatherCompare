<?php
/**
* Web page aimed at users of the Europe Live Weather Compare App, which hosts a webview of this page.
* Live weather data for cities across Europe is shown in tabular form and is sortable.
* Author: Ben Lee-Rodgers
* Date: Jan 2013
*/

$scriptbeg = microtime(get_as_float);
$root = '/home/nwweathe/public_html/';
$appPath = $root.'CP_Solutions/WxApp/';

// if(!isset($_SESSION)) {
    // session_start();
// }

$expTime = 3600 * 24 * 1000;
if($_COOKIE['me']) {
    $me = true;
}
elseif(isset($_GET['blr'])) {
    $me = true;
    setcookie("me", true, time() + $expTime);
}

$desktop = isset($_GET['desktop']);
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>City Rankings</title>
		<?php
		if(!$desktop) {
			echo ' <meta name="viewport" content="width=device-width, user-scalable=no" />';
			//$style = 'wide';
		} //else { $style = 'main'; }
		//echo '<link rel="stylesheet" type="text/css" href="./'.$style.'style.css" media="screen" title="screen" />';
		?>
		<meta name="description" content="City weather rankings across Europe" />
	<meta name="keywords" content="weather, Europe, nw3, data, records, statistics, rankings, live" />
	<meta http-equiv="content-type" content="application/xhtml+xml; charset=ISO-8859-1" />
	<meta http-equiv="content-language" content="en-GB" />
	<link rel="stylesheet" type="text/css" href="./valcolstyle.php" media="screen" title="screen" />
	
	<?php if($desktop && !$me) { include('ggltrack.php'); } ?>
	<script type="text/javascript" src="./js.js"></script>
	
	<style type="text/css">
	<?php require 'style.php'; ?>
	</style>
	</head>
	<body onload="setup()">
		<!-- ##### Header ##### -->
	<div id="background">
<div id="page">
<div id="header">
  <?php echo date('D d M Y, H:i', 50 + (int) file_get_contents($root."CP_Solutions/updated.txt")); ?>
</div>

	<!-- ##### Main ##### -->
	<div id="main">

<?php
//Connect to MySQL server
$con = mysql_connect("localhost","nwweathe_blr","uclgc02appwx");
if (!$con) {
	die('Connection error: ' . mysql_error());
}
//Connect to database
$db = mysql_select_db("nwweathe_wxApp", $con);
if (!$db) {
	die('Database error: ' . mysql_error());
}
//Query database
$result = mysql_query("SELECT `Name`,`Temperature`,`Rain`,`Wind`,`Humidity`,`Pressure`,`Condition` FROM `CityWeather` ORDER BY `Name` ASC");
if (!$result) {
	die ('Query error: ' . mysql_error());
}


//read and process user input
$query = isset($_GET['cities']) ? $_GET['cities'] : 'Hampstead';
$userCities = explode(',',$query);

//Produce output from SQL query
$cnt = 0;
$data = array();

while($row = mysql_fetch_assoc($result)) {
	$subCnt = 0;
	foreach($row as $col) {
		$data[$subCnt][$cnt] = $col;
		$subCnt++;
	}
	$isUserCity = in_array($data[0][$cnt], $userCities);
	$data[$subCnt][$cnt] = (int) $isUserCity;
   $cnt++;
}

//Make table
echo '
	<table>
	<tr>
		<th colspan="4">
			<span id="leftArrow" class="sideArrow" onclick="changeVar(false)">
			<!-- <span class="sideArrowText">Humidity</span><br /> -->
				&#8678; 
			</span>
			<span id="tableTitle" onclick="changeVar(true)">Live Euro Wx</span>
			<span id="rightArrow" class="sideArrow" onclick="changeVar(true)">
				<!-- <span class="sideArrowText">Wind</span> <br /> --> 
				&#8680;
			</span>
		</th>
	</tr>
	<tr class="table-top">
		<td id="city" onclick="restore()">
			<div class="tableSubTitle">City</div>
			<div class="tableHeadSortArrows"></div>
		</td>
		<!-- <td id="country" onclick="countrysort()">
		<div class="tableSubTitle">Country</div>
		<div class="tableHeadSortArrows"></div>
		</td> -->
		<td id="wxvarName" onclick="updateGUI(true)">
			<div id="wxvarTitle" class="tableSubTitle"> <!--<img src="./thermom8_small.png" alt="img_curr" />--></div>
			<div class="tableHeadSortArrows"></div>
		</td>
		<td id="wxvarRank" onclick="updateGUI(true)">Rank</td>
	</tr>';

for($i = 0; $i < $cnt; $i++) {
	echo '
	<tr class="'.alternateColour($i, "row").' r">
		<td class="city"></td>
		<td class="value"></td>
		<td class="rank"></td>
	</tr>';
}
echo "</table>";

?>
		</div>

<!-- ##### Footer ##### -->
<div id="footer">
	<div>
		<a href="#header">Top</a>
	</div>
	<div>
		&copy; 2012-<?php echo date('Y'); ?>, B. Lee-Rodgers
	</div>
	<div>
		NB: Accuracy and reliability of data is not guaranteed
	</div>
	<div>
		<span style="font-size:85%">
			<?php $phpload = roundToDp( microtime(get_as_float) - $scriptbeg, 3 );
				echo 'Version 0 | Script executed in ' . $phpload . 's'; ?>
		</span>
	</div>
</div>

</div>
</div>
<?php
//Store data on the webpage for the JS script to use, source:
//	http://stackoverflow.com/questions/899327/how-to-store-data-on-a-page-and-retrieve-it-through-jquery
echo '<script type="text/javascript">
	window.data = 
	' . json_encode($data) . '
	</script>';
?>
	</body>
</html>
<?php
$desktop = $desktop ? 'Desktop ' : 'Mobile ';
if(!$me) {
	file_put_contents( $appPath."visitLog.txt", date("H:i:s d/m/Y") . "\t" .
		$phpload . "\t" . "$desktop" . "\t" .  $_SERVER['REMOTE_ADDR']. ' ' .
		$_SERVER['HTTP_USER_AGENT']	. "\r\n", FILE_APPEND );
}

function roundToDp($value, $dp = 1) {
	$dp = '.'.$dp.'f';
	return sprintf("%$dp", $value);
}

function alternateColour($i, $name) {
        $type = ($i % 2 == 0) ? 'light' : 'dark';
	return $name . '-' . $type; 
}
?>