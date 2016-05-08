/*namespace*/

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

/*$("#compare-btn").click(function() {});*/

var progressTimer = {
	update : function() {
		$.getJSON("DiffProgressServlet", function(progress) {
			$(".progress-bar").css["aria-valuenow"] = progress;
		});
	},

	progressListener : null,
	start : function() {
		this.progressListener = setInterval(this.update, 1000);
	},

	stop : function() {
		clearInterval(this.progressListener);
	}
}

// //////////////////////////////////////////
var guid = function() {
	  return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
	    s4() + '-' + s4() + s4() + s4();
};
var s4 = function() {
	  return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
};
var uid = guid();
var app = {
	url : 'DiffServlet',
	initialize : function() {
//		$('login-name').focus();
		$("#progress-div").hide();
		$("#compare-btn").click = app.post;
		
		app.listen();
	},
	listen : function() {
		$('#comet-frame').attr('src', app.url + '?' + uid);
	},

		
	/*login : function() {
		var name = $F('login-name');
		if (!name.length > 0) {
			$('system-message').style.color = 'red';
			$('login-name').focus();
			return;
		}

		var query = 'action=login' + '&name=' + encodeURI($F('login-name'));
		new Ajax.Request(app.url, {
			postBody : query,
			onFailure : function(r) {
				$('display').style.color = 'red';
				$('display').textContent = 'Fail to make ajax call ' + r.status
						+ " Error";
			},
			onSuccess : function() {
				$('system-message').style.color = '#2d2b3d';
				$('system-message').innerHTML = name + ':';

				$('login-button').disabled = true;
				$('login-form').style.display = 'none';
				$('message-form').style.display = '';
				$('message').focus();
			}
		});
	},*/
/*	post : function() {
		var message = $F('message');
		if (!message > 0) {
			return;
		}
		$('message').disabled = true;
		$('post-button').disabled = true;

		var query = 'action=post' + '&name=' + encodeURI($F('login-name'))
				+ '&message=' + encodeURI(message);
		new Ajax.Request(app.url, {
			postBody : query,
			onComplete : function() {
				$('message').disabled = false;
				$('post-button').disabled = false;
				$('message').focus();
				$('message').value = '';
			}
		});
	},*/
	
	post : function() {
		clearAlertMessage();

		var formData = new FormData();
		var base_file = $('#input-base-pdf');
		var test_file = $('#input-test-pdf');
		var ret = false;
		if (base_file.prop('files')[0] === undefined || base_file.prop('files')[0] === null) {
			showMessage("alert-warning", "Please select a base PDF file.");
			ret = true;
		}

		if (test_file.prop('files')[0] === undefined || test_file.prop('files')[0] === null) {
			showMessage("alert-warning", "Please select a comparison PDF file.");
			ret = true;
		}
		if (ret) {
			return;
		}

		formData.append("base_pdf", base_file.prop('files')[0]);
		formData.append("test_pdf", test_file.prop('files')[0]);

//		formData.append("uid", app.uid);
		
		$('#display').empty();

		$.ajax({
			type : 'POST',
			url : app.url + '?' + uid,
			data : formData,
			processData : false,
			contentType : false,

			beforeSend : function() {
				$("#progress-div").show();
				$("#compare-btn").hide();

				progressTimer.start();
			},

			complete : function() {
				$("#progress-div").hide();
				$("#compare-btn").show();

				progressTimer.stop();
			},

			success : function(res) {
				console.log('success.');
				console.log(res);
			},

			error : function(r) {
				showMessage("alert-danger", "Can't get server!");
			}
		});
	},
	
	update : function(data) {
		var p = document.createElement('p');
		p.innerHTML = data.name + ' : ' + data.message;

		$('#display').append(p);

//		new Fx.Scroll('display').down();
	},
	timeout : function(data) {
//		uid = guid();
		app.listen();
	}
};

/*var rules = {
	'#login-name' : function(elem) {
		Event.observe(elem, 'keydown', function(e) {
			if (e.keyCode == 13) {
				$('login-button').focus();
			}
		});
	},
	'#login-button' : function(elem) {
		elem.onclick = app.login;
	},
	'#message' : function(elem) {
		Event.observe(elem, 'keydown', function(e) {
			if (e.keyCode == 13) {
				$('post-button').focus();
			} else if (e.e.ctrlKey && e.keyCode == 13) {
				$('message').textContent = "\n"
			}
		});
	},
	'#post-button' : function(elem) {
		elem.onclick = app.post;
	}
};*/
Behaviour.addLoadEvent(app.initialize);
//Behaviour.register(rules);
