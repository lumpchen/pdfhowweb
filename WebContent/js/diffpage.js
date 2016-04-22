var showMessage = function(alert, msg) {
	/* alert-success | alert-info | alert-warning | alert-danger */
	var alertdiv = document.createElement("div");
	var classVal = "alert " + alert + " alert-dismissible";
	$(alertdiv).attr({
		"class" : classVal,
		"role" : "alert",
		"style" : "margin:0"
	});

	var closeBtn = document.createElement("button");
	$(closeBtn).attr({
		"type" : "button",
		"class" : "close",
		"data-dismiss" : "alert",
		"aria-label" : "Close"
	});

	$(closeBtn).append("<span aria-hidden=\"true\">&times;</span>");
	$(alertdiv).append(closeBtn);

	span = document.createElement("strong");
	$(span).text("Warning!");
	$(alertdiv).append(span);

	span = document.createElement("span");
	$(span).html("   " + msg);
	$(alertdiv).append(span);

	$("#info-bar").append(alertdiv);
}

var clearAlertMessage = function() {
	$("#info-bar").empty();
}

$("#compare-btn").click(function() {

	clearAlertMessage();

	var formData = new FormData();
	var base_file = $('#input-base-pdf');
	var test_file = $('#input-test-pdf');
	var ret = false;
	if (base_file.prop('files')[0] === undefined) {
		showMessage("alert-warning", "Please select a base PDF file.");
		ret = true;
	}

	if (test_file.prop('files')[0] === undefined) {
		showMessage("alert-warning", "Please select a comparison PDF file.");
		ret = true;
	}
	if (ret) {
		return;
	}

	formData.append("base_pdf", base_file.prop('files')[0]);
	formData.append("test_file", test_file.prop('files')[0]);

	formData.append("accountnum", 123456);

	$.ajax({
		type : 'POST',
		url : 'DiffServlet',
		data : formData,
		processData : false,
		contentType : false,
		success : function(res) {
			console.log('success.');
			console.log(res);
		},

		error : function(r) {
			showMessage("alert-danger", "Can't get server!");
		}
	});
});