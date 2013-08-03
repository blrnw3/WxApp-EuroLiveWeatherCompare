<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>WxApp Usage Stats</title>
		<?php
		require '../config.php';
		require './functions.php';
		if(!$desktop) {
			echo ' <meta name="viewport" content="width=device-width, user-scalable=no" />';
		} else {
			$deskOn = '&desktop';
		}
		?>
		<meta name="description" content="City weather rankings across Europe" />
		<meta name="keywords" content="weather, Europe, nw3, data, records, statistics, rankings, live" />
		<meta http-equiv="content-type" content="application/xhtml+xml; charset=ISO-8859-1" />
		<meta http-equiv="content-language" content="en-GB" />
		
		<?php // if($desktop && !$me) {  include('ggltrack.php'); } ?>
		
		<link rel="stylesheet" type="text/css" href="./valcolstyle.php" media="screen" title="screen" />
		<style type="text/css">
		<?php require 'style.php'; ?>
		</style>
	</head>
	<body>
		<!-- ##### Header ##### -->
	<div id="background">
<div id="page">
<div id="header">
  <?php echo date('D d M Y, H:i', 50 + (int) file_get_contents($API_root ."updated.txt")); ?>
</div>

	<!-- ##### Main ##### -->
	<div id="main">
	<h2>Stats since August 2013</h2>
<?php
//Connect to server
$con = mysql_connect("localhost",$db_username,$db_password);
if (!$con) {
	die('Fatal Error');
}

//Connect to database
$db = mysql_select_db($db_name, $con);
if (!$db) {
	die('Fatal Error');
}

//Query database
$cond = isset($_GET['recency']) && ($_GET['recency'] == 0);
$type = $cond ? "`LastGet` DESC, `Accesses` DESC" : "`Accesses` DESC, `LastGet` DESC";
$link = $cond ? "Popularity" : "Recency";
$linkVal = $cond ? 1 : 0;

$result1 = mysql_query("SELECT SUM( `Accesses` ) AS cnt FROM `$db_table`");
$result = mysql_query("SELECT `Name`, `Country`, `Accesses`, `LastGet` FROM `$db_table` ORDER BY ".$type);
$result2 = mysql_query("SELECT COUNT(`Accesses`) FROM `$db_table` WHERE `Accesses` = 0");

$overall = mysql_fetch_array($result1);
$unuseda = mysql_fetch_array($result2);
$unused = $unuseda[0];
$totCnt = $overall[0];
$reqCnt = floor($totCnt / 9);

$self = $_SERVER['PHP_SELF'];
echo "<p style='margin-left:0.6em;'>
	<b>$reqCnt</b> total requests ($totCnt individual)<br /><b>$unused</b> cities are unqueried.</p>
	<a href='". $self ."?recency=". $linkVal. $deskOn ."'>Sort by $link</a>
";

//Produce output from query
$c = 0;
$currTime = time();
echo  '
	<table>
	<tr>
		<th id="tableTitle" colspan="5">Most Popular Requests</th>
	</tr>
	<tr class="table-top">
		<td id="city">City</td>
		<td id="cnt">Country</td>
		<td id="wxvarName">Count</td>
		<td id="wxvarLast">Last</td>
		<td id="wxvarRank">Rank</td>
	</tr>';
while($row = mysql_fetch_array($result)) {
	echo '<tr class="'.alternateColour($c, "row").' r">
		<td class="city">'.$row[0].'</td>
		<td class="city">'.$row[1].'</td>
		<td class="'.valcolr($row[2]).'">'.$row[2].'</td>
		<td class="'.valcolr(($currTime - $row['LastGet'])/200).'">'. secsToReadable($currTime - $row['LastGet']) .'</td>
		<td class="rank">'.$c.'</td>
	</tr>';
	$c++;
}

echo "</table>";

mysql_close($con);
?>
		</div>

<!-- ##### Footer ##### -->
<div id="footer">
	<div>
		<a href="#header">Top</a>
	</div>
	<div>
		&copy; 2012-<?php echo date('Y'); ?>, Ben Lee-Rodgers
	</div>
	<div>
		NB: Accuracy and reliability of data is not guaranteed
	</div>
	<div>
		<span style="font-size:85%">
			<?php $phpload = roundToDp( microtime(get_as_float) - $scriptbeg, 3 );
				echo 'Version 1.0 | Script executed in ' . $phpload . 's'; ?>
		</span>
	</div>
</div>

</div>
</div>

	</body>
</html>
<?php
if(!$me) {
	file_put_contents( $WEB_root .'visitStatsLog.txt', date("H:i:s d/m/Y") . "\t" .
		$phpload . "\t" . "$deskon  " . $link. "\t" .  $ipaddy. ' ' .
		$_SERVER['HTTP_USER_AGENT'] . "\r\n", FILE_APPEND );
}
?>
