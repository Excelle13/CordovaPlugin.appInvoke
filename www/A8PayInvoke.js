var exec = require('cordova/exec');

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'A8PayInvoke', 'coolMethod', [arg0]);
};

exports.getExtra = function (arg0, success, error) {
    exec(success, error, 'A8PayInvoke', 'getExtra', [arg0]);
};
exports.getExtras = function (arg0, success, error) {
    exec(success, error, 'A8PayInvoke', 'getExtras');
};
exports.invokeJL = function (params, extra, success, error) {
	var output = [params];

		if(extra != undefined) {
			output.push(extra);
		}
		else {
			output.push(null);
		}
    exec(success, error, 'A8PayInvoke', 'invokeJL', output);
};
