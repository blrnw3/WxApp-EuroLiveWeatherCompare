<?php require('setup.php'); ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <title>City Rankings</title>
        <meta name="viewport" content="width=device-width, user-scalable=no" />
        <meta name="description" content="City weather rankings across Europe" />
	<meta name="keywords" content="weather, Europe, nw3, data, records, statistics, rankings, live" />
	<meta http-equiv="content-type" content="application/xhtml+xml; charset=ISO-8859-1" />
	<meta http-equiv="content-language" content="en-GB" />
	<link rel="stylesheet" type="text/css" href="./valcolstyle.php" media="screen" title="screen" />
	<link rel="stylesheet" type="text/css" href="./widestyle.css" media="screen" title="screen" />
	<?php // include('ggltrack.php'); ?>

	<script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"></script>
	<script type="text/javascript" src="./js.js"></script>
	
    </head>
    <body>
		<!-- ##### Header ##### -->
	<div id="background">
<div id="page">
<div id="header" onclick="debug()">
  <?php echo date('r '); ?>
</div>

	<!-- ##### Main ##### -->
	<div id="main">

<?php
 require('functions.php');
 
//Connect to SQL server
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
$result = mysql_query("SELECT * FROM `CityWeather` ORDER BY `Name` ASC");
if (!$result) {
	die ('Query error: ' . mysql_error());
}


//Produce output from query
$len = isset($_GET['length']) ? $_GET['length'] : 110;
if($len > 110) { $len =  110; }
$cnt = 0;
$data = array();

while($cnt <= $len && $row = mysql_fetch_assoc($result)) {
    $subCnt = 0;
    foreach($row as $col) {
        if($subCnt >= 0 && $subCnt <= 10) {
            $data[$subCnt][$cnt] = $col;
       // echo $col . ",";
        }
        $subCnt++;
    }
   // echo '<br />';
   $cnt++;
}
//print_r($data);

//Make table
echo "<table>";

tableHead("City Weather", 2);

tr();
td("City");
td("Temperature");
tr_end();

for($i = 0; $i < $cnt; $i++) {
    tr(null);
    td($data[0][$i], alternateColour($i));
    td( $data[4][$i], valcolr($data[4][$i], 0) );
    tr_end();
}
echo "</table>";

?>
	    </div>

<!-- ##### Footer ##### -->
<div id="footer">
	<div>
		<a href="#header">Top</a> |
		<a href="/sitev3/contact.php" title="E-mail me">Contact</a> |
		<a href="http://nw3weather.co.uk" title="Browse to homepage">Home</a>
	</div>
	<div>
		&copy; 2012-<?php echo date('Y'); ?>, B. Lee-Rodgers<span> | Version 0</span>
	</div>
	<div>
		<span>Caution: accuracy and reliability of data cannot be guaranteed.</span>
	</div>
	<div>
		<span style="font-size:85%"><a href="http://validator.w3.org/check?uri=referer" title="check the W3C validity of this page">XHTML and CSS valid</a> |
			<?php $phpload = microtime(get_as_float) - $scriptbeg;
				echo 'PHP executed in ' . roundToDp($phpload,3) . 's'; ?>
		</span>
	</div>

</div>

</div>
</div>
<?php
//http://stackoverflow.com/questions/899327/how-to-store-data-on-a-page-and-retrieve-it-through-jquery
echo '<script type="text/javascript">
    window.data = ' . json_encode($data) .
    '</script>';
?>
    </body>
</html>