package com.cordova.plugin.android.tsa;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import android.annotation.TargetApi;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import io.blocko.coinstack.util.PDFController;
import io.blocko.coinstack.util.TSAGatewayClientAsync;
import io.blocko.coinstack.util.TSAGatewayException;

@TargetApi(7)
public class TSAClient extends CordovaPlugin {
	private static String TAG = "TSA";
	public static CallbackContext mCallbackContext;
	public static PluginResult mPluginResult;

	/**
	 * Constructor.
	 */
	public TSAClient() {
	}

	/**
	 * Sets the context of the Command. This can then be used to do things like
	 * get file paths associated with the Activity.
	 *
	 * @param cordova
	 *            The context of the main Activity.
	 * @param webView
	 *            The CordovaWebView Cordova is running in.
	 */

	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		Log.v(TAG, "Init TSAGateway");
		mPluginResult = new PluginResult(PluginResult.Status.NO_RESULT);

		if (android.os.Build.VERSION.SDK_INT < 23) {
			return;
		}
	}

	/**
	 * Executes the request and returns PluginResult.
	 *
	 * @param action
	 *            The action to execute.
	 * @param args
	 *            JSONArry of arguments for the plugin.
	 * @param callbackContext
	 *            The callback id used when calling back into JavaScript.
	 * @return A PluginResult object with a status and message.
	 */
	public boolean execute(final String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		mCallbackContext = callbackContext;
		Log.v(TAG, "TSA action: " + action);
		final JSONObject arg_object = args.getJSONObject(0);
		if (action.equals("stampDocument")) {            
			if (!arg_object.has("filePath")) {
                JSONObject resultJson = new JSONObject();
                resultJson.put("status", "error");
                resultJson.put("error", "Missing file path");
                mPluginResult = new PluginResult(PluginResult.Status.OK);
                mCallbackContext.success(resultJson);
                mCallbackContext.sendPluginResult(mPluginResult);
                return true;
			}

            if (!arg_object.has("outputPath")) {
                JSONObject resultJson = new JSONObject();
                resultJson.put("status", "error");
                resultJson.put("error", "Missing output file path");
                mPluginResult = new PluginResult(PluginResult.Status.OK);
                mCallbackContext.success(resultJson);
                mCallbackContext.sendPluginResult(mPluginResult);
                return true;
			}

            if (!arg_object.has("gateway")) {
                JSONObject resultJson = new JSONObject();
                resultJson.put("status", "error");
                resultJson.put("error", "Missing TSA gateway URL");
                mPluginResult = new PluginResult(PluginResult.Status.OK);
                mCallbackContext.success(resultJson);
                mCallbackContext.sendPluginResult(mPluginResult);
                return true;
			}

            final String filePath = arg_object.getString("filePath");
            final String outputPath = arg_object.getString("outputPath"); 
            final String gateway = arg_object.getString("gateway");

            boolean insecure = false;
            if (arg_object.has("insecure")) {
                insecure = arg_object.getBoolean("insecure");
            }

            FileInputStream raw;
            
            try {
                raw  = new FileInputStream(filePath);
            } catch (FileNotFoundException e) {
                JSONObject resultJson = new JSONObject();
                resultJson.put("status", "error");
                resultJson.put("error", "File not found");
                mPluginResult = new PluginResult(PluginResult.Status.OK);
                mCallbackContext.success(resultJson);
                mCallbackContext.sendPluginResult(mPluginResult);
                return true;
            }

            final PDFController controller = new PDFController(raw);
            final String hash = controller.calculateHash();

            // stamp document
            TSAGatewayClientAsync client = new TSAGatewayClientAsync(gateway, insecure);
            client.requestStamp(hash, new TSAGatewayClientAsync.Callback() {
                @Override
                public void onSuccess(String stamp) {
                    try {
                        File tempFile = new File(outputPath);
                        controller.writeStampId(tempFile, stamp);
                    } catch (IOException e) {
                        try {
                            JSONObject resultJson = new JSONObject();
                            resultJson.put("status", "error");
                            resultJson.put("error", "Failed to write output file");
                            mPluginResult = new PluginResult(PluginResult.Status.OK);
                            mCallbackContext.success(resultJson);
                            mCallbackContext.sendPluginResult(mPluginResult);
                            return;
                        } catch (JSONException e2) {
                            e2.printStackTrace();
                        }
                    }
                    try {
                        JSONObject resultJson = new JSONObject();
                        resultJson.put("status", "ok");
                        resultJson.put("hash", hash);
                        resultJson.put("stamp", stamp);
                        mPluginResult = new PluginResult(PluginResult.Status.OK);
                        mCallbackContext.success(resultJson);
                        mCallbackContext.sendPluginResult(mPluginResult);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    return;
                }

                @Override
                public void onTSAFailure(TSAGatewayException e) {
                    try {
                        JSONObject resultJson = new JSONObject();
                        resultJson.put("status", "error");
                        resultJson.put("error", "Failed to stamp document using TSA - " + e.getErrorCode());
                        mPluginResult = new PluginResult(PluginResult.Status.OK);
                        mCallbackContext.success(resultJson);
                        mCallbackContext.sendPluginResult(mPluginResult);
                    } catch (JSONException e2) {
                        e2.printStackTrace();
                    }
                    return;
                }

                @Override
                public void onIOFailure(IOException e) {
                    try {
                        e.printStackTrace();
                        JSONObject resultJson = new JSONObject();
                        resultJson.put("status", "error");
                        resultJson.put("error", "Failed to connect to TSA");
                        mPluginResult = new PluginResult(PluginResult.Status.OK);
                        mCallbackContext.success(resultJson);
                        mCallbackContext.sendPluginResult(mPluginResult);
                    } catch (JSONException e2) {
                        e2.printStackTrace();
                    }
                    return;
                }
            });
            return true;
		}
		return false;
	}

	public static boolean setPluginResultError(String errorMessage) {
		mCallbackContext.error(errorMessage);
		mPluginResult = new PluginResult(PluginResult.Status.ERROR);
		return false;
	}
}