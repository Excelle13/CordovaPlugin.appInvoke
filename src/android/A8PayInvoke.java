package com.ttebd.a8PayInvoke;

import android.content.Intent;
import android.content.pm.PackageManager;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


public class A8PayInvoke extends CordovaPlugin {
    private static final String TAG = "A8PayInvoke";
    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        switch (action) {
            case "coolMethod":
                Log.e(TAG, "execute coolMethod");
                this.coolMethod(args.getString(0), callbackContext);
                return true;
            case "getExtras":
                this.getExtras(callbackContext);
                return true;
            case "getExtra":
                this.getExtra(args, callbackContext);
                return true;
            case "invokeJL":
                this.cordova.getThreadPool().execute(new Runnable() {
                    public void run() {
                        Log.e(TAG, "execute invokeJL method ");
                        Log.e(TAG, "params-->" + args.toString());
                        invokeJL(args, callbackContext);
                    }
                });
                return true;
        }
        return false;
    }

    /**
     * This is a test method when you first run plug-ins.
     *
     * @param message
     * @param callbackContext
     */
    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    /**
     * This is the main method to call third party applications
     *
     * @param args
     * @param callback
     */
    private void invokeJL(JSONArray args, CallbackContext callback) {
        Intent LaunchIntent;
        JSONObject params;
        JSONArray flags;
        JSONArray component;

        JSONObject extra;
        String key;

        try {
            if (args.get(0) instanceof JSONObject) {
                params = args.getJSONObject(0);

                /**
                 * set application
                 */
                if (params.has("application")) {
                    PackageManager manager = cordova.getActivity().getApplicationContext().getPackageManager();
                    LaunchIntent = manager.getLaunchIntentForPackage(params.getString("application"));

                    if (LaunchIntent == null) {
                        callback.error("Application \"" + params.getString("application") + "\" not found!");
                        return;
                    }
                }

                /**
                 * set intent
                 */
                else if (params.has("intent")) {
                    LaunchIntent = new Intent(params.getString("intent"));
                } else {
                    LaunchIntent = new Intent();
                }


                /**
                 * set package
                 */
                if (params.has("package")) {
                    LaunchIntent.setPackage(params.getString("package"));
                }

                /**
                 * set action
                 */
                if (params.has("action")) {
                    LaunchIntent.setAction(params.getString("action"));
                }

                /**
                 * set category
                 */
                if (params.has("category")) {
                    LaunchIntent.addCategory("mispos");
                }

                /**
                 * set type
                 */
                if (params.has("type")) {
                    LaunchIntent.setType(params.getString("type"));
                }


                /**
                 */
                if (params.has("uri")) {
                    LaunchIntent.setData(Uri.parse(params.getString("uri")));
                }


                if (!args.isNull(1)) {
                    extra = args.getJSONObject(1);
                    Iterator<String> iter = extra.keys();
                    Bundle bundle = new Bundle();

                    while (iter.hasNext()) {
                        key = iter.next();
                        Object value = extra.get(key);

                        if (value instanceof String) {
                            bundle.putString(key, extra.getString(key));
                        } else {
                            callback.error("参数类型不匹配，应为string类型");
                        }

                    }
                    LaunchIntent.putExtras(bundle);
                }

//        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
//        pluginResult.setKeepCallback(true);


//        try {
                if (params.has("intentstart") && "startActivityForResult".equals(params.getString("intentstart"))) {

//          cordova.setActivityResultCallback(this);
                    callbackContext = callback;
                    cordova.startActivityForResult(this, LaunchIntent, 31);
                } else {
                    cordova.getActivity().startActivity(LaunchIntent);
                }
//        callback.sendPluginResult(pluginResult);

       /* } catch (ActivityNotFoundException e) {
          Log.e(TAG, "------ActivityNotFoundException --------");

//          e.printStackTrace();

//          Toast.makeText(cordova.getActivity(), "该应用未安装，请检查后再试！", Toast.LENGTH_SHORT).show();
        }*/


//                callback.sendPluginResult(pluginResult);
            } else {
                callback.error("Incorrect params, array is not array object!");
            }
        } catch (Exception e) {
            try {
                JSONObject errMessageHeader = new JSONObject();
                JSONObject errMessageObj = new JSONObject();
                errMessageObj.put("repCode", "02");
                errMessageObj.put("message", "应用未安装");
                errMessageHeader.put("header", errMessageObj);
                callback.error(errMessageHeader);
            } catch (Exception e1) {

            }
//      e.printStackTrace();
//      callback.error(e.getClass() + ": " + e.getMessage());

        }
    }


    /**
     * getExtras
     */
    private void getExtras(CallbackContext callback) {
        try {
            Bundle extras = cordova.getActivity().getIntent().getExtras();
            JSONObject info = new JSONObject();

            if (extras != null) {
                for (String key : extras.keySet()) {
                    info.put(key, extras.get(key).toString());
                }
            }

            callback.success(info);
        } catch (JSONException e) {
            callback.error(e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * getExtra
     */
    private void getExtra(JSONArray args, CallbackContext callback) {
        try {
            String extraName = args.getString(0);
            Intent extraIntent = cordova.getActivity().getIntent();

            if (extraIntent.hasExtra(extraName)) {
                String extraValue = extraIntent.getStringExtra(extraName);

                if (extraValue == null) {
                    extraValue = (extraIntent.getParcelableExtra(extraName)).toString();
                }

                callback.success(extraValue);
            } else {
                callback.error("extra field not found");
            }
        } catch (JSONException e) {
            callback.error(e.getMessage());
            e.printStackTrace();
        }
    }


    // Acceptance of the results returned by third party applications
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.e(TAG, "execute onActivityResult");

//        System.out.println("plugin status---", PluginResult.Status.ERROR);

        if (requestCode == 31 && intent!= null) {
            PluginResult pluginResult;
            JSONObject result = new JSONObject();
            try {
//                result.put("_ACTION_requestCode_", requestCode);
//                result.put("_ACTION_resultCode_", resultCode);

                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    for (String key : bundle.keySet()) {
                        result.put(key, bundle.get(key));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e(TAG, "result--》 " + result);
            pluginResult = new PluginResult(PluginResult.Status.OK, result);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
        } else {
            JSONObject errObj = new JSONObject();
            try {
                errObj.put("rc", "05");
                errObj.put("repText", "调用出错，没有数据返回");
            } catch (JSONException e) {
                Log.e(TAG, "onActivityResult:" + errObj);
            }
            PluginResult pluginResult;
            pluginResult = new PluginResult(PluginResult.Status.ERROR, errObj);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
        }
    }
}
