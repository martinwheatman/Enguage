package org.enguage.sign;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

import org.enguage.sign.interpretant.Response;
import org.enguage.util.strings.Strings;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Assets {
	private Assets() {}
	
	public  static final String NAME = "assets";
	public  static final int      ID = 20901783; //Strings.hash( NAME );
	
	private static Activity  context = null; // if null, not on Android
	public  static Activity  context() {return context;}
	public  static void      context( Activity activity ) {context = activity;}
	
	public static InputStream getStream( String name ) {
		InputStream is = null;
		try {
			is = context().getAssets().open( name );
		} catch (IOException ignore) {
			//audit.error( "Assets.getStream(): gone missing: "+ name );
		}
		return is;
	}
	public static String path() {return context.getExternalFilesDir(".").getPath();}
	public static String[] list( String path ) {
		if (path.endsWith("/.")) path = path.substring(0, path.length()-2);
		String[] names = null;
		try {
			names = context().getAssets().list( path );
		} catch (IOException iox) {}
		return names;
	}
	public static Strings perform( Strings cmds ) {
		Strings rc = Response.notOkay();
		if (!cmds.isEmpty()) {
			String cmd = cmds.remove( 0 );
			if (!cmds.isEmpty() && cmd.equals("torch")) {
				CameraManager mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
				try {
					String ids[] = mCameraManager.getCameraIdList();
					if (ids.length == 0)
						rc = new Strings( "there are no light IDs" );
					else {
						cmd = cmds.remove( 0 );
						if (cmd.equals( "on" )) {
							mCameraManager.setTorchMode( ids[ 0 ], true );
							rc = new Strings( "ok, torch is "+ cmd );
							//cm.turnOnTorchWithStrengthLevel("", 0);
						} else if (cmd.equals( "off" )) {
							mCameraManager.setTorchMode(ids[ 0 ], false);
							rc = new Strings( "ok torch is now off" );
							//cm.turnOnTorchWithStrengthLevel("", 0);
					}	}
				} catch (Exception x) {
					rc = new Strings( "an exception occurred in turning light "+ cmd );
		}	}	}
		return rc;
}	}
