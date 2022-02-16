var $Osc = {
    hover: function(event) {
        event.target.style.backgroundColor = "red";
    },
    out: function(event) {
        event.target.style.backgroundColor = "green";
    }

};
var $OscElement = document.getElementById("home_header_button");
$OscElement.addEventListener("mouseover", $Osc.hover, false);
$OscElement.addEventListener("mouseout", $Osc.out, false);